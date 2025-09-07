package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para actualización de usuario")
public record ActualizarUsuarioRequestDTO(
        @Schema(description = "Correo electrónico del usuario", example = "juan@email.com", required = true)
        @NotBlank(message = "correo es obligatorio")
        String correo,
        @Schema(description = "Contraseña del usuario", example = "password123", required = true)
        @NotBlank(message = "clave es obligatoria")
        String clave
) {
}
