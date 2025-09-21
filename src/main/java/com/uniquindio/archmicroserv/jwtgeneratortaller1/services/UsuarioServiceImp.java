package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.config.JWTUtils;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.config.Constants;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.*;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions.UsuarioNotFoundException;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.messaging.EventoPublisher;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.CodigoValidacion;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.TipoAccion;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.repositories.UsuarioRepo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class UsuarioServiceImp {

    private final UsuarioRepo usuarioRepo;
    //private final EmailServiceImp emailService;
    private final JWTUtils jWTUtils;
    private final EventoPublisher eventoPublisher;


    public void registrarUsuario(@Valid DatosUsuario datosUsuario) throws Exception {
        Usuario usuario = Usuario.builder()
                .usuario(datosUsuario.getUsuario())
                .clave(datosUsuario.getClave())
                .correo(datosUsuario.getCorreo())
                .numeroTelefono(datosUsuario.getNumeroTelefono())
                .codigoValidacion(new CodigoValidacion())
                .build();
        if (usuarioRepo.findById(datosUsuario.getUsuario()).isPresent()) {
            throw new Exception("El usuario ya existe");
        }
        usuarioRepo.save(usuario);

        /*
        Después de regitrar un usuario, el servicio envia el evento de dominio a RabbitMQ
        para publicarlo, y que el microservicio del orquestador puedan reaccionar a este evento.
         */

        //Publicar evento de dominio a RabbitMQ
        EventoDominio evento = EventoDominio.of(
                TipoAccion.REGISTRO_USUARIO,
                Map.of("usuario", usuario.getUsuario(),
                        "correo", usuario.getCorreo(),
                        "numeroTelefono", usuario.getNumeroTelefono()
                )
        );
        eventoPublisher.publicar(evento);
    }

    public void cambiarClave(CambioClaveDTO datos) throws Exception {
        Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(datos.usuario());
        if (usuarioEncontrado.isPresent()) {
            Usuario usuario = usuarioEncontrado.get();
            if (usuario.getCodigoValidacion().getCodigo().equals(datos.codigo())) {
                if(usuario.getCodigoValidacion().getFechaCreacion().plusMinutes(15).isBefore(LocalDateTime.now())){
                    throw new Exception("El codigo ha expirado");
                }
                usuario.setClave(datos.clave());
                usuarioRepo.save(usuario);

                /*
                Se ha creado el evento de dominio para la acción de cambio de clave
                que será publicado en RabbitMQ para que otros microservicios puedan
                reaccionar a este evento.
                 */
                EventoDominio evento = EventoDominio.of(
                        TipoAccion.AUTENTICACION_CLAVES,
                        Map.of(
                                "usuario", usuario.getUsuario(),
                                "correo", usuario.getCorreo(),
                                "fecha", LocalDateTime.now().toString(),
                                "numeroTelefono", usuario.getNumeroTelefono()
                        )
                );

                eventoPublisher.publicar(evento);
            } else {
                throw new Exception("Codigo incorrecto");
            }
        } else {
            throw new Exception("Usuario no encontrado");
        }
    }

    public void actualizarDatos(DatosUsuario datosUsuario) throws Exception {
        Optional<Usuario> usuarioObtenido = usuarioRepo.findById(datosUsuario.getUsuario());
        if (usuarioObtenido.isEmpty()) {
            throw new Exception("Usuario no encontrado");
        }
        Usuario usuario = usuarioObtenido.get();
        usuario.setCorreo(datosUsuario.getCorreo());
        usuario.setClave(datosUsuario.getClave());
        usuarioRepo.save(usuario);
    }

    /**
     * Metodo para generar diferentes cadenas de texto las cuales serán usadas para
     * los códigos de
     * recuperación y validacion
     * 
     * @return código aleatorio de 6 digitos
     */
    private String generarCodigoValidacion() {
        StringBuilder codigo = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < Constants.CODIGO_VALIDACION_LENGTH; i++) {
            int idx = random.nextInt(Constants.ALFABETO_CODIGO.length());
            codigo.append(Constants.ALFABETO_CODIGO.charAt(idx));
        }
        return codigo.toString();
    }

    public void enviarCodigoRecuperacion(@Valid String nombreUsuario) throws Exception {
        try {
            System.out.println("=== INICIO enviarCodigoRecuperacion ===");
            System.out.println("Buscando usuario: " + nombreUsuario);

            Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(nombreUsuario);
            if (usuarioEncontrado.isPresent()) {
                Usuario usuario = usuarioEncontrado.get();
                System.out.println("Usuario encontrado: " + usuario.getUsuario() + ", Email: " + usuario.getCorreo());
                System.out.println("Rol del usuario: " + usuario.getRol());
                System.out.println("CodigoValidacion inicial: " + usuario.getCodigoValidacion());

                String codigo = generarCodigoValidacion();
                System.out.println("Código generado: " + codigo);

                try {
                    // Verificar si codigoValidacion existe, si no, crearlo

                    System.out.println("CodigoValidacion es null, creando uno nuevo...");
                    usuario.setCodigoValidacion(CodigoValidacion.builder()
                            .codigo(codigo)
                            .fechaCreacion(LocalDateTime.now())
                            .build());
                    System.out.println("Código establecido en CodigoValidacion");
                } catch (Exception e) {
                    System.out.println("ERROR al establecer código: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
                /* 
                try {
                    usuario.getCodigoValidacion().setFechaCreacion(LocalDateTime.now());
                    System.out.println("Fecha establecida en CodigoValidacion");
                } catch (Exception e) {
                    System.out.println("ERROR al establecer fecha: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }*/

                try {
                    // Guardar el usuario con el código de validación actualizado
                    System.out.println("Intentando guardar usuario...");
                    usuarioRepo.save(usuario);
                    System.out.println("Usuario guardado exitosamente con código de validación");
                } catch (Exception e) {
                    System.out.println("ERROR al guardar usuario: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }

                EventoDominio evento = EventoDominio.of(
                        TipoAccion.RECUPERAR_PASSWORD,
                        Map.of(
                                "usuario", usuario.getUsuario(),
                                "correo", usuario.getCorreo(),
                                "codigo", codigo,
                                "fecha", usuario.getCodigoValidacion().getFechaCreacion().toString()
                        )
                );

                eventoPublisher.publicar(evento);
                /*
                Este es el metodo para enviar el email, pero se ha comentado
                porque ahora los emails se van a manejar por medio del orquestador

                try {
                    System.out.println("Intentando enviar email...");
                    emailService.sendEmail(new EmailDTO(
                            "Codigo de recuperacion de clave",
                            "El codigo de recuperacion es " + codigo + " tienes hasta 15 minutos",
                            usuario.getCorreo()));
                    System.out.println("Email enviado exitosamente");
                } catch (Exception e) {
                    System.out.println("ERROR al enviar email: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }*/

                System.out.println("=== FIN enviarCodigoRecuperacion - EXITOSO ===");
            } else {
                System.out.println("Usuario NO encontrado en la base de datos");
                throw new UsuarioNotFoundException(Constants.MSG_USUARIO_NO_EXISTENTE);
            }
        } catch (Exception e) {
            System.out.println("=== ERROR en enviarCodigoRecuperacion ===");
            System.out.println("Mensaje de error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<Usuario> obtenerUsuarios(@Valid int pagina) throws Exception {
        Pageable pageable = PageRequest.of(pagina, Constants.TAMANO_PAGINA_DEFAULT, Sort.by("usuario"));
        Page<Usuario> usuarios = usuarioRepo.findAll(pageable);
        List<Usuario> listaUsuarios = usuarios.getContent();
        if (usuarios.getTotalElements() == 0) {
            throw new Exception("Esa pagina no existe");
        }
        return listaUsuarios;
    }

    public boolean existeUsuario(@Valid DatosUsuario request) {
        Optional<Usuario> usuario = usuarioRepo.findById(request.getUsuario());
        return usuario.isPresent() && usuario.get().getCorreo().equals(request.getCorreo()) &&
                usuario.get().getClave().equals(request.getClave());
    }

    public TokenDTO login(DatosUsuario datos) throws Exception {
        Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(datos.getUsuario());
        if (usuarioEncontrado.isEmpty()) {
            throw new Exception("Usuario no encontrado");
        }
        Usuario usuario = usuarioEncontrado.get();
        // TODO encriptar contrasena
        if (!datos.getClave().equals(usuario.getClave())) {
            throw new Exception("Contrasena invalida");
        }
        Map<String, Object> map = buildClaims(usuario);

        /*
        Se ha creado el evento de dominio para la acción de autenticación
        que será publicado en RabbitMQ para que otros microservicios puedan
        reaccionar a este evento.
         */
        EventoDominio evento = EventoDominio.of(
                TipoAccion.AUTENTICACION,
                Map.of(
                        "usuario", usuario.getUsuario(),
                        "correo", usuario.getCorreo(),
                        "fecha", Instant.now().toString(),
                        "numeroTelefono", usuario.getNumeroTelefono()
                )
        );

        eventoPublisher.publicar(evento);
        return new TokenDTO(jWTUtils.generarToken(usuario.getCorreo(), map));
    }

    private Map<String, Object> buildClaims(Usuario usuario) {
        System.out.println("USUARIO: " + usuario.getUsuario());
        System.out.println("CORREO: " + usuario.getCorreo());
        System.out.println("ROL: " + usuario.getRol());
        return Map.of(
                "usuario", usuario.getUsuario(),
                "correo", usuario.getCorreo(),
                "rol", usuario.getRol());
    }

    public void eliminarUsuario(String usuario) throws Exception {
        Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(usuario);
        if (usuarioEncontrado.isPresent()) {
            usuarioRepo.delete(usuarioEncontrado.get());
        } else {
            throw new Exception("Usuario no encontrado");
        }
    }
}
