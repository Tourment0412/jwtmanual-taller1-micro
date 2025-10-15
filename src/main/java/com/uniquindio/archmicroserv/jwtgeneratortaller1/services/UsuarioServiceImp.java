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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class UsuarioServiceImp {

    private final UsuarioRepo usuarioRepo;
    //private final EmailServiceImp emailService;
    private final JWTUtils jWTUtils;
    private final EventoPublisher eventoPublisher;


    public void registrarUsuario(@Valid DatosUsuario datosUsuario) throws Exception {
        log.info("Iniciando registro de usuario: {}", datosUsuario.getUsuario());
        Usuario usuario = Usuario.builder()
                .usuario(datosUsuario.getUsuario())
                .clave(datosUsuario.getClave())
                .correo(datosUsuario.getCorreo())
                .numeroTelefono(datosUsuario.getNumeroTelefono())
                .codigoValidacion(new CodigoValidacion())
                .build();
        try {
            if (usuarioRepo.findById(datosUsuario.getUsuario()).isPresent()) {
                throw new Exception("El usuario ya existe");
            }
            log.debug("Usuario creado, guardando en base de datos");
            usuarioRepo.save(usuario);
            log.info("Usuario guardado exitosamente: {}", usuario.getUsuario());
        } catch (Exception e) {
            log.error("Error guardando usuario {}: {}", datosUsuario.getUsuario(), e.getMessage(), e);
            throw e;
        }

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
        try {
            eventoPublisher.publicar(evento);
            log.info("Evento de registro publicado para usuario: {}", usuario.getUsuario());
        } catch (Exception e) {
            log.error("Error publicando evento para usuario {}: {}", usuario.getUsuario(), e.getMessage(), e);
            throw e;
        }
        log.info("Registro de usuario completado: {}", usuario.getUsuario());
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
        
        // Verificar si el correo ya existe en otro usuario
        Optional<Usuario> usuarioConMismoCorreo = usuarioRepo.findByCorreo(datosUsuario.getCorreo());
        if (usuarioConMismoCorreo.isPresent() && !usuarioConMismoCorreo.get().getUsuario().equals(datosUsuario.getUsuario())) {
            throw new Exception("El correo electrónico ya está en uso por otro usuario");
        }
        
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
            log.info("Iniciando proceso de recuperación de clave para usuario: {}", nombreUsuario);

            Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(nombreUsuario);
            if (usuarioEncontrado.isPresent()) {
                Usuario usuario = usuarioEncontrado.get();
                log.debug("Usuario encontrado: {}, Email: {}, Rol: {}", 
                    usuario.getUsuario(), usuario.getCorreo(), usuario.getRol());

                String codigo = generarCodigoValidacion();
                log.debug("Código de recuperación generado");

                try {
                    // Verificar si codigoValidacion existe, si no, crearlo
                    log.debug("Estableciendo código de validación");
                    usuario.setCodigoValidacion(CodigoValidacion.builder()
                            .codigo(codigo)
                            .fechaCreacion(LocalDateTime.now())
                            .build());
                    log.debug("Código establecido correctamente");
                } catch (Exception e) {
                    log.error("Error al establecer código de validación: {}", e.getMessage(), e);
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
                    log.debug("Guardando usuario con código de validación");
                    usuarioRepo.save(usuario);
                    log.info("Usuario guardado exitosamente con código de validación");
                } catch (Exception e) {
                    log.error("Error al guardar usuario {}: {}", nombreUsuario, e.getMessage(), e);
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

                log.info("Proceso de recuperación de clave completado para usuario: {}", nombreUsuario);
            } else {
                log.warn("Usuario no encontrado en la base de datos: {}", nombreUsuario);
                throw new UsuarioNotFoundException(Constants.MSG_USUARIO_NO_EXISTENTE);
            }
        } catch (Exception e) {
            log.error("Error en proceso de recuperación de clave para {}: {}", nombreUsuario, e.getMessage(), e);
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

    public TokenDTO login(LoginRequest datos) throws Exception {
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
                        "fecha", LocalDateTime.now().minusHours(5).toString(),
                        "numeroTelefono", usuario.getNumeroTelefono()
                )
        );

        eventoPublisher.publicar(evento);
        return new TokenDTO(jWTUtils.generarToken(usuario.getCorreo(), map));
    }

    private Map<String, Object> buildClaims(Usuario usuario) {
        log.debug("Construyendo claims para usuario: {}, correo: {}, rol: {}", 
            usuario.getUsuario(), usuario.getCorreo(), usuario.getRol());
        return Map.of(
                "usuario", usuario.getUsuario(),
                "correo", usuario.getCorreo(),
                "rol", usuario.getRol());
    }

    public void eliminarUsuario(String usuario) throws UsuarioNotFoundException {
        Optional<Usuario> usuarioEncontrado = usuarioRepo.findById(usuario);
        if (usuarioEncontrado.isPresent()) {
            usuarioRepo.delete(usuarioEncontrado.get());
        } else {
            throw new UsuarioNotFoundException("Usuario no encontrado");
        }
    }
}
