package com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar un usuario que ya existe en el sistema.
 */
public class UsuarioYaExisteException extends RuntimeException {
    
    public UsuarioYaExisteException(String message) {
        super(message);
    }
    
    public UsuarioYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}

