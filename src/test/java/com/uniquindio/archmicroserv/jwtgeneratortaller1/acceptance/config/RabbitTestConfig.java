package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Configuración de RabbitMQ para las pruebas de integración con Cucumber.
 * 
 * Esta clase se encarga de configurar el entorno de mensajería necesario para
 * que las pruebas puedan simular el comportamiento real de la aplicación con
 * colas de mensajes y eventos de dominio.
 * 
 * Funcionalidades principales:
 * - Crea un exchange de eventos de dominio para la comunicación entre servicios
 * - Configura un administrador de RabbitMQ para gestionar colas y exchanges
 * - Declara automáticamente los recursos necesarios al iniciar las pruebas
 * 
 * @author Sistema de Pruebas
 * @version 1.0
 * @since 2024
 */
@TestConfiguration
public class RabbitTestConfig {

    /**
     * Crea un exchange de tipo Topic para eventos de dominio.
     * 
     * Un TopicExchange permite enrutar mensajes basándose en patrones de routing key.
     * Es ideal para eventos de dominio porque permite:
     * - Enrutar mensajes por tipo de evento (usuario.creado, usuario.eliminado)
     * - Suscribirse a múltiples tipos de eventos con patrones (usuario.*)
     * - Mantener la flexibilidad en el enrutamiento de mensajes
     * 
     * Configuración:
     * - Nombre: "dominio.events"
     * - Durable: true (sobrevive reinicios del servidor)
     * - Auto-delete: false (no se elimina automáticamente)
     * 
     * @return TopicExchange configurado para eventos de dominio
     */
    @Bean
    public TopicExchange dominioEventsExchange() {
        return new TopicExchange("dominio.events", true, false);
    }

    /**
     * Crea y configura un administrador de RabbitMQ para las pruebas.
     * 
     * RabbitAdmin es responsable de:
     * - Declarar exchanges, colas y bindings automáticamente
     * - Gestionar la configuración de RabbitMQ
     * - Asegurar que los recursos necesarios estén disponibles
     * 
     * Al declarar el exchange al iniciar, se garantiza que:
     * - El exchange esté disponible antes de que la aplicación lo use
     * - No haya errores de "exchange no encontrado" durante las pruebas
     * - La configuración sea consistente entre ejecuciones
     * 
     * @param connectionFactory Factory de conexiones de RabbitMQ (inyectado por Spring)
     * @param dominioEventsExchange Exchange de eventos de dominio (inyectado por Spring)
     * @return RabbitAdmin configurado y listo para usar
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory, TopicExchange dominioEventsExchange) {
        // Crear el administrador con la factory de conexiones
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        
        // Declarar el exchange al iniciar para asegurar disponibilidad
        admin.declareExchange(dominioEventsExchange);
        
        return admin;
    }
}


