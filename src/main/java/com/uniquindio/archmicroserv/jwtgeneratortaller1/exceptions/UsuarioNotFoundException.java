package com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions;

/**
 * Excepción lanzada cuando no se puede encontrar un usuario en el sistema.
 */
public class UsuarioNotFoundException extends RuntimeException {
    
    public UsuarioNotFoundException(String message) {
        super(message);
    }
    
    public UsuarioNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
