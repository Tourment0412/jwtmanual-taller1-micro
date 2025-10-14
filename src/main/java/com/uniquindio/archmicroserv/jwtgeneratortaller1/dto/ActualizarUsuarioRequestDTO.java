package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import lombok.Data;

@Data
public class ActualizarUsuarioRequestDTO {
    private String usuario;
    private String correo;
    private String numeroTelefono;
}
