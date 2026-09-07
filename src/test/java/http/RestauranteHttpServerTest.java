package http;

import config.FabricaDeSeguridad;
import controller.RestauranteController;
import org.junit.jupiter.api.Test;
import seguridad.dominio.Rol;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-02 sobre HTTP.
 *
 * Actualizada en HU-05: antes bastaba con enviar el encabezado X-Rol para
 * declararse ADMINISTRADOR. Ahora hay que autenticarse primero y usar el token.
 */
class RestauranteHttpServerTest {

    @Test
    void postRestaurante_conDatosValidos_retorna201YBodyDeContrato() throws Exception {
        FabricaDeSeguridad seguridad = FabricaDeSeguridad.paraPruebas("secreto-de-pruebas-sprint-1")
                .conCuenta(1L, "admin@plazoleta.com", "Admin12345", Rol.ADMINISTRADOR);

        RestauranteController controller = new RestauranteController();
        RestauranteHttpServer server = new RestauranteHttpServer(
                0, controller, seguridad);
        int port = server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String base = "http://localhost:" + port;

            HttpResponse<String> login = client.send(HttpRequest.newBuilder()
                    .uri(URI.create(base + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"correo\":\"admin@plazoleta.com\",\"clave\":\"Admin12345\"}"))
                    .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, login.statusCode());

            int desde = login.body().indexOf("\"token\":\"") + 9;
            String token = login.body().substring(desde, login.body().indexOf('"', desde));

            String body = "{"
                    + "\"nombre\":\"La Casona\","
                    + "\"nit\":\"123456789\","
                    + "\"direccion\":\"Calle 123 #45-67\","
                    + "\"telefono\":\"+573001234567\","
                    + "\"urlLogo\":\"https://example.com/logo.png\","
                    + "\"idPropietario\":10"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/restaurantes"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());
            assertTrue(response.body().contains("\"success\":true"));
            assertTrue(response.body().contains("La Casona"));
        } finally {
            server.stop();
        }
    }
}
