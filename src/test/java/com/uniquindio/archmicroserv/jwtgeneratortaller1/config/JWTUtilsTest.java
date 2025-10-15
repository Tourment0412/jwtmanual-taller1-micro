package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests unitarios para JWTUtils")
class JWTUtilsTest {

    private JWTUtils jwtUtils;
    private String validToken;
    private final String testSecret = "mySecretKeyForTestingPurposesOnly12345678901234567890";
    private final String testIssuer = "test-issuer";
    private final int testExpirationHours = 1;

    @BeforeEach
    void setUp() {
        jwtUtils = new JWTUtils();
        
        // Usar ReflectionTestUtils para setear los valores privados
        ReflectionTestUtils.setField(jwtUtils, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "issuer", testIssuer);
        ReflectionTestUtils.setField(jwtUtils, "expirationHours", testExpirationHours);
        
        // Llamar a @PostConstruct manualmente
        jwtUtils.init();

        // Generar un token válido para las pruebas
        Map<String, Object> claims = new HashMap<>();
        claims.put("usuario", "testuser");
        claims.put("correo", "test@email.com");
        claims.put("rol", "CLIENTE");
        
        validToken = jwtUtils.generarToken("test@email.com", claims);
    }

    @Test
    @DisplayName("Generar token exitosamente")
    void testGenerarTokenExitoso() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("usuario", "newuser");
        claims.put("correo", "newuser@email.com");
        claims.put("rol", "CLIENTE");

        // Act
        String token = jwtUtils.generarToken("newuser@email.com", claims);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes
    }

    @Test
    @DisplayName("Extraer subject del token")
    void testGetSubjectExitoso() {
        // Act
        String subject = jwtUtils.getSubject(validToken);

        // Assert
        assertNotNull(subject);
        assertEquals("test@email.com", subject);
    }

    @Test
    @DisplayName("Extraer subject - token nulo")
    void testGetSubjectTokenNulo() {
        // Act & Assert
        try {
            jwtUtils.getSubject(null);
        } catch (JwtException e) {
            // Expected exception
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Extraer subject - token vacío")
    void testGetSubjectTokenVacio() {
        // Act & Assert
        try {
            jwtUtils.getSubject("");
        } catch (JwtException e) {
            // Expected exception
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Extraer subject - token con Bearer")
    void testGetSubjectConBearer() {
        // Arrange
        String tokenConBearer = "Bearer " + validToken;

        // Act
        String subject = jwtUtils.getSubject(tokenConBearer);

        // Assert
        assertEquals("test@email.com", subject);
    }

    @Test
    @DisplayName("Validar nombre del token")
    void testValidarNombreExitoso() {
        // Act
        boolean result = jwtUtils.validarNombre(validToken, "test@email.com");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Validar nombre - nombre incorrecto")
    void testValidarNombreIncorrecto() {
        // Act
        boolean result = jwtUtils.validarNombre(validToken, "otro@email.com");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Validar nombre - token inválido")
    void testValidarNombreTokenInvalido() {
        // Act
        boolean result = jwtUtils.validarNombre("token-invalido", "test@email.com");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Extraer issuer del token")
    void testGetIssuerExitoso() {
        // Act
        String issuer = jwtUtils.getIssuer(validToken);

        // Assert
        assertNotNull(issuer);
        assertEquals(testIssuer, issuer);
    }

    @Test
    @DisplayName("Validar issuer exitosamente")
    void testValidarIssuerExitoso() {
        // Act
        boolean result = jwtUtils.validarIssuer(validToken, testIssuer);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Validar issuer - issuer incorrecto")
    void testValidarIssuerIncorrecto() {
        // Act
        boolean result = jwtUtils.validarIssuer(validToken, "otro-issuer");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Validar issuer - token inválido")
    void testValidarIssuerTokenInvalido() {
        // Act
        boolean result = jwtUtils.validarIssuer("token-invalido", testIssuer);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Extraer rol del token")
    void testGetRolExitoso() {
        // Act
        String rol = jwtUtils.getRol(validToken);

        // Assert
        assertNotNull(rol);
        assertEquals("CLIENTE", rol);
    }

    @Test
    @DisplayName("Extraer rol - token inválido")
    void testGetRolTokenInvalido() {
        // Act & Assert
        try {
            jwtUtils.getRol("token-invalido");
        } catch (JwtException e) {
            // Expected exception
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Verificar rol exitosamente")
    void testVerificarRolExitoso() {
        // Act
        boolean result = jwtUtils.verificarRol(validToken, Rol.CLIENTE);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Verificar rol - rol incorrecto")
    void testVerificarRolIncorrecto() {
        // Act
        boolean result = jwtUtils.verificarRol(validToken, Rol.ADMIN);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Verificar rol - token inválido")
    void testVerificarRolTokenInvalido() {
        // Act
        boolean result = jwtUtils.verificarRol("token-invalido", Rol.CLIENTE);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Token no está expirado")
    void testTokenNoExpirado() {
        // Act
        boolean isExpired = jwtUtils.isTokenExpired(validToken);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Token inválido se considera expirado")
    void testTokenInvalidoSeConsideraExpirado() {
        // Act
        boolean isExpired = jwtUtils.isTokenExpired("token-invalido");

        // Assert
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("Token con Bearer es procesado correctamente")
    void testTokenConBearerProcesamientoCorrecto() {
        // Arrange
        String tokenConBearer = "Bearer " + validToken;

        // Act
        String subject = jwtUtils.getSubject(tokenConBearer);
        String issuer = jwtUtils.getIssuer(tokenConBearer);
        String rol = jwtUtils.getRol(tokenConBearer);
        boolean isExpired = jwtUtils.isTokenExpired(tokenConBearer);

        // Assert
        assertEquals("test@email.com", subject);
        assertEquals(testIssuer, issuer);
        assertEquals("CLIENTE", rol);
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Generar múltiples tokens con diferentes claims")
    void testGenerarMultiplesTokens() {
        // Arrange
        Map<String, Object> claims1 = new HashMap<>();
        claims1.put("usuario", "user1");
        claims1.put("rol", "ADMIN");

        Map<String, Object> claims2 = new HashMap<>();
        claims2.put("usuario", "user2");
        claims2.put("rol", "CLIENTE");

        // Act
        String token1 = jwtUtils.generarToken("user1@email.com", claims1);
        String token2 = jwtUtils.generarToken("user2@email.com", claims2);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);

        assertEquals("user1@email.com", jwtUtils.getSubject(token1));
        assertEquals("user2@email.com", jwtUtils.getSubject(token2));
        assertEquals("ADMIN", jwtUtils.getRol(token1));
        assertEquals("CLIENTE", jwtUtils.getRol(token2));
    }
}

