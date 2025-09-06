package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar de la API")
public record MessageDTO<T>(
        @Schema(description = "Indica si hubo un error en la operación", example = "false", required = true)
        boolean error, 
        @Schema(description = "Datos de respuesta o mensaje", required = true)
        T respuesta
) {
}
