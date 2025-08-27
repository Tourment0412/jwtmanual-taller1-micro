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
    @Column(name = "usuario")
    private String usuario;
    @Column(name = "correo", unique = true, nullable = false, length = 50)
    private String correo;
    @Column(name = "clave", nullable = false, length = 50)
    private String clave;

    @Embedded
    private CodigoValidacion codigoValidacion;

    @Builder
    public Usuario(String usuario, String clave, String correo) {
        this.usuario = usuario;
        this.clave = clave;
        this.correo = correo;
    }



}
