package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JWTUtils {

     private SecretKey key;

    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:3600}")
    private int expirationHours;
    
    @Value("${jwt.issuer}")
    private String issuer;

    public JWTUtils() {
        // Constructor por defecto para compatibilidad
    }
    
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(String correo, Map <String, Object > claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(correo)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS))) // expira en horas configurables
                .signWith(key) // firma con clave secreta
                .compact();
    }

     /**
     * Elimina el prefijo Bearer si existe
     */
    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * Obtiene el payload (Claims) del token
     */
    private Claims getClaims(String token) throws JwtException {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException("Token no puede ser nulo o vacío");
        }
        String clean = cleanToken(token);
        JwtParser parser = Jwts.parser().verifyWith(key).build();
        return parser.parseSignedClaims(clean).getPayload();
    }

    /**
     * Extrae el subject (username) del token
     */
    public String getSubject(String token) throws JwtException {
        return getClaims(token).getSubject();
    }

    /**
     * Verifica si el subject coincide con el nombre recibido
     */
    public boolean validarNombre(String token, String nombre) {
        try {
            return Objects.equals(getSubject(token).trim(), nombre.trim());
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extrae el issuer del token
     */
    public String getIssuer(String token) throws JwtException {
        return getClaims(token).getIssuer();
    }

    /**
     * Valida si el issuer del token coincide con el esperado
     */
    public boolean validarIssuer(String token, String issuer) {
        try {
            return Objects.equals(getIssuer(token).trim(), issuer.trim());
        } catch (JwtException e) {
            return false;
        }
    }
    /**
     * Extrae el rol del token
     */
    public String getRol(String token) throws JwtException {
        return (String) getClaims(token).get("rol");
    }

    /**
     * Extrae el usuario del token
     */
    public String getUsuario(String token) throws JwtException {
        return (String) getClaims(token).get("usuario");
    }


    /**
     * Verifica si el rol del token coincide con el esperado
     */
    public boolean verificarRol(String token, Rol rol) {
        try {
            return Objects.equals(getRol(token).trim(), rol.getNombre().trim());
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Verifica si el token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // si no se puede parsear lo consideramos inválido
        }
    }
}