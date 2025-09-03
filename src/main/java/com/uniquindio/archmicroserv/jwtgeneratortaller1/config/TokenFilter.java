package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Rol;

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
    private final String ISSUER = Constants.ISSUER;
    private static final ObjectMapper mapper = new ObjectMapper();

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
        // Permitir acceso libre a rutas de documentación y swagger
        if (requestURI.equals("/v3/api-docs.yaml") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getToken(request);
        /*
         * if (token == null) {
         * crearRespuestaError("El token es obligatorio",
         * HttpServletResponse.SC_UNAUTHORIZED, response);
         * return;
         * }
         */
        boolean error = false;
        // Solo protegemos /admin y /usuario
        try {
            if (requestURI.startsWith("/admin")) {
                error = verificarValidezTokenAdmin(response, token, Rol.ADMIN);
            } else if (requestURI.startsWith("/usuario")) {
                error = verificarValidezTokenCliente(response, token);
            } else {
                error = false;
            }
        } catch (JwtException e) {
            crearRespuestaError("El token es inválido o ha expirado",
                    HttpServletResponse.SC_UNAUTHORIZED, response);
            return;
        }
        if (!error) {
            filterChain.doFilter(request, response);
        }
        // Si no hubo errores -> continuar

    }

    private boolean verificarValidezTokenAdmin(HttpServletResponse response, String token, Rol rol)
            throws IOException {

        boolean validoIssuer = jwtUtils.validarIssuer(token, ISSUER);
        System.out.println(validoIssuer);
        if (!validoIssuer) {
            crearRespuestaError("El emisor del token no es válido",
                    HttpServletResponse.SC_FORBIDDEN, response);
            return true;
        }
        boolean validoRol = jwtUtils.verificarRol(token, rol);
        if (!validoRol) {
            crearRespuestaError("El rol del token no es válido",
                    HttpServletResponse.SC_FORBIDDEN, response);
            return true;
        }
        return false;
    }

    private boolean verificarValidezTokenCliente(HttpServletResponse response, String token)
            throws IOException {

        boolean validoIssuer = jwtUtils.validarIssuer(token, ISSUER);
        System.out.println(validoIssuer);
        if (!validoIssuer) {
            crearRespuestaError("El emisor del token no es válido",
                    HttpServletResponse.SC_FORBIDDEN, response);
            return true;
        }
        boolean validoRol = jwtUtils.verificarRol(token, Rol.CLIENTE);
        if (!validoRol) {
            crearRespuestaError("El rol del token no es válido",
                    HttpServletResponse.SC_FORBIDDEN, response);
            return true;
        }
        return false;
    }

    private String getToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer "))
                ? header.replace("Bearer ", "")
                : null;
    }

    private void crearRespuestaError(String mensaje, int codigoError, HttpServletResponse response)
            throws IOException {
        var dto = new MessageDTO<String>(true, mensaje);
        response.setContentType("application/json");
        response.setStatus(codigoError);
        response.getWriter().write(mapper.writeValueAsString(dto));
        response.getWriter().flush();
    }
}