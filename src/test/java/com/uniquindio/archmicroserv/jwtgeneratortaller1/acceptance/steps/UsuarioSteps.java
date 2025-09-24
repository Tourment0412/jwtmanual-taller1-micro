package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.steps;

import io.cucumber.java.es.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UsuarioSteps {

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    private Response lastResponse;
    private String ultimoUsuario = "user_" + System.currentTimeMillis();
    private String ultimoPassword = "Passw0rd*123";
    private String ultimoTelefono = "3001234567";
    private String adminToken;
    private String ultimoToken;

    private String baseUrl() {
        return "http://localhost:" + port + (contextPath == null ? "" : contextPath) + "/v1";
    }

    @Dado("que el servicio está disponible")
    public void servicioDisponible() {
        // No-op: el @SpringBootTest inicia la app
    }

    @Cuando("registro un usuario con datos válidos")
    public void registroUsuarioValido() {
        var body = """
        {
          "usuario":"%s",
          "correo":"%s@example.com",
          "numeroTelefono":"%s",
          "clave":"%s",
          "nombres":"Nombre",
          "apellidos":"Apellido"
        }
        """.formatted(ultimoUsuario, ultimoUsuario, ultimoTelefono, ultimoPassword);

        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(baseUrl() + "/usuarios");

        if (lastResponse.statusCode() == 201 || lastResponse.statusCode() == 200) {
            // ok
        }
    }

    @Entonces("la respuesta debe tener estado {int}")
    public void validarEstado(int status) {
        lastResponse.then().statusCode(status);
    }

    @Y("el cuerpo debe indicar éxito")
    public void cuerpoIndicaExito() {
        String raw = lastResponse.getBody() != null ? lastResponse.getBody().asString() : null;
        assertThat(raw, allOf(notNullValue(), not(blankOrNullString())));
    }

    @Dado("que existe un usuario registrado válido")
    public void existeUsuarioValido() {
        registroUsuarioValido();
        lastResponse.then().statusCode(anyOf(is(201), is(200)));
    }

    // Alias para escenarios que usan "Y existe un usuario registrado válido"
    @Y("existe un usuario registrado válido")
    public void existeUsuarioValidoAlias() {
        existeUsuarioValido();
    }

    @Cuando("inicio sesión con credenciales correctas")
    public void loginConCredencialesCorrectas() {
        var body = """
        {
          "usuario":"%s",
          "clave":"%s"
        }
        """.formatted(ultimoUsuario, ultimoPassword);

        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(baseUrl() + "/sesiones");

        if (lastResponse.statusCode() == 200) {
            // Prefer respuesta.token → TokenDTO(token)
            ultimoToken = lastResponse.jsonPath().getString("respuesta.token");
            if (ultimoToken == null) {
                ultimoToken = lastResponse.jsonPath().getString("token");
            }
        }
    }

    @Entonces("debo obtener un token JWT válido")
    public void deboObtenerTokenValido() {
        assertThat(ultimoToken, allOf(notNullValue(), not(blankOrNullString())));
    }

    @Cuando("solicito código de recuperación para ese usuario")
    public void solicitarCodigoRecuperacion() {
        var body = """
        {
          "usuario":"%s"
        }
        """.formatted(ultimoUsuario);

        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(baseUrl() + "/codigos");
    }

    @Dado("que inicio sesión como admin")
    public void inicioSesionComoAdmin() {
        // Ajusta usuario/clave admin según tus datos de prueba
        var body = """
        {
          "usuario":"admin",
          "clave":"admin123"
        }
        """;

        var resp = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(baseUrl() + "/sesiones");

        resp.then().statusCode(200);
        adminToken = resp.jsonPath().getString("respuesta.token");
        if (adminToken == null) {
            adminToken = resp.jsonPath().getString("token");
        }
        assertThat(adminToken, not(blankOrNullString()));
    }

    @Cuando("consulto la lista de usuarios en la página {int}")
    public void consultarListaUsuarios(int pagina) {
        lastResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .get(baseUrl() + "/usuarios?page=" + pagina);
    }

    @Y("el cuerpo debe contener una lista de usuarios")
    public void cuerpoContieneListaUsuarios() {
        // API usa campo "respuesta"
        lastResponse.then().body("respuesta", notNullValue());
    }

    @Cuando("elimino ese usuario")
    public void eliminarEseUsuario() {
        lastResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .delete(baseUrl() + "/usuarios/" + ultimoUsuario);
    }
}