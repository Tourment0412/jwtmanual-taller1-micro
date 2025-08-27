package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioClaveDTO(
        @NotBlank(message = "Se debe indicar el usuario")
        String usuario,
        @NotBlank(message = "Debe indicar la nueva clave")
        String clave,
        @NotBlank(message = "Debe indicar el codigo de validacion")
        String codigo
) {
}
