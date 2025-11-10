# JWT Manual Taller 1 Microservice - Documentación de Implementación

## Descripción General

El microservicio JWT Manual Taller 1 es un servicio de dominio desarrollado en Java con Spring Boot que gestiona la autenticación y autorización de usuarios mediante tokens JWT. Proporciona funcionalidades completas para registro, autenticación, gestión de usuarios y generación de tokens de acceso.

## Arquitectura

### Componentes Principales

El microservicio sigue una arquitectura en capas:

1. **Controllers**: Manejan las solicitudes HTTP y definen los endpoints de la API
2. **Services**: Contienen la lógica de negocio
3. **Repository**: Capa de acceso a datos (JPA)
4. **Model**: Entidades de dominio
5. **DTO**: Objetos de transferencia de datos
6. **Config**: Configuración de seguridad, JWT y RabbitMQ
7. **Health Indicators**: Monitoreo de salud de componentes

### Tecnologías Utilizadas

- **Spring Boot 3.x**: Framework principal
- **Spring Data JPA**: Acceso a datos con Hibernate
- **PostgreSQL**: Base de datos relacional
- **Spring Security**: Seguridad y autenticación
- **JJWT**: Generación y validación de tokens JWT
- **Spring AMQP**: Integración con RabbitMQ
- **Spring Boot Actuator**: Health checks y métricas
- **Swagger/OpenAPI**: Documentación de API

## Modelo de Datos

### Entidad Usuario

La entidad `Usuario` representa un usuario del sistema:

- **usuario** (String, PK): Nombre de usuario único
- **correo** (String, Unique): Correo electrónico único
- **clave** (String): Contraseña hasheada (BCrypt)
- **numeroTelefono** (String): Número de teléfono del usuario
- **fechaCreacion** (LocalDateTime): Fecha de registro
- **fechaUltimaModificacion** (LocalDateTime): Fecha de última actualización

### Relaciones

- **Relación 1:1 con Perfil**: Cada usuario puede tener un perfil asociado (gestionado por otro microservicio)
- **Sin relaciones JPA directas**: La relación con perfil es lógica mediante `usuarioId`

## Endpoints de la API

### Endpoints Públicos

#### Registro de Usuario

- **Endpoint**: `POST /v1/usuarios`
- **Descripción**: Registra un nuevo usuario en el sistema
- **Autenticación**: No requerida
- **Request Body**:
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
- **Request Body**:
```json
{
  "usuario": "testuser",
  "clave": "password123"
}
```
- **Response**: Token JWT y datos del usuario

#### Health Check

- **Endpoint**: `GET /v1/health`
- **Descripción**: Verifica el estado de salud del servicio
- **Autenticación**: No requerida
- **Response**: Estado de base de datos, aplicación y RabbitMQ

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

## Componentes de Implementación

### Controllers

#### PublicController

Controlador para endpoints públicos (registro, login, health checks).

**Métodos principales**:
- `registrarUsuario()`: Registro de nuevos usuarios
- `autenticar()`: Autenticación y generación de token JWT
- `health()`: Health check general
- `readiness()`: Readiness check
- `liveness()`: Liveness check

#### UsuarioController

Controlador para operaciones de usuarios (requiere autenticación).

**Métodos principales**:
- `obtenerUsuario()`: Obtiene información de usuario
- `actualizarUsuario()`: Actualiza información de usuario
- `cambiarClave()`: Cambia contraseña de usuario
- `enviarCodigo()`: Envía código de verificación

#### Admin

Controlador para operaciones administrativas (requiere rol ADMIN).

**Métodos principales**:
- `eliminarUsuario()`: Elimina usuario del sistema
- `listarUsuarios()`: Lista todos los usuarios (con paginación)

### Services

#### UsuarioServiceImp

Servicio que contiene la lógica de negocio para gestión de usuarios.

**Responsabilidades**:
- Validación de datos de usuario
- Hash de contraseñas con BCrypt
- Validación de credenciales
- Gestión de códigos de verificación
- Coordinación con repositorio

**Métodos principales**:
- `registrarUsuario()`: Registra nuevo usuario con validaciones
- `autenticar()`: Valida credenciales y retorna datos del usuario
- `obtenerUsuario()`: Obtiene usuario por nombre
- `actualizarUsuario()`: Actualiza datos de usuario
- `cambiarClave()`: Cambia contraseña con validaciones
- `enviarCodigoVerificacion()`: Genera y envía código de verificación
- `eliminarUsuario()`: Elimina usuario del sistema

#### HealthService

Servicio que coordina todos los health checks del sistema.

**Health Indicators**:
- `DatabaseHealthIndicator`: Verifica conexión a base de datos
- `ApplicationHealthIndicator`: Verifica estado de la aplicación
- `RabbitMQHealthIndicator`: Verifica conexión a RabbitMQ

**Métodos**:
- `getHealth()`: Retorna estado general de salud
- `getReadiness()`: Retorna estado de preparación
- `getLiveness()`: Retorna estado de vida

### Configuración

#### JWTUtils

Utilidad para generación y validación de tokens JWT.

**Funcionalidades**:
- Generación de tokens con claims personalizados
- Validación de tokens
- Extracción de información del token
- Configuración de expiración

**Claims del token**:
- `sub`: Nombre de usuario
- `iat`: Fecha de emisión
- `exp`: Fecha de expiración
- `rol`: Rol del usuario (USER, ADMIN)

#### SecurityConfig

Configuración de seguridad de Spring Security.

**Configuraciones**:
- Desactivación de CSRF (para API REST)
- Configuración de filtros JWT
- Configuración de rutas públicas y protegidas
- Configuración de roles y permisos

#### RabbitMQConfig

Configuración de RabbitMQ para mensajería asíncrona.

**Configuraciones**:
- Exchange: `dominio.events` (tipo topic)
- Connection Factory: Configurado desde variables de entorno
- Message Converter: Jackson para serialización JSON

#### Constants

Constantes utilizadas en toda la aplicación.

**Constantes principales**:
- Expiración de tokens
- Roles de usuario
- Mensajes de error
- Configuraciones de seguridad

## Flujos de Procesamiento

### Flujo de Registro de Usuario

1. Cliente envía `POST /v1/usuarios` con datos del usuario
2. `PublicController.registrarUsuario()` recibe la solicitud
3. Valida datos de entrada (usuario, correo, clave obligatorios)
4. `UsuarioServiceImp.registrarUsuario()` se ejecuta:
   - Verifica que usuario y correo no existan
   - Hashea la contraseña con BCrypt
   - Crea nueva entidad `Usuario`
   - Guarda en base de datos
5. Retorna respuesta HTTP 201 con mensaje de éxito

### Flujo de Autenticación

1. Cliente envía `POST /v1/sesiones` con credenciales
2. `PublicController.autenticar()` recibe la solicitud
3. `UsuarioServiceImp.autenticar()` se ejecuta:
   - Busca usuario por nombre
   - Valida contraseña con BCrypt
   - Si es válido, genera token JWT con `JWTUtils`
4. Retorna respuesta HTTP 200 con token JWT y datos del usuario

### Flujo de Validación de Token

1. Cliente envía solicitud con header `Authorization: Bearer <token>`
2. `JwtAuthenticationFilter` intercepta la solicitud
3. Extrae y valida el token con `JWTUtils`
4. Si es válido, establece autenticación en contexto de seguridad
5. Permite acceso al endpoint solicitado

### Flujo de Health Check

1. Cliente envía `GET /v1/health`
2. `PublicController.health()` recibe la solicitud
3. `HealthService.getHealth()` se ejecuta:
   - Verifica estado de base de datos
   - Verifica estado de aplicación
   - Verifica estado de RabbitMQ
4. Retorna respuesta HTTP 200 con estado de todos los componentes

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

### application.properties

Configuración adicional:
- Configuración de logging
- Configuración de Spring Boot Actuator
- Configuración de JPA/Hibernate
- Configuración de Swagger/OpenAPI

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

### Protección de Endpoints

- **Públicos**: Registro, login, health checks
- **Protegidos**: Operaciones de usuario (requieren token JWT)
- **Administrativos**: Eliminación de usuarios (requieren rol ADMIN)

## Integración con RabbitMQ

### Eventos Publicados

El microservicio publica eventos de dominio a RabbitMQ:

- **ELIMINACION_USUARIO**: Cuando se elimina un usuario
- **REGISTRO_USUARIO**: Cuando se registra un nuevo usuario (opcional)

### Configuración

- **Exchange**: `dominio.events` (tipo topic)
- **Routing Keys**: `auth.deleted`, `auth.created`
- **Formato**: JSON con estructura estándar de eventos

## Health Checks

### DatabaseHealthIndicator

Verifica la conexión a la base de datos PostgreSQL:
- Ejecuta query simple (`SELECT 1`)
- Retorna UP si la conexión es exitosa
- Retorna DOWN si hay error de conexión

### ApplicationHealthIndicator

Verifica el estado general de la aplicación:
- Verifica que la aplicación esté corriendo
- Retorna información de versión y estado

### RabbitMQHealthIndicator

Verifica la conexión a RabbitMQ:
- Intenta establecer conexión
- Retorna UP si la conexión es exitosa
- Retorna DOWN si hay error de conexión

## Testing

### Estructura de Tests

- **Unit Tests**: Pruebas de servicios y lógica de negocio
- **Integration Tests**: Pruebas de endpoints con `MockMvc`
- **Repository Tests**: Pruebas de acceso a datos

### Cobertura de Tests

El proyecto incluye tests para:
- Servicios principales
- Controladores
- Utilidades JWT
- Health indicators

## Despliegue

### Docker

El microservicio incluye un `Dockerfile` para contenedorización.

### Docker Compose

Configurado en `docker-compose.unified.yml`:
- Puerto: 8080
- Dependencias: PostgreSQL, RabbitMQ
- Health checks configurados

## Monitoreo y Logging

### Spring Boot Actuator

- **Health Endpoint**: `/actuator/health`
- **Info Endpoint**: `/actuator/info`
- **Metrics**: Disponibles en `/actuator/metrics`

### Logging

- **SLF4J + Logback**: Configuración de logging
- **Niveles**: INFO, WARN, ERROR
- **Formato**: JSON estructurado para producción

## Consideraciones de Seguridad

1. **Tokens JWT**: Almacenados de forma segura en el cliente
2. **Contraseñas**: Nunca se almacenan en texto plano
3. **Validación de entrada**: Validación exhaustiva de datos
4. **Rate limiting**: Considerar implementar límites de solicitudes
5. **HTTPS**: Usar en producción para proteger tokens en tránsito

## Mejoras Futuras

1. **Refresh Tokens**: Implementar tokens de actualización
2. **2FA**: Autenticación de dos factores
3. **OAuth2**: Integración con proveedores OAuth2
4. **Rate Limiting**: Límites de solicitudes por usuario
5. **Auditoría**: Registro de acciones de usuarios
6. **Password Policy**: Políticas de contraseñas más estrictas

