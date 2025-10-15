package com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Tests unitarios para GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Manejar UsuarioNotFoundException")
    void testHandleUsuarioNotFound() {
        // Arrange
        UsuarioNotFoundException exception = new UsuarioNotFoundException("Usuario no encontrado");

        // Act
        ResponseEntity<MessageDTO<?>> response = exceptionHandler.handleUsuarioNotFound(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().error());
        assertEquals("Usuario no encontrado", response.getBody().respuesta());
    }

    @Test
    @DisplayName("Manejar CodigoValidacionException")
    void testHandleCodigoValidacion() {
        // Arrange
        CodigoValidacionException exception = new CodigoValidacionException("Código inválido");

        // Act
        ResponseEntity<MessageDTO<?>> response = exceptionHandler.handleCodigoValidacion(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().error());
        assertEquals("Código inválido", response.getBody().respuesta());
    }

    @Test
    @DisplayName("Manejar EmailException")
    void testHandleEmailException() {
        // Arrange
        EmailException exception = new EmailException("Error al enviar correo");

        // Act
        ResponseEntity<MessageDTO<?>> response = exceptionHandler.handleEmailException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().error());
        assertTrue(response.getBody().respuesta().toString().contains("Error al enviar email"));
    }

    @Test
    @DisplayName("Manejar MethodArgumentNotValidException")
    void testHandleValidationExceptions() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError error1 = new FieldError("usuario", "correo", "debe ser un email válido");
        FieldError error2 = new FieldError("usuario", "clave", "no puede estar vacío");
        List<FieldError> errors = Arrays.asList(error1, error2);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(errors);

        // Act
        ResponseEntity<MessageDTO<?>> response = exceptionHandler.handleValidationExceptions(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().error());
        assertTrue(response.getBody().respuesta().toString().contains("Error de validación"));
    }

    @Test
    @DisplayName("Manejar Exception genérica")
    void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Error inesperado");

        // Act
        ResponseEntity<MessageDTO<?>> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().error());
        assertTrue(response.getBody().respuesta().toString().contains("Error interno del servidor"));
    }

    @Test
    @DisplayName("Manejar UsuarioNotFoundException con mensaje personalizado")
    void testHandleUsuarioNotFoundMensajePersonalizado() {
        // Arrange
        String mensajePersonalizado = "El usuario 'admin' no fue encontrado en la base de datos";
        UsuarioNotFoundException exception = new UsuarioNotFoundException(mensajePersonalizado);

        // Act
        ResponseEntity<MessageDTO<?>> response = exceptionHandler.handleUsuarioNotFound(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mensajePersonalizado, response.getBody().respuesta());
    }
}

