package http;

import config.FabricaDeSeguridad;
import controller.RestauranteController;
import org.junit.jupiter.api.Test;
import seguridad.dominio.Rol;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HU-04 sobre HTTP.
 *
 * Actualizada en HU-05: la identidad del propietario ya no llega en X-User-Id,
 * sale del token firmado.
 */
class PlatoHttpServerTest {

    private HttpClient cliente;
    private String base;

    @Test
    void putPlato_modificaSoloPrecioYDescripcion() throws Exception {
        FabricaDeSeguridad seguridad = FabricaDeSeguridad.paraPruebas("secreto-de-pruebas-sprint-1")
                .conCuenta(1L, "admin@plazoleta.com", "Admin12345", Rol.ADMINISTRADOR)
                .conCuenta(10L, "duenio@correo.com", "Duenio12345", Rol.PROPIETARIO);

        RestauranteHttpServer server = new RestauranteHttpServer(
                0, new RestauranteController(), seguridad);
        int port = server.start();

        try {
            cliente = HttpClient.newHttpClient();
            base = "http://localhost:" + port;

            String tokenAdmin = token("admin@plazoleta.com", "Admin12345");
            String tokenDuenio = token("duenio@correo.com", "Duenio12345");

            HttpResponse<String> restauranteResponse = enviar("POST", "/restaurantes", tokenAdmin,
                    "{\"nombre\":\"La Casona\",\"nit\":\"123456789\","
                            + "\"direccion\":\"Calle 123\",\"telefono\":\"+573001234567\","
                            + "\"urlLogo\":\"https://example.com/logo.png\","
                            + "\"idPropietario\":10}");
            assertEquals(201, restauranteResponse.statusCode());

            HttpResponse<String> platoResponse = enviar("POST", "/platos", tokenDuenio,
                    "{\"nombre\":\"Pizza\",\"precio\":18000,"
                            + "\"descripcion\":\"Margarita\","
                            + "\"urlImagen\":\"https://example.com/pizza.png\","
                            + "\"categoria\":\"Pizzas\",\"idRestaurante\":1}");
            assertEquals(201, platoResponse.statusCode());

            HttpResponse<String> modificarResponse = enviar("PUT", "/platos/1", tokenDuenio,
                    "{\"precio\":20000,\"descripcion\":\"Margarita familiar\"}");

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

    private String token(String correo, String clave) throws Exception {
        HttpResponse<String> login = enviar("POST", "/auth/login", null,
                "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}");
        assertEquals(200, login.statusCode());
        int desde = login.body().indexOf("\"token\":\"") + 9;
        return login.body().substring(desde, login.body().indexOf('"', desde));
    }

    private HttpResponse<String> enviar(String metodo, String ruta, String token, String cuerpo)
            throws Exception {
        HttpRequest.Builder constructor = HttpRequest.newBuilder()
                .uri(URI.create(base + ruta))
                .header("Content-Type", "application/json");
        if (token != null) {
            constructor.header("Authorization", "Bearer " + token);
        }
        if ("PUT".equals(metodo)) {
            constructor.PUT(HttpRequest.BodyPublishers.ofString(cuerpo));
        } else {
            constructor.POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        }
        return cliente.send(constructor.build(), HttpResponse.BodyHandlers.ofString());
    }
}
