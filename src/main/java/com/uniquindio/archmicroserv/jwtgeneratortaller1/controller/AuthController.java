package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.LoginRequest;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.security.JWTUtils;

import java.util.Map;

@RestController
public class AuthController {

    private final JWTUtils jwtUtils;

    public AuthController(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (request.getUsuario() == null || request.getUsuario().isBlank() ||
            request.getClave() == null || request.getClave().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Atributos de usuario y contraseña son obligatorios"));
        }
        String token = jwtUtils.generarToken(request.getUsuario());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
