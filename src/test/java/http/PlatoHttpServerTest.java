package http;

import controller.RestauranteController;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatoHttpServerTest {
    @Test
    void putPlato_modificaSoloPrecioYDescripcion() throws Exception {
        RestauranteHttpServer server = new RestauranteHttpServer(0, new RestauranteController());
        int port = server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String base = "http://localhost:" + port;
            HttpRequest crearRestaurante = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/restaurantes"))
                    .header("Content-Type", "application/json")
                    .header("X-Rol", "ADMINISTRADOR")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"nombre\":\"La Casona\",\"nit\":\"123456789\","
                                    + "\"direccion\":\"Calle 123\",\"telefono\":\"+573001234567\","
                                    + "\"urlLogo\":\"https://example.com/logo.png\","
                                    + "\"idPropietario\":10}"))
                    .build();
            HttpResponse<String> restauranteResponse = client.send(
                    crearRestaurante, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, restauranteResponse.statusCode());

            HttpRequest crearPlato = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/platos"))
                    .header("Content-Type", "application/json")
                    .header("X-Rol", "PROPIETARIO")
                    .header("X-User-Id", "10")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"nombre\":\"Pizza\",\"precio\":18000,"
                                    + "\"descripcion\":\"Margarita\","
                                    + "\"urlImagen\":\"https://example.com/pizza.png\","
                                    + "\"categoria\":\"Pizzas\",\"idRestaurante\":1}"))
                    .build();
            HttpResponse<String> platoResponse = client.send(
                    crearPlato, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, platoResponse.statusCode());

            HttpRequest modificarPlato = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/platos/1"))
                    .header("Content-Type", "application/json")
                    .header("X-Rol", "PROPIETARIO")
                    .header("X-User-Id", "10")
                    .PUT(HttpRequest.BodyPublishers.ofString(
                            "{\"precio\":20000,\"descripcion\":\"Margarita familiar\"}"))
                    .build();
            HttpResponse<String> modificarResponse = client.send(
                    modificarPlato, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, modificarResponse.statusCode());
            assertTrue(modificarResponse.body().contains("\"precio\":20000"));
            assertTrue(modificarResponse.body().contains("\"descripcion\":\"Margarita familiar\""));
            assertTrue(modificarResponse.body().contains("\"nombre\":\"Pizza\""));
            assertTrue(modificarResponse.body().contains("\"categoria\":\"Pizzas\""));
            assertTrue(modificarResponse.body().contains("\"activo\":true"));
        } finally {
            server.stop();
        }
    }
}
