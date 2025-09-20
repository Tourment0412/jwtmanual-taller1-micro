package com.uniquindio.archmicroserv.jwtgeneratortaller1.model;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(name = "usuario", length = 255)
    private String usuario;
    @Column(name = "correo", nullable = false, length = 255)
    private String correo;
    @Column(name = "clave", nullable = false, length = 255)
    private String clave;
    @Column(name = "numero_telefono", nullable = false, length = 20)
    private String numeroTelefono;
    @Column(name = "rol", nullable = false)
    private Rol rol;


    @Embedded
    private CodigoValidacion codigoValidacion;

    @Builder
    public Usuario(String usuario, String clave, String correo, String numeroTelefono, CodigoValidacion codigoValidacion) {
        this.usuario = usuario;
        this.clave = clave;
        this.correo = correo;
        this.numeroTelefono = numeroTelefono;
        this.rol = Rol.getRolByName("CLIENTE");
        this.codigoValidacion = codigoValidacion;
    }

    public String getRol() {
        return rol.getNombre();
    }   



}
