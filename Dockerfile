# ==========================================
# ETAPA 1: Construcción (Build Stage)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar el archivo pom.xml primero para aprovechar el caché de Docker con las dependencias
COPY pom.xml .

# Descargar las dependencias en modo offline para acelerar construcciones subsecuentes
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar la aplicación omitiendo las pruebas unitarias para agilizar el build del contenedor
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: Ejecución (Runtime Stage)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# UID/GID del usuario de ejecución. En la VPS se pasan los del usuario dueño de la carpeta del proyecto
# (APP_UID/APP_GID en .env) para que los archivos de ./logs montados desde el host le pertenezcan.
ARG APP_UID=1000
ARG APP_GID=1000

# Crear un usuario y grupo "spring" no privilegiado para ejecutar la aplicación (Hardening)
RUN addgroup -S -g ${APP_GID} spring && adduser -S -u ${APP_UID} -G spring spring

# Directorio temporal y directorio de logs con permisos para el usuario de ejecución
RUN mkdir -p /tmp/tomcat /app/logs && chown -R spring:spring /tmp/tomcat /app/logs

# Copiar el archivo JAR compilado desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Asignar la propiedad del archivo JAR al usuario spring
RUN chown spring:spring app.jar

# Cambiar al usuario no-root "spring"
USER spring

# Exponer el puerto por defecto de la aplicación
EXPOSE 8080

# Parámetros recomendados de JVM para entornos contenerizados y directorio temporal configurable
ENTRYPOINT ["java", "-XX:+UseG1GC", "-Djava.security.egd=file:/dev/./urandom", "-Djava.io.tmpdir=/tmp/tomcat", "-jar", "app.jar"]
