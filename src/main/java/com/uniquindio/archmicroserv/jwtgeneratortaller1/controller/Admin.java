package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class Admin {


    private final UsuarioServiceImp usuarioService;

    @GetMapping("/get-all")
    public ResponseEntity<List<Usuario>> obtenerUsuarios(@Valid @RequestParam(defaultValue = "0") int pagina) {
        usuarioService.obtenerUsuarios(pagina);
        return ResponseEntity.ok(usuarioService.obtenerUsuarios(pagina));
    }
    
}
