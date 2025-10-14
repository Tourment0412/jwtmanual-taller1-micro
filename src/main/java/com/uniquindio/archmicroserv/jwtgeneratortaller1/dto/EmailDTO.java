package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import lombok.Data;

@Data
public class EmailDTO {
    private String destinatario;
    private String asunto;
    private String mensaje;
    private String tipo;
}
