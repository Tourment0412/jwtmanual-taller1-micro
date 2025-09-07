package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.config.JWTUtils;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.CambioClaveDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.CambioClaveRequestDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.TokenDTO;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1")
public class PublicController {


    private final UsuarioServiceImp usuarioService;

    public PublicController(JWTUtils jwtUtils, UsuarioServiceImp usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Tag(name = "Registro de usuarios", description = "Registra un nuevo usuario")
    @Operation(
            summary = "Registrar usuario",
            description = "Registra un nuevo usuario en la base de datos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de registro inválidos o incompletos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El usuario ya existe en el sistema",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            )
    })
    @PostMapping("/registros")
    public ResponseEntity<MessageDTO<?>> registrarUsuario(@Valid @RequestBody DatosUsuario datosUsuario) {
        if (datosUsuario.getUsuario() == null || datosUsuario.getUsuario().isBlank() ||
                datosUsuario.getCorreo() == null || datosUsuario.getCorreo().isBlank() ||
                datosUsuario.getClave() == null || datosUsuario.getClave().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageDTO<>(true, "Atributos de usuario, correo contraseña son obligatorios"));
        }
        try {
            usuarioService.registrarUsuario(datosUsuario);
            return ResponseEntity
                    .status(HttpStatus.CREATED) // 201
                    .body(new MessageDTO<>(false, "Usuario registrado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409
                    .body(new MessageDTO<>(true, "El usuario ya existe en el sistema"));
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
                    description = "Token de autenticación generado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales de autenticación incorrectas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario no encontrado en el sistema",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            )
    })
    @PostMapping("/sesiones")
    public ResponseEntity<MessageDTO<?>> login(@Valid @RequestBody DatosUsuario request) {
        try {
                TokenDTO tokendto= usuarioService.login(request);
                return ResponseEntity.ok(new MessageDTO<>(false, tokendto));
        } catch (Exception e) {
                if (e.getMessage().equals("Contrasena invalida")) {
                    return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED) // 401
                            .body(new MessageDTO<>(true, "Credenciales de autenticación incorrectas"));
                } else {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN) // 403
                            .body(new MessageDTO<>(true, "Usuario no encontrado en el sistema"));
                }
        }
        
    }

    @Tag(name = "Enviao de codigo de recuperacion",
            description = "Hace que se envie un codigo de verificacion al correo de la cuenta")
    @Operation(
            summary = "Recuperar clave",
            description = "Inicia el proceso para poder obtner una nueva cuenta del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Código de verificación enviado exitosamente al correo",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nombre de usuario es obligatorio",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado en el sistema",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            )
    })
    @PostMapping("/usuarios/{usuario}/recuperacion-contrasena")
    public ResponseEntity<MessageDTO<?>> recuperarClave(@PathVariable String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageDTO<>(true, "El usuario es obligatorio"));
        }
        try {
            usuarioService.enviarCodigoRecuperacion(usuario);
            return ResponseEntity.ok(new MessageDTO<>(false, "Código de verificación enviado exitosamente al correo"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body(new MessageDTO<>(true, "Usuario no encontrado en el sistema"));
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
                    description = "Contraseña cambiada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Código de verificación incorrecto o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado en el sistema",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            )
    })
    @PatchMapping("/usuarios/{usuario}/contrasenas")
    public ResponseEntity<MessageDTO<?>> cambiarClave(@PathVariable String usuario, @Valid @RequestBody CambioClaveRequestDTO datosCambio) {
        if (usuario == null || usuario.isBlank() || datosCambio == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageDTO<>(true, "Datos de cambio de clave son obligatorios"));
        }
        try {
            CambioClaveDTO datosCompletos = new CambioClaveDTO(usuario, datosCambio.clave(), datosCambio.codigo());
            usuarioService.cambiarClave(datosCompletos);
            return ResponseEntity.ok(new MessageDTO<>(false, "Contraseña cambiada exitosamente"));
        } catch (Exception e) {
            if (e.getMessage().equals("Usuario no encontrado")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND) // 404
                        .body(new MessageDTO<>(true, "Usuario no encontrado en el sistema"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN) // 403
                        .body(new MessageDTO<>(true, "Código de verificación incorrecto o expirado"));
            }
        }
    }


}
