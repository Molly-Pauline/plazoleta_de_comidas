package http;

import controller.RestauranteController;
import controller.RestauranteRequestDTO;
import controller.RestauranteResponseDTO;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class RestauranteHttpServerTest {

    @Test
    void postRestaurante_conDatosValidos_retorna201YBodyDeContrato() throws Exception {
        RestauranteController controller = new RestauranteController();
        RestauranteHttpServer server = new RestauranteHttpServer(0, controller);
        int port = server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "{"
                    + "\"nombre\":\"La Casona\","
                    + "\"nit\":\"123456789\","
                    + "\"direccion\":\"Calle 123 #45-67\","
                    + "\"telefono\":\"+573001234567\","
                    + "\"urlLogo\":\"https://example.com/logo.png\","
                    + "\"idPropietario\":10"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/restaurantes"))
                    .header("Content-Type", "application/json")
                    .header("X-Rol", "ADMINISTRADOR")
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
