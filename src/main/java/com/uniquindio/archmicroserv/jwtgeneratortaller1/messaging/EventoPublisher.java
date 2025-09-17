package com.uniquindio.archmicroserv.jwtgeneratortaller1.messaging;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.EventoDominio;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(EventoDominio evento) {
        rabbitTemplate.convertAndSend(
                "dominio.events",                // exchange
                evento.tipoAccion().routingKey(), // routing key desde enum
                evento                           // body (serializado como JSON)
        );
    }
}

