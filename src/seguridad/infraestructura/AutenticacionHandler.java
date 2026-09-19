package seguridad.infraestructura;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seguridad.aplicacion.ResultadoLogin;
import seguridad.aplicacion.ServicioDeAutenticacion;
import seguridad.dominio.CredencialesInvalidasException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Endpoint publico POST /auth/login.
 *
 * Es la UNICA ruta que no exige credencial. Todo lo demas pasa por el filtro.
 * La respuesta nunca incluye la clave ni el hash.
 */
public class AutenticacionHandler implements HttpHandler {

    private static final Pattern CAMPO =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private final ServicioDeAutenticacion servicioDeAutenticacion;

    public AutenticacionHandler(ServicioDeAutenticacion servicioDeAutenticacion) {
        this.servicioDeAutenticacion = servicioDeAutenticacion;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                responder(exchange, 405, "{\"success\":false,\"message\":\"Metodo no permitido\"}");
                return;
            }

            String cuerpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> campos = leerCampos(cuerpo);

            ResultadoLogin resultado = servicioDeAutenticacion.login(
                    campos.get("correo"), campos.get("clave"));

            responder(exchange, 200, "{"
                    + "\"success\":true,"
                    + "\"message\":\"Autenticacion correcta\","
                    + "\"token\":\"" + resultado.getToken() + "\","
                    + "\"idUsuario\":" + resultado.getIdUsuario() + ","
                    + "\"rol\":\"" + resultado.getRol().name() + "\","
                    + "\"expiraEnSegundos\":" + resultado.getExpiraEnSegundos()
                    + "}");
        } catch (CredencialesInvalidasException ex) {
            // 401 y mensaje unico: no se revela si el correo existe.
            responder(exchange, 401, "{\"success\":false,\"message\":\"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            responder(exchange, 400, "{\"success\":false,\"message\":\"Solicitud invalida\"}");
        }
    }

    private Map<String, String> leerCampos(String cuerpo) {
        Map<String, String> campos = new HashMap<>();
        if (cuerpo == null || cuerpo.isBlank()) {
            return campos;
        }
        Matcher matcher = CAMPO.matcher(cuerpo);
        while (matcher.find()) {
            campos.put(matcher.group(1), matcher.group(2).replace("\\\"", "\""));
        }
        return campos;
    }

    private void responder(HttpExchange exchange, int estado, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(estado, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
