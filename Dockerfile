# ============================================================
# Stage 1: Build
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar pom.xml y descargar dependencias (caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Crear usuario no-root para seguridad
RUN groupadd -r library && useradd -r -g library library

# Crear directorio de logs
RUN mkdir -p /app/logs && chown -R library:library /app

# Copiar JAR desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Cambiar al usuario no-root
USER library

# Puerto de la aplicación
EXPOSE 8080

# Variables de entorno con valores por defecto
ENV SERVER_PORT=8080 \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=library_db \
    DB_USERNAME=library_user \
    DB_PASSWORD=library_pass \
    JWT_SECRET=VGhpc0lzQVZlcnlMb25nU2VjcmV0S2V5Rm9yTGlicmFyeVN5c3RlbTIwMjQ= \
    JWT_EXPIRATION=900000 \
    JWT_REFRESH_EXPIRATION=604800000 \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Arrancar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
