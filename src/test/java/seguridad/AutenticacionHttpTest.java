package seguridad;

import config.FabricaDeSeguridad;
import controller.RestauranteController;
import http.RestauranteHttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seguridad.dominio.Rol;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-05. Pruebas de integracion de rutas protegidas sobre el servidor real.
 */
class AutenticacionHttpTest {

    private static final String RESTAURANTE = "{\"nombre\":\"La Casona\",\"nit\":\"123456789\","
            + "\"direccion\":\"Calle 123\",\"telefono\":\"+573001234567\","
            + "\"urlLogo\":\"https://example.com/logo.png\",\"idPropietario\":10}";

    private RestauranteHttpServer servidor;
    private HttpClient cliente;
    private String base;

    @BeforeEach
    void levantarServidor() throws Exception {
        FabricaDeSeguridad seguridad = FabricaDeSeguridad.paraPruebas("secreto-de-pruebas-sprint-1")
                .conCuenta(1L, "admin@plazoleta.com", "Admin12345", Rol.ADMINISTRADOR)
                .conCuenta(10L, "duenio@correo.com", "Duenio12345", Rol.PROPIETARIO);

        servidor = new RestauranteHttpServer(0, new RestauranteController(),
                seguridad);
        base = "http://localhost:" + servidor.start();
        cliente = HttpClient.newHttpClient();
    }

    @AfterEach
    void apagarServidor() {
        servidor.stop();
    }

    @Test
    void loginValido_retorna200YToken() throws Exception {
        HttpResponse<String> respuesta = login("admin@plazoleta.com", "Admin12345");

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"token\""));
        assertTrue(respuesta.body().contains("\"rol\":\"ADMINISTRADOR\""));
        assertFalse(respuesta.body().contains("Admin12345"));
    }

    @Test
    void loginInvalido_retorna401() throws Exception {
        assertEquals(401, login("admin@plazoleta.com", "equivocada").statusCode());
        assertEquals(401, login("nadie@plazoleta.com", "Admin12345").statusCode());
    }

    @Test
    void rutaProtegidaSinCredencial_retorna401() throws Exception {
        assertEquals(401, crearRestaurante(null).statusCode());
    }

    @Test
    void rutaProtegidaConCredencialInvalida_retorna401() throws Exception {
        assertEquals(401, crearRestaurante("token-inventado.firma-falsa").statusCode());
    }

    @Test
    void administrador_puedeCrearRestaurante() throws Exception {
        HttpResponse<String> respuesta = crearRestaurante(token("admin@plazoleta.com", "Admin12345"));

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"success\":true"));
    }

    @Test
    void propietario_noPuedeCrearRestaurante() throws Exception {
        assertEquals(403, crearRestaurante(token("duenio@correo.com", "Duenio12345")).statusCode());
    }

    // ---- HU-01 sobre HTTP, protegido por HU-05 ----

    @Test
    void crearPropietarioSinCredencial_retorna401() throws Exception {
        assertEquals(401, crearPropietario(null, "ana@correo.com", "12345678").statusCode());
    }

    @Test
    void propietario_noPuedeCrearOtroPropietario() throws Exception {
        String tokenPropietario = token("duenio@correo.com", "Duenio12345");

        assertEquals(403,
                crearPropietario(tokenPropietario, "ana@correo.com", "12345678").statusCode());
    }

    @Test
    void administrador_creaPropietarioSinExponerLaClave() throws Exception {
        HttpResponse<String> respuesta = crearPropietario(
                token("admin@plazoleta.com", "Admin12345"), "ana@correo.com", "12345678");

        assertEquals(201, respuesta.statusCode());
        assertTrue(respuesta.body().contains("\"rol\":\"PROPIETARIO\""));
        assertFalse(respuesta.body().contains("ClaveSegura123"));
        assertFalse(respuesta.body().contains("$2a$"));
    }

    @Test
    void propietarioCreadoPorHttp_puedeAutenticarseEnseguida() throws Exception {
        crearPropietario(token("admin@plazoleta.com", "Admin12345"), "nueva@correo.com", "55554444");

        HttpResponse<String> login = login("nueva@correo.com", "ClaveSegura123");

        assertEquals(200, login.statusCode());
        assertTrue(login.body().contains("\"rol\":\"PROPIETARIO\""));
    }

    @Test
    void menorDeEdad_seRechaza() throws Exception {
        HttpResponse<String> respuesta = enviarPropietario(
                token("admin@plazoleta.com", "Admin12345"),
                "{\"nombre\":\"Nino\",\"apellido\":\"Perez\","
                        + "\"documentoDeIdentidad\":\"99999999\",\"celular\":\"+573001112233\","
                        + "\"fechaNacimiento\":\"" + java.time.LocalDate.now().minusYears(15) + "\","
                        + "\"correo\":\"nino@correo.com\",\"clave\":\"ClaveSegura123\"}");

        assertEquals(400, respuesta.statusCode());
    }

    // ---------- utilidades ----------

    private HttpResponse<String> login(String correo, String clave) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(base + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }

    private String token(String correo, String clave) throws Exception {
        String cuerpo = login(correo, clave).body();
        int desde = cuerpo.indexOf("\"token\":\"") + 9;
        return cuerpo.substring(desde, cuerpo.indexOf('"', desde));
    }

    private HttpResponse<String> crearPropietario(String token, String correo, String documento)
            throws Exception {
        return enviarPropietario(token,
                "{\"nombre\":\"Ana\",\"apellido\":\"Garcia\","
                        + "\"documentoDeIdentidad\":\"" + documento + "\","
                        + "\"celular\":\"+573001112233\",\"fechaNacimiento\":\"1990-05-20\","
                        + "\"correo\":\"" + correo + "\",\"clave\":\"ClaveSegura123\"}");
    }

    private HttpResponse<String> enviarPropietario(String token, String cuerpo) throws Exception {
        HttpRequest.Builder constructor = HttpRequest.newBuilder()
                .uri(URI.create(base + "/propietarios"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        if (token != null) {
            constructor.header("Authorization", "Bearer " + token);
        }
        return cliente.send(constructor.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> crearRestaurante(String token) throws Exception {
        HttpRequest.Builder constructor = HttpRequest.newBuilder()
                .uri(URI.create(base + "/restaurantes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(RESTAURANTE));
        if (token != null) {
            constructor.header("Authorization", "Bearer " + token);
        }
        return cliente.send(constructor.build(), HttpResponse.BodyHandlers.ofString());
    }
}
