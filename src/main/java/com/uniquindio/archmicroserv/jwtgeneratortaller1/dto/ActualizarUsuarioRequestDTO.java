package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualización de usuario")
public record ActualizarUsuarioRequestDTO(
        @Schema(description = "Correo electrónico del usuario", example = "juan@email.com", required = false)
        String correo,
        @Schema(description = "Contraseña del usuario", example = "password123", required = false)
        String clave,
        @Schema(description = "Número de teléfono del usuario", example = "+573001234567", required = false)
        String numeroTelefono
) {
}
