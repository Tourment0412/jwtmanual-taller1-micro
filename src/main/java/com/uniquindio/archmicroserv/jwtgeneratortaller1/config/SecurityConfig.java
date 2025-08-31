package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF si es API REST
            .authorizeHttpRequests(auth -> auth
                // Permitir sin autenticación estos endpoints
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Cualquier otra ruta se maneja por tu TokenFilter
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable()) // Deshabilita login por formulario
            .httpBasic(basic -> basic.disable()); // Deshabilita auth básica por defecto

        return http.build();
    }
}
