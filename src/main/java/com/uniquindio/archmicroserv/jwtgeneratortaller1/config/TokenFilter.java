package com.uniquindio.archmicroserv.jwtgeneratortaller1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.dto.MessageDTO;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol;

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
        // Normalizar dobles barras y quitar trailing slash (salvo "/")
        if (requestURI != null) {
            requestURI = requestURI.replaceAll("/+", "/");
            if (requestURI.length() > 1 && requestURI.endsWith("/")) {
                requestURI = requestURI.substring(0, requestURI.length() - 1);
            }
        }
        String method = request.getMethod();
        
        // Permitir acceso libre a rutas de documentación y swagger
        if (requestURI.equals("/v3/api-docs.yaml") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Rutas públicas que no requieren autenticación
        if (esRutaPublica(requestURI, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getToken(request);
        boolean error = false;
        
        try {
            // Rutas que requieren autenticación de administrador
            if (esRutaAdmin(requestURI, method)) {
                if (token == null || token.trim().isEmpty()) {
                    crearRespuestaError("Token de autenticación requerido",
                            HttpServletResponse.SC_UNAUTHORIZED, response);
                    return;
                }
                error = verificarValidezTokenAdmin(response, token, Rol.ADMIN);
            } 
            // Rutas que requieren autenticación de cliente
            else if (esRutaUsuario(requestURI, method)) {
                if (token == null || token.trim().isEmpty()) {
                    crearRespuestaError("Token de autenticación requerido",
                            HttpServletResponse.SC_UNAUTHORIZED, response);
                    return;
                }
                // Extraer usuario del path para validar que solo acceda a sus propios datos
                String usuarioPath = extraerUsuarioDelPath(requestURI);
                error = verificarValidezTokenCliente(response, token, usuarioPath);
            } 
            // Otras rutas (públicas)
            else {
                error = false;
            }
        } catch (JwtException e) {
            crearRespuestaError("El token es inválido o ha expirado",
                    HttpServletResponse.SC_UNAUTHORIZED, response);
            return;
        } catch (Exception e) {
            crearRespuestaError("Error interno del servidor durante la autenticación",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
            return;
        }
        
        if (!error) {
            filterChain.doFilter(request, response);
        }

    }

    private boolean verificarValidezTokenAdmin(HttpServletResponse response, String token, Rol rol)
            throws IOException {

        try {
            // Verificar si el token está expirado
            if (jwtUtils.isTokenExpired(token)) {
                crearRespuestaError("El token ha expirado",
                        HttpServletResponse.SC_UNAUTHORIZED, response);
                return true;
            }

            boolean validoIssuer = jwtUtils.validarIssuer(token, ISSUER);
            if (!validoIssuer) {
                crearRespuestaError("El emisor del token no es válido",
                        HttpServletResponse.SC_FORBIDDEN, response);
                return true;
            }
            
            boolean validoRol = jwtUtils.verificarRol(token, rol);
            if (!validoRol) {
                crearRespuestaError("El rol del token no es válido para esta operación",
                        HttpServletResponse.SC_FORBIDDEN, response);
                return true;
            }
            return false;
        } catch (JwtException e) {
            crearRespuestaError("El token es inválido o malformado",
                    HttpServletResponse.SC_UNAUTHORIZED, response);
            return true;
        } catch (Exception e) {
            crearRespuestaError("Error interno del servidor durante la validación del token",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
            return true;
        }
    }

    private boolean verificarValidezTokenCliente(HttpServletResponse response, String token, String usuarioPath)
            throws IOException {

        try {
            // Verificar si el token está expirado
            if (jwtUtils.isTokenExpired(token)) {
                crearRespuestaError("El token ha expirado",
                        HttpServletResponse.SC_UNAUTHORIZED, response);
                return true;
            }

            boolean validoIssuer = jwtUtils.validarIssuer(token, ISSUER);
            if (!validoIssuer) {
                crearRespuestaError("El emisor del token no es válido",
                        HttpServletResponse.SC_FORBIDDEN, response);
                return true;
            }
            
            String rolToken = jwtUtils.getRol(token);
            boolean validoRol = jwtUtils.verificarRol(token, Rol.CLIENTE);
            
            // Si es ADMIN, permitir acceso a cualquier usuario
            if (Rol.ADMIN.getNombre().equals(rolToken)) {
                return false;
            }
            
            // Si es CLIENTE, verificar que solo acceda a sus propios datos
            if (!validoRol) {
                crearRespuestaError("El rol del token no es válido para esta operación",
                        HttpServletResponse.SC_FORBIDDEN, response);
                return true;
            }
            
            // Validar que el usuario del token coincida con el usuario del path
            if (usuarioPath != null && !usuarioPath.isEmpty()) {
                String usuarioToken = jwtUtils.getUsuario(token);
                if (usuarioToken == null || !usuarioToken.trim().equals(usuarioPath.trim())) {
                    crearRespuestaError("No tiene permisos para acceder a los datos de otro usuario",
                            HttpServletResponse.SC_FORBIDDEN, response);
                    return true;
                }
            }
            
            return false;
        } catch (JwtException e) {
            crearRespuestaError("El token es inválido o malformado",
                    HttpServletResponse.SC_UNAUTHORIZED, response);
            return true;
        } catch (Exception e) {
            crearRespuestaError("Error interno del servidor durante la validación del token",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
            return true;
        }
    }
    
    /**
     * Extrae el nombre de usuario del path de la URI
     * Ejemplo: /v1/usuarios/testuser -> testuser
     */
    private String extraerUsuarioDelPath(String requestURI) {
        if (requestURI == null || !requestURI.startsWith("/v1/usuarios/")) {
            return null;
        }
        String[] partes = requestURI.split("/");
        if (partes.length >= 4) {
            return partes[3]; // /v1/usuarios/{usuario}
        }
        return null;
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

    /**
     * Identifica si una ruta es pública (no requiere autenticación)
     */
    private boolean esRutaPublica(String requestURI, String method) {
        return ("POST".equals(method) && 
                   (requestURI.equals("/v1/usuarios")      // registro
                    || requestURI.equals("/v1/sesiones")    // login
                    || requestURI.equals("/v1/codigos")))   // recuperar clave (ruta pública real)
               ||
               ("PATCH".equals(method) && requestURI.matches("^/v1/usuarios/[^/]+/contrasena$") ); // cambiar clave
    }

    /**
     * Identifica si una ruta requiere autenticación de administrador
     */
    private boolean esRutaAdmin(String requestURI, String method) {
        return (requestURI.equals("/v1/usuarios") && "GET".equals(method)) || // GET /v1/usuarios (obtener usuarios)
               (requestURI.startsWith("/v1/usuarios/") && "DELETE".equals(method)); // DELETE /v1/usuarios/{usuario} (eliminar usuario)
    }

    /**
     * Identifica si una ruta requiere autenticación de cliente/usuario
     */
    private boolean esRutaUsuario(String requestURI, String method) {
        return (requestURI.startsWith("/v1/usuarios/") && "PATCH".equals(method) && 
               !requestURI.endsWith("/contrasena")) || // PATCH /v1/usuarios/{usuario} (actualizar usuario)
               (requestURI.startsWith("/v1/usuarios/") && "GET".equals(method)); // GET /v1/usuarios/{usuario} (obtener usuario)
    }
}