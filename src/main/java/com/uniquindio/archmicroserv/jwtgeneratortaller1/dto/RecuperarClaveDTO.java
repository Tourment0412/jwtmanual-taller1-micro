package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import jakarta.validation.constraints.NotBlank;

public record RecuperarClaveDTO(
        @NotBlank(message = "El usuario es obligatorio")
        String usuario
) {
}
