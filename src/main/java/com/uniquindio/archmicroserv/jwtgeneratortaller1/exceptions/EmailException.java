package com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions;

/**
 * Excepción lanzada cuando hay errores relacionados con el envío de emails.
 */
public class EmailException extends RuntimeException {
    
    public EmailException(String message) {
        super(message);
    }
    
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}