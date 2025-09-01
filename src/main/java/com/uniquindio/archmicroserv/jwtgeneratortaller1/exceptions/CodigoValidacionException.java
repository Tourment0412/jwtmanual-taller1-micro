package com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions;

/**
 * Excepción lanzada cuando hay errores relacionados con códigos de validación.
 */
public class CodigoValidacionException extends RuntimeException {
    
    public CodigoValidacionException(String message) {
        super(message);
    }
    
    public CodigoValidacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
