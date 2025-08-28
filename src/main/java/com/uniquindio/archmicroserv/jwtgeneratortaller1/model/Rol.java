package com.uniquindio.archmicroserv.jwtgeneratortaller1.model;

public enum Rol {
    ADMIN("ADMIN"), CLIENTE("CLIENTE");

    private String nombre;

    Rol(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
