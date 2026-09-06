package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controller.RestauranteController;
import controller.RestauranteRequestDTO;
import controller.RestauranteResponseDTO;
import controller.PlatoController;
import controller.PlatoRequestDTO;
import controller.PlatoResponseDTO;
import controller.PlatoUpdateRequestDTO;
import repository.PlatoRepository;
import service.RolAutenticado;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestauranteHttpServer {
    private final int port;
    private final RestauranteController controller;
    private final PlatoController platoController;
    private HttpServer server;

    public RestauranteHttpServer(int port, RestauranteController controller) {
        this.port = port;
        this.controller = controller;
        this.platoController = new PlatoController(new PlatoRepository(), controller.getRepository());
    }

    public int start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/restaurantes", this::handleCreateRestaurant);
        server.createContext("/platos", this::handlePlatos);
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

            RestauranteRequestDTO request = parseRestaurantJson(body);
            RestauranteResponseDTO response = controller.crearRestaurante(request, autenticado(exchange, rol));
            sendJson(exchange, 201, toJson(response));
        } catch (SecurityException ex) {
            sendJson(exchange, 403, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    private void handlePlatos(HttpExchange exchange) throws IOException {
        try {
            String roleHeader = exchange.getRequestHeaders().getFirst("X-Rol");
            String rol = roleHeader == null ? null : roleHeader.trim();
            if (rol == null || rol.isBlank()) {
                sendJson(exchange, 401, "{\"success\":false,\"message\":\"No autenticado\"}");
                return;
            }

            RolAutenticado usuario = autenticado(exchange, rol);
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if ("POST".equals(exchange.getRequestMethod()) && "/platos".equals(path)) {
                PlatoResponseDTO response = platoController.crearPlato(parsePlatoJson(body), usuario);
                sendJson(exchange, 201, toJson(response));
                return;
            }

            if ("PUT".equals(exchange.getRequestMethod()) && path.matches("/platos/\\d+")) {
                Long idPlato = Long.valueOf(path.substring(path.lastIndexOf('/') + 1));
                PlatoResponseDTO response = platoController.modificarPlato(
                        idPlato, parseUpdatePlatoJson(body), usuario);
                sendJson(exchange, 200, toJson(response));
                return;
            }

            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Metodo no permitido\"}");
        } catch (SecurityException ex) {
            sendJson(exchange, 403, jsonError(ex.getMessage()));
        } catch (Exception ex) {
            sendJson(exchange, 400, jsonError(ex.getMessage()));
        }
    }

    private RolAutenticado autenticado(HttpExchange exchange, String rol) {
        String idHeader = exchange.getRequestHeaders().getFirst("X-User-Id");
        Long idUsuario = idHeader == null || idHeader.isBlank() ? 10L : Long.valueOf(idHeader);
        return new RolAutenticado(idUsuario, rol);
    }

    private RestauranteRequestDTO parseRestaurantJson(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body requerido");
        }

        RestauranteRequestDTO dto = new RestauranteRequestDTO();
        Map<String, String> values = parseValues(body);

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

    private PlatoRequestDTO parsePlatoJson(String body) {
        Map<String, String> values = parseValues(body);
        PlatoRequestDTO dto = new PlatoRequestDTO();
        dto.setNombre(values.get("nombre"));
        dto.setPrecio(parseInteger(values.get("precio"), "precio"));
        dto.setDescripcion(values.get("descripcion"));
        dto.setUrlImagen(values.get("urlImagen"));
        dto.setCategoria(values.get("categoria"));
        dto.setIdRestaurante(parseLong(values.get("idRestaurante"), "idRestaurante"));
        return dto;
    }

    private PlatoUpdateRequestDTO parseUpdatePlatoJson(String body) {
        Map<String, String> values = parseValues(body);
        return new PlatoUpdateRequestDTO(
                parseInteger(values.get("precio"), "precio"),
                values.get("descripcion")
        );
    }

    private Map<String, String> parseValues(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body requerido");
        }
        Map<String, String> values = new HashMap<>();
        Matcher matcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|-?\\d+|true|false|null)")
                .matcher(body);
        while (matcher.find()) {
            String value = matcher.group(2);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1).replace("\\\"", "\"");
            }
            values.put(matcher.group(1), "null".equals(value) ? null : value);
        }
        return values;
    }

    private Integer parseInteger(String value, String field) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " debe ser un entero");
        }
    }

    private Long parseLong(String value, String field) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " debe ser numérico");
        }
    }

    private String jsonError(String message) {
        String safeMessage = message == null ? "Error de solicitud" : message.replace("\"", "\\\"");
        return "{\"success\":false,\"message\":\"" + safeMessage + "\"}";
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
                + "\"id\":" + response.getRestaurante().getId() + ","
                + "\"nombre\":\"" + response.getRestaurante().getNombre() + "\","
                + "\"nit\":\"" + response.getRestaurante().getNit() + "\","
                + "\"direccion\":\"" + response.getRestaurante().getDireccion() + "\","
                + "\"telefono\":\"" + response.getRestaurante().getTelefono() + "\","
                + "\"urlLogo\":\"" + response.getRestaurante().getUrlLogo() + "\","
                + "\"idPropietario\":" + response.getRestaurante().getIdPropietario() + "} }";
    }

    private String toJson(PlatoResponseDTO response) {
        return "{\"success\":" + response.isSuccess() + ",\"message\":\"" + response.getMessage()
                + "\",\"plato\":{"
                + "\"id\":" + response.getPlato().getId() + ","
                + "\"nombre\":\"" + response.getPlato().getNombre() + "\","
                + "\"precio\":" + response.getPlato().getPrecio() + ","
                + "\"descripcion\":\"" + response.getPlato().getDescripcion() + "\","
                + "\"urlImagen\":\"" + response.getPlato().getUrlImagen() + "\","
                + "\"categoria\":\"" + response.getPlato().getCategoria() + "\","
                + "\"idRestaurante\":" + response.getPlato().getIdRestaurante() + ","
                + "\"activo\":" + response.getPlato().isActivo() + "}}";
    }
}
