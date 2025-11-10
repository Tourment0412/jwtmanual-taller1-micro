# JWT Manual Taller 1 Microservice

Microservicio de dominio desarrollado en Java con Spring Boot que gestiona la autenticación y autorización de usuarios mediante tokens JWT. Proporciona funcionalidades completas para registro, autenticación, gestión de usuarios y generación de tokens de acceso.

## Funcionalidades

- Registro de usuarios
- Autenticación con generación de tokens JWT
- Gestión de usuarios (consulta, actualización, eliminación)
- Cambio de contraseña con código de verificación
- Health checks (database, application, RabbitMQ)
- Publicación de eventos de dominio a RabbitMQ

## Endpoints de la API

### Endpoints Públicos

#### Registro de Usuario

- **Endpoint**: `POST /v1/usuarios`
- **Descripción**: Registra un nuevo usuario en el sistema
- **Autenticación**: No requerida

**Request Body**:
```json
{
  "usuario": "testuser",
  "correo": "test@example.com",
  "clave": "password123",
  "numeroTelefono": "+573001234567"
}
```

#### Autenticación (Login)

- **Endpoint**: `POST /v1/sesiones`
- **Descripción**: Autentica un usuario y genera token JWT
- **Autenticación**: No requerida

**Request Body**:
```json
{
  "usuario": "testuser",
  "clave": "password123"
}
```

**Response**: Token JWT y datos del usuario

#### Health Check

- **Endpoint**: `GET /v1/health`
- **Descripción**: Verifica el estado de salud del servicio
- **Autenticación**: No requerida

**Response**:
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "database",
      "status": "UP",
      "data": {}
    },
    {
      "name": "application",
      "status": "UP",
      "data": {}
    },
    {
      "name": "rabbitmq",
      "status": "UP",
      "data": {}
    }
  ]
}
```

#### Readiness Check

- **Endpoint**: `GET /v1/health/ready`
- **Descripción**: Verifica si el servicio está listo para recibir tráfico
- **Autenticación**: No requerida

#### Liveness Check

- **Endpoint**: `GET /v1/health/live`
- **Descripción**: Verifica si el servicio está vivo
- **Autenticación**: No requerida

### Endpoints Protegidos

#### Obtener Usuario

- **Endpoint**: `GET /v1/usuarios/{usuario}`
- **Descripción**: Obtiene información de un usuario
- **Autenticación**: Requerida (JWT Bearer Token)

#### Actualizar Usuario

- **Endpoint**: `PATCH /v1/usuarios/{usuario}`
- **Descripción**: Actualiza información de un usuario
- **Autenticación**: Requerida (JWT Bearer Token)

#### Cambiar Contraseña

- **Endpoint**: `PATCH /v1/usuarios/{usuario}/clave`
- **Descripción**: Cambia la contraseña de un usuario
- **Autenticación**: Requerida (JWT Bearer Token)

#### Enviar Código de Verificación

- **Endpoint**: `POST /v1/usuarios/{usuario}/codigo`
- **Descripción**: Envía código de verificación al usuario
- **Autenticación**: Requerida (JWT Bearer Token)

#### Eliminar Usuario

- **Endpoint**: `DELETE /v1/usuarios/{usuario}`
- **Descripción**: Elimina un usuario del sistema
- **Autenticación**: Requerida (JWT Bearer Token, rol ADMIN)

## Tecnologías

- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Spring Security
- JJWT (Generación y validación de tokens JWT)
- Spring AMQP (RabbitMQ)
- Spring Boot Actuator
- Swagger/OpenAPI

## Modelo de Datos

### Entidad Usuario

- **usuario** (String, PK): Nombre de usuario único
- **correo** (String, Unique): Correo electrónico único
- **clave** (String): Contraseña hasheada (BCrypt)
- **numeroTelefono** (String): Número de teléfono del usuario
- **fechaCreacion** (LocalDateTime): Fecha de registro
- **fechaUltimaModificacion** (LocalDateTime): Fecha de última actualización

## Configuración

### Variables de Entorno

```properties
# Base de Datos
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-domain:5432/domain
SPRING_DATASOURCE_USERNAME=domain_user
SPRING_DATASOURCE_PASSWORD=domain_pass

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# RabbitMQ
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=domain_user
SPRING_RABBITMQ_PASSWORD=domain_pass
SPRING_RABBITMQ_VIRTUAL_HOST=foro

# Server
SERVER_PORT=8080
```

## Uso

### Registro de Usuario

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

### Autenticación

```bash
curl -X POST http://localhost:8080/v1/sesiones \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": "testuser",
    "clave": "password123"
  }'
```

### Obtener Usuario (con token)

```bash
curl -X GET http://localhost:8080/v1/usuarios/testuser \
  -H "Authorization: Bearer <token>"
```

## Estructura del Proyecto

```
jwtmanual-taller1-micro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/uniquindio/archmicroserv/jwtgeneratortaller1/
│   │   │       ├── Jwtgeneratortaller1Application.java
│   │   │       ├── controller/
│   │   │       │   ├── PublicController.java
│   │   │       │   ├── UsuarioController.java
│   │   │       │   └── Admin.java
│   │   │       ├── services/
│   │   │       │   ├── UsuarioServiceImp.java
│   │   │       │   └── HealthService.java
│   │   │       ├── config/
│   │   │       │   ├── JWTUtils.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── Constants.java
│   │   │       ├── model/
│   │   │       │   └── Usuario.java
│   │   │       ├── dto/
│   │   │       │   ├── DatosUsuario.java
│   │   │       │   ├── LoginRequest.java
│   │   │       │   └── TokenDTO.java
│   │   │       └── messaging/
│   │   │           └── EventoPublisher.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── docs/
│   └── IMPLEMENTATION.md
├── Dockerfile
├── pom.xml
└── README.md
```

## Integración con Docker Compose

El microservicio está configurado en `docker-compose.unified.yml` y se ejecuta en el puerto **8080**.

## Health Checks

El servicio incluye health checks para:
- **Database**: Verifica conexión a PostgreSQL
- **Application**: Verifica estado general de la aplicación
- **RabbitMQ**: Verifica conexión a RabbitMQ

```bash
curl http://localhost:8080/v1/health
```

## Seguridad

### Autenticación JWT

- **Algoritmo**: HS256 (HMAC SHA-256)
- **Expiración**: Configurable (por defecto 24 horas)
- **Claims**: Usuario, rol, fechas de emisión y expiración
- **Validación**: En cada solicitud protegida

### Hash de Contraseñas

- **Algoritmo**: BCrypt
- **Rounds**: 10 (configurable)
- **Salt**: Generado automáticamente por BCrypt

### Roles y Permisos

- **USER**: Usuario normal (puede gestionar su propia cuenta)
- **ADMIN**: Administrador (puede gestionar todos los usuarios)

## Integración con RabbitMQ

El microservicio publica eventos de dominio a RabbitMQ:
- **Exchange**: `dominio.events` (tipo topic)
- **Routing Keys**: `auth.created`, `auth.login`, `auth.password_recovery`, `auth.password_changed`
- **Eventos**: `REGISTRO_USUARIO`, `AUTENTICACION`, `RECUPERAR_PASSWORD`, `AUTENTICACION_CLAVES`

## Testing

El proyecto incluye tests para:
- Servicios principales
- Controladores
- Utilidades JWT
- Health indicators

## Notas

- Para documentación detallada, consultar `docs/IMPLEMENTATION.md`
- Los tokens JWT deben almacenarse de forma segura en el cliente
- Las contraseñas nunca se almacenan en texto plano
- Se recomienda usar HTTPS en producción para proteger tokens en tránsito

