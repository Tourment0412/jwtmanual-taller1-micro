package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.ActualizarUsuarioRequestDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para UsuarioController")
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioServiceImp usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ActualizarUsuarioRequestDTO actualizarRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        actualizarRequest = new ActualizarUsuarioRequestDTO("newemail@test.com", "newpassword", null);
    }

    @Test
    @DisplayName("PATCH /v1/usuarios/{usuario} - Actualizar usuario exitosamente")
    void testActualizarDatosExitoso() throws Exception {
        // Arrange
        doNothing().when(usuarioService).actualizarDatos(any(DatosUsuario.class));

        // Act & Assert
        mockMvc.perform(patch("/v1/usuarios/testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.respuesta").value("Usuario actualizado exitosamente"));

        verify(usuarioService, times(1)).actualizarDatos(any(DatosUsuario.class));
    }

    @Test
    @DisplayName("PATCH /v1/usuarios/{usuario} - Usuario no encontrado")
    void testActualizarDatosUsuarioNoEncontrado() throws Exception {
        // Arrange
        doThrow(new Exception("Usuario no encontrado"))
                .when(usuarioService).actualizarDatos(any(DatosUsuario.class));

        // Act & Assert
        mockMvc.perform(patch("/v1/usuarios/noexiste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Usuario no encontrado en el sistema"));

        verify(usuarioService, times(1)).actualizarDatos(any(DatosUsuario.class));
    }

    @Test
    @DisplayName("PATCH /v1/usuarios/{usuario} - Correo ya existe en otro usuario")
    void testActualizarDatosCorreoYaExiste() throws Exception {
        // Arrange
        doThrow(new Exception("El correo electrónico ya está en uso por otro usuario"))
                .when(usuarioService).actualizarDatos(any(DatosUsuario.class));

        // Act & Assert
        mockMvc.perform(patch("/v1/usuarios/testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("El correo electrónico ya está en uso por otro usuario"));

        verify(usuarioService, times(1)).actualizarDatos(any(DatosUsuario.class));
    }

    @Test
    @DisplayName("PATCH /v1/usuarios/{usuario} - Error genérico")
    void testActualizarDatosErrorGenerico() throws Exception {
        // Arrange
        doThrow(new Exception("Otro tipo de error"))
                .when(usuarioService).actualizarDatos(any(DatosUsuario.class));

        // Act & Assert
        mockMvc.perform(patch("/v1/usuarios/testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value(containsString("Error interno del servidor")));

        verify(usuarioService, times(1)).actualizarDatos(any(DatosUsuario.class));
    }
}

