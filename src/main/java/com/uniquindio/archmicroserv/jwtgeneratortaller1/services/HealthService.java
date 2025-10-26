package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.HealthCheckDTO;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que coordina todos los health checks de la aplicación
 * siguiendo la especificación MicroProfile Health
 */
@Service
public class HealthService {

    private final Map<String, HealthIndicator> healthIndicators;

    public HealthService(DatabaseHealthIndicator databaseHealthIndicator,
                        ApplicationHealthIndicator applicationHealthIndicator,
                        RabbitMQHealthIndicator rabbitMQHealthIndicator) {
        this.healthIndicators = new HashMap<>();
        this.healthIndicators.put("database", databaseHealthIndicator);
        this.healthIndicators.put("application", applicationHealthIndicator);
        this.healthIndicators.put("rabbitmq", rabbitMQHealthIndicator);
    }

    /**
     * Verifica el estado general de salud de todos los componentes
     */
    public HealthCheckDTO getHealth() {
        List<HealthCheckDTO.HealthCheck> checks = new ArrayList<>();
        boolean allUp = true;

        for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {
            Health health = entry.getValue().health();
            String status = health.getStatus().getCode();
            
            if (!"UP".equals(status)) {
                allUp = false;
            }

            checks.add(new HealthCheckDTO.HealthCheck(
                    entry.getKey(),
                    status,
                    health.getDetails()
            ));
        }

        String overallStatus = allUp ? "UP" : "DOWN";
        return new HealthCheckDTO(overallStatus, checks);
    }

    /**
     * Verifica el estado de preparación (readiness)
     * La aplicación está lista para recibir tráfico si la base de datos está disponible
     */
    public HealthCheckDTO getReadiness() {
        List<HealthCheckDTO.HealthCheck> checks = new ArrayList<>();
        
        // Para readiness, verificamos componentes críticos: base de datos y RabbitMQ
        Health dbHealth = healthIndicators.get("database").health();
        
        // Crear un check de Readiness según el formato especificado
        Map<String, Object> data = new HashMap<>();
        data.put("from", dbHealth.getDetails().get("from"));
        data.put("status", "READY");
        
        checks.add(new HealthCheckDTO.HealthCheck(
                "Readiness check",
                dbHealth.getStatus().getCode(),
                data
        ));

        boolean ready = "UP".equals(dbHealth.getStatus().getCode());
        String overallStatus = ready ? "UP" : "DOWN";
        return new HealthCheckDTO(overallStatus, checks);
    }

    /**
     * Verifica el estado de vivacidad (liveness)
     * La aplicación está viva si está en ejecución
     */
    public HealthCheckDTO getLiveness() {
        List<HealthCheckDTO.HealthCheck> checks = new ArrayList<>();
        
        // Para liveness, solo verificamos que la aplicación esté ejecutándose
        Health appHealth = healthIndicators.get("application").health();
        
        // Crear un check de Liveness según el formato especificado
        Map<String, Object> data = new HashMap<>();
        data.put("from", appHealth.getDetails().get("from"));
        data.put("status", "ALIVE");
        
        checks.add(new HealthCheckDTO.HealthCheck(
                "Liveness check",
                appHealth.getStatus().getCode(),
                data
        ));

        String overallStatus = appHealth.getStatus().getCode();
        return new HealthCheckDTO(overallStatus, checks);
    }
}
