package com.uniquindio.archmicroserv.jwtgeneratortaller1.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
public enum Rol {
    ADMIN("ADMIN"), CLIENTE("CLIENTE");

    private String nombre;

    /**
     * Metodo contructor de los roles
     * @param nombre
     */
     Rol(String nombre) {
         this.nombre = nombre;
     }
     
     /**
      * Metodo para obtener un rol a partir de su nombre
      * @param nombre
      * @return Enum del Rol
      */
     public static Rol getRolByName(String nombre) {
         for (Rol rol : values()) {
             if (rol.getNombre().equalsIgnoreCase(nombre)) {
                 return rol;
             }
         }
         return null;
     }
}
