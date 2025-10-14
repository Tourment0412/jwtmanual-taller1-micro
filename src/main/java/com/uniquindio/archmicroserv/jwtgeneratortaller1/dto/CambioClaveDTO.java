package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import lombok.Data;

@Data
public class CambioClaveDTO {
    private String claveActual;
    private String claveNueva;
    private String confirmacionClave;
}
