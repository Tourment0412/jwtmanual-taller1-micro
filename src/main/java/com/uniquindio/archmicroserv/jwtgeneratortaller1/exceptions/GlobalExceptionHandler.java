package com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Proporciona respuestas consistentes para diferentes tipos de errores.
 */
// @RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones cuando no se encuentra un usuario.
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<MessageDTO<?>> handleUsuarioNotFound(UsuarioNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageDTO<>(true, e.getMessage()));
    }

    /**
     * Maneja excepciones relacionadas con códigos de validación.
     */
    @ExceptionHandler(CodigoValidacionException.class)
    public ResponseEntity<MessageDTO<?>> handleCodigoValidacion(CodigoValidacionException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageDTO<>(true, e.getMessage()));
    }

    /**
     * Maneja excepciones relacionadas con el envío de emails.
     */
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<MessageDTO<?>> handleEmailException(EmailException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageDTO<>(true, "Error al enviar email: " + e.getMessage()));
    }

    /**
     * Maneja excepciones de validación de argumentos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageDTO<?>> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageDTO<>(true, "Error de validación: " + errors));
    }

    /**
     * Maneja excepciones generales no capturadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageDTO<?>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageDTO<>(true, "Error interno del servidor: " + e.getMessage()));
    }
}
