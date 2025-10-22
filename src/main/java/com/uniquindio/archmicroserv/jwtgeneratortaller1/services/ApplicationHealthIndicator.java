package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health check para verificar la disponibilidad de la aplicación (liveness)
 * Indica si la aplicación está ejecutándose correctamente
 */
@Component("application")
public class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Aquí puedes agregar lógica más compleja si necesitas
        // verificar el estado interno de la aplicación
        return Health.up()
                .withDetail("application", "JWT Generator Service")
                .withDetail("status", "Running")
                .build();
    }
}
