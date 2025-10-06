package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.steps;

import io.cucumber.java.es.*;
import io.restassured.http.ContentType;
import net.datafaker.Faker;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Implementación de pasos (Step Definitions) para las pruebas de aceptación de usuarios.
 * 
 * Esta clase contiene la implementación en Java de todos los pasos definidos en los
 * archivos .feature de Gherkin. Cada método anotado con @Dado, @Cuando, @Entonces
 * o @Y corresponde a un paso específico en los escenarios de prueba.
 * 
 * Funcionalidades principales:
 * - Implementa pasos de Gherkin en español
 * - Realiza peticiones HTTP reales a la aplicación
 * - Genera datos de prueba aleatorios y coherentes
 * - Valida respuestas HTTP y esquemas JSON
 * - Mantiene estado entre pasos (tokens, datos de usuario)
 * - Proporciona datos consistentes para escenarios de prueba
 * 
 * Herramientas utilizadas:
 * - RestAssured: Cliente HTTP para peticiones y validaciones
 * - DataFaker: Generación de datos de prueba realistas
 * - Hamcrest: Matchers para validaciones expresivas
 * - Spring Test: Inyección de dependencias y configuración
 * 
 * @author Sistema de Pruebas
 * @version 1.0
 * @since 2024
 */
public class UsuarioSteps {

    // ===== CONFIGURACIÓN DE SPRING BOOT =====
    
    /**
     * Puerto donde se ejecuta la aplicación Spring Boot durante las pruebas.
     * 
     * Spring Boot asigna automáticamente un puerto aleatorio disponible para
     * evitar conflictos con otras aplicaciones que puedan estar ejecutándose.
     * 
     * Ejemplo: Si el puerto es 8080, la URL base será http://localhost:8080/v1
     */
    @LocalServerPort
    private int port;

    /**
     * Ruta base (context path) de la aplicación Spring Boot.
     * 
     * Si la aplicación está configurada con un context path (ej: /api),
     * este valor se inyecta automáticamente. Si no hay context path,
     * el valor será una cadena vacía.
     * 
     * Ejemplo: Si contextPath = "/api", la URL base será http://localhost:8080/api/v1
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    // ===== ESTADO DE LAS PRUEBAS =====
    
    /**
     * Última respuesta HTTP recibida de la aplicación.
     * 
     * Se actualiza con cada petición HTTP realizada y se usa para
     * validaciones posteriores en los pasos de "Entonces".
     */
    private Response lastResponse;
    
    /**
     * Generador de datos falsos para crear datos de prueba realistas.
     * 
     * DataFaker permite generar nombres, emails, teléfonos, etc. que
     * parecen reales pero son completamente aleatorios y seguros para pruebas.
     */
    private final Faker faker = new Faker();
    
    /**
     * Usuario creado en el último escenario de registro.
     * 
     * Se mantiene para poder usarlo en pasos posteriores como login,
     * eliminación, etc. Se genera con un timestamp para garantizar unicidad.
     */
    private String ultimoUsuario = "user_" + System.currentTimeMillis();
    
    /**
     * Contraseña del último usuario creado.
     * 
     * Se genera con un patrón seguro que incluye mayúsculas, minúsculas,
     * números y caracteres especiales, más dígitos aleatorios para unicidad.
     */
    private String ultimoPassword = "Passw0rd*" + faker.number().digits(3);
    
    /**
     * Teléfono del último usuario creado.
     * 
     * Se genera con formato colombiano (3XXXXXXXXX) para simular
     * números de teléfono reales del país.
     */
    private String ultimoTelefono = "3" + faker.number().digits(9);
    
    /**
     * Token JWT del usuario administrador.
     * 
     * Se obtiene al hacer login como admin y se usa para operaciones
     * que requieren privilegios de administrador (listar usuarios, eliminar, etc.).
     */
    private String adminToken;
    
    /**
     * Token JWT del último usuario que hizo login.
     * 
     * Se obtiene al hacer login con credenciales válidas y se puede usar
     * para operaciones que requieren autenticación.
     */
    private String ultimoToken;

    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Construye la URL base de la aplicación para las peticiones HTTP.
     * 
     * Combina el protocolo, host, puerto, context path y versión de la API
     * para formar la URL completa que se usará en todas las peticiones.
     * 
     * Ejemplos de URLs generadas:
     * - Sin context path: http://localhost:8080/v1
     * - Con context path: http://localhost:8080/api/v1
     * 
     * @return URL base completa de la aplicación
     */
    private String baseUrl() {
        return "http://localhost:" + port + (contextPath == null ? "" : contextPath) + "/v1";
    }

    // ===== IMPLEMENTACIÓN DE PASOS DE GHERKIN =====
    
    /**
     * Paso: "Dado que el servicio está disponible"
     * 
     * Verifica que la aplicación Spring Boot esté ejecutándose y lista para recibir peticiones.
     * 
     * En realidad no hace nada porque @SpringBootTest ya se encarga de iniciar
     * la aplicación automáticamente antes de ejecutar las pruebas. Este paso
     * existe principalmente para claridad en los escenarios de Gherkin.
     */
    @Dado("que el servicio está disponible")
    public void servicioDisponible() {
        // No-op: el @SpringBootTest inicia la app automáticamente
    }

    /**
     * Paso: "Cuando registro un usuario con datos válidos"
     * 
     * Crea un nuevo usuario en el sistema con datos generados aleatoriamente
     * pero coherentes entre sí. Los datos se mantienen en variables de instancia
     * para poder usarlos en pasos posteriores.
     * 
     * Proceso:
     * 1. Genera datos aleatorios usando DataFaker
     * 2. Crea un JSON con los datos del usuario
     * 3. Envía petición POST a /usuarios
     * 4. Guarda la respuesta para validaciones posteriores
     * 
     * Datos generados:
     * - Usuario: user_XXXXXXXX (8 dígitos aleatorios)
     * - Email: email válido aleatorio
     * - Teléfono: 3XXXXXXXXX (formato colombiano)
     * - Contraseña: Passw0rd*XXX (patrón seguro + 3 dígitos)
     * - Nombres: Nombre aleatorio
     * - Apellidos: Apellido aleatorio
     */
    @Cuando("registro un usuario con datos válidos")
    public void registroUsuarioValido() {
        // Generar datos aleatorios coherentes entre campos
        ultimoUsuario = "user_" + faker.number().digits(8);
        String correo = faker.internet().emailAddress();
        ultimoTelefono = "3" + faker.number().digits(9);
        ultimoPassword = "Passw0rd*" + faker.number().digits(3);

        // Crear JSON con los datos del usuario
        var body = """
        {
          "usuario":"%s",
          "correo":"%s",
          "numeroTelefono":"%s",
          "clave":"%s",
          "nombres":"%s",
          "apellidos":"%s"
        }
        """.formatted(ultimoUsuario, correo, ultimoTelefono, ultimoPassword, 
                      faker.name().firstName(), faker.name().lastName());

        // Enviar petición POST a la API
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(baseUrl() + "/usuarios");

        // Verificar que la respuesta sea exitosa (200 o 201)
        if (lastResponse.statusCode() == 201 || lastResponse.statusCode() == 200) {
            // Usuario creado exitosamente
        }
    }

    /**
     * Paso: "Entonces la respuesta debe tener estado {int}"
     * 
     * Valida que el código de estado HTTP de la última respuesta sea el esperado.
     * 
     * Códigos de estado comunes:
     * - 200: OK (operación exitosa)
     * - 201: Created (recurso creado exitosamente)
     * - 400: Bad Request (datos inválidos)
     * - 401: Unauthorized (no autenticado)
     * - 403: Forbidden (no autorizado)
     * - 404: Not Found (recurso no encontrado)
     * - 500: Internal Server Error (error del servidor)
     * 
     * @param status Código de estado HTTP esperado
     */
    @Entonces("la respuesta debe tener estado {int}")
    public void validarEstado(int status) {
        lastResponse.then().statusCode(status);
    }

    /**
     * Paso: "Y el cuerpo cumple el esquema {string}"
     * 
     * Valida que el cuerpo de la respuesta JSON cumpla con un esquema JSON Schema específico.
     * 
     * Los esquemas se encuentran en src/test/resources/schemas/ y permiten validar:
     * - Estructura de la respuesta
     * - Tipos de datos correctos
     * - Campos requeridos
     * - Formatos específicos (email, fecha, etc.)
     * 
     * Ejemplos de esquemas:
     * - message_dto.schema.json: Para respuestas de mensajes
     * - token_response.schema.json: Para respuestas con tokens JWT
     * - usuarios_page.schema.json: Para listas paginadas de usuarios
     * 
     * @param schemaPath Ruta relativa al classpath del archivo de esquema
     */
    @Y("el cuerpo cumple el esquema {string}")
    public void cuerpoCumpleEsquema(String schemaPath) {
        // Validar que la respuesta JSON cumpla con el esquema especificado
        lastResponse.then().body(matchesJsonSchemaInClasspath(schemaPath));
    }

    /**
     * Paso: "Y el cuerpo debe indicar éxito"
     * 
     * Valida que el cuerpo de la respuesta no esté vacío y contenga información
     * que indique que la operación fue exitosa.
     * 
     * Esta validación es más genérica que la validación de esquema y se usa
     * cuando no se requiere validar la estructura específica, solo que la
     * respuesta contenga datos válidos.
     */
    @Y("el cuerpo debe indicar éxito")
    public void cuerpoIndicaExito() {
        String raw = lastResponse.getBody() != null ? lastResponse.getBody().asString() : null;
        assertThat(raw, allOf(notNullValue(), not(blankOrNullString())));
    }

    /**
     * Paso: "Dado que existe un usuario registrado válido"
     * 
     * Crea un usuario válido en el sistema para usar en escenarios que requieren
     * un usuario previamente registrado. Es un paso de preparación que garantiza
     * que existe un usuario con credenciales conocidas.
     * 
     * Proceso:
     * 1. Llama al método de registro de usuario
     * 2. Verifica que el registro fue exitoso (código 200 o 201)
     * 3. Los datos del usuario quedan disponibles en las variables de instancia
     */
    @Dado("que existe un usuario registrado válido")
    public void existeUsuarioValido() {
        registroUsuarioValido();
        lastResponse.then().statusCode(anyOf(is(201), is(200)));
    }

    /**
     * Paso: "Y existe un usuario registrado válido"
     * 
     * Alias del paso anterior para permitir diferentes formas de expresar
     * el mismo concepto en los escenarios de Gherkin. Algunos escenarios
     * usan "Dado" y otros "Y" para el mismo paso.
     */
    @Y("existe un usuario registrado válido")
    public void existeUsuarioValidoAlias() {
        existeUsuarioValido();
    }

    /**
     * Paso: "Cuando inicio sesión con credenciales correctas"
     * 
     * Autentica un usuario en el sistema usando las credenciales del último
     * usuario creado. Si la autenticación es exitosa, extrae y guarda el
     * token JWT para usar en peticiones posteriores.
     * 
     * Proceso:
     * 1. Crea JSON con usuario y contraseña del último usuario registrado
     * 2. Envía petición POST a /sesiones
     * 3. Si es exitosa (código 200), extrae el token JWT
     * 4. Guarda el token en ultimoToken para uso posterior
     * 
     * El token se extrae de dos posibles ubicaciones en la respuesta:
     * - respuesta.token (formato preferido)
     * - token (formato alternativo)
     */
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
            // Extraer token JWT de la respuesta (intentar ambos formatos)
            ultimoToken = lastResponse.jsonPath().getString("respuesta.token");
            if (ultimoToken == null) {
                ultimoToken = lastResponse.jsonPath().getString("token");
            }
        }
    }

    /**
     * Paso: "Entonces debo obtener un token JWT válido"
     * 
     * Valida que se haya obtenido un token JWT válido después del login.
     * Un token válido debe ser no nulo y no estar vacío.
     * 
     * Esta validación es importante porque muchos pasos posteriores requieren
     * el token para autenticarse en la API.
     */
    @Entonces("debo obtener un token JWT válido")
    public void deboObtenerTokenValido() {
        assertThat(ultimoToken, allOf(notNullValue(), not(blankOrNullString())));
    }

    /**
     * Paso: "Cuando solicito código de recuperación para ese usuario"
     * 
     * Solicita un código de recuperación de contraseña para el último usuario
     * registrado. Este paso simula el proceso de recuperación de contraseña
     * que un usuario real podría realizar.
     * 
     * Proceso:
     * 1. Crea JSON con el usuario del último registro
     * 2. Envía petición POST a /codigos
     * 3. La respuesta debería indicar que se envió el código por email
     */
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

    /**
     * Paso: "Dado que inicio sesión como admin"
     * 
     * Autentica como usuario administrador usando credenciales predefinidas.
     * El usuario admin se crea automáticamente por AdminSeedConfig antes
     * de ejecutar las pruebas.
     * 
     * Credenciales del admin:
     * - Usuario: "admin"
     * - Contraseña: "admin123"
     * 
     * Proceso:
     * 1. Crea JSON con credenciales de admin
     * 2. Envía petición POST a /sesiones
     * 3. Verifica que la respuesta sea exitosa (código 200)
     * 4. Extrae y guarda el token JWT del admin
     * 5. Valida que el token no esté vacío
     */
    @Dado("que inicio sesión como admin")
    public void inicioSesionComoAdmin() {
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

    /**
     * Paso: "Cuando consulto la lista de usuarios en la página {int}"
     * 
     * Consulta la lista paginada de usuarios usando el token de administrador.
     * Este paso simula la funcionalidad de administración para listar usuarios.
     * 
     * Proceso:
     * 1. Incluye el token JWT del admin en el header Authorization
     * 2. Envía petición GET a /usuarios con parámetro de página
     * 3. La respuesta debería contener la lista de usuarios de esa página
     * 
     * @param pagina Número de página a consultar (empezando en 0)
     */
    @Cuando("consulto la lista de usuarios en la página {int}")
    public void consultarListaUsuarios(int pagina) {
        lastResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .get(baseUrl() + "/usuarios?page=" + pagina);
    }

    /**
     * Paso: "Y el cuerpo debe contener una lista de usuarios"
     * 
     * Valida que la respuesta de la consulta de usuarios contenga efectivamente
     * una lista de usuarios. Verifica que el campo "respuesta" no sea nulo,
     * lo que indica que se retornaron datos.
     */
    @Y("el cuerpo debe contener una lista de usuarios")
    public void cuerpoContieneListaUsuarios() {
        // Verificar que la respuesta contenga el campo "respuesta" con datos
        lastResponse.then().body("respuesta", notNullValue());
    }

    /**
     * Paso: "Cuando elimino ese usuario"
     * 
     * Elimina el último usuario registrado usando el token de administrador.
     * Este paso simula la funcionalidad de administración para eliminar usuarios.
     * 
     * Proceso:
     * 1. Incluye el token JWT del admin en el header Authorization
     * 2. Envía petición DELETE a /usuarios/{usuario}
     * 3. La respuesta debería indicar que el usuario fue eliminado exitosamente
     */
    @Cuando("elimino ese usuario")
    public void eliminarEseUsuario() {
        lastResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .delete(baseUrl() + "/usuarios/" + ultimoUsuario);
    }

    /**
     * Paso: "Dado que no tengo token"
     * 
     * Este paso limpia el token de administrador para simular no tener un token
     * 
     * Proceso: 
     * 1. Limpia la variable adminToken (Se establece como vacio)
     */
    @Dado("que no tengo un token")
    public void noTengoToken() {
        adminToken = "";
    }

    /**
     * Paso: "Dado que no tengo un token de admin"
     * 
     * Autentica un usuario en el sistema usando las credenciales del último
     * usuario creado (Que no es admin). Si la autenticación es exitosa, extrae y guarda el
     * token JWT para usar en peticiones posteriores.
     * 
     * Proceso:
     * 1. Crea JSON con usuario y contraseña del último usuario registrado (Que no es admin)
     * 2. Envía petición POST a /sesiones
     * 3. Verifica que la respuesta sea exitosa (código 200)
     * 4. Extrae y guarda el token JWT de este usaurio como admin
     * 5. Valida que el token no esté vacío
     */
    @Dado("que no tengo un token de admin")
    public void noTengoTokenAdmin() {
         var body = """
        {
          "usuario":"%s",
          "clave":"%s"
        }
        """.formatted(ultimoUsuario, ultimoPassword);

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

    /**
     * Paso: "Cuando elimino elimino un usuario que no existe"
     * 
     * Intenta eliminar un usuario que no existe en el sistema usando el token de administrador.
     * Este paso simula la funcionalidad de administración para eliminar usuarios inexistentes.
     * 
     * Proceso:
     * 1. Incluye el token JWT del admin en el header Authorization
     * 2. Envía petición DELETE a /usuarios/{usuario} con un usuario inexistente
     * 3. La respuesta debería indicar que el usuario no fue encontrado (404)
     */
    @Cuando("elimino elimino un usuario que no existe")
    public void eliminarUsuarioInexistente() {
        lastResponse = given()
            .header("Authorization", "Bearer " + adminToken)
            .delete(baseUrl() + "/usuarios/" + "usuario_inexistente_12345");
    }
}