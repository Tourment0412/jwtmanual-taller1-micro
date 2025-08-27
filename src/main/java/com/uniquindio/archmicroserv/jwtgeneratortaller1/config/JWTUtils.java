package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

@Component
public class JWTUtils {

     private final SecretKey key;

    /*Esto toca que cambiarlo con un archivo de variables de entorno o algo ya que en el momemento esta
    Hardcodeada en el codigo*/
    private final String secret = "secretsecretsecretsecretsecretsecretsecretsecret";

    public JWTUtils() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(String usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario)
                .issuer("ingesis.uniquindio.edu.co") 
                .issuedAt(Date.from(now)) 
                .expiration(Date.from(now.plus(1, ChronoUnit.HOURS))) // expira en 1h
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
    private Claims getClaims(String token) {
        String clean = cleanToken(token);
        JwtParser parser = Jwts.parser().verifyWith(key).build();
        return parser.parseSignedClaims(clean).getPayload();
    }

    /**
     * Extrae el subject (username) del token
     */
    public String getSubject(String token) {
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
    public String getIssuer(String token) {
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