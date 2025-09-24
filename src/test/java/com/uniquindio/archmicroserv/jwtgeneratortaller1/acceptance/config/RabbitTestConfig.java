package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitTestConfig {

    @Bean
    public TopicExchange dominioEventsExchange() {
        return new TopicExchange("dominio.events", true, false);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory, TopicExchange dominioEventsExchange) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        // Declara el exchange al iniciar
        admin.declareExchange(dominioEventsExchange);
        return admin;
    }
}


