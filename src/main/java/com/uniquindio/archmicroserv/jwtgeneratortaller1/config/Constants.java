package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

/**
 * Clase que contiene constantes utilizadas en toda la aplicación.
 * Centraliza valores comunes para facilitar su mantenimiento.
 */
public final class Constants {
    
    // Constantes de JWT
    public static final String ISSUER = "ingesis.uniquindio.edu.co";
    public static final int TOKEN_EXPIRATION_HOURS = 1;
    
    // Constantes de códigos de validación
    public static final int CODIGO_VALIDACION_LENGTH = 6;
    public static final String ALFABETO_CODIGO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    // Constantes de tiempo
    public static final int MINUTOS_EXPIRACION_CODIGO = 15;
    
    // Constantes de paginación
    public static final int TAMANO_PAGINA_DEFAULT = 10;
    
    // Constantes de validación
    public static final int LONGITUD_MINIMA_USUARIO = 3;
    public static final int LONGITUD_MAXIMA_USUARIO = 50;
    
    // Constantes de roles
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_CLIENTE = "CLIENTE";
    
    // Constantes de mensajes
    public static final String MSG_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String MSG_USUARIO_NO_EXISTENTE = "Usuario no existente";
    public static final String MSG_CODIGO_INCORRECTO = "Código incorrecto";
    public static final String MSG_USUARIO_YA_EXISTE = "El usuario ya existe";
    public static final String MSG_CONTRASENA_INVALIDA = "Contraseña inválida";
    
    // Constructor privado para prevenir instanciación
    private Constants() {
        throw new UnsupportedOperationException("Esta clase no debe ser instanciada");
    }
}
