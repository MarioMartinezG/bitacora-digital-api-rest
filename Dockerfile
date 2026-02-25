# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cachear dependencias antes del código fuente.
# Solo se invalida si cambia build.gradle o settings.gradle.
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Compilar y empaquetar (sin tests; correrlos en un paso de CI separado)
COPY src ./src
RUN ./gradlew bootWar -x test --no-daemon

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no-root para producción
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/build/libs/app.war app.war

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
