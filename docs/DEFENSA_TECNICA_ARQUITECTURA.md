# Defensa técnica: API Gateway y Cifrado en reposo

**Proyecto:** AprendIA — backend (`aprendia-backend`)
**Fecha:** 30 de julio de 2026
**Alcance:** justificación técnica de dos decisiones de arquitectura pedidas por el profesor de Arquitectura Orientada a Servicios, más una guía paso a paso para demostrarlas.

---

## 1. API Gateway

### 1.1 El problema

Antes de este cambio, la app móvil (`icheja_mobile`) le pegaba **directo a tres backends distintos**, cada uno con su URL hardcodeada en el cliente:
- `aprendia-backend` (autenticación, catálogos, usuarios)
- Un microservicio externo de evaluación de lectura (`20.245.212.209:5000`)
- Un microservicio externo de evaluación de escritura (`20.245.212.209:5001`)

Y lo que existía como "puerta de entrada" (`nginx.conf`) era un *reverse proxy* simple: todo el tráfico se reenviaba a un único backend, sin lógica de enrutamiento — no era un Gateway, era solo terminación TLS + forwarding.

### 1.2 Por qué Spring Cloud Gateway y no solo Nginx

Se evaluaron dos caminos:

1. **Extender el Nginx existente** con reglas de enrutamiento por path hacia los 3 servicios — más rápido de montar, pero sigue siendo configuración de infraestructura, no un componente de software con lógica propia.
2. **Spring Cloud Gateway** (elegido): un servicio Java real, separado, que demuestra el patrón con código — coherente con que el resto de la arquitectura ya es Spring Boot, y es el estándar del ecosistema para este problema exacto (enrutar y centralizar preocupaciones transversales entre microservicios).

### 1.3 Diseño

Proyecto nuevo e independiente: `aprendia-gateway` (Spring Boot 3.4.0 + Spring Cloud Gateway 2024.0.0, reactivo sobre Netty/WebFlux).

**Rutas** (definidas en código, no en YAML — ver §1.4):

| Ruta pública del Gateway | Destino real | Transformación |
|---|---|---|
| `/backend/**` | `aprendia-backend:8080` | Reescribe `/backend/x` → `/api/x` (el context-path real del backend) |
| `/evaluate/reading` | Microservicio IA (`:5000`) | Reescribe a `/evaluate` |
| `/evaluate/writing` | Microservicio IA (`:5001`) | Reescribe a `/image/compare` |

**Preocupación transversal centralizada**: un `GlobalFilter` ([LoggingGlobalFilter.java](../../aprendia-gateway/src/main/java/com/aprendia/gateway/filter/LoggingGlobalFilter.java)) registra método, ruta, código de respuesta y latencia de **cada** request que pasa por el Gateway — sin que cada servicio downstream tenga que implementar su propio logging de acceso. Este es el ejemplo mínimo y más claro de *por qué* centralizar en un Gateway aporta valor real, no solo cumplir con el nombre del patrón.

### 1.4 Detalle técnico: por qué las rutas están en código Java y no en `application.yml`

Spring Cloud Gateway soporta declarar rutas en YAML, pero el filtro `RewritePath` usa la sintaxis `${grupo}` para referirse a grupos capturados por regex — y esa misma sintaxis es la que usa Spring Boot para resolver *placeholders* de propiedades al cargar el YAML. Esto genera un conflicto conocido: Spring intenta resolver `${segment}` como una propiedad de configuración antes de que el Gateway lo use como su propio placeholder de regex. La solución fue declarar las rutas con el DSL Java (`RouteLocatorBuilder`) en [GatewayRoutesConfig.java](../../aprendia-gateway/src/main/java/com/aprendia/gateway/config/GatewayRoutesConfig.java) — mismo resultado, sin el conflicto, y como beneficio extra: rutas más legibles y con nombres explícitos (`aprendia-backend`, `evaluate-reading`, `evaluate-writing`).

### 1.5 Qué NO se implementó todavía (y por qué)

- **Validación de JWT en el Gateway**: hoy `aprendia-backend` sigue validando el JWT directamente. Centralizarlo en el Gateway es la evolución natural (evitaría que cada servicio downstream tenga que validar), pero no había un segundo servicio Spring que se beneficiara de esto todavía — se documenta como mejora futura en vez de construirse sin un caso de uso real.
- **CORS centralizado**: similar — hoy el Gateway sí tiene su propio CORS (necesario porque intercepta el preflight antes de reenviar), pero `aprendia-backend` también mantiene el suyo por compatibilidad con quien le pegue directo sin pasar por el Gateway.

---

## 2. Cifrado en reposo

### 2.1 El hallazgo

Existía una propiedad `app.encryption.secret` (`ENCRYPTION_SECRET` en `.env`) declarada en `application.yml` pero **usada por ninguna clase Java** — infraestructura preparada y nunca conectada. Mientras tanto, `Person.curp` (identificación nacional mexicana), `Person.phone` y los campos de `Address` (calle, número, colonia, código postal) se guardaban en **texto plano** en Postgres.

### 2.2 Qué se cifra y qué no

**Sí**: `curp`, `phone` (en `Person`); `street`, `exteriorNumber`, `settlement`, `zipCode` (en `Address`) — es la información de identificación y localización más sensible del estudiante.

**No** (por ahora): nombres, fecha de nacimiento, género. No son el dato que un atacante usaría para suplantar identidad o localizar físicamente a un menor, y cifrarlos habría ampliado el alcance del cambio sin una ganancia de seguridad proporcional.

### 2.3 Por qué cifrado determinístico (y no AES estándar con IV aleatorio)

`Person.curp` tiene una restricción `UNIQUE` en la base de datos y se busca por igualdad exacta (`existsByCurp`, `findByCurp`, usado para no registrar el mismo estudiante dos veces). Un cifrado con IV aleatorio produce un resultado **distinto cada vez**, incluso para el mismo CURP — eso rompe tanto el `UNIQUE` como cualquier búsqueda por igualdad.

La solución: **IV determinístico derivado del propio texto plano vía HMAC-SHA256** (`IV = HMAC-SHA256(clave_mac, texto_plano)`, truncado a 12 bytes). El mismo CURP siempre genera el mismo IV y por tanto el mismo texto cifrado — se preserva la unicidad y las búsquedas sin tocar una sola línea de `PersonRepository` ni de `UserServiceImpl` (Hibernate aplica el conversor también a los parámetros de búsqueda, de forma transparente).

**Trade-off aceptado y documentado**: el cifrado determinístico revela *qué filas comparten el mismo valor* (dos estudiantes con el mismo CURP se ven como el mismo blob cifrado), pero no el valor en sí. Es el trade-off estándar y aceptado en la industria para poder mantener una columna cifrada que además necesita ser buscable/única — la alternativa (cifrado aleatorio + una columna extra de hash solo para búsqueda) da más margen de seguridad pero exige tocar el esquema y las queries; se descartó por ahora al no haber un requisito que lo exigiera.

### 2.4 Cómo funciona (resumen técnico)

- `EncryptedStringConverter` ([EncryptedStringConverter.java](../src/main/java/com/aprendia/backend/security/crypto/EncryptedStringConverter.java)): un `AttributeConverter<String,String>` de JPA, aplicado vía `@Convert` en los campos listados en §2.2.
- Algoritmo: **AES-256-GCM** (autenticado — detecta si el dato fue manipulado, no solo lo oculta), usando únicamente clases del JDK (`javax.crypto`), sin dependencias externas nuevas.
- Se derivan **dos claves separadas** (`encKey` para cifrar, `macKey` para el IV) a partir del mismo secreto configurado, para no reutilizar una clave en dos primitivas criptográficas distintas — buena práctica estándar de separación de claves.
- Se guarda `Base64(IV || texto_cifrado || tag_de_autenticación)` en la columna. Esto ocupa más espacio que el texto plano original, por lo que la migración `V15` ensanchó las columnas afectadas.

---

## 3. Guía para la demo con el profesor

Todo esto ya está corriendo en local (Docker) con datos de prueba reales. Sugerencia de orden: **Gateway → Cifrado** (de lo más visible/arquitectónico a lo más "bajo el capó").

### Preparación (antes de empezar)
```bash
cd aprendia-backend
docker compose up -d --build      # backend + BD
cd ../aprendia-gateway
docker compose up -d --build      # Gateway (se une a la red del backend)
```
Swagger disponible en `http://localhost:8080/api/swagger-ui.html`. pgAdmin conectado a `localhost:5433 / aprendiadb / aprendia_app`.

### 3.1 Demostrar el API Gateway
1. Abre `nginx.conf` en pantalla y muestra el `location /` que reenvía todo a un solo backend — **"esto es lo que había antes: un proxy, no un gateway"**.
2. Muestra [GatewayRoutesConfig.java](../../aprendia-gateway/src/main/java/com/aprendia/gateway/config/GatewayRoutesConfig.java): explica las 3 rutas hacia 3 servicios distintos.
3. En terminal, compara la misma petición por las dos vías:
   ```bash
   curl http://localhost:8080/api/v1/health        # directo al backend
   curl http://localhost:8081/backend/v1/health     # a través del Gateway — misma respuesta
   ```
4. Corre `docker logs aprendia_gateway --tail 20` y muestra las líneas de log del `LoggingGlobalFilter` (método, ruta, status, latencia) — **"esto es la preocupación transversal centralizada: ningún servicio downstream tiene que loguear esto por su cuenta"**.

### 3.2 Demostrar el cifrado
1. En pgAdmin, `SELECT curp, phone FROM persons;` — el profesor ve un blob Base64 ilegible.
2. En la misma sesión, `GET /v1/users/students/{id}` (autenticado) — la respuesta trae el CURP y teléfono **en texto plano, correctos**. **"La aplicación nunca ve el dato cifrado — Hibernate descifra al leer, sin que el código de negocio se entere."**
3. Punto extra de defensa: intenta registrar un estudiante con un CURP que ya existe → sigue rechazándose con el mismo error de siempre, **prueba de que la unicidad se mantiene sobre el dato cifrado** (por ser determinístico).

### Preguntas que probablemente haga el profesor (y respuesta corta)

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué Spring Cloud Gateway y no Kong/Nginx/Zuul? | Ecosistema ya 100% Spring Boot; se aprovecha el mismo stack, el mismo equipo lo entiende, y el Gateway se puede versionar/testear como cualquier servicio Spring. |
| ¿El cifrado es reversible? | Sí, por diseño — es cifrado (no hash), la aplicación necesita mostrar el CURP real al admin. Lo que se protege es el acceso directo a la base de datos, no el uso legítimo desde la aplicación. |
| ¿Por qué determinístico no es menos seguro? | Solo revela igualdad entre filas, nunca el valor. Es el trade-off aceptado en toda la industria para columnas cifradas que necesitan ser únicas/buscables (ej. cifrado determinístico de AWS/Google Cloud para este mismo caso). |
