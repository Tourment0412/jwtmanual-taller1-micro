# Guía de Inicio Rápido - JWT Manual Taller 1 Microservice

Esta guía te permitirá poner en marcha el microservicio de autenticación JWT en tu entorno local y ejecutar pruebas básicas.

## Requisitos Previos

- Java 17 o superior
- Maven 3.6 o superior
- PostgreSQL 12 o superior
- Docker y Docker Compose (opcional, para servicios dependientes)

## Instalación Rápida

### 1. Clonar y Compilar

```bash
cd jwtmanual-taller1-micro
./mvnw clean install -DskipTests
```

### 2. Configurar Base de Datos

#### Opción A: PostgreSQL Local

Crear base de datos:
```sql
CREATE DATABASE domain;
CREATE USER domain_user WITH PASSWORD 'domain_pass';
GRANT ALL PRIVILEGES ON DATABASE domain TO domain_user;
```

#### Opción B: PostgreSQL con Docker

```bash
docker run -d --name postgres-domain \
  -e POSTGRES_DB=domain \
  -e POSTGRES_USER=domain_user \
  -e POSTGRES_PASSWORD=domain_pass \
  -p 5432:5432 \
  postgres:15
```

### 3. Configurar Variables de Entorno

Crear archivo `src/main/resources/application-local.properties`:

```properties
# Base de Datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/domain
SPRING_DATASOURCE_USERNAME=domain_user
SPRING_DATASOURCE_PASSWORD=domain_pass

# JWT
JWT_SECRET=mySecretKey123456789012345678901234567890
JWT_EXPIRATION=86400000

# RabbitMQ (opcional)
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Server
SERVER_PORT=8080
```

### 4. Ejecutar Localmente

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Verificación Inicial

### Health Check

```bash
curl http://localhost:8080/v1/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "checks": [
    {"name": "database", "status": "UP"},
    {"name": "application", "status": "UP"}
  ]
}
```

### Readiness Check

```bash
curl http://localhost:8080/v1/health/ready
```

### Liveness Check

```bash
curl http://localhost:8080/v1/health/live
```

## Pruebas Básicas

### 1. Registro de Usuario

```bash
curl -X POST http://localhost:8080/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": "testuser",
    "correo": "test@example.com",
    "clave": "password123",
    "numeroTelefono": "+573001234567"
  }'
```

Respuesta esperada: HTTP 201 con mensaje de éxito

### 2. Autenticación (Login)

```bash
curl -X POST http://localhost:8080/v1/sesiones \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": "testuser",
    "clave": "password123"
  }'
```

Respuesta esperada: HTTP 200 con token JWT

Guardar el token:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/v1/sesiones \
  -H "Content-Type: application/json" \
  -d '{"usuario":"testuser","clave":"password123"}' | jq -r '.token')
echo $TOKEN
```

### 3. Obtener Usuario (con token)

```bash
curl -X GET http://localhost:8080/v1/usuarios/testuser \
  -H "Authorization: Bearer $TOKEN"
```

Respuesta esperada: HTTP 200 con datos del usuario

### 4. Actualizar Usuario

```bash
curl -X PATCH http://localhost:8080/v1/usuarios/testuser \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "correo": "newemail@example.com",
    "numeroTelefono": "+573009876543"
  }'
```

Respuesta esperada: HTTP 200 con datos actualizados

### 5. Cambiar Contraseña

Primero, obtener código de verificación:
```bash
curl -X POST http://localhost:8080/v1/usuarios/testuser/codigo \
  -H "Authorization: Bearer $TOKEN"
```

Luego, cambiar contraseña:
```bash
curl -X PATCH http://localhost:8080/v1/usuarios/testuser/clave \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "codigo": "<codigo_recibido>",
    "clave": "newpassword123"
  }'
```

## Ejecutar Tests

### Tests Unitarios

```bash
./mvnw test
```

### Tests con Cobertura

```bash
./mvnw test jacoco:report
```

Ver reporte en: `target/site/jacoco/index.html`

## Validar Token JWT

### Decodificar Token (sin verificar firma)

Usar herramienta online como https://jwt.io o:

```bash
echo $TOKEN | cut -d. -f2 | base64 -d | jq
```

### Verificar Token con API

El token se valida automáticamente en cada solicitud protegida. Si el token es inválido o expirado, recibirás HTTP 401.

## Verificar Base de Datos

### Conectar a PostgreSQL

```bash
psql -h localhost -U domain_user -d domain
```

### Verificar Tabla de Usuarios

```sql
SELECT usuario, correo, fecha_creacion FROM usuarios;
```

## Troubleshooting

### Error: Connection refused a PostgreSQL

Verificar que PostgreSQL esté corriendo:
```bash
psql -h localhost -U postgres -c "SELECT version();"
```

### Error: Token inválido o expirado

Verificar que el token no haya expirado. El tiempo de expiración por defecto es 24 horas (86400000 ms).

Generar un nuevo token con login.

### Error: Usuario ya existe

El usuario ya está registrado. Intentar con otro nombre de usuario o eliminar el usuario existente (requiere rol ADMIN).

### Error: Health check falla

Verificar que todos los componentes estén disponibles:
- Base de datos: `psql -h localhost -U domain_user -d domain`
- RabbitMQ (si está configurado): `curl http://localhost:15672`

## Próximos Pasos

- Revisar `docs/IMPLEMENTATION.md` para detalles de arquitectura
- Configurar integración con RabbitMQ para eventos de dominio
- Explorar documentación Swagger/OpenAPI si está habilitada
- Revisar logs en salida de consola para debugging

