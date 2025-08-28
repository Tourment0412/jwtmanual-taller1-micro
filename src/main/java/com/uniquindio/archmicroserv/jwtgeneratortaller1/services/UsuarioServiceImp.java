package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;


import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.CambioClaveDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.EmailDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.repositories.UsuarioRepo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class UsuarioServiceImp {

    private final UsuarioRepo usuarioRepo;
    private final EmailServiceImp emailService;

    public void registrarUsuario(@Valid DatosUsuario datosUsuario) throws Exception {
        Usuario usuario = new Usuario(
                datosUsuario.getUsuario(),
                datosUsuario.getCorreo(),
                datosUsuario.getClave()
        );
        if (usuarioRepo.findById(datosUsuario.getUsuario()).isPresent()) {
              throw new Exception("El usuario ya existe");
        }
        usuarioRepo.save(usuario);
    }

    public void cambiarClave(CambioClaveDTO datos) throws Exception {
        Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(datos.usuario());
        if (usuarioEncontrado.isPresent()) {
            Usuario usuario = usuarioEncontrado.get();
            if (usuario.getCodigoValidacion().getCodigo().equals(datos.codigo())) {
                usuario.setClave(datos.clave());
            } else {
                throw new Exception("Codigo incorrecto ");
            }
        } else {
            throw new Exception("Usuario no encontrado");
        }
    }

    public void actualizarDatos(DatosUsuario datosUsuario) {

    }

    /**
     * Metodo para generar diferentes cadenas de texto las cuales serán usadas para los códigos de
     * recuperación y validacion
     * @return código aleatorio de 6 digitos
     */
    private String generarCodigoValidacion() {
        String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int idx = random.nextInt(alfabeto.length());
            codigo.append(alfabeto.charAt(idx));
        }
        return codigo.toString();
    }

    public void enviarCodigoRecuperacion(@Valid String nombreUsuario) throws Exception {
        Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(nombreUsuario);
        if (usuarioEncontrado.isPresent()) {
            Usuario usuario = usuarioEncontrado.get();
            String codigo = generarCodigoValidacion();
            usuario.getCodigoValidacion().setCodigo(codigo);
            usuario.getCodigoValidacion().setFechaCreacion(LocalDateTime.now());
            usuario.setClave(nombreUsuario);
            emailService.sendEmail(new EmailDTO(
                    "Codigo de recuperacion de clave",
                    "El codigo de recuperacion es" + codigo+" tienes hasta 15 minutos",
                    usuario.getCorreo()
            ));
        } else {
            throw new Exception("Usuario no existente");
        }
    }


    public List<Usuario> obtenerUsuarios(@Valid int pagina) {
        Pageable pageable = PageRequest.of(pagina, 10, Sort.by("usuario"));
        Page<Usuario> usuarios = usuarioRepo.findAll(pageable);
        List<Usuario> listaUsuarios = usuarios.getContent();
        return listaUsuarios;
    }

    public boolean existeUsuario(@Valid DatosUsuario request) {
        Optional<Usuario> usuario = usuarioRepo.findById(request.getUsuario());
        return usuario.isPresent() && usuario.get().getCorreo().equals(request.getCorreo()) &&
                usuario.get().getClave().equals(request.getClave());
    }

     //TODO añadir el claim del rol cuando se tenga un getRol() en el usuario
    private Map<String, Object> buildClaims(Usuario usuario) {
        return Map.of(
                "usuario", usuario.getUsuario(),
                "correo",usuario.getCorreo()
                // "rol", usuario.getRol()  // Ejemplo de otro claim
        );
    }
}
