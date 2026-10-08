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

# Crear un usuario y grupo de sistema "spring" no privilegiado para ejecutar la aplicación (Hardening)
RUN addgroup -S spring && adduser -S spring -G spring

# Crear un directorio temporal seguro y asignar permisos
RUN mkdir -p /tmp/tomcat && chown -R spring:spring /tmp/tomcat

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
