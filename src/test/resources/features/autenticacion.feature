# language: es
# =============================================================================
# ARCHIVO DE CARACTERÍSTICAS (FEATURES) - AUTENTICACIÓN JWT
# =============================================================================
# 
# Este archivo define las características y escenarios de prueba para el servicio
# de autenticación JWT. Cubre registro, login, recuperación de contraseña y health checks.

Característica: Autenticación y Gestión de Usuarios JWT
  
  Antecedentes:
    Dado que el servicio de autenticación está disponible

  # ===== REGISTRO DE USUARIO =====
  Escenario: Registrar un nuevo usuario
    Cuando registro un usuario con datos válidos
    Entonces la respuesta debe tener estado 201
    Y el cuerpo debe indicar éxito

  # ===== AUTENTICACIÓN =====
  Escenario: Iniciar sesión con credenciales válidas
    Dado que existe un usuario registrado válido
    Cuando inicio sesión con credenciales correctas
    Entonces la respuesta debe tener estado 200
    Y debo obtener un token JWT válido

  # ===== CONSULTA DE USUARIO =====
  Escenario: Consultar datos de usuario autenticado
    Dado que existe un usuario registrado válido
    Y que he iniciado sesión exitosamente
    Cuando consulto los datos del usuario
    Entonces la respuesta debe tener estado 200
    Y el cuerpo debe contener los datos del usuario

  # ===== ACTUALIZACIÓN DE USUARIO =====
  Escenario: Actualizar datos de usuario
    Dado que existe un usuario registrado válido
    Y que he iniciado sesión exitosamente
    Cuando actualizo los datos del usuario
    Entonces la respuesta debe tener estado 200
    Y el cuerpo debe indicar éxito

  # ===== RECUPERACIÓN DE CONTRASEÑA =====
  Escenario: Solicitar código de recuperación de contraseña
    Dado que existe un usuario registrado válido
    Cuando solicito código de recuperación para ese usuario
    Entonces la respuesta debe tener estado 200

  # ===== HEALTH CHECKS =====
  Escenario: Verificar salud del servicio
    Cuando consulto el endpoint de health check
    Entonces la respuesta debe tener estado 200
    Y el cuerpo debe indicar que el servicio está UP

  Escenario: Verificar readiness del servicio
    Cuando consulto el endpoint de readiness
    Entonces la respuesta debe tener estado 200
    Y el cuerpo debe indicar que el servicio está listo

  Escenario: Verificar liveness del servicio
    Cuando consulto el endpoint de liveness
    Entonces la respuesta debe tener estado 200
    Y el cuerpo debe indicar que el servicio está vivo

