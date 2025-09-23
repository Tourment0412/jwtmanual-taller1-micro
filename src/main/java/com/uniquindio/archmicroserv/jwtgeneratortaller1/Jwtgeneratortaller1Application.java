package com.uniquindio.archmicroserv.jwtgeneratortaller1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Jwtgeneratortaller1Application {

    private static final Logger log = LoggerFactory.getLogger(Jwtgeneratortaller1Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Jwtgeneratortaller1Application.class, args);
    }

    @Bean
    CommandLineRunner rabbitHealth(RabbitTemplate rabbitTemplate) {
        return args -> {
            try {
                // Ejecuta una operación no destructiva para validar conexión/canal
                rabbitTemplate.execute(channel -> {
                    log.info("Conexión AMQP establecida. Channel open? {}", channel.isOpen());
                    return null;
                });
            } catch (Exception ex) {
                log.error("Error estableciendo conexión AMQP al iniciar la app", ex);
            }
        };
    }

    // Forzar el uso del converter JSON si está definido
    @Bean
    public java.util.function.Consumer<RabbitTemplate> rabbitTemplateCustomizer(MessageConverter messageConverter) {
        return template -> template.setMessageConverter(messageConverter);
    }
}
