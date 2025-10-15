package com.uniquindio.archmicroserv.jwtgeneratortaller1.messaging;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.EventoDominio;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.TipoAccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para EventoPublisher")
class EventoPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventoPublisher eventoPublisher;

    @BeforeEach
    void setUp() {
        // Setup común si es necesario
    }

    @Test
    @DisplayName("Publicar evento de registro de usuario")
    void testPublicarEventoRegistro() {
        // Arrange
        Map<String, Object> datos = new HashMap<>();
        datos.put("usuario", "testuser");
        datos.put("correo", "test@email.com");
        datos.put("numeroTelefono", "+1234567890");
        
        EventoDominio evento = EventoDominio.of(TipoAccion.REGISTRO_USUARIO, datos);

        // Act
        eventoPublisher.publicar(evento);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("dominio.events"),
                eq(TipoAccion.REGISTRO_USUARIO.routingKey()),
                eq(evento)
        );
    }

    @Test
    @DisplayName("Publicar evento de autenticación")
    void testPublicarEventoAutenticacion() {
        // Arrange
        Map<String, Object> datos = new HashMap<>();
        datos.put("usuario", "testuser");
        datos.put("correo", "test@email.com");
        datos.put("fecha", "2024-10-15T10:30:00");
        
        EventoDominio evento = EventoDominio.of(TipoAccion.AUTENTICACION, datos);

        // Act
        eventoPublisher.publicar(evento);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("dominio.events"),
                eq(TipoAccion.AUTENTICACION.routingKey()),
                eq(evento)
        );
    }

    @Test
    @DisplayName("Publicar evento de recuperar password")
    void testPublicarEventoRecuperarPassword() {
        // Arrange
        Map<String, Object> datos = new HashMap<>();
        datos.put("usuario", "testuser");
        datos.put("correo", "test@email.com");
        datos.put("codigo", "ABC123");
        
        EventoDominio evento = EventoDominio.of(TipoAccion.RECUPERAR_PASSWORD, datos);

        // Act
        eventoPublisher.publicar(evento);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("dominio.events"),
                eq(TipoAccion.RECUPERAR_PASSWORD.routingKey()),
                eq(evento)
        );
    }

    @Test
    @DisplayName("Publicar evento de autenticación de claves")
    void testPublicarEventoAutenticacionClaves() {
        // Arrange
        Map<String, Object> datos = new HashMap<>();
        datos.put("usuario", "testuser");
        datos.put("correo", "test@email.com");
        datos.put("fecha", "2024-10-15T10:30:00");
        
        EventoDominio evento = EventoDominio.of(TipoAccion.AUTENTICACION_CLAVES, datos);

        // Act
        eventoPublisher.publicar(evento);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("dominio.events"),
                eq(TipoAccion.AUTENTICACION_CLAVES.routingKey()),
                eq(evento)
        );
    }

    @Test
    @DisplayName("Publicar múltiples eventos")
    void testPublicarMultiplesEventos() {
        // Arrange
        Map<String, Object> datos1 = new HashMap<>();
        datos1.put("usuario", "user1");
        EventoDominio evento1 = EventoDominio.of(TipoAccion.REGISTRO_USUARIO, datos1);

        Map<String, Object> datos2 = new HashMap<>();
        datos2.put("usuario", "user2");
        EventoDominio evento2 = EventoDominio.of(TipoAccion.AUTENTICACION, datos2);

        // Act
        eventoPublisher.publicar(evento1);
        eventoPublisher.publicar(evento2);

        // Assert
        verify(rabbitTemplate, times(2)).convertAndSend(
                anyString(),
                anyString(),
                any(EventoDominio.class)
        );
    }
}

