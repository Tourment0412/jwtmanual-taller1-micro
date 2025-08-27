package com.uniquindio.archmicroserv.jwtgeneratortaller1.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Embeddable
@AttributeOverrides({
        @AttributeOverride(name = "codigo", column = @Column(name = "codigoRecuperacion")),
        @AttributeOverride(name = "fechaCreacion", column = @Column(name = "fechaCodigo"))
})
public class CodigoValidacion {

    //Attributes for the class
    @EqualsAndHashCode.Include
    private String codigo;
    private LocalDateTime fechaCreacion;

    //Constructor method for the class
    @Builder
    public CodigoValidacion(LocalDateTime fechaCreacion, String codigo) {
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
    }

}