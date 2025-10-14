package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import lombok.Data;

@Data
public class CambioClaveRequestDTO {
    private String claveActual;
    private String claveNueva;
}
