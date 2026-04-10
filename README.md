# Bitacora Digital API REST

API REST para la bitacora digital del curso institucional "En sus marcas, listos, RAC!" - Plataforma TEIA.

## Tecnologias

- Java 21
- Spring Boot 3.5.5
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Gradle
- Docker

## Requisitos

- JDK 21+
- PostgreSQL 14+
- Gradle 8+ (o usar el wrapper incluido)
- Docker y Docker Compose (opcional)

## Instalacion Local

1. Clonar el repositorio:
```bash
git clone <url-repositorio>
cd bitacora-digital-api-rest
```

2. Crear la base de datos:
```sql
CREATE DATABASE teia_db;
CREATE USER teia_user WITH PASSWORD 'teia_password';
GRANT ALL PRIVILEGES ON DATABASE teia_db TO teia_user;
```

3. Ejecutar los scripts SQL en orden:
```bash
psql -U teia_user -d teia_db -f src/main/resources/teia_DDL.sql
psql -U teia_user -d teia_db -f src/main/resources/teia_DML.sql
```

4. Compilar y ejecutar:
```bash
./gradlew bootRun
```

## Docker

### Dockerfile

Crear archivo `Dockerfile` en la raiz del proyecto:

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew && ./gradlew build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
```

### Docker Compose

Crear archivo `docker-compose.yml`:

```yaml
services:
  db:
    image: postgres:16-alpine
    container_name: teia-postgres
    environment:
      POSTGRES_DB: teia_db
      POSTGRES_USER: teia_user
      POSTGRES_PASSWORD: teia_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./src/main/resources/teia_DDL.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./src/main/resources/teia_DML.sql:/docker-entrypoint-initdb.d/02-data.sql
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U teia_user -d teia_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  api:
    build: .
    container_name: teia-api
    environment:
      DB_URL: jdbc:postgresql://db:5432/teia_db
      DB_USERNAME: teia_user
      DB_PASSWORD: teia_password
      JWT_SECRET: ${JWT_SECRET:-/kgDXgvjsnczjJKOWUC910j6TeR04o8Z/H+VBy9ce0lBmnnZsX0Mzr9xwzg4ZHT80sUphiM8uw3PT70z0twsSA==}
      TUTOR_SERVICE_URL: ${TUTOR_SERVICE_URL:-http://host.docker.internal:8000}
      TUTOR_SERVICE_TIMEOUT: ${TUTOR_SERVICE_TIMEOUT:-120000}
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy

volumes:
  postgres_data:
```

### Comandos Docker

```bash
# Construir e iniciar todos los servicios
docker-compose up -d --build

# Ver logs
docker-compose logs -f api

# Detener servicios
docker-compose down

# Detener y eliminar volumenes (reset BD)
docker-compose down -v
```

### Variables de Entorno para Produccion

Crear archivo `.env` para configuracion sensible:

```env
DB_URL=jdbc:postgresql://db:5432/teia_db
DB_USERNAME=teia_user
DB_PASSWORD=<password-seguro>
JWT_SECRET=<clave-secreta-generada>
```

## Configuracion

Variables de entorno (tienen valores por defecto para desarrollo):

| Variable | Descripcion | Default |
|----------|-------------|---------|
| `DB_URL` | URL de conexion PostgreSQL | `jdbc:postgresql://localhost:5432/teia_db` |
| `DB_USERNAME` | Usuario de BD | `teia_user` |
| `DB_PASSWORD` | Contrasena de BD | `teia_password` |
| `JWT_SECRET` | Clave secreta para JWT | (incluida en properties) |
| `TUTOR_SERVICE_URL` | URL del servicio de tutor inteligente | `http://localhost:8000` |
| `TUTOR_SERVICE_TIMEOUT` | Timeout para llamadas al tutor (ms) | `120000` |

## Estructura del Proyecto

```
src/main/java/com/diginexa/bitacora/
├── annotations/       # Conversores JSONB personalizados
├── config/           # Configuracion de seguridad, JWT y WebSocket
├── constants/        # Constantes (codigos de seccion, tipos de notificacion)
├── controllers/      # Controladores REST
├── dtos/             # Objetos de transferencia
│   ├── bitacora/     # DTOs de bitacora (secciones, equipo, temas)
│   ├── notificacion/ # DTOs de notificaciones y calendario
│   └── tutor/        # DTOs del tutor inteligente
├── entities/         # Entidades JPA
├── exceptions/       # Manejo de excepciones
│   ├── domain/       # Excepciones de dominio
│   ├── security/     # Excepciones de seguridad
│   └── validation/   # Excepciones de validacion
├── repositories/     # Repositorios Spring Data
├── services/         # Logica de negocio
├── utils/            # Utilidades (EmailUtils)
└── validation/       # Validaciones personalizadas
```

## API Endpoints

### Autenticacion
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesion |
| POST | `/api/auth/register` | Registrar usuario |
| POST | `/api/auth/refresh` | Refrescar token |
| POST | `/api/auth/logout` | Cerrar sesion |

### Menu
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/menu/{roleId}` | Obtener menu por rol |
| GET | `/api/menu/my-menu` | Obtener menu del usuario actual |

### Bitacora - Secciones
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/bitacora/secciones/{codigo}/usuario/{id}` | Obtener respuestas de seccion |
| PUT | `/api/bitacora/secciones/{codigo}` | Guardar respuestas de seccion |
| GET | `/api/bitacora/secciones/usuario/{id}` | Listar todas las secciones del usuario |
| DELETE | `/api/bitacora/secciones/{codigo}/usuario/{id}` | Eliminar respuestas de seccion |

### Bitacora - Equipo Docente
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/bitacora/equipo-docente/usuario/{id}` | Listar equipo docente |
| POST | `/api/bitacora/equipo-docente/usuario/{id}` | Agregar miembro |
| PUT | `/api/bitacora/equipo-docente/{id}` | Actualizar miembro |
| DELETE | `/api/bitacora/equipo-docente/{id}` | Eliminar miembro |
| PUT | `/api/bitacora/equipo-docente/usuario/{id}/reordenar` | Reordenar miembros |

### Bitacora - Temas y Contenido
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/bitacora/temas/usuario/{id}` | Listar temas |
| POST | `/api/bitacora/temas/usuario/{id}` | Crear tema |
| PUT | `/api/bitacora/temas/{id}` | Actualizar tema |
| DELETE | `/api/bitacora/temas/{id}` | Eliminar tema |
| POST | `/api/bitacora/temas/{id}/subtemas` | Agregar subtema |
| PUT | `/api/bitacora/temas/{id}/subtemas/{indice}` | Actualizar subtema |
| DELETE | `/api/bitacora/temas/{id}/subtemas/{indice}` | Eliminar subtema |

### Bitacora - Progreso
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/bitacora/progreso/usuario/{id}` | Obtener progreso general |
| GET | `/api/bitacora/progreso/usuario/{id}/seccion/{codigo}` | Obtener progreso de seccion |
| PUT | `/api/bitacora/progreso/usuario/{id}/seccion/{codigo}` | Actualizar progreso |

### Tutor Inteligente (Proxy a teia-ai-services)
| Metodo | Endpoint | Auth | Descripcion |
|--------|----------|------|-------------|
| GET | `/api/tutor/status` | No | Estado del sistema y modelos disponibles |
| GET | `/api/tutor/health` | No | Verificar conectividad con Ollama |
| GET | `/api/tutor/modules` | Si | Obtener modulos del curso |
| POST | `/api/tutor/ask` | Si | Enviar pregunta al tutor inteligente |
| GET | `/api/tutor/documents` | Si | Listar documentos disponibles |
| POST | `/api/tutor/index` | Si | Iniciar proceso de indexacion de documentos |
| GET | `/api/tutor/index/status/{taskId}` | Si | Consultar estado de indexacion |
| POST | `/api/tutor/documents/upload` | Si | Subir documentos (multipart/form-data) |

**Ejemplo de request para `/api/tutor/ask`:**
```json
{
  "question": "Dime que puedes hacer?",
  "module": "Ajustes Razonables",
  "user_id": "mario.martinez",
  "session_id": "123456789",
  "user_role": "estudiante",
  "course_id": "ajustes_razonables"
}
```

**Ejemplo de upload de documentos con curl:**
```bash
curl --location --request POST 'http://localhost:8080/api/tutor/documents/upload' \
  --header 'Authorization: Bearer <token>' \
  --form 'files=@"/path/to/document1.pdf"' \
  --form 'files=@"/path/to/document2.pdf"'
```

**Respuesta de `/api/tutor/documents`:**
```json
{
  "documents": [
    {
      "filename": "documento.pdf",
      "path": "pdfs\\documento.pdf",
      "size_bytes": 167204,
      "extension": ".pdf",
      "modified_at": "2026-01-20T21:17:13.526793"
    }
  ],
  "total_count": 1,
  "raw_path": "C:\\data\\raw"
}
```

**Respuesta de `/api/tutor/index`:**
```json
{
  "task_id": "task-uuid-123",
  "status": "started",
  "message": "Indexacion iniciada"
}
```

**Respuesta de `/api/tutor/index/status/{taskId}`:**
```json
{
  "task_id": "f3683b82-e723-4885-b891-d97d61790f5c",
  "status": "running",
  "message": "Generating embeddings...",
  "started_at": "2026-01-24T21:04:34.631403",
  "completed_at": null,
  "documents_processed": 8,
  "chunks_created": 559,
  "error": null
}
```

### Notificaciones
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/notificaciones/usuario/{usuarioId}` | Obtener todas las notificaciones de un usuario |
| GET | `/api/notificaciones/usuario/{usuarioId}/no-leidas` | Obtener notificaciones no leidas |
| GET | `/api/notificaciones/usuario/{usuarioId}/resumen` | Obtener resumen (conteo por prioridad) |
| GET | `/api/notificaciones/usuario/{usuarioId}/tipo/{tipo}` | Obtener notificaciones por tipo |
| GET | `/api/notificaciones/{id}` | Obtener notificacion por ID |
| PUT | `/api/notificaciones/{id}/leer` | Marcar notificacion como leida |
| PUT | `/api/notificaciones/usuario/{usuarioId}/leer-todas` | Marcar todas como leidas |
| DELETE | `/api/notificaciones/{id}` | Eliminar notificacion |

### Solicitudes de Sesion
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/solicitudes-sesion` | Crear solicitud de sesion |
| GET | `/api/solicitudes-sesion/estudiante/{estudianteId}` | Obtener solicitudes de un estudiante |
| GET | `/api/solicitudes-sesion/tutor/{tutorId}` | Obtener solicitudes de un tutor |
| GET | `/api/solicitudes-sesion/tutor/{tutorId}/pendientes` | Obtener solicitudes pendientes de un tutor |
| GET | `/api/solicitudes-sesion/{id}` | Obtener solicitud por ID |
| PUT | `/api/solicitudes-sesion/{id}/responder` | Responder a una solicitud (tutor) |
| DELETE | `/api/solicitudes-sesion/{id}/estudiante/{estudianteId}` | Cancelar solicitud (estudiante) |

### Asignacion Tutor-Estudiante
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/tutor-estudiante/asignar` | Asignar tutor a estudiante |
| GET | `/api/tutor-estudiante/tutor/{tutorId}/estudiantes` | Obtener estudiantes asignados a un tutor |
| GET | `/api/tutor-estudiante/estudiante/{estudianteId}/tutor` | Obtener tutor asignado a un estudiante |
| GET | `/api/tutor-estudiante/estudiante/{estudianteId}/tiene-tutor` | Verificar si estudiante tiene tutor |
| DELETE | `/api/tutor-estudiante/{id}` | Desactivar asignacion |

### Calendario de Modulos
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/calendario` | Listar todos los calendarios activos |
| GET | `/api/calendario/seccion/{seccionCodigo}` | Obtener calendario por seccion |
| GET | `/api/calendario/{id}` | Obtener calendario por ID |
| POST | `/api/calendario` | Crear nuevo calendario |
| PUT | `/api/calendario/{id}` | Actualizar calendario |
| DELETE | `/api/calendario/{id}` | Eliminar calendario |
| GET | `/api/calendario/proximos?dias=7` | Obtener proximos vencimientos |
| POST | `/api/calendario/verificar-vencimientos` | Ejecutar verificacion manual |

### Configuracion de Notificaciones
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/configuracion/notificaciones` | Listar todas las configuraciones |
| GET | `/api/configuracion/notificaciones/{clave}` | Obtener configuracion por clave |
| PUT | `/api/configuracion/notificaciones/{clave}?valor={valor}` | Actualizar valor de configuracion |

## Base de Datos

Esquema: `teia`

### Tablas Principales

| Tabla | Descripcion |
|-------|-------------|
| `usuarios` | Usuarios del sistema |
| `roles` | Roles (estudiante, tutor, admin) |
| `menus` / `menu_items` | Configuracion de menus por rol |
| `respuestas_seccion` | Respuestas de formularios (JSONB) |
| `equipo_docente` | Miembros del equipo docente |
| `temas_contenido` | Temas con subtemas (JSONB) |
| `progreso_secciones` | Estado de avance por seccion |
| `notificaciones` | Notificaciones del sistema |
| `tutor_estudiante` | Asignaciones tutor-estudiante |
| `solicitudes_sesion` | Solicitudes de sesion de tutoria |
| `calendario_modulo` | Fechas de vencimiento por modulo |
| `configuracion_notificacion` | Configuracion del sistema de notificaciones |

## WebSocket - Notificaciones en Tiempo Real

La aplicacion soporta notificaciones en tiempo real mediante WebSocket con STOMP sobre SockJS. Las notificaciones se envian automaticamente cuando ocurren eventos como solicitudes de sesion entre estudiantes y tutores.

### Arquitectura

```
Estudiante                    Servidor                         Tutor
    |                            |                               |
    |  POST /solicitudes-sesion  |                               |
    |--------------------------->|                               |
    |                            |  1. Guarda solicitud en BD    |
    |                            |  2. Persiste notificacion     |
    |                            |  3. Envia via WebSocket       |
    |                            |------------------------------>|
    |                            |     /user/queue/notificaciones|
    |  201 Created               |                               |
    |<---------------------------|                               |
```

### Flujo de Notificaciones Bidireccional

El sistema soporta notificaciones en ambas direcciones:

1. **Estudiante → Tutor:** Cuando el estudiante crea una solicitud de sesion
2. **Tutor → Estudiante:** Cuando el tutor responde a la solicitud (acepta/rechaza/completa)

### Configuracion del Endpoint

| Configuracion | Valor |
|---------------|-------|
| Endpoint WebSocket | `/ws/notificaciones` |
| Protocolo | STOMP sobre SockJS |
| Destino de suscripcion | `/user/queue/notificaciones` |
| Autenticacion | JWT en header `Authorization` |

### Requisitos Importantes

- El cliente debe conectarse con un **token JWT valido** del usuario que recibira las notificaciones
- Para solicitudes de sesion, el **tutor** debe estar conectado con su propio token para recibir las notificaciones
- Para respuestas de solicitud, el **estudiante** debe estar conectado con su propio token
- El token se envia en el header `Authorization: Bearer {token}` durante la conexion STOMP

### Nota Tecnica: Identificacion de Usuario

El sistema identifica a los usuarios conectados por WebSocket usando el **username** (parte del correo antes del `@`), no el correo completo ni el ID numerico.

Por ejemplo:
- Correo: `tutor@unbosque.edu.co`
- Username en sesion STOMP: `tutor`

Esto significa que cuando el servidor envia una notificacion, internamente resuelve el ID del usuario a su username para encontrar la sesion WebSocket correcta.

### Tipos de Notificacion

El sistema soporta 4 tipos de notificaciones, todas enviadas via WebSocket en tiempo real:

| Tipo | Descripcion | Destinatario | Disparador |
|------|-------------|--------------|------------|
| `SOLICITUD_SESION` | Estudiante solicita sesion con tutor | Tutor | `POST /api/solicitudes-sesion` |
| `RESPUESTA_SOLICITUD` | Tutor responde a solicitud de sesion | Estudiante | `PUT /api/solicitudes-sesion/{id}/responder` |
| `VENCIMIENTO_PROXIMO` | Modulo/seccion proxima a vencer | Estudiante o Tutor | Scheduler automatico |
| `UMBRAL_ALCANZADO` | Estudiante alcanzo umbral de progreso | Tutor | Actualizacion de progreso |

#### SOLICITUD_SESION

Notificacion enviada al tutor cuando un estudiante solicita una sesion de tutoria.

- **Disparador:** `POST /api/solicitudes-sesion`
- **Destinatario:** Tutor asignado al estudiante
- **Prioridad:** INFO

```json
{
    "tipo": "SOLICITUD_SESION",
    "prioridad": "INFO",
    "severity": "info",
    "titulo": "Nueva solicitud de sesion",
    "mensaje": "El estudiante Maria Garcia ha solicitado una sesion contigo.",
    "datosAdicionales": {
        "solicitudId": 123,
        "estudianteId": 1055,
        "nombreEstudiante": "Maria Garcia",
        "motivo": "Necesito ayuda con el modulo 2"
    }
}
```

#### RESPUESTA_SOLICITUD

Notificacion enviada al estudiante cuando el tutor responde a su solicitud de sesion.

- **Disparador:** `PUT /api/solicitudes-sesion/{id}/responder`
- **Destinatario:** Estudiante que creo la solicitud
- **Prioridad:** Variable segun el estado de la respuesta
  - `SUCCESS`: Si el tutor acepta la solicitud
  - `ALERTA`: Si el tutor rechaza la solicitud
  - `INFO`: Si el tutor marca la sesion como completada

```json
{
    "tipo": "RESPUESTA_SOLICITUD",
    "prioridad": "SUCCESS",
    "severity": "success",
    "titulo": "Solicitud de sesion aceptada",
    "mensaje": "El tutor Juan Perez ha aceptado tu solicitud de sesion.",
    "datosAdicionales": {
        "solicitudId": 123,
        "tutorId": 1056,
        "nombreTutor": "Juan Perez",
        "estado": "ACEPTADA",
        "notasTutor": "Te contactare por correo para coordinar la sesion"
    }
}
```

**Ejemplo de solicitud rechazada:**
```json
{
    "tipo": "RESPUESTA_SOLICITUD",
    "prioridad": "ALERTA",
    "severity": "warn",
    "titulo": "Solicitud de sesion rechazada",
    "mensaje": "El tutor Juan Perez ha rechazado tu solicitud de sesion.",
    "datosAdicionales": {
        "solicitudId": 123,
        "tutorId": 1056,
        "nombreTutor": "Juan Perez",
        "estado": "RECHAZADA",
        "notasTutor": "En este momento no tengo disponibilidad, intenta de nuevo la proxima semana"
    }
}
```

#### VENCIMIENTO_PROXIMO

Notificacion enviada cuando un modulo o seccion esta proximo a vencer. Se envia tanto al estudiante como a su tutor.

- **Disparador:** Scheduler automatico (8:00 AM diario) o `POST /api/calendario/verificar-vencimientos`
- **Destinatario:** Estudiante (sobre sus modulos) y Tutor (sobre modulos de sus estudiantes)
- **Prioridad:** Variable segun dias restantes y estado de progreso
  - `CRITICO`: Menos de 2 dias y progreso < 50%
  - `ALERTA`: Menos de 5 dias y progreso < 75%
  - `INFO`: Otros casos

```json
{
    "tipo": "VENCIMIENTO_PROXIMO",
    "prioridad": "ALERTA",
    "severity": "warn",
    "titulo": "Modulo proximo a vencer",
    "mensaje": "El modulo 'Introduccion al RAC' vence en 3 dias",
    "datosAdicionales": {
        "seccionCodigo": "MOD_01",
        "nombreModulo": "Introduccion al RAC",
        "fechaLimite": "2026-01-27",
        "diasRestantes": 3,
        "estadoProgreso": "EN_PROGRESO",
        "estudianteId": 1055,
        "nombreEstudiante": "Maria Garcia"
    }
}
```

#### UMBRAL_ALCANZADO

Notificacion enviada al tutor cuando un estudiante alcanza un umbral de progreso configurado (ej: 25%, 50%, 75%, 100%).

- **Disparador:** `PUT /api/bitacora/progreso/usuario/{id}/seccion/{codigo}`
- **Destinatario:** Tutor del estudiante
- **Prioridad:** SUCCESS

```json
{
    "tipo": "UMBRAL_ALCANZADO",
    "prioridad": "SUCCESS",
    "severity": "success",
    "titulo": "Umbral de progreso alcanzado",
    "mensaje": "El estudiante Maria Garcia ha alcanzado el 75% en el modulo Introduccion al RAC",
    "datosAdicionales": {
        "estudianteId": 1055,
        "nombreEstudiante": "Maria Garcia",
        "seccionCodigo": "MOD_01",
        "nombreModulo": "Introduccion al RAC",
        "porcentajeAlcanzado": 75
    }
}
```

### Prioridades de Notificacion

| Prioridad | Severity | Color | Uso |
|-----------|----------|-------|-----|
| `CRITICO` | error | Rojo | Vencimientos inminentes sin progreso |
| `ALERTA` | warn | Naranja | Vencimientos proximos, solicitudes rechazadas |
| `INFO` | info | Azul | Solicitudes de sesion, informacion general |
| `SUCCESS` | success | Verde | Logros, umbrales alcanzados, solicitudes aceptadas |

### Ejemplo de Prueba: Flujo Estudiante → Tutor

**Paso 1:** Asignar un tutor a un estudiante (si no existe la asignacion)
```http
POST http://localhost:8080/api/tutor-estudiante/asignar
Authorization: Bearer {token_admin_o_tutor}
Content-Type: application/json

{
    "tutorId": 1056,
    "estudianteId": 1055
}
```

**Paso 2:** Conectar el cliente WebSocket del tutor
- Ingresar el token JWT del **tutor** (usuario 1056)
- Suscribirse a `/user/queue/notificaciones`

**Paso 3:** Crear una solicitud de sesion como estudiante
```http
POST http://localhost:8080/api/solicitudes-sesion
Authorization: Bearer {token_estudiante}
Content-Type: application/json

{
    "estudianteId": 1055,
    "motivo": "Necesito ayuda con el modulo 2"
}
```

**Paso 4:** Verificar que el tutor recibe la notificacion `SOLICITUD_SESION` en tiempo real

### Ejemplo de Prueba: Flujo Tutor → Estudiante

**Paso 1:** Conectar el cliente WebSocket del estudiante
- Ingresar el token JWT del **estudiante** (usuario 1055)
- Suscribirse a `/user/queue/notificaciones`

**Paso 2:** Responder a la solicitud como tutor
```http
PUT http://localhost:8080/api/solicitudes-sesion/{id}/responder
Authorization: Bearer {token_tutor}
Content-Type: application/json

{
    "estado": "ACEPTADA",
    "notasTutor": "Te contactare por correo para coordinar la sesion"
}
```

**Paso 3:** Verificar que el estudiante recibe la notificacion `RESPUESTA_SOLICITUD` en tiempo real

### Configuracion del Sistema

Las notificaciones WebSocket pueden habilitarse/deshabilitarse via configuracion:

```http
PUT http://localhost:8080/api/configuracion/notificaciones/websocket.habilitado?valor=true
Authorization: Bearer {token}
```

## Ejecucion de Tests

```bash
./gradlew test
```

## Build para Produccion

```bash
./gradlew build -x test
```

El artefacto se genera en `build/libs/bitacora-digital-0.0.1-SNAPSHOT.war`
