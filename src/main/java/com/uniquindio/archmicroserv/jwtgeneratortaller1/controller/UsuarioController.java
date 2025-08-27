package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UsuarioController {

    private final UsuarioServiceImp usuarioService;

    public UsuarioController(UsuarioServiceImp usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarDatos(@Valid @RequestBody DatosUsuario datosUsuario) {
        try {

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body(Map.of("error", e.getMessage()));
        }
        usuarioService.actualizarDatos(datosUsuario);
        return ResponseEntity.ok().build();
    }

}
