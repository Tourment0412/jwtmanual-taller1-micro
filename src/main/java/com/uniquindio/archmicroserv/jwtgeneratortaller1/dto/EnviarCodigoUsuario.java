package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EnviarCodigoUsuario(
     @Schema(description = "Nombre de usuario para el cual se generará el código de recuperación", 
            example = "juan123")
    String usuario
) {

}
