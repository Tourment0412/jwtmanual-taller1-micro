package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import jakarta.validation.constraints.NotBlank;

public class DatosUsuario {

    @NotBlank(message = "usuario es obligatorio")
    private String usuario;

    @NotBlank(message = "correo es obligatorio")
    private String correo;

    @NotBlank(message = "clave es obligatoria")
    private String clave;

    // Getters y setters
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}