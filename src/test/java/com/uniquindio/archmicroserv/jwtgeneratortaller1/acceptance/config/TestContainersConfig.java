package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuración de TestContainers para las pruebas de integración con Cucumber.
 * 
 * Esta clase se encarga de crear y gestionar contenedores Docker reales para
 * simular el entorno de producción durante las pruebas. Utiliza TestContainers
 * para proporcionar infraestructura real (PostgreSQL y RabbitMQ) de manera
 * aislada y reproducible.
 * 
 * Ventajas de usar TestContainers:
 * - Infraestructura real: No mocks, sino servicios reales
 * - Aislamiento: Cada ejecución de pruebas tiene su propio entorno
 * - Reproducibilidad: Mismo entorno en cualquier máquina
 * - Limpieza automática: Los contenedores se eliminan al terminar
 * 
 * Servicios configurados:
 * - PostgreSQL 16: Base de datos principal
 * - RabbitMQ 3.13: Sistema de mensajería
 * 
 * @author Sistema de Pruebas
 * @version 1.0
 * @since 2024
 */
public class TestContainersConfig {

    /**
     * Contenedor PostgreSQL para las pruebas de integración.
     * 
     * Configuración:
     * - Imagen: postgres:16-alpine (versión estable y ligera)
     * - Base de datos: "testdb"
     * - Usuario: "test"
     * - Contraseña: "test"
     * 
     * Características:
     * - Se inicia automáticamente al cargar la clase
     * - Puerto asignado dinámicamente para evitar conflictos
     * - Datos persistentes durante la ejecución de pruebas
     * - Se elimina automáticamente al terminar
     * 
     * Nota: Los contenedores se cierran automáticamente cuando la JVM termina,
     * por lo que no es necesario cerrarlos manualmente.
     */
    @SuppressWarnings("resource") // Los contenedores se cierran automáticamente al terminar la JVM
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("testdb")      // Nombre de la base de datos de prueba
            .withUsername("test")            // Usuario de conexión
            .withPassword("test");           // Contraseña de conexión

    /**
     * Contenedor RabbitMQ para las pruebas de integración.
     * 
     * Configuración:
     * - Imagen: rabbitmq:3.13-management (incluye interfaz web de administración)
     * - Virtual Host: "/" (host virtual por defecto)
     * 
     * Características:
     * - Incluye interfaz de administración web (puerto 15672)
     * - Puerto AMQP asignado dinámicamente
     * - Configuración mínima para pruebas
     * - Se elimina automáticamente al terminar
     * 
     * Nota: Los contenedores se cierran automáticamente cuando la JVM termina,
     * por lo que no es necesario cerrarlos manualmente.
     */
    static final RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    /**
     * Bloque estático que inicia los contenedores al cargar la clase.
     * 
     * Este bloque se ejecuta una sola vez cuando la clase se carga por primera vez.
     * Los contenedores se mantienen activos durante toda la ejecución de las pruebas
     * y se eliminan automáticamente cuando la JVM termina.
     * 
     * Orden de inicio:
     * 1. PostgreSQL (base de datos)
     * 2. RabbitMQ (mensajería)
     * 
     * Nota: El inicio de contenedores puede tomar varios segundos en la primera ejecución
     * debido a la descarga de imágenes Docker.
     */
    static {
        postgres.start();    // Iniciar contenedor PostgreSQL
        rabbit.start();      // Iniciar contenedor RabbitMQ
    }

    /**
     * Registra propiedades dinámicas de Spring para conectar la aplicación con los contenedores.
     * 
     * Este método se ejecuta automáticamente por Spring Test y sobrescribe las propiedades
     * de configuración de la aplicación para que use los contenedores en lugar de
     * configuraciones locales o de producción.
     * 
     * El uso de @DynamicPropertySource permite:
     * - Sobrescribir propiedades después de que Spring cargue application.properties
     * - Usar valores dinámicos (URLs de contenedores con puertos aleatorios)
     * - Mantener la configuración de la aplicación intacta
     * 
     * @param registry Registro de propiedades dinámicas de Spring
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // ===== CONFIGURACIÓN DE POSTGRESQL =====
        registry.add("spring.datasource.url", postgres::getJdbcUrl);                    // URL de conexión JDBC
        registry.add("spring.datasource.username", postgres::getUsername);              // Usuario de la base de datos
        registry.add("spring.datasource.password", postgres::getPassword);              // Contraseña de la base de datos
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver"); // Driver JDBC
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");                  // Actualizar esquema automáticamente
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect"); // Dialecto de Hibernate

        // ===== CONFIGURACIÓN DE RABBITMQ =====
        registry.add("spring.rabbitmq.host", rabbit::getHost);                          // Host del broker RabbitMQ
        registry.add("spring.rabbitmq.port", () -> rabbit.getAmqpPort());              // Puerto AMQP (5672)
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);            // Usuario administrador
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);            // Contraseña administrador
        registry.add("spring.rabbitmq.virtual-host", () -> "/");                       // Host virtual por defecto
        registry.add("spring.autoconfigure.exclude", () -> "");                        // No excluir autoconfiguraciones
        registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");   // No iniciar listeners automáticamente
    }
}


