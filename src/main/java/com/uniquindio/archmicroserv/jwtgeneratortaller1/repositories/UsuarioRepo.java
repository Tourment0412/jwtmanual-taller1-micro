package com.uniquindio.archmicroserv.jwtgeneratortaller1.repositories;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepo extends JpaRepository<Usuario, String> {
    
    Optional<Usuario> findByCorreo(String correo);

}
