package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controller.RestauranteController;
import controller.RestauranteRequestDTO;
import controller.RestauranteResponseDTO;
import service.RolAutenticado;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RestauranteHttpServer {
    private final int port;
    private final RestauranteController controller;
    private HttpServer server;

    public RestauranteHttpServer(int port, RestauranteController controller) {
        this.port = port;
        this.controller = controller;
    }

    public int start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/restaurantes", this::handleCreateRestaurant);
        server.setExecutor(null);
        server.start();
        return server.getAddress().getPort();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleCreateRestaurant(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"message\":\"Metodo no permitido\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String roleHeader = exchange.getRequestHeaders().getFirst("X-Rol");
            String rol = roleHeader == null ? null : roleHeader.trim();

            if (rol == null || rol.isBlank()) {
                sendJson(exchange, 401, "{\"success\":false,\"message\":\"No autenticado\"}");
                return;
            }

            RestauranteRequestDTO request = parseJson(body);
            RestauranteResponseDTO response = controller.crearRestaurante(request, new RolAutenticado(10L, rol));
            sendJson(exchange, 201, toJson(response));
        } catch (SecurityException ex) {
            sendJson(exchange, 403, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    private RestauranteRequestDTO parseJson(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body requerido");
        }

        RestauranteRequestDTO dto = new RestauranteRequestDTO();
        Map<String, String> values = new java.util.HashMap<>();
        String normalized = body.replace("{", "").replace("}", "").replace("\"", "");
        String[] pairs = normalized.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length >= 2) {
                values.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        dto.setNombre(values.get("nombre"));
        dto.setNit(values.get("nit"));
        dto.setDireccion(values.get("direccion"));
        dto.setTelefono(values.get("telefono"));
        dto.setUrlLogo(values.get("urlLogo"));
        if (values.get("idPropietario") != null) {
            dto.setIdPropietario(Long.valueOf(values.get("idPropietario")));
        }
        return dto;
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String toJson(RestauranteResponseDTO response) {
        return "{\"success\":" + response.isSuccess() + ",\"message\":\"" + response.getMessage() + "\",\"restaurante\":{"
                + "\"nombre\":\"" + response.getRestaurante().getNombre() + "\","
                + "\"nit\":\"" + response.getRestaurante().getNit() + "\","
                + "\"direccion\":\"" + response.getRestaurante().getDireccion() + "\","
                + "\"telefono\":\"" + response.getRestaurante().getTelefono() + "\","
                + "\"urlLogo\":\"" + response.getRestaurante().getUrlLogo() + "\","
                + "\"idPropietario\":" + response.getRestaurante().getIdPropietario() + "} }";
    }
}
