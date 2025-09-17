package com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums;

public enum TipoAccion {
    REGISTRO_USUARIO("auth.registered"),
    AUTENTICACION("auth.login"),
    RECUPERAR_PASSWORD("auth.password_recovery"),
    AUTENTICACION_CLAVES("auth.key_auth");

    private final String routingKey;

    TipoAccion(String routingKey) {
        this.routingKey = routingKey;
    }

    public String routingKey() {
        return routingKey;
    }
}

