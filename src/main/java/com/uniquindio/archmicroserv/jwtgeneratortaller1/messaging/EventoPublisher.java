package com.uniquindio.archmicroserv.jwtgeneratortaller1.messaging;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.EventoDominio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(EventoDominio evento) {
        log.info("Publicando evento de dominio: tipo={}, id={}", 
            evento.tipoAccion(), evento.id());
        log.debug("Routing key: {}", evento.tipoAccion().routingKey());
        rabbitTemplate.convertAndSend(
                "dominio.events",                // exchange
                evento.tipoAccion().routingKey(), // routing key desde enum
                evento                           // body (serializado como JSON)
        );
        log.debug("Evento publicado exitosamente en exchange 'dominio.events'");
    }
}

