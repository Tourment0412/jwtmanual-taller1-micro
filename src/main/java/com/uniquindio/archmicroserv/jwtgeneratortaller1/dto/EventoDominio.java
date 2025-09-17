package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.TipoAccion;

import java.time.Instant;
import java.util.UUID;

public record EventoDominio(
        String id,             // UUID único del mensaje
        TipoAccion tipoAccion, // Enum que indica la acción
        Instant timestamp,     // Cuándo se generó
        Object payload         // Los datos asociados (puede ser un Map o DTO concreto)
) {
    public static EventoDominio of(TipoAccion tipo, Object payload) {
        return new EventoDominio(UUID.randomUUID().toString(), tipo, Instant.now(), payload);
    }
}

