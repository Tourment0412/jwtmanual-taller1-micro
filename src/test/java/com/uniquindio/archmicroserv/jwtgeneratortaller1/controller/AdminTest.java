package com.uniquindio.archmicroserv.jwtgeneratortaller1.controller;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.exceptions.UsuarioNotFoundException;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.services.UsuarioServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para Admin Controller")
class AdminTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioServiceImp usuarioService;

    @InjectMocks
    private Admin adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    @DisplayName("GET /v1/usuarios - Obtener usuarios exitosamente")
    void testObtenerUsuariosExitoso() throws Exception {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setUsuario("testuser");
        usuario.setCorreo("test@email.com");
        usuario.setRol(com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol.CLIENTE);
        
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioService.obtenerUsuarios(0)).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/v1/usuarios")
                        .param("pagina", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.respuesta").isArray());

        verify(usuarioService, times(1)).obtenerUsuarios(0);
    }

    @Test
    @DisplayName("GET /v1/usuarios - Número de página negativo")
    void testObtenerUsuariosPaginaNegativa() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/usuarios")
                        .param("pagina", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("El numero de pagina no puede ser negativo"));

        verify(usuarioService, never()).obtenerUsuarios(anyInt());
    }

    @Test
    @DisplayName("GET /v1/usuarios - Página no existe")
    void testObtenerUsuariosPaginaNoExiste() throws Exception {
        // Arrange
        when(usuarioService.obtenerUsuarios(99)).thenThrow(new Exception("Esa pagina no existe"));

        // Act & Assert
        mockMvc.perform(get("/v1/usuarios")
                        .param("pagina", "99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Página solicitada no existe o no contiene usuarios"));

        verify(usuarioService, times(1)).obtenerUsuarios(99);
    }

    @Test
    @DisplayName("GET /v1/usuarios - Sin parámetro (default 0)")
    void testObtenerUsuariosSinParametro() throws Exception {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setUsuario("testuser");
        usuario.setCorreo("test@email.com");
        usuario.setRol(com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol.CLIENTE);
        
        List<Usuario> usuarios = Arrays.asList(usuario);
        when(usuarioService.obtenerUsuarios(0)).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false));

        verify(usuarioService, times(1)).obtenerUsuarios(0);
    }

    @Test
    @DisplayName("DELETE /v1/usuarios/{usuario} - Eliminar usuario exitosamente")
    void testEliminarUsuarioExitoso() throws Exception {
        // Arrange
        doNothing().when(usuarioService).eliminarUsuario("testuser");

        // Act & Assert
        mockMvc.perform(delete("/v1/usuarios/testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.respuesta").value("Usuario eliminado exitosamente del sistema"));

        verify(usuarioService, times(1)).eliminarUsuario("testuser");
    }

    @Test
    @DisplayName("DELETE /v1/usuarios/{usuario} - Usuario no encontrado")
    void testEliminarUsuarioNoEncontrado() throws Exception {
        // Arrange
        doThrow(new UsuarioNotFoundException("Usuario no encontrado"))
                .when(usuarioService).eliminarUsuario("noexiste");

        // Act & Assert
        mockMvc.perform(delete("/v1/usuarios/noexiste")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("Usuario no encontrado en el sistema"));

        verify(usuarioService, times(1)).eliminarUsuario("noexiste");
    }

    @Test
    @DisplayName("DELETE /v1/usuarios/{usuario} - Usuario vacío")
    void testEliminarUsuarioVacio() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/v1/usuarios/ ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.respuesta").value("El usuario es obligatorio"));

        verify(usuarioService, never()).eliminarUsuario(anyString());
    }
}

