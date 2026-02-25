# Guía de Despliegue — GCP Cloud Run + Cloud SQL

**Proyecto:** Bitacora Digital API REST
**Stack:** Spring Boot 3.5.5 · Java 21 · PostgreSQL 15
**Última actualización:** 2026-02-24

---

## Arquitectura

```
GitHub (develop)
     │
     └──push──> Cloud Build ──build──> Artifact Registry
                                              │
                                         deploy──> Cloud Run (bitacora-api)
                                                          │
                                              Cloud SQL Auth Proxy (socket Unix)
                                                          │
                                                    Cloud SQL (PostgreSQL 15)

Secret Manager ──────────────────────────────────────────┘
(DB_URL, DB_PASSWORD, JWT_SECRET, MAIL_USERNAME, MAIL_PASSWORD)
```

**Flujo de CI/CD:** cada push a la rama `develop` dispara Cloud Build automáticamente.
El build tarda ~3 minutos. La URL pública de Cloud Run se actualiza sin downtime.

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

```bash
# DB_URL con connection name de Cloud SQL (reemplaza PROJECT_ID)
echo -n "jdbc:postgresql:///teia_db?cloudSqlInstance=PROJECT_ID:us-central1:bitacora-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory&ipTypes=PUBLIC,PRIVATE" \
  | gcloud secrets create bitacora-db-url --data-file=-

# Contraseña de la base de datos
echo -n "<TU_PASSWORD_SEGURO>" \
  | gcloud secrets create bitacora-db-password --data-file=-

# JWT Secret (generado aleatoriamente)
openssl rand -base64 64 | gcloud secrets create bitacora-jwt-secret --data-file=-

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
```

---

## Paso 6 — Configurar permisos IAM

```bash
PROJECT_ID=$(gcloud config get-value project)
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")

# Cloud Build puede desplegar en Cloud Run
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$PROJECT_NUMBER@cloudbuild.gserviceaccount.com" \
  --role="roles/run.admin"

# Cloud Build puede leer secretos
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$PROJECT_NUMBER@cloudbuild.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

# Cloud Build puede actuar como la cuenta de servicio de Cloud Run
gcloud iam service-accounts add-iam-policy-binding \
  $PROJECT_NUMBER-compute@developer.gserviceaccount.com \
  --member="serviceAccount:$PROJECT_NUMBER@cloudbuild.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"

# Cloud Run puede conectarse a Cloud SQL
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/cloudsql.client"

# Cloud Run puede leer secretos
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
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
| `_DB_USERNAME` | `teia_user` |
| `_REGION` | `us-central1` |

---

## Paso 8 — Primer deploy

### Opción A: Disparar manualmente desde la CLI

```bash
gcloud builds submit --config=cloudbuild.yaml \
  --substitutions=_CLOUD_SQL_CONN="PROJECT_ID:us-central1:bitacora-db",_DB_USERNAME="teia_user",_REGION="us-central1"
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
| `DB_USERNAME` | Env var | Usuario de PostgreSQL |
| `DB_URL` | Secret Manager | JDBC URL con Cloud SQL socket |
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
El servicio está configurado con `--min-instances=1` para mantener el scheduler
de notificaciones activo. Costo adicional: ~$3-5/mes en memoria idle.

### Logs de archivo
En Cloud Run el sistema de archivos es efímero; los logs escritos en
`logs/bitacora-app.log` no persisten. Usar **Cloud Logging** para consultar
todos los logs del servicio.

### WebSockets
El servicio usa `--session-affinity` para que las conexiones WebSocket (STOMP)
mantengan afinidad con la misma instancia.

---

## Comandos útiles de mantenimiento

```bash
# Ver revisiones desplegadas
gcloud run revisions list --service=bitacora-api --region=us-central1

# Rollback a una revision anterior
gcloud run services update-traffic bitacora-api \
  --region=us-central1 \
  --to-revisions=REVISION_NAME=100

# Actualizar un secreto
echo -n "NUEVO_VALOR" | gcloud secrets versions add bitacora-jwt-secret --data-file=-

# Ver estado de Cloud SQL
gcloud sql instances describe bitacora-db --format="value(state)"

# Conectarse a la BD desde local (requiere gcloud CLI)
gcloud sql connect bitacora-db --user=teia_user --database=teia_db
```
