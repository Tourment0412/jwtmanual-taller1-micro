package com.uniquindio.archmicroserv.jwtgeneratortaller1.services;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.config.JWTUtils;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.*;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions.UsuarioNotFoundException;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.messaging.EventoPublisher;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.CodigoValidacion;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.repositories.UsuarioRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para UsuarioServiceImp")
class UsuarioServiceImpTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private EventoPublisher eventoPublisher;

    @InjectMocks
    private UsuarioServiceImp usuarioService;

    private DatosUsuario datosUsuario;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        datosUsuario = new DatosUsuario();
        datosUsuario.setUsuario("testuser");
        datosUsuario.setCorreo("test@email.com");
        datosUsuario.setClave("password123");
        datosUsuario.setNumeroTelefono("+1234567890");

        usuario = new Usuario();
        usuario.setUsuario("testuser");
        usuario.setCorreo("test@email.com");
        usuario.setClave("password123");
        usuario.setNumeroTelefono("+1234567890");
        usuario.setRol(com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol.CLIENTE);
        usuario.setCodigoValidacion(CodigoValidacion.builder()
                .fechaCreacion(LocalDateTime.now())
                .codigo("ABC123")
                .build());
    }

    @Test
    @DisplayName("Registrar usuario exitosamente")
    void testRegistrarUsuarioExitoso() throws Exception {
        // Arrange
        when(usuarioRepo.findById(datosUsuario.getUsuario())).thenReturn(Optional.empty());
        when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(eventoPublisher).publicar(any(EventoDominio.class));

        // Act
        usuarioService.registrarUsuario(datosUsuario);

        // Assert
        verify(usuarioRepo, times(1)).findById(datosUsuario.getUsuario());
        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(eventoPublisher, times(1)).publicar(any(EventoDominio.class));
    }

    @Test
    @DisplayName("Registrar usuario - usuario ya existe")
    void testRegistrarUsuarioYaExiste() {
        // Arrange
        when(usuarioRepo.findById(datosUsuario.getUsuario())).thenReturn(Optional.of(usuario));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.registrarUsuario(datosUsuario);
        });

        assertEquals("El usuario ya existe", exception.getMessage());
        verify(usuarioRepo, times(1)).findById(datosUsuario.getUsuario());
        verify(usuarioRepo, never()).save(any(Usuario.class));
        verify(eventoPublisher, never()).publicar(any(EventoDominio.class));
    }

    @Test
    @DisplayName("Login exitoso")
    void testLoginExitoso() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsuario("testuser");
        loginRequest.setClave("password123");
        when(usuarioRepo.findById(loginRequest.getUsuario())).thenReturn(Optional.of(usuario));
        when(jwtUtils.generarToken(anyString(), anyMap())).thenReturn("fake-jwt-token");
        doNothing().when(eventoPublisher).publicar(any(EventoDominio.class));

        // Act
        TokenDTO result = usuarioService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("fake-jwt-token", result.token());
        verify(usuarioRepo, times(1)).findById(loginRequest.getUsuario());
        verify(jwtUtils, times(1)).generarToken(anyString(), anyMap());
        verify(eventoPublisher, times(1)).publicar(any(EventoDominio.class));
    }

    @Test
    @DisplayName("Login - usuario no encontrado")
    void testLoginUsuarioNoEncontrado() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsuario("noexiste");
        loginRequest.setClave("password123");
        when(usuarioRepo.findById(loginRequest.getUsuario())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.login(loginRequest);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepo, times(1)).findById(loginRequest.getUsuario());
        verify(jwtUtils, never()).generarToken(anyString(), anyMap());
    }

    @Test
    @DisplayName("Login - contraseña inválida")
    void testLoginContrasenaInvalida() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsuario("testuser");
        loginRequest.setClave("wrongpassword");
        when(usuarioRepo.findById(loginRequest.getUsuario())).thenReturn(Optional.of(usuario));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.login(loginRequest);
        });

        assertEquals("Contrasena invalida", exception.getMessage());
        verify(usuarioRepo, times(1)).findById(loginRequest.getUsuario());
        verify(jwtUtils, never()).generarToken(anyString(), anyMap());
    }

    @Test
    @DisplayName("Enviar código de recuperación exitosamente")
    void testEnviarCodigoRecuperacionExitoso() throws Exception {
        // Arrange
        when(usuarioRepo.findById("testuser")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(eventoPublisher).publicar(any(EventoDominio.class));

        // Act
        usuarioService.enviarCodigoRecuperacion("testuser");

        // Assert
        verify(usuarioRepo, times(1)).findById("testuser");
        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(eventoPublisher, times(1)).publicar(any(EventoDominio.class));
    }

    @Test
    @DisplayName("Enviar código de recuperación - usuario no encontrado")
    void testEnviarCodigoRecuperacionUsuarioNoEncontrado() {
        // Arrange
        when(usuarioRepo.findById("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioService.enviarCodigoRecuperacion("noexiste");
        });

        verify(usuarioRepo, times(1)).findById("noexiste");
        verify(usuarioRepo, never()).save(any(Usuario.class));
        verify(eventoPublisher, never()).publicar(any(EventoDominio.class));
    }

    @Test
    @DisplayName("Cambiar clave exitosamente")
    void testCambiarClaveExitoso() throws Exception {
        // Arrange
        CodigoValidacion codigoValidacion = CodigoValidacion.builder()
                .codigo("ABC123")
                .fechaCreacion(LocalDateTime.now())
                .build();
        usuario.setCodigoValidacion(codigoValidacion);

        CambioClaveDTO cambioClaveDTO = new CambioClaveDTO("testuser", "newpassword", "ABC123");
        when(usuarioRepo.findById("testuser")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(eventoPublisher).publicar(any(EventoDominio.class));

        // Act
        usuarioService.cambiarClave(cambioClaveDTO);

        // Assert
        assertEquals("newpassword", usuario.getClave());
        verify(usuarioRepo, times(1)).findById("testuser");
        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(eventoPublisher, times(1)).publicar(any(EventoDominio.class));
    }

    @Test
    @DisplayName("Cambiar clave - código incorrecto")
    void testCambiarClaveCodigoIncorrecto() {
        // Arrange
        CodigoValidacion codigoValidacion = CodigoValidacion.builder()
                .codigo("ABC123")
                .fechaCreacion(LocalDateTime.now())
                .build();
        usuario.setCodigoValidacion(codigoValidacion);

        CambioClaveDTO cambioClaveDTO = new CambioClaveDTO("testuser", "newpassword", "WRONG");
        when(usuarioRepo.findById("testuser")).thenReturn(Optional.of(usuario));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.cambiarClave(cambioClaveDTO);
        });

        assertEquals("Codigo incorrecto", exception.getMessage());
        verify(usuarioRepo, times(1)).findById("testuser");
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Cambiar clave - código expirado")
    void testCambiarClaveCodigoExpirado() {
        // Arrange
        CodigoValidacion codigoValidacion = CodigoValidacion.builder()
                .codigo("ABC123")
                .fechaCreacion(LocalDateTime.now().minusMinutes(20)) // Expirado
                .build();
        usuario.setCodigoValidacion(codigoValidacion);

        CambioClaveDTO cambioClaveDTO = new CambioClaveDTO("testuser", "newpassword", "ABC123");
        when(usuarioRepo.findById("testuser")).thenReturn(Optional.of(usuario));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.cambiarClave(cambioClaveDTO);
        });

        assertEquals("El codigo ha expirado", exception.getMessage());
        verify(usuarioRepo, times(1)).findById("testuser");
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Actualizar datos del usuario exitosamente")
    void testActualizarDatosExitoso() throws Exception {
        // Arrange
        DatosUsuario nuevosDatos = new DatosUsuario();
        nuevosDatos.setUsuario("testuser");
        nuevosDatos.setCorreo("newemail@test.com");
        nuevosDatos.setClave("newpassword");

        when(usuarioRepo.findById("testuser")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.findByCorreo("newemail@test.com")).thenReturn(Optional.empty());
        when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.actualizarDatos(nuevosDatos);

        // Assert
        verify(usuarioRepo, times(1)).findById("testuser");
        verify(usuarioRepo, times(1)).findByCorreo("newemail@test.com");
        verify(usuarioRepo, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Actualizar datos - usuario no encontrado")
    void testActualizarDatosUsuarioNoEncontrado() {
        // Arrange
        DatosUsuario nuevosDatos = new DatosUsuario();
        nuevosDatos.setUsuario("noexiste");
        nuevosDatos.setCorreo("email@test.com");
        nuevosDatos.setClave("password");

        when(usuarioRepo.findById("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.actualizarDatos(nuevosDatos);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepo, times(1)).findById("noexiste");
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Actualizar datos - correo ya existe en otro usuario")
    void testActualizarDatosCorreoYaExiste() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setUsuario("otrousuario");
        otroUsuario.setCorreo("existente@test.com");
        otroUsuario.setClave("pass");
        otroUsuario.setNumeroTelefono("+999999");
        otroUsuario.setRol(com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol.CLIENTE);
        otroUsuario.setCodigoValidacion(CodigoValidacion.builder()
                .fechaCreacion(LocalDateTime.now())
                .codigo("XYZ789")
                .build());

        DatosUsuario nuevosDatos = new DatosUsuario();
        nuevosDatos.setUsuario("testuser");
        nuevosDatos.setCorreo("existente@test.com");
        nuevosDatos.setClave("password");

        when(usuarioRepo.findById("testuser")).thenReturn(Optional.of(usuario));
        when(usuarioRepo.findByCorreo("existente@test.com")).thenReturn(Optional.of(otroUsuario));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.actualizarDatos(nuevosDatos);
        });

        assertEquals("El correo electrónico ya está en uso por otro usuario", exception.getMessage());
        verify(usuarioRepo, times(1)).findById("testuser");
        verify(usuarioRepo, times(1)).findByCorreo("existente@test.com");
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Obtener usuarios - página válida")
    void testObtenerUsuariosExitoso() throws Exception {
        // Arrange
        List<Usuario> usuarios = Arrays.asList(usuario);
        Page<Usuario> page = new PageImpl<>(usuarios);
        when(usuarioRepo.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        List<Usuario> result = usuarioService.obtenerUsuarios(0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(usuarioRepo, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener usuarios - página no existe")
    void testObtenerUsuariosPaginaNoExiste() {
        // Arrange
        Page<Usuario> emptyPage = new PageImpl<>(Collections.emptyList());
        when(usuarioRepo.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            usuarioService.obtenerUsuarios(99);
        });

        assertEquals("Esa pagina no existe", exception.getMessage());
        verify(usuarioRepo, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Existe usuario - usuario válido")
    void testExisteUsuarioValido() {
        // Arrange
        when(usuarioRepo.findById(datosUsuario.getUsuario())).thenReturn(Optional.of(usuario));

        // Act
        boolean result = usuarioService.existeUsuario(datosUsuario);

        // Assert
        assertTrue(result);
        verify(usuarioRepo, times(1)).findById(datosUsuario.getUsuario());
    }

    @Test
    @DisplayName("Existe usuario - usuario no existe")
    void testExisteUsuarioNoExiste() {
        // Arrange
        when(usuarioRepo.findById(datosUsuario.getUsuario())).thenReturn(Optional.empty());

        // Act
        boolean result = usuarioService.existeUsuario(datosUsuario);

        // Assert
        assertFalse(result);
        verify(usuarioRepo, times(1)).findById(datosUsuario.getUsuario());
    }

    @Test
    @DisplayName("Eliminar usuario exitosamente")
    void testEliminarUsuarioExitoso() throws UsuarioNotFoundException {
        // Arrange
        when(usuarioRepo.findById("testuser")).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepo).delete(any(Usuario.class));

        // Act
        usuarioService.eliminarUsuario("testuser");

        // Assert
        verify(usuarioRepo, times(1)).findById("testuser");
        verify(usuarioRepo, times(1)).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("Eliminar usuario - usuario no encontrado")
    void testEliminarUsuarioNoEncontrado() {
        // Arrange
        when(usuarioRepo.findById("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioService.eliminarUsuario("noexiste");
        });

        verify(usuarioRepo, times(1)).findById("noexiste");
        verify(usuarioRepo, never()).delete(any(Usuario.class));
    }
}

