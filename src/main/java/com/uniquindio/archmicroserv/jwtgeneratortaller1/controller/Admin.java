package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/admin")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class Admin {


    private final UsuarioServiceImp usuarioService;

    @Tag(name = "Obtener Usuarios", description = "Obtiene parte de los usuarios del sistema")
    @Operation(
            summary = "Obtener usaurios",
            description = "Obtiene una pagina de los usuarios que hay en el sistem"
    )
    @Parameter(
            description = "Numero de pagina",
            required =true,
            example = "2"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = ""
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = ""
            )
    })
    @GetMapping("/get-all")
    public ResponseEntity<Object> obtenerUsuarios(@Valid @RequestParam(defaultValue = "0") int pagina) {
        try {
            return ResponseEntity.ok(usuarioService.obtenerUsuarios(pagina));
        } catch (Exception e) {
            if (e.getMessage().equals("Esa pagina no existe")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND) // 404
                        .body(Map.of("error", e.getMessage()));
            }
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    
}
