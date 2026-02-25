# Guía de Despliegue — GCP Cloud Run + Cloud SQL

**Proyecto:** Bitacora Digital API REST
**Stack:** Spring Boot 3.5.5 · Java 21 · PostgreSQL 15
**Última actualización:** 2026-02-25

---

## Arquitectura

```
GitHub (develop)
     │
     └──push──> Cloud Build ──build──> Artifact Registry
                                              │
                                         deploy──> Cloud Run (bitacora-api)
                                                          │           │
                                                     api (8080)  cloud-sql-proxy
                                                                      │ TCP localhost:5432
                                                               Cloud SQL (PostgreSQL 15)

Secret Manager ──────────────────────────────────────────┘
(DB_PASSWORD, JWT_SECRET, MAIL_USERNAME, MAIL_PASSWORD)
```

**Flujo de CI/CD:** cada push a la rama `develop` dispara Cloud Build automáticamente.
El build tarda ~3 minutos. La URL pública de Cloud Run se actualiza sin downtime.

**Multi-container:** Cloud Run corre en gen2 con dos contenedores: el API Spring Boot
y el Cloud SQL Auth Proxy como sidecar. El proxy autentica con Cloud SQL via la
Compute Engine SA y expone PostgreSQL en `localhost:5432` via TCP plano, sin
dependencias GCP en el código de la aplicación.

---

## Prerrequisitos

- Google Cloud CLI instalada y autenticada (`gcloud auth login`)
- Proyecto GCP creado con facturación habilitada
- Repositorio en GitHub conectado a Cloud Build (ver Paso 7)
- Permisos de propietario o editor en el proyecto GCP

---

## Paso 1 — Habilitar las APIs necesarias

```bash
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  secretmanager.googleapis.com
```

---

## Paso 2 — Crear el repositorio en Artifact Registry

```bash
gcloud artifacts repositories create bitacora \
  --repository-format=docker \
  --location=us-central1 \
  --description="Imagenes Docker - Bitacora Digital"
```

Autenticar Docker con Artifact Registry:

```bash
gcloud auth configure-docker us-central1-docker.pkg.dev
```

---

## Paso 3 — Crear la instancia de Cloud SQL (PostgreSQL 15)

```bash
# Instancia db-f1-micro es suficiente para el piloto (~$10/mes)
gcloud sql instances create bitacora-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --storage-auto-increase \
  --backup \
  --enable-point-in-time-recovery
```

> El aprovisionamiento tarda ~5 minutos.

### Crear base de datos y usuario

```bash
gcloud sql databases create teia_db --instance=bitacora-db

gcloud sql users create teia_user \
  --instance=bitacora-db \
  --password=TU_PASSWORD_SEGURO
```

### Obtener el Connection Name (guardar este valor)

```bash
gcloud sql instances describe bitacora-db --format="value(connectionName)"
# Salida esperada: PROJECT_ID:us-central1:bitacora-db
```

---

## Paso 4 — Cargar el esquema SQL inicial

```bash
# Conectarse a la instancia (requiere Cloud SQL Auth Proxy o IP autorizada)
gcloud sql connect bitacora-db --user=teia_user --database=teia_db

# Dentro del prompt de psql, ejecutar los scripts:
\i src/main/resources/teia_DDL.sql
\i src/main/resources/teia_DML.sql
\q
```

---

## Paso 5 — Crear los secretos en Secret Manager

Reemplaza los valores entre `<...>` con los reales antes de ejecutar.

> **Nota:** `DB_URL` ya NO se gestiona como secreto. El valor
> `jdbc:postgresql://127.0.0.1:5432/teia_db` está fijo en `cloudrun-service.yaml`
> porque el Cloud SQL Auth Proxy sidecar expone la BD en localhost.

```bash
# Contraseña de la base de datos
echo -n "<TU_PASSWORD_SEGURO>" \
  | gcloud secrets create bitacora-db-password --data-file=-

# JWT Secret — IMPORTANTE: el | tr -d '\n' elimina el salto de línea final,
# que causaría un error "Illegal base64 character" al arrancar la aplicación.
openssl rand -base64 64 | tr -d '\n' \
  | gcloud secrets create bitacora-jwt-secret --data-file=-

# Correo SMTP (cuenta de Gmail)
echo -n "<correo@gmail.com>" \
  | gcloud secrets create bitacora-mail-username --data-file=-

# App Password de Gmail (no es la contraseña de la cuenta)
# Generar en: Google Account > Seguridad > Verificacion en 2 pasos > Contrasenas de aplicacion
echo -n "<APP_PASSWORD>" \
  | gcloud secrets create bitacora-mail-password --data-file=-
```

### Verificar secretos creados

```bash
gcloud secrets list
# Debe mostrar: bitacora-db-password, bitacora-jwt-secret,
#               bitacora-mail-username, bitacora-mail-password
```

---

## Paso 6 — Configurar permisos IAM

> **Importante:** el trigger de Cloud Build ejecuta los pasos usando la
> **Compute Engine default SA** (`PROJECT_NUMBER-compute@developer.gserviceaccount.com`),
> no la Cloud Build SA. Todos los roles deben otorgarse a esa cuenta.

```bash
PROJECT_ID=$(gcloud config get-value project)
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")
COMPUTE_SA="$PROJECT_NUMBER-compute@developer.gserviceaccount.com"
CLOUDBUILD_SA="$PROJECT_NUMBER@cloudbuild.gserviceaccount.com"

# Compute Engine SA: desplegar en Cloud Run
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$COMPUTE_SA" \
  --role="roles/run.admin"

# Compute Engine SA: leer secretos
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$COMPUTE_SA" \
  --role="roles/secretmanager.secretAccessor"

# Compute Engine SA: escribir logs (requerido por CLOUD_LOGGING_ONLY)
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$COMPUTE_SA" \
  --role="roles/logging.logWriter"

# Compute Engine SA: conectarse a Cloud SQL (para el sidecar proxy)
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$COMPUTE_SA" \
  --role="roles/cloudsql.client"

# Compute Engine SA: subir imágenes a Artifact Registry
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$COMPUTE_SA" \
  --role="roles/artifactregistry.writer"

# Cloud Build SA: actuar como Compute Engine SA (iam.serviceAccountUser)
gcloud iam service-accounts add-iam-policy-binding $COMPUTE_SA \
  --member="serviceAccount:$CLOUDBUILD_SA" \
  --role="roles/iam.serviceAccountUser"
```

---

## Paso 7 — Conectar GitHub y crear el Trigger de Cloud Build

1. Ir a **GCP Console → Cloud Build → Triggers**
2. Clic en **Conectar repositorio** → seleccionar **GitHub** → autorizar y elegir el repo `bitacora-digital-api-rest`
3. Clic en **Crear trigger** con la siguiente configuración:

| Campo | Valor |
|---|---|
| Nombre | `deploy-develop` |
| Evento | Push a una rama |
| Rama (regex) | `^develop$` |
| Configuración de compilación | `cloudbuild.yaml` (en raíz del repo) |

4. En **Variables de sustitución**, agregar:

| Variable | Valor |
|---|---|
| `_CLOUD_SQL_CONN` | `PROJECT_ID:us-central1:bitacora-db` |
| `_REGION` | `us-central1` |

> Reemplaza `PROJECT_ID` con el ID real del proyecto GCP.

---

## Paso 8 — Primer deploy

### Opción A: Disparar manualmente desde la CLI

```bash
gcloud builds submit --config=cloudbuild.yaml \
  --substitutions=_CLOUD_SQL_CONN="PROJECT_ID:us-central1:bitacora-db",_REGION="us-central1"
```

### Opción B: Push a develop (trigger automático)

```bash
git push origin develop
# Cloud Build se dispara automáticamente en ~segundos
```

---

## Paso 9 — Verificar el despliegue

```bash
# Obtener la URL pública del servicio
gcloud run services describe bitacora-api \
  --region=us-central1 \
  --format="value(status.url)"

# Verificar que los dos contenedores estén activos (api + cloud-sql-proxy)
gcloud run services describe bitacora-api \
  --region=us-central1 \
  --format="value(spec.template.spec.containers[].name)"

# Ver logs en tiempo real
gcloud logging tail \
  "resource.type=cloud_run_revision AND resource.labels.service_name=bitacora-api" \
  --format="value(textPayload)"
```

La API estará disponible en: `https://bitacora-api-XXXX-uc.a.run.app`

---

## Flujo de trabajo para iteraciones del piloto

```
1. Desarrollar y probar localmente (docker-compose up)
2. git push origin develop
3. Cloud Build tarda ~3 minutos
4. La URL del cliente se actualiza automáticamente
```

---

## Variables de entorno en Cloud Run

| Variable | Origen | Descripción |
|---|---|---|
| `PORT` | Cloud Run (automático) | Puerto del servidor |
| `DB_URL` | Valor fijo en `cloudrun-service.yaml` | `jdbc:postgresql://127.0.0.1:5432/teia_db` |
| `DB_USERNAME` | Valor fijo en `cloudrun-service.yaml` | Usuario de PostgreSQL |
| `DB_PASSWORD` | Secret Manager | Contraseña de PostgreSQL |
| `JWT_SECRET` | Secret Manager | Clave de firma JWT |
| `MAIL_USERNAME` | Secret Manager | Cuenta de correo SMTP |
| `MAIL_PASSWORD` | Secret Manager | App password de Gmail |

---

## Notas importantes

### Zona horaria del Scheduler
Cloud Run corre en **UTC**. Colombia es **UTC-5**.
El cron configurado en `NOTIFICACIONES_SCHEDULER_CRON` debe usar UTC:
- `0 0 13 * * ?` = 8:00 AM Colombia (13:00 UTC)

### Instancias mínimas
El servicio está configurado con `autoscaling.knative.dev/minScale: "1"` para mantener
el scheduler de notificaciones activo. Costo adicional: ~$3-5/mes en memoria idle.

### Logs de archivo
En Cloud Run el sistema de archivos es efímero; los logs escritos en
`logs/bitacora-app.log` no persisten. Usar **Cloud Logging** para consultar
todos los logs del servicio.

### WebSockets
El servicio usa `run.googleapis.com/session-affinity: "true"` para que las
conexiones WebSocket (STOMP) mantengan afinidad con la misma instancia.

### Portabilidad
El código de la aplicación no tiene dependencias GCP. Para desplegar en otro
entorno basta con cambiar el valor de `DB_URL` a la URL JDBC del destino.
El `cloudrun-service.yaml` y el `cloudbuild.yaml` son específicos de GCP.

---

## Comandos útiles de mantenimiento

```bash
# Ver revisiones desplegadas
gcloud run revisions list --service=bitacora-api --region=us-central1

# Rollback a una revision anterior
gcloud run services update-traffic bitacora-api \
  --region=us-central1 \
  --to-revisions=REVISION_NAME=100

# Actualizar un secreto (ejemplo: rotar el JWT secret)
openssl rand -base64 64 | tr -d '\n' \
  | gcloud secrets versions add bitacora-jwt-secret --data-file=-

# Ver estado de Cloud SQL
gcloud sql instances describe bitacora-db --format="value(state)"

# Conectarse a la BD desde local (requiere gcloud CLI)
gcloud sql connect bitacora-db --user=teia_user --database=teia_db
```
