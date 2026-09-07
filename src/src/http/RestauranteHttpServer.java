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
import controller.PropietarioController;
import controller.PropietarioRequestDTO;
import controller.PropietarioResponseDTO;
import service.PropietarioService;
import repository.PlatoRepository;
import service.RolAutenticado;
import config.FabricaDeSeguridad;
import seguridad.aplicacion.ServicioDeAutenticacion;
import seguridad.dominio.CredencialesInvalidasException;
import seguridad.infraestructura.AutenticacionHandler;

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
    // HU-05: sin esto el servidor confiaba en el encabezado X-Rol que enviaba el cliente.
    private final ServicioDeAutenticacion servicioDeAutenticacion;
    private final PropietarioController propietarioController;
    private HttpServer server;

    public RestauranteHttpServer(int port, RestauranteController controller) {
        this(port, controller, FabricaDeSeguridad.porDefecto());
    }

    public RestauranteHttpServer(int port, RestauranteController controller,
                                 FabricaDeSeguridad seguridad) {
        this.port = port;
        this.controller = controller;
        this.servicioDeAutenticacion = seguridad.getServicioDeAutenticacion();
        this.platoController = new PlatoController(new PlatoRepository(), controller.getRepository());
        // Comparte el MISMO repositorio que usa el login: un propietario creado
        // por este endpoint puede autenticarse enseguida.
        this.propietarioController = new PropietarioController(
                new PropietarioService(seguridad.getPropietarioRepository()),
                seguridad.getPoliticaDeAutorizacion());
    }

    public int start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        // Unica ruta publica del Sprint 1.
        server.createContext("/auth/login", new AutenticacionHandler(servicioDeAutenticacion));
        // Rutas protegidas: exigen Authorization: Bearer <token>.
        server.createContext("/propietarios", this::handleCreatePropietario);
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

    /** HU-01 sobre HTTP, protegido por HU-05: solo ADMINISTRADOR. */
    private void handleCreatePropietario(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"message\":\"Metodo no permitido\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            RolAutenticado usuario = autenticado(exchange);

            PropietarioResponseDTO response =
                    propietarioController.crearPropietario(parsePropietarioJson(body), usuario);
            sendJson(exchange, 201, toJson(response));
        } catch (CredencialesInvalidasException ex) {
            sendJson(exchange, 401, jsonError(ex.getMessage()));
        } catch (SecurityException ex) {
            sendJson(exchange, 403, jsonError(ex.getMessage()));
        } catch (Exception ex) {
            sendJson(exchange, 400, jsonError(ex.getMessage()));
        }
    }

    private PropietarioRequestDTO parsePropietarioJson(String body) {
        Map<String, String> values = parseValues(body);
        PropietarioRequestDTO dto = new PropietarioRequestDTO();
        dto.setNombre(values.get("nombre"));
        dto.setApellido(values.get("apellido"));
        dto.setDocumentoDeIdentidad(values.get("documentoDeIdentidad"));
        dto.setCelular(values.get("celular"));
        dto.setFechaNacimiento(values.get("fechaNacimiento"));
        dto.setCorreo(values.get("correo"));
        dto.setClave(values.get("clave"));
        return dto;
    }

    private String toJson(PropietarioResponseDTO response) {
        return "{\"success\":" + response.isSuccess()
                + ",\"message\":\"" + response.getMessage() + "\",\"propietario\":{"
                + "\"id\":" + response.getId() + ","
                + "\"nombre\":\"" + response.getNombre() + "\","
                + "\"apellido\":\"" + response.getApellido() + "\","
                + "\"documentoDeIdentidad\":\"" + response.getDocumentoDeIdentidad() + "\","
                + "\"celular\":\"" + response.getCelular() + "\","
                + "\"correo\":\"" + response.getCorreo() + "\","
                + "\"rol\":\"" + response.getRol() + "\"}}";
    }

    private void handleCreateRestaurant(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"message\":\"Metodo no permitido\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            RolAutenticado usuario = autenticado(exchange);

            RestauranteRequestDTO request = parseRestaurantJson(body);
            RestauranteResponseDTO response = controller.crearRestaurante(request, usuario);
            sendJson(exchange, 201, toJson(response));
        } catch (CredencialesInvalidasException ex) {
            sendJson(exchange, 401, jsonError(ex.getMessage()));
        } catch (SecurityException ex) {
            sendJson(exchange, 403, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    private void handlePlatos(HttpExchange exchange) throws IOException {
        try {
            RolAutenticado usuario = autenticado(exchange);
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
        } catch (CredencialesInvalidasException ex) {
            sendJson(exchange, 401, jsonError(ex.getMessage()));
        } catch (SecurityException ex) {
            sendJson(exchange, 403, jsonError(ex.getMessage()));
        } catch (Exception ex) {
            sendJson(exchange, 400, jsonError(ex.getMessage()));
        }
    }

    /**
     * HU-05. Antes esto leia X-Rol y X-User-Id directamente de la peticion, asi
     * que cualquier cliente podia declararse ADMINISTRADOR. Ahora la identidad
     * sale de un token firmado: si el cliente lo altera, la firma no coincide.
     */
    private RolAutenticado autenticado(HttpExchange exchange) {
        return servicioDeAutenticacion.autenticar(
                exchange.getRequestHeaders().getFirst("Authorization"));
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
            throw new IllegalArgumentException(field + " debe ser num??rico");
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
