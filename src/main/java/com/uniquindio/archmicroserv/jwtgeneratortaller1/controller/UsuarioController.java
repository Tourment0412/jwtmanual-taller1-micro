package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/usuario")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioServiceImp usuarioService;

    public UsuarioController(UsuarioServiceImp usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarDatos(@Valid @RequestBody DatosUsuario datosUsuario) {
        if (datosUsuario.getUsuario() == null || datosUsuario.getUsuario().isBlank() ||
                datosUsuario.getCorreo() == null || datosUsuario.getCorreo().isBlank() ||
                datosUsuario.getClave() == null || datosUsuario.getClave().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Atributos de usuario, correo contraseña son obligatorios"));
        }
        try {
            usuarioService.actualizarDatos(datosUsuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
