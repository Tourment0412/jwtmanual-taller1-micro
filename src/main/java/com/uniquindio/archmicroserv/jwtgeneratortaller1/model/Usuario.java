package com.uniquindio.archmicroserv.jwtgeneratortaller1.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "Usuarios")
public class Usuario {

    @Id
    @Column(name = "usuario", length = 20)
    private String usuario;
    @Column(name = "correo", nullable = false, length = 20)
    private String correo;
    @Column(name = "clave", nullable = false, length = 20)
    private String clave;
    @Column(name = "rol", nullable = false, length = 15)
    private Rol rol;


    @Embedded
    private CodigoValidacion codigoValidacion;

    @Builder
    public Usuario(String usuario, String clave, String correo) {
        this.usuario = usuario;
        this.clave = clave;
        this.correo = correo;
        this.rol=Rol.CLIENTE;
    }



}
