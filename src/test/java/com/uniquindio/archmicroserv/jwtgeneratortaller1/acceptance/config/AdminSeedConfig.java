package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.config;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.repositories.UsuarioRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AdminSeedConfig {

    @Bean
    public Object seedAdminUser(UsuarioRepo usuarioRepo) {
        return new Object() {
            @PostConstruct
            public void init() {
                usuarioRepo.findById("admin").orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsuario("admin");
                    u.setCorreo("admin@example.com");
                    u.setNumeroTelefono("3000000000");
                    u.setClave("admin123");
                    u.setRol(Rol.getRolByName("ADMIN"));
                    return usuarioRepo.save(u);
                });
            }
        };
    }
}


