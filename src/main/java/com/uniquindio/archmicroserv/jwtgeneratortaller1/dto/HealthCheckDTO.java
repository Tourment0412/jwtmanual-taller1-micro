package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * DTO que representa el estado de salud de la aplicación
 * siguiendo la especificación MicroProfile Health
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estado de salud de la aplicación")
public record HealthCheckDTO(
        @Schema(description = "Estado general: UP, DOWN, OUT_OF_SERVICE, UNKNOWN", example = "UP")
        String status,
        
        @Schema(description = "Versión del servicio", example = "1.0.0")
        String version,
        
        @Schema(description = "Tiempo al aire en segundos", example = "3600")
        Long uptime,
        
        @Schema(description = "Lista de verificaciones de salud individuales")
        List<HealthCheck> checks
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Verificación de salud individual")
    public record HealthCheck(
            @Schema(description = "Nombre del componente verificado", example = "database")
            String name,
            
            @Schema(description = "Estado del componente: UP o DOWN", example = "UP")
            String status,
            
            @Schema(description = "Datos adicionales del componente")
            Map<String, Object> data
    ) {}
}
