package com.uniquindio.archmicroserv.jwtgeneratortaller1.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RolConverter implements AttributeConverter<Rol, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Rol rol) {
        if (rol == null) {
            return null;
        }
        return rol.ordinal();
    }

    @Override
    public Rol convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return Rol.values()[dbData];
    }
}
