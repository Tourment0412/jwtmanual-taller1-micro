# language: es
Característica: Gestión de usuarios (registro, login y administración)

  Antecedentes:
    Dado que el servicio está disponible

  Escenario: Registrar un nuevo usuario
    Cuando registro un usuario con datos válidos
    Entonces la respuesta debe tener estado 201
    Y el cuerpo debe indicar éxito

  Escenario: Iniciar sesión con credenciales válidas
    Dado que existe un usuario registrado válido
    Cuando inicio sesión con credenciales correctas
    Entonces la respuesta debe tener estado 200
    Y debo obtener un token JWT válido

  Escenario: Solicitar código de recuperación de contraseña
    Dado que existe un usuario registrado válido
    Cuando solicito código de recuperación para ese usuario
    Entonces la respuesta debe tener estado 200

  @admin
  Escenario: Listar usuarios con token de admin
    Dado que inicio sesión como admin
    Cuando consulto la lista de usuarios en la página 0
    Entonces la respuesta debe tener estado 200
    Y el cuerpo debe contener una lista de usuarios

  @admin
  Escenario: Eliminar un usuario con token de admin
    Dado que inicio sesión como admin
    Y existe un usuario registrado válido
    Cuando elimino ese usuario
    Entonces la respuesta debe tener estado 200


