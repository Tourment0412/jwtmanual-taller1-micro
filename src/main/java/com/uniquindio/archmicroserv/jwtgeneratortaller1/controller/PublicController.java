package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.config.JWTUtils;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.CambioClaveDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
@RequestMapping("/publico")
public class PublicController {

    private final JWTUtils jwtUtils;

    private final UsuarioServiceImp usuarioService;

    public PublicController(JWTUtils jwtUtils, UsuarioServiceImp usuarioService) {
        this.jwtUtils = jwtUtils;
        this.usuarioService = usuarioService;
    }

    @Tag(name = "Registro de usuarios", description = "Registra un nuevo usuario")
    @Operation(
            summary = "Registrar usuario",
            description = "Registra un nuevo usuario en la base de datos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Atributos de usuario, correo y contraseña son obligatorios"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El usuario ya existe"
            )
    })
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

    @Tag(name = "Login de usuario",
            description = "Permite iniciar sesion al usario")
    @Operation(
            summary = "Iniciar sesion",
            description = "Genera el token de autenticacion para el usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token de autenticacion generado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Atributos de correo y contraseña son obligatorios"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe el usuario con los datos indicados"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody DatosUsuario request) {
        if (
                request.getCorreo() == null || request.getCorreo().isBlank() ||
                request.getClave() == null || request.getClave().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Atributos de usuario, correo contraseña son obligatorios"));
        }
        if (usuarioService.existeUsuario(request)) {
            String token = jwtUtils.generarToken(request.getCorreo(), null);
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body(Map.of("error", "No existe el usuario con los datos indicados"));
        }
    }

    @Tag(name = "Recuperacion de clave",
            description = "Hace que se envie un codigo de verificacion al correo de la cuenta")
    @Operation(
            summary = "Recuperar clave",
            description = "Inicia el proceso para poder obtner una nueva cuenta del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Codigo de verificacion enviado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El usuario es obligatorio"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no existente"
            )
    })
    @PostMapping("/recuperarClave")
    public ResponseEntity<?> recuperarClave(@Valid @RequestBody String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "El usuario es obligatorio"));
        }
        try {
            usuarioService.enviarCodigoRecuperacion(usuario);
            return ResponseEntity.ok(Map.of("message", "Codigo de verificacion enviado exitosamente"));
        } catch (Exception e) {
            if (e.getMessage().equals("Usuario no existente")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND) // 404
                        .body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                        .body(Map.of("error", "Error interno del servidor"));
            }
        }
    }

    @Tag(name = "Cambio de clave",
            description = "Cambia la clave un usuario")
    @Operation(
            summary = "Cambiar contrasena",
            description = "Mediante el ingreso del codigo recibido por email, permite" +
                    "establoecer una nueva contrasena"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Clave cambiada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de cambio de clave son obligatorios"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Codigo de verificacion incorrecto"
            )
    })
    @PostMapping("/cambiarContraseña")
    public ResponseEntity<?> cambiarClave(@Valid @RequestBody CambioClaveDTO datosCambio) {
        if (datosCambio == null || datosCambio.usuario() == null || datosCambio.usuario().isBlank() ||
            datosCambio.codigo() == null || datosCambio.codigo().isBlank() ||
            datosCambio.clave() == null || datosCambio.clave().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Datos de cambio de clave son obligatorios"));
        }
        try {
            usuarioService.cambiarClave(datosCambio);
            return ResponseEntity.ok(Map.of("message", "Clave cambiada exitosamente"));
        } catch (Exception e) {
            if (e.getMessage().equals("Usuario no encontrado")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND) // 404
                        .body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN) // 403
                        .body(Map.of("error", "Codigo de verificacion incorrecto"));
            }
        }
    }

}
