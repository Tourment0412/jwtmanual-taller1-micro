package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.config.JWTUtils;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.*;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions.UsuarioNotFoundException;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para PublicController")
class PublicControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioServiceImp usuarioService;

    @Mock
    private JWTUtils jwtUtils;

    @InjectMocks
    private PublicController publicController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private DatosUsuario datosUsuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicController).build();
        
        datosUsuario = new DatosUsuario();
        datosUsuario.setUsuario("testuser");
        datosUsuario.setCorreo("test@email.com");
        datosUsuario.setClave("password123");
        datosUsuario.setNumeroTelefono("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsuario("testuser");
        loginRequest.setClave("password123");
    }

    @Test
    @DisplayName("POST /v1/usuarios - Registrar usuario exitosamente")
    void testRegistrarUsuarioExitoso() throws Exception {
        // Arrange
        doNothing().when(usuarioService).registrarUsuario(any(DatosUsuario.class));

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.respuesta").value("Usuario registrado exitosamente"));

        verify(usuarioService, times(1)).registrarUsuario(any(DatosUsuario.class));
    }

    @Test
    @DisplayName("POST /v1/usuarios - Datos incompletos")
    void testRegistrarUsuarioDatosIncompletos() throws Exception {
        // Arrange
        DatosUsuario datosIncompletos = new DatosUsuario();
        datosIncompletos.setUsuario("testuser");
        datosIncompletos.setCorreo("");  // Vacío
        datosIncompletos.setClave("");   // Vacío

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosIncompletos)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).registrarUsuario(any(DatosUsuario.class));
    }

    @Test
    @DisplayName("POST /v1/usuarios - Usuario ya existe (conflict)")
    void testRegistrarUsuarioYaExiste() throws Exception {
        // Arrange
        doThrow(new DataIntegrityViolationException("Usuario ya existe"))
                .when(usuarioService).registrarUsuario(any(DatosUsuario.class));

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosUsuario)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("El usuario ya existe en el sistema"));

        verify(usuarioService, times(1)).registrarUsuario(any(DatosUsuario.class));
    }

    @Test
    @DisplayName("POST /v1/usuarios - Error interno del servidor")
    void testRegistrarUsuarioErrorInterno() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error inesperado"))
                .when(usuarioService).registrarUsuario(any(DatosUsuario.class));

        // Act & Assert
        mockMvc.perform(post("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosUsuario)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Error interno del servidor"));

        verify(usuarioService, times(1)).registrarUsuario(any(DatosUsuario.class));
    }

    @Test
    @DisplayName("POST /v1/sesiones - Login exitoso")
    void testLoginExitoso() throws Exception {
        // Arrange
        TokenDTO tokenDTO = new TokenDTO("fake-jwt-token");
        when(usuarioService.login(any(LoginRequest.class))).thenReturn(tokenDTO);

        // Act & Assert
        mockMvc.perform(post("/v1/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.respuesta.token").value("fake-jwt-token"));

        verify(usuarioService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /v1/sesiones - Contraseña inválida")
    void testLoginContrasenaInvalida() throws Exception {
        // Arrange
        when(usuarioService.login(any(LoginRequest.class)))
                .thenThrow(new Exception("Contrasena invalida"));

        // Act & Assert
        mockMvc.perform(post("/v1/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Credenciales de autenticación incorrectas"));

        verify(usuarioService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /v1/sesiones - Usuario no encontrado")
    void testLoginUsuarioNoEncontrado() throws Exception {
        // Arrange
        when(usuarioService.login(any(LoginRequest.class)))
                .thenThrow(new Exception("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/v1/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Usuario no encontrado en el sistema"));

        verify(usuarioService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /v1/codigos - Enviar código de recuperación exitosamente")
    void testRecuperarClaveExitoso() throws Exception {
        // Arrange
        EnviarCodigoUsuario request = new EnviarCodigoUsuario("testuser");
        doNothing().when(usuarioService).enviarCodigoRecuperacion(anyString());

        // Act & Assert
        mockMvc.perform(post("/v1/codigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.respuesta").value("Código de verificación enviado exitosamente al correo"));

        verify(usuarioService, times(1)).enviarCodigoRecuperacion("testuser");
    }

    @Test
    @DisplayName("POST /v1/codigos - Usuario vacío")
    void testRecuperarClaveUsuarioVacio() throws Exception {
        // Arrange
        EnviarCodigoUsuario request = new EnviarCodigoUsuario("");

        // Act & Assert
        mockMvc.perform(post("/v1/codigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("El usuario es obligatorio"));

        verify(usuarioService, never()).enviarCodigoRecuperacion(anyString());
    }

    @Test
    @DisplayName("POST /v1/codigos - Usuario no encontrado")
    void testRecuperarClaveUsuarioNoEncontrado() throws Exception {
        // Arrange
        EnviarCodigoUsuario request = new EnviarCodigoUsuario("noexiste");
        doThrow(new UsuarioNotFoundException("Usuario no encontrado"))
                .when(usuarioService).enviarCodigoRecuperacion("noexiste");

        // Act & Assert
        mockMvc.perform(post("/v1/codigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Usuario no encontrado en el sistema"));

        verify(usuarioService, times(1)).enviarCodigoRecuperacion("noexiste");
    }

    @Test
    @DisplayName("PATCH /v1/usuarios/{usuario}/contrasena - Cambiar clave exitosamente")
    void testCambiarClaveExitoso() throws Exception {
        // Arrange
        CambioClaveRequestDTO request = new CambioClaveRequestDTO("newpassword", "ABC123");
        doNothing().when(usuarioService).cambiarClave(any(CambioClaveDTO.class));

        // Act & Assert
        mockMvc.perform(patch("/v1/usuarios/testuser/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.respuesta").value("Contraseña cambiada exitosamente"));

        verify(usuarioService, times(1)).cambiarClave(any(CambioClaveDTO.class));
    }

    @Test
    @DisplayName("PATCH /v1/usuarios/{usuario}/contrasena - Usuario no encontrado")
    void testCambiarClaveUsuarioNoEncontrado() throws Exception {
        // Arrange
        CambioClaveRequestDTO request = new CambioClaveRequestDTO("newpassword", "ABC123");
        doThrow(new Exception("Usuario no encontrado"))
                .when(usuarioService).cambiarClave(any(CambioClaveDTO.class));

        // Act & Assert
        mockMvc.perform(patch("/v1/usuarios/noexiste/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Usuario no encontrado en el sistema"));

        verify(usuarioService, times(1)).cambiarClave(any(CambioClaveDTO.class));
    }

    @Test
    @DisplayName("PATCH /v1/usuarios/{usuario}/contrasena - Código incorrecto")
    void testCambiarClaveCodigoIncorrecto() throws Exception {
        // Arrange
        CambioClaveRequestDTO request = new CambioClaveRequestDTO("newpassword", "WRONG");
        doThrow(new Exception("Codigo incorrecto"))
                .when(usuarioService).cambiarClave(any(CambioClaveDTO.class));

        // Act & Assert
        mockMvc.perform(patch("/v1/usuarios/testuser/contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Código de verificación incorrecto o expirado"));

        verify(usuarioService, times(1)).cambiarClave(any(CambioClaveDTO.class));
    }
}

