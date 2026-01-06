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
version: '3.8'

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

## Estructura del Proyecto

```
src/main/java/com/diginexa/bitacora/
├── annotations/       # Conversores JSONB personalizados
├── config/           # Configuracion de seguridad y JWT
├── constants/        # Constantes (codigos de seccion)
├── controllers/      # Controladores REST
├── dtos/             # Objetos de transferencia
├── entities/         # Entidades JPA
├── exceptions/       # Manejo de excepciones
├── repositories/     # Repositorios Spring Data
├── services/         # Logica de negocio
└── validation/       # Validaciones personalizadas
```

## API Endpoints

### Autenticacion
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesion |
| POST | `/api/auth/register` | Registrar usuario |
| POST | `/api/auth/refresh` | Refrescar token |

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

## Ejecucion de Tests

```bash
./gradlew test
```

## Build para Produccion

```bash
./gradlew build -x test
```

El artefacto se genera en `build/libs/bitacora-digital-0.0.1-SNAPSHOT.war`
