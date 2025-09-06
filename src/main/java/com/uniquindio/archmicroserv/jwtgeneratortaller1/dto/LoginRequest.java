package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para inicio de sesión")
public class LoginRequest {

    @Schema(description = "Nombre de usuario o correo electrónico", example = "juan@email.com", required = true)
    @NotBlank(message = "usuario es obligatorio")
    private String usuario;

    @Schema(description = "Contraseña del usuario", example = "password123", required = true)
    @NotBlank(message = "clave es obligatoria")
    private String clave;

    // Getters y setters
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
