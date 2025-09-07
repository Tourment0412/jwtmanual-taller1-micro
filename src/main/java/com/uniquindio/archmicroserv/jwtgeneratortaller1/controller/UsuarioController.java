package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.ActualizarUsuarioRequestDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/usuarios")

public class UsuarioController {

    private final UsuarioServiceImp usuarioService;

    public UsuarioController(UsuarioServiceImp usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Tag(name = "Actualizar datos de usuario",
            description = "Actualiza los datos de la cuenta del usuario autenticado")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza parcialmente los datos de un usuario existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"error\": false, \"respuesta\": \"Usuario actualizado exitosamente\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticación requerido, expirado o inválido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Token requerido",
                                            value = "{\"error\": true, \"respuesta\": \"Token de autenticación requerido\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Token expirado",
                                            value = "{\"error\": true, \"respuesta\": \"El token ha expirado\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Token inválido",
                                            value = "{\"error\": true, \"respuesta\": \"El token es inválido o malformado\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Token con emisor o rol inválido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Emisor inválido",
                                            value = "{\"error\": true, \"respuesta\": \"El emisor del token no es válido\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Rol inválido",
                                            value = "{\"error\": true, \"respuesta\": \"El rol del token no es válido para esta operación\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado en el sistema",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"error\": true, \"respuesta\": \"Usuario no encontrado en el sistema\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El correo electrónico ya está en uso por otro usuario",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"error\": true, \"respuesta\": \"El correo electrónico ya está en uso por otro usuario\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor durante la actualización o validación del token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Error de validación",
                                            value = "{\"error\": true, \"respuesta\": \"Error interno del servidor durante la validación del token\"}"
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Error de actualización",
                                            value = "{\"error\": true, \"respuesta\": \"Error interno del servidor\"}"
                                    )
                            }
                    )
            )
    })
    @PatchMapping("/{usuario}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MessageDTO<?>> actualizarDatos(@PathVariable String usuario, @Valid @RequestBody ActualizarUsuarioRequestDTO datosUsuario) {
        try {
            DatosUsuario datosCompletos = new DatosUsuario();
            datosCompletos.setUsuario(usuario);
            datosCompletos.setCorreo(datosUsuario.correo());
            datosCompletos.setClave(datosUsuario.clave());
            usuarioService.actualizarDatos(datosCompletos);
            return ResponseEntity.ok(new MessageDTO<>(false, "Usuario actualizado exitosamente"));
        } catch (Exception e) {
            if (e.getMessage().equals("Usuario no encontrado")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND) // 404
                        .body(new MessageDTO<>(true, "Usuario no encontrado en el sistema"));
            } else if (e.getMessage().equals("El correo electrónico ya está en uso por otro usuario")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT) // 409
                        .body(new MessageDTO<>(true, "El correo electrónico ya está en uso por otro usuario"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND) // 404 - fallback para otros errores
                        .body(new MessageDTO<>(true, "Usuario no encontrado en el sistema"));
            }
        }
    }


}
