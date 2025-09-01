package com.uniquindio.archmicroserv.jwtgeneratortaller1.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    @Column(name = "rol", nullable = false)
    @Convert(converter = RolConverter.class)
    private Rol rol;


    @Embedded
    private CodigoValidacion codigoValidacion;

    @Builder
    public Usuario(String usuario, String clave, String correo) {
        this.usuario = usuario;
        this.clave = clave;
        this.correo = correo;
        this.rol = Rol.getRolByName("CLIENTE");
        this.codigoValidacion = CodigoValidacion.builder()
                .codigo("")
                .fechaCreacion(LocalDateTime.now())
                .build();
    }


    public String getRol() {
        return rol.getNombre();
    }   



}
