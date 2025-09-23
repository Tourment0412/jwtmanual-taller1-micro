package com.uniquindio.archmicroserv.jwtgeneratortaller1.messaging;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.EventoDominio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventoPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventoPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    void configurePublisherCallbacks() {
        // Asegurar que los returns se reporten si el mensaje no puede ser enrutado
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback(this::onConfirm);
        rabbitTemplate.setReturnsCallback(this::onReturned);
        log.info("RabbitTemplate callbacks configurados (mandatory=true, confirms/returns activos)");
    }

    private void onConfirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.debug("Broker ACK del publish. correlationData={} ", correlationData);
        } else {
            log.error("Broker NACK del publish. correlationData={} cause={}", correlationData, cause);
        }
    }

    private void onReturned(ReturnedMessage returned) {
        log.error("Mensaje RETURNED por el broker: replyCode={} replyText={} exchange={} routingKey={} body={}",
                returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), new String(returned.getMessage().getBody()));
    }

    public void publicar(EventoDominio evento) {
        try {
            log.info("Publicando evento: {}", evento);
            final String routingKey = evento.tipoAccion().routingKey();
            log.info("Routing key a usar: {}", routingKey);

            rabbitTemplate.convertAndSend(
                    "dominio.events",                // exchange
                    routingKey,                        // routing key desde enum
                    evento                             // body (serializado como JSON)
            );

            log.debug("convertAndSend invocado exitosamente");
        } catch (Exception ex) {
            log.error("Error publicando evento a RabbitMQ", ex);
            throw ex;
        }
    }
}

