package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Health check para verificar la disponibilidad de RabbitMQ
 */
@Component("rabbitmq")
public class RabbitMQHealthIndicator implements HealthIndicator {

    private static final Instant START_TIME = Instant.now();
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQHealthIndicator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Health health() {
        try {
            // Intenta obtener información de la conexión
            var connectionFactory = rabbitTemplate.getConnectionFactory();
            if (connectionFactory != null) {
                return Health.up()
                        .withDetail("messaging", "RabbitMQ")
                        .withDetail("status", "READY")
                        .withDetail("from", START_TIME.toString())
                        .build();
            } else {
                return Health.down()
                        .withDetail("messaging", "RabbitMQ")
                        .withDetail("status", "Connection factory not available")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("messaging", "RabbitMQ")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
