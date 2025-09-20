package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos de usuario para registro y actualización")
public class DatosUsuario {

    @Schema(description = "Nombre de usuario único", example = "juan123", required = true)
    @NotBlank(message = "usuario es obligatorio")
    private String usuario;

    @Schema(description = "Correo electrónico del usuario", example = "juan@email.com", required = true)
    @NotBlank(message = "correo es obligatorio")
    private String correo;

    @Schema(description = "Número de teléfono del usuario", example = "+571234567890", required = false)
    @NotBlank(message = "numero de telefono es obligatorio")
    private String numeroTelefono;

    @Schema(description = "Contraseña del usuario", example = "password123", required = true)
    @NotBlank(message = "clave es obligatoria")
    private String clave;


    // Getters y setters
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }
}