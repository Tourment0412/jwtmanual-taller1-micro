package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final String ISSUER = "ingesis.uniquindio.edu.co";  

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Configuración CORS
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Origin, Accept, Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String requestURI = request.getRequestURI();

        // Solo protegemos /saludo
        if (requestURI.startsWith("/saludo")) {
            String token = getToken(request);
            if (token == null) {
                crearRespuestaError("El token es obligatorio",
                        HttpServletResponse.SC_UNAUTHORIZED, response);
                return;
            }

            try {
                // Extraer nombre del parámetro
                String nombreParam = request.getParameter("nombre");
                if (nombreParam == null || nombreParam.isBlank()) {
                    crearRespuestaError("Solicitud no válida: El nombre es obligatorio",
                            HttpServletResponse.SC_BAD_REQUEST, response);
                    return;
                }

                boolean validoIssuer = jwtUtils.validarIssuer(token, ISSUER);
                System.out.println(validoIssuer);
                if (!validoIssuer) {
                    crearRespuestaError("El emisor del token no es válido",
                            HttpServletResponse.SC_FORBIDDEN, response);
                    return;
                }
                // Validar nombre con el token
                boolean valido = jwtUtils.validarNombre(token, nombreParam);
                if (!valido) {
                    crearRespuestaError("El nombre no coincide con el token",
                            HttpServletResponse.SC_FORBIDDEN, response);
                    return;
                }

            } catch (JwtException e) {
                crearRespuestaError("El token es inválido o ha expirado",
                        HttpServletResponse.SC_UNAUTHORIZED, response);
                return;
            }
        }

        // Si no hubo errores -> continuar
        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer "))
                ? header.replace("Bearer ", "")
                : null;
    }

    private void crearRespuestaError(String mensaje, int codigoError, HttpServletResponse response)
            throws IOException {
        var dto = new MessageDTO(true, mensaje);
        response.setContentType("application/json");
        response.setStatus(codigoError);
        response.getWriter().write(new ObjectMapper().writeValueAsString(dto));
        response.getWriter().flush();
        response.getWriter().close();
    }
}