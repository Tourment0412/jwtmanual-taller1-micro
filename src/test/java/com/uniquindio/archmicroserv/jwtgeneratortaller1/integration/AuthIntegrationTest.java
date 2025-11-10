package com.uniquindio.archmicroserv.jwtgeneratortaller1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.config.JWTUtils;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.LoginDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.RegistroDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.repository.UsuarioRepository;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Test
    @DisplayName("Registro de usuario - Flujo completo exitoso")
    void testRegistroUsuario_Success() throws Exception {
        // Given
        RegistroDTO registroDTO = RegistroDTO.builder()
                .usuario("testuser" + System.currentTimeMillis())
                .correo("test" + System.currentTimeMillis() + "@example.com")
                .password("SecurePass123!")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usuario").value(registroDTO.getUsuario()))
                .andExpect(jsonPath("$.correo").value(registroDTO.getCorreo()))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login de usuario - Flujo completo exitoso")
    void testLoginUsuario_Success() throws Exception {
        // Given - Primero registramos un usuario
        String username = "loginuser" + System.currentTimeMillis();
        String email = "login" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";

        RegistroDTO registroDTO = RegistroDTO.builder()
                .usuario(username)
                .correo(email)
                .password(password)
                .build();

        mockMvc.perform(post("/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)));

        // When - Ahora intentamos hacer login
        LoginDTO loginDTO = LoginDTO.builder()
                .usuario(username)
                .password(password)
                .build();

        // Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario").value(username));
    }

    @Test
    @DisplayName("Login con credenciales incorrectas")
    void testLogin_WrongCredentials() throws Exception {
        // Given
        LoginDTO loginDTO = LoginDTO.builder()
                .usuario("nonexistentuser")
                .password("wrongpassword")
                .build();

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
        RegistroDTO registroDTO = RegistroDTO.builder()
                .usuario(username)
                .correo("duplicate" + System.currentTimeMillis() + "@example.com")
                .password("Password123!")
                .build();

        // Registramos el usuario por primera vez
        mockMvc.perform(post("/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)));

        // When & Then - Intentamos registrar el mismo usuario otra vez
        RegistroDTO registroDTO2 = RegistroDTO.builder()
                .usuario(username)
                .correo("another" + System.currentTimeMillis() + "@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Validar formato de token JWT generado")
    void testTokenFormat() throws Exception {
        // Given
        RegistroDTO registroDTO = RegistroDTO.builder()
                .usuario("tokenuser" + System.currentTimeMillis())
                .correo("token" + System.currentTimeMillis() + "@example.com")
                .password("Password123!")
                .build();

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

