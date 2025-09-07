package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para cambio de contraseña con código de verificación")
public record CambioClaveRequestDTO(
        @Schema(description = "Nueva contraseña", example = "nuevaPassword123", required = true)
        @NotBlank(message = "Debe indicar la nueva clave")
        String clave,
        @Schema(description = "Código de verificación enviado por email", example = "123456", required = true)
        @NotBlank(message = "Debe indicar el codigo de validacion")
        String codigo
) {
}
