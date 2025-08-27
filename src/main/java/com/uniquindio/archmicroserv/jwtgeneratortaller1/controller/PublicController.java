package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.CambioClaveDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.security.JWTUtils;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PublicController {

    private final JWTUtils jwtUtils;

    private final UsuarioServiceImp usuarioService;

    public PublicController(JWTUtils jwtUtils, UsuarioServiceImp usuarioService) {
        this.jwtUtils = jwtUtils;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody DatosUsuario datosUsuario) {
        if (datosUsuario.getUsuario() == null || datosUsuario.getUsuario().isBlank() ||
                datosUsuario.getCorreo() == null || datosUsuario.getCorreo().isBlank() ||
                datosUsuario.getClave() == null || datosUsuario.getClave().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Atributos de usuario, correo contraseña son obligatorios"));
        }
        try {
            usuarioService.registrarUsuario(datosUsuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409
                    .body(Map.of("error", e.getMessage()));
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody DatosUsuario request) {
        if (request.getUsuario() == null || request.getUsuario().isBlank() ||
                request.getCorreo() == null || request.getCorreo().isBlank() ||
                request.getClave() == null || request.getClave().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Atributos de usuario, correo contraseña son obligatorios"));
        }
        if (usuarioService.existeUsuario(request)) {
            String token = jwtUtils.generarToken(request.getUsuario());
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body(Map.of("error", "No existe el usuario con los datos indicados"));
        }
    }



    @PostMapping("/recuperarClave")
    public ResponseEntity<?> recuperarClave(@Valid @RequestBody String usuario) {
        try {
            usuarioService.enviarCodigoRecuperacion(usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cambiarContraseña")
    public ResponseEntity<?> cambiarClave(@Valid @RequestBody CambioClaveDTO datosCambio) {
        try {
            usuarioService.cambiarClave(datosCambio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<List<Usuario>> obtenerUsuarios(@Valid @RequestBody int pagina) {
        usuarioService.obtenerUsuarios(pagina);
        return ResponseEntity.ok(usuarioService.obtenerUsuarios(pagina));
    }

}
