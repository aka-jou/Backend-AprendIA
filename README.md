# AprendIA - Servicio Backend (Java Spring Boot)

Este proyecto constituye la base del backend del sistema **AprendIA**, diseñado desde cero siguiendo principios de arquitectura limpia por capas, seguridad robusta (JWT) y contenedores optimizados listos para producción.

---

## 🛠️ Requisitos Previos

Antes de comenzar, asegúrate de tener instalado en tu máquina local:
- **Git** (para control de versiones).
- **Docker** y **Docker Compose** (V2.x o superior).
- **Java 21 (LTS)** y **Maven** (Opcional, solo si deseas compilar y ejecutar de forma local fuera de Docker).

---

## 🚀 Guía de Inicio Rápido

Sigue estos sencillos pasos para clonar, configurar, construir y ejecutar la aplicación en contenedores.

### 1. Clonar el Repositorio
Abre tu terminal y ejecuta el siguiente comando para descargar el código del backend:
```bash
git clone <url-de-este-repositorio> aprendia-backend
cd aprendia-backend
```

### 2. Configurar el Archivo de Entorno (`.env`)
El proyecto está diseñado para inyectar todas las credenciales sensibles a través de variables de entorno, evitando guardar secretos en el código fuente.

Copia la plantilla de configuración `.env.example` para crear tu archivo `.env` local:
```bash
cp .env.example .env
```
Abre el archivo `.env` con tu editor preferido y personaliza los valores si es necesario. Por defecto, ya incluye configuraciones seguras y listas para usar:
* **`DB_USERNAME`**: `aprendia_app` (Usuario ad-hoc sin privilegios de superusuario global).
* **`DB_PASSWORD`**: Contraseña compleja y robusta por defecto.
* **`JWT_SECRET`**: Clave de firma simétrica de 256 bits para firmar los tokens JWT.

### 3. Construir y Levantar los Contenedores
Docker Compose construirá automáticamente la imagen del backend en base al `Dockerfile` multi-etapa y descargará la base de datos PostgreSQL con soporte vectorial (`pgvector`).

Para iniciar la base de datos y la aplicación en segundo plano (`detached mode`), ejecuta:
```bash
docker compose up --build -d
```

> [!NOTE]
> Al iniciar por primera vez, el contenedor de PostgreSQL (`db-aprend-ia`) creará la base de datos `aprendiadb` y el usuario `aprendia_app` con permisos de propietario. Una vez listo, el backend iniciará y **Flyway** ejecutará de forma automática el script `V1__init_schema.sql` para crear las tablas base (`users`, `roles`, `user_roles`).

---

## 🔍 Monitoreo y Verificación

### 1. Comprobar el Estado de los Contenedores
Para verificar que ambos contenedores se están ejecutando correctamente y comprobar los puertos expuestos, ejecuta:
```bash
docker compose ps
```

Deberías ver una salida similar a esta indicando que ambos servicios están `running`:
```text
NAME          IMAGE                  COMMAND                  SERVICE        CREATED         STATUS                   PORTS
aprendia_api  aprendia-backend-app   "java -XX:+UseG1GC..."   aprendia-api   1 minute ago    Up 1 minute              0.0.0.0:8080->8080/tcp
aprendia_db   pgvector/pgvector:pg16 "docker-entrypoint.s…"   db-aprend-ia   1 minute ago    Up 1 minute (healthy)    0.0.0.0:5432->5432/tcp
```

### 2. Revisar los Logs de Ejecución
Para visualizar e inspeccionar las salidas de consola y logs del servidor del backend en tiempo real:
```bash
docker compose logs -f aprendia-api
```

### 3. Validar el Estado de Salud (Health Check)
La aplicación cuenta con Spring Boot Actuator configurado. Puedes hacer una solicitud HTTP GET al endpoint de salud para verificar la conectividad de la aplicación y la base de datos:
```bash
curl -s http://localhost:8080/api/actuator/health
```

**Respuesta exitosa esperada (JSON):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "discovery": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## 💻 Uso de la API y Endpoints de Prueba

### 1. Registrar un nuevo Usuario
Por defecto, la API tiene restringidos todos los endpoints de negocio. Lo primero que debes hacer es registrar una cuenta de usuario. Los registros están expuestos en la ruta pública `/api/auth/register`.

**Comando cURL para Registro:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alonso_dev",
    "email": "alonso@aprendia.com",
    "password": "contrasena_segura_123"
  }'
```

**Respuesta JSON esperada:**
```json
{
  "id": 1,
  "username": "alonso_dev",
  "email": "alonso@aprendia.com",
  "isActive": true,
  "roles": ["ROLE_USER"]
}
```

### 2. Iniciar Sesión (Login) y Obtener Token JWT
Una vez registrado, inicia sesión para autenticarte y obtener tu token Bearer JWT.

**Comando cURL para Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alonso_dev",
    "password": "contrasena_segura_123"
  }'
```

**Respuesta JSON esperada:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbG9uc29fZGV2IiwiaWF0IjoxNzM...",
  "tokenType": "Bearer",
  "username": "alonso_dev",
  "email": "alonso@aprendia.com",
  "roles": ["ROLE_USER"]
}
```
> [!IMPORTANT]
> Copia el valor de `"accessToken"` de la respuesta. Lo necesitarás para las solicitudes a los endpoints protegidos.

### 3. Probar Endpoints Protegidos

#### A. Acceso Denegado (Sin Token)
Si intentas acceder a un perfil de usuario o ruta de negocio sin proporcionar el token Bearer, el filtro de Spring Security rechazará la petición inmediatamente con un estado **401 Unauthorized**:
```bash
curl -i http://localhost:8080/api/users/1
```
**Respuesta:**
```text
HTTP/1.1 401 Unauthorized
Content-Type: application/json;charset=UTF-8

{"error": "Unauthorized", "message": "No autorizado para acceder a este recurso."}
```

#### B. Acceso Exitoso (Con Token JWT)
Realiza la petición incluyendo el token en las cabeceras HTTP (`Authorization: Bearer <TU_TOKEN>`):
```bash
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <COPIA_AQUÍ_EL_TOKEN_JWT>"
```
**Respuesta:**
```json
{
  "id": 1,
  "username": "alonso_dev",
  "email": "alonso@aprendia.com",
  "isActive": true,
  "roles": ["ROLE_USER"]
}
```

---

## 📖 Documentación Interactiva (Swagger / OpenAPI UI)

Cuando la aplicación esté corriendo, puedes acceder a la consola interactiva de **Swagger UI** en tu navegador web:
👉 **[http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)**

Desde esta interfaz podrás:
- Consultar el esquema de todos los DTOs de petición y respuesta.
- Autenticarte ingresando el token JWT usando el botón **"Authorize"** en la esquina superior derecha (escribe `Bearer <tu_token>`).
- Probar interactivamente cada uno de los endpoints de la API REST directamente desde el navegador.

---

## 🛑 Detener los Contenedores
Para detener y eliminar los contenedores y redes creadas por Docker Compose sin perder los datos de la base de datos (los datos se conservan en un volumen persistente):
```bash
docker compose down
```

Si deseas borrar también los datos almacenados de la base de datos:
```bash
docker compose down -v
```
