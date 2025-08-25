package com.uniquindio.archmicroserv.jwtgeneratortaller1.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JWTUtils {

    private final String secretKey = "secretsecretsecretsecretsecretsecretsecretsecret";

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generarToken(String usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario)
                .issuer("ingesis.uniquindio.edu.co") 
                .issuedAt(Date.from(now)) 
                .expiration(Date.from(now.plus(1, ChronoUnit.HOURS))) // expira en 1h
                .signWith(getKey()) // firma con clave secreta
                .compact();
    }
}