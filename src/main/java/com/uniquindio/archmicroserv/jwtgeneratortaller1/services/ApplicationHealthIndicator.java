package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Health check para verificar la disponibilidad de la aplicación (liveness)
 * Indica si la aplicación está ejecutándose correctamente
 */
@Component("application")
public class ApplicationHealthIndicator implements HealthIndicator {

    private static final Instant START_TIME = Instant.now();
    
    @Value("${info.app.version:0.0.1}")
    private String version;

    @Override
    public Health health() {
        long uptimeSeconds = Instant.now().getEpochSecond() - START_TIME.getEpochSecond();
        String uptimeFormatted = formatUptime(uptimeSeconds);
        
        return Health.up()
                .withDetail("application", "JWT Generator Service")
                .withDetail("version", version)
                .withDetail("status", "ALIVE")
                .withDetail("from", START_TIME.toString())
                .withDetail("uptime", uptimeFormatted)
                .build();
    }
    
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, secs);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}
