package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/v1")
@AllArgsConstructor
public class Admin {


    private final UsuarioServiceImp usuarioService;

    @Tag(name = "Obtener Usuarios", description = "Obtiene parte de los usuarios del sistema")
    @Operation(
            summary = "Obtener usuarios",
            description = "Obtiene una pagina de los usuarios que hay en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Número de página inválido o fuera de rango",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Página solicitada no existe o no contiene usuarios",
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
    @GetMapping("/usuarios")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MessageDTO<?>> obtenerUsuarios(@Valid @RequestParam(defaultValue = "0") int pagina) {
        if (pagina < 0) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageDTO<>(true, "El numero de pagina no puede ser negativo"));
        }
        try {
            return ResponseEntity.ok( new MessageDTO<>(false, usuarioService.obtenerUsuarios(pagina)));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body(new MessageDTO<>(true, "Página solicitada no existe o no contiene usuarios"));
        }
    }

    @Tag(name = "Eliminación de usuario",
            description = "Elimina un usuario del sistema (solo administradores)")
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario existente del sistema. Requiere privilegios de administrador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario eliminado exitosamente del sistema",
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
    @DeleteMapping("/usuarios/{usuario}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MessageDTO<?>> eliminarUsuario(@PathVariable String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageDTO<>(true, "El usuario es obligatorio"));
        }
        try {
            usuarioService.eliminarUsuario(usuario);
            return ResponseEntity.ok(new MessageDTO<>(false, "Usuario eliminado exitosamente del sistema"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageDTO<>(true, "Usuario no encontrado en el sistema"));
        }
    }
    
}
