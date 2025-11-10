package com.uniquindio.archmicroserv.jwtgeneratortaller1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.LoginRequest;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.DatosUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de integración - Autenticación y Autorización")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Registro de usuario - Flujo completo exitoso")
    void testRegistroUsuario_Success() throws Exception {
        // Given
        DatosUsuario datosUsuario = new DatosUsuario();
        datosUsuario.setUsuario("testuser" + System.currentTimeMillis());
        datosUsuario.setCorreo("test" + System.currentTimeMillis() + "@example.com");
        datosUsuario.setClave("SecurePass123!");
        datosUsuario.setNumeroTelefono("+573001234567");

        // When & Then
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosUsuario)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login de usuario - Flujo completo exitoso")
    void testLoginUsuario_Success() throws Exception {
        // Given - Primero registramos un usuario
        String username = "loginuser" + System.currentTimeMillis();
        String email = "login" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";

        DatosUsuario registroDTO = new DatosUsuario();
        registroDTO.setUsuario(username);
        registroDTO.setCorreo(email);
        registroDTO.setClave(password);
        registroDTO.setNumeroTelefono("+573001234567");

        mockMvc.perform(post("/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)));

        // When - Ahora intentamos hacer login
        LoginRequest loginDTO = new LoginRequest();
        loginDTO.setUsuario(username);
        loginDTO.setClave(password);

        // Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login con credenciales incorrectas")
    void testLogin_WrongCredentials() throws Exception {
        // Given
        LoginRequest loginDTO = new LoginRequest();
        loginDTO.setUsuario("nonexistentuser");
        loginDTO.setClave("wrongpassword");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registro con usuario duplicado")
    void testRegistro_DuplicateUser() throws Exception {
        // Given
        String username = "duplicateuser" + System.currentTimeMillis();
        DatosUsuario registroDTO = new DatosUsuario();
        registroDTO.setUsuario(username);
        registroDTO.setCorreo("duplicate" + System.currentTimeMillis() + "@example.com");
        registroDTO.setClave("Password123!");
        registroDTO.setNumeroTelefono("+573001234567");

        // Registramos el usuario por primera vez
        mockMvc.perform(post("/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)));

        // When & Then - Intentamos registrar el mismo usuario otra vez
        DatosUsuario registroDTO2 = new DatosUsuario();
        registroDTO2.setUsuario(username);
        registroDTO2.setCorreo("another" + System.currentTimeMillis() + "@example.com");
        registroDTO2.setClave("Password123!");
        registroDTO2.setNumeroTelefono("+573001234567");

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Validar formato de token JWT generado")
    void testTokenFormat() throws Exception {
        // Given
        DatosUsuario registroDTO = new DatosUsuario();
        registroDTO.setUsuario("tokenuser" + System.currentTimeMillis());
        registroDTO.setCorreo("token" + System.currentTimeMillis() + "@example.com");
        registroDTO.setClave("Password123!");
        registroDTO.setNumeroTelefono("+573001234567");

        // When
        String response = mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - Validamos que el token tiene el formato correcto
        String token = objectMapper.readTree(response).get("token").asText();
        assert token.startsWith("eyJ"); // JWT tokens start with "eyJ"
        assert token.split("\\.").length == 3; // JWT has 3 parts
    }
}

