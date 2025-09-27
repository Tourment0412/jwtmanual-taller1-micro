package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuración principal de Spring Boot para las pruebas de integración con Cucumber.
 * 
 * Esta clase actúa como el "puente" entre Cucumber y Spring Boot, permitiendo que
 * los escenarios de Cucumber puedan usar todas las funcionalidades de Spring,
 * incluyendo inyección de dependencias, configuración de beans y acceso a la
 * aplicación web completa.
 * 
 * Funcionalidades principales:
 * - Integra Cucumber con el contexto de Spring Boot
 * - Inicia la aplicación web en un puerto aleatorio
 * - Importa todas las configuraciones necesarias para las pruebas
 * - Activa el perfil de test para configuraciones específicas
 * - Proporciona acceso a la aplicación completa (controladores, servicios, repositorios)
 * 
 * Arquitectura de configuración:
 * - TestContainersConfig: Infraestructura (PostgreSQL, RabbitMQ)
 * - RabbitTestConfig: Configuración de mensajería
 * - AdminSeedConfig: Datos de prueba (usuario admin)
 * 
 * @author Sistema de Pruebas
 * @version 1.0
 * @since 2024
 */
@CucumberContextConfiguration  // Le dice a Cucumber que use Spring como contexto de aplicación
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)  // Inicia la aplicación web completa
@Import({ TestContainersConfig.class, RabbitTestConfig.class, AdminSeedConfig.class })  // Importa configuraciones de prueba
@ActiveProfiles("test")  // Activa el perfil de test
public class CucumberSpringConfig extends TestContainersConfig {
    
    // Esta clase no necesita métodos adicionales porque:
    // 1. @CucumberContextConfiguration permite que Cucumber use Spring
    // 2. @SpringBootTest inicia la aplicación completa
    // 3. @Import carga todas las configuraciones necesarias
    // 4. @ActiveProfiles activa configuraciones específicas de test
    // 5. La herencia de TestContainersConfig proporciona la infraestructura base
}


