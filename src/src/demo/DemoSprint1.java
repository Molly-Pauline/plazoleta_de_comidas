package demo;

import controller.RestauranteController;
import http.RestauranteHttpServer;
import config.FabricaDeSeguridad;
import seguridad.dominio.Rol;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

/**
 * Guion de demostracion en vivo del Sprint 1 (seccion 16 de la guia).
 *
 * Ejecuta de punta a punta: login, rechazo de credenciales, proteccion por rol,
 * proteccion por pertenencia y el flujo Propietario -> Restaurante -> Plato.
 *
 * Ejecutar con:  mvn -q compile exec:java -Dexec.mainClass=demo.DemoSprint1
 * o directamente: java -cp target/classes demo.DemoSprint1
 */
public class DemoSprint1 {

    private static int correctas = 0;
    private static int fallidas = 0;
    private static HttpClient cliente;
    private static String base;

    public static void main(String[] args) throws Exception {
        FabricaDeSeguridad seguridad = FabricaDeSeguridad
                .paraPruebas("secreto-de-demostracion-sprint-1")
                .conCuenta(1L, "admin@plazoleta.com", "Admin12345", Rol.ADMINISTRADOR);

        RestauranteController controller = new RestauranteController();
        RestauranteHttpServer servidor = new RestauranteHttpServer(
                0, controller, seguridad);
        int puerto = servidor.start();
        cliente = HttpClient.newHttpClient();
        base = "http://localhost:" + puerto;

        try {
            titulo("1. HU-05  Login del administrador");
            Respuesta loginAdmin = post("/auth/login",
                    "{\"correo\":\"admin@plazoleta.com\",\"clave\":\"Admin12345\"}", null);
            verificar("Login valido responde 200", loginAdmin.estado == 200);
            verificar("La respuesta NO contiene la clave",
                    !loginAdmin.cuerpo.contains("Admin12345"));
            verificar("La respuesta NO contiene el hash", !loginAdmin.cuerpo.contains("$2a$"));
            String tokenAdmin = extraer(loginAdmin.cuerpo, "token");

            Respuesta claveMala = post("/auth/login",
                    "{\"correo\":\"admin@plazoleta.com\",\"clave\":\"equivocada\"}", null);
            Respuesta correoInexistente = post("/auth/login",
                    "{\"correo\":\"nadie@plazoleta.com\",\"clave\":\"Admin12345\"}", null);
            verificar("Clave incorrecta responde 401", claveMala.estado == 401);
            verificar("Correo inexistente responde 401", correoInexistente.estado == 401);
            verificar("Mensaje UNICO: no revela si el correo existe",
                    claveMala.cuerpo.equals(correoInexistente.cuerpo));

            titulo("2. HU-01  Crear propietario por HTTP (solo ADMINISTRADOR)");
            verificar("Sin credencial responde 401",
                    post("/propietarios", propietarioJson("ana@correo.com", "12345678",
                            "1990-05-20", "ClaveSegura123"), null).estado == 401);

            Respuesta creacionAna = post("/propietarios",
                    propietarioJson("ana@correo.com", "12345678", "1990-05-20", "ClaveSegura123"),
                    tokenAdmin);
            verificar("ADMINISTRADOR crea propietario (201)", creacionAna.estado == 201);
            verificar("La respuesta NO expone la clave",
                    !creacionAna.cuerpo.contains("ClaveSegura123"));
            verificar("La respuesta NO expone el hash", !creacionAna.cuerpo.contains("$2a$"));
            verificar("El rol lo asigna el servidor",
                    creacionAna.cuerpo.contains("\"rol\":\"PROPIETARIO\""));
            Long idAna = Long.valueOf(extraerNumero(creacionAna.cuerpo, "id"));

            verificar("Menor de edad rechazado (400)",
                    post("/propietarios", propietarioJson("nino@correo.com", "99999999",
                            LocalDate.now().minusYears(15).toString(), "Clave12345"),
                            tokenAdmin).estado == 400);
            verificar("Documento con letras rechazado (400)",
                    post("/propietarios", propietarioJson("malo@correo.com", "ABC123",
                            "1990-05-20", "Clave12345"), tokenAdmin).estado == 400);
            verificar("Correo invalido rechazado (400)",
                    post("/propietarios", propietarioJson("sin-arroba", "77777777",
                            "1990-05-20", "Clave12345"), tokenAdmin).estado == 400);

            titulo("3. HU-05  Proteccion de rutas");
            verificar("Sin credencial responde 401",
                    post("/restaurantes", restauranteJson(2L), null).estado == 401);
            verificar("Token alterado responde 401",
                    post("/restaurantes", restauranteJson(2L), tokenAdmin + "x").estado == 401);
            verificar("Token con firma valida pero rol cambiado a mano responde 401",
                    post("/restaurantes", restauranteJson(2L), manipularRol(tokenAdmin)).estado == 401);

            titulo("4. Matriz de autorizacion");
            Respuesta loginAna = post("/auth/login",
                    "{\"correo\":\"ana@correo.com\",\"clave\":\"ClaveSegura123\"}", null);
            verificar("El propietario recien creado puede autenticarse", loginAna.estado == 200);
            String tokenAna = extraer(loginAna.cuerpo, "token");

            verificar("PROPIETARIO NO puede crear restaurante (403)",
                    post("/restaurantes", restauranteJson(idAna), tokenAna).estado == 403);
            Respuesta creado = post("/restaurantes", restauranteJson(idAna), tokenAdmin);
            verificar("ADMINISTRADOR si puede crear restaurante (201)", creado.estado == 201);

            titulo("5. HU-03 / HU-04  Pertenencia del restaurante");
            verificar("ADMINISTRADOR NO puede crear plato (403)",
                    post("/platos", platoJson(1L), tokenAdmin).estado == 403);
            Respuesta plato = post("/platos", platoJson(1L), tokenAna);
            verificar("El propietario duenio si puede crear plato (201)", plato.estado == 201);
            verificar("El plato nace activo", plato.cuerpo.contains("\"activo\":true"));

            Respuesta creacionLuis = post("/propietarios",
                    propietarioJson("luis@correo.com", "87654321", "1992-03-10", "OtraClave123"),
                    tokenAdmin);
            verificar("Segundo propietario creado (201)", creacionLuis.estado == 201);
            String tokenLuis = extraer(post("/auth/login",
                    "{\"correo\":\"luis@correo.com\",\"clave\":\"OtraClave123\"}", null).cuerpo, "token");
            verificar("Un propietario ajeno NO puede modificar el plato (403)",
                    put("/platos/1", "{\"precio\":99000,\"descripcion\":\"Secuestrado\"}", tokenLuis).estado == 403);

            Respuesta modificado = put("/platos/1",
                    "{\"precio\":20000,\"descripcion\":\"Margarita familiar\"}", tokenAna);
            verificar("El duenio si puede modificar precio y descripcion (200)", modificado.estado == 200);
            verificar("El precio cambio", modificado.cuerpo.contains("\"precio\":20000"));
            verificar("El nombre NO cambio", modificado.cuerpo.contains("\"nombre\":\"Pizza\""));
            verificar("La categoria NO cambio", modificado.cuerpo.contains("\"categoria\":\"Pizzas\""));
            verificar("El segundo propietario tiene id propio",
                    !String.valueOf(idAna).equals(extraerNumero(creacionLuis.cuerpo, "id")));

            System.out.println();
            System.out.println("=======================================================");
            System.out.printf("  RESULTADO: %d correctas, %d fallidas%n", correctas, fallidas);
            System.out.println("=======================================================");
        } finally {
            servidor.stop();
        }

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    // ---------- utilidades ----------

    private record Respuesta(int estado, String cuerpo) { }

    private static Respuesta post(String ruta, String cuerpo, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(base + ruta))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        HttpResponse<String> r = cliente.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Respuesta(r.statusCode(), r.body());
    }

    private static Respuesta put(String ruta, String cuerpo, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(base + ruta))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(cuerpo));
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        HttpResponse<String> r = cliente.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Respuesta(r.statusCode(), r.body());
    }

    private static String propietarioJson(String correo, String documento,
                                          String fechaNacimiento, String clave) {
        return "{\"nombre\":\"Ana\",\"apellido\":\"Garcia\","
                + "\"documentoDeIdentidad\":\"" + documento + "\","
                + "\"celular\":\"+573001112233\","
                + "\"fechaNacimiento\":\"" + fechaNacimiento + "\","
                + "\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}";
    }

    /** Extrae un campo numerico del JSON (sin comillas). */
    private static String extraerNumero(String json, String campo) {
        int i = json.indexOf("\"" + campo + "\":");
        if (i < 0) {
            return "";
        }
        int desde = i + campo.length() + 3;
        int hasta = desde;
        while (hasta < json.length() && (Character.isDigit(json.charAt(hasta)))) {
            hasta++;
        }
        return json.substring(desde, hasta);
    }

    private static String restauranteJson(Long idPropietario) {
        return "{\"nombre\":\"La Casona\",\"nit\":\"123456789\",\"direccion\":\"Calle 123\","
                + "\"telefono\":\"+573001234567\",\"urlLogo\":\"https://example.com/logo.png\","
                + "\"idPropietario\":" + idPropietario + "}";
    }

    private static String platoJson(Long idRestaurante) {
        return "{\"nombre\":\"Pizza\",\"precio\":18000,\"descripcion\":\"Margarita\","
                + "\"urlImagen\":\"https://example.com/pizza.png\",\"categoria\":\"Pizzas\","
                + "\"idRestaurante\":" + idRestaurante + "}";
    }

    private static String extraer(String json, String campo) {
        int i = json.indexOf("\"" + campo + "\":\"");
        if (i < 0) {
            return "";
        }
        int desde = i + campo.length() + 4;
        return json.substring(desde, json.indexOf('"', desde));
    }

    /** Cambia el rol dentro del token dejando la firma vieja: debe ser rechazado. */
    private static String manipularRol(String token) {
        String cuerpo = new String(java.util.Base64.getUrlDecoder()
                .decode(token.substring(0, token.indexOf('.'))));
        String alterado = cuerpo.replace("PROPIETARIO", "ADMINISTRADOR");
        if (alterado.equals(cuerpo)) {
            alterado = cuerpo.replace("ADMINISTRADOR", "PROPIETARIO");
        }
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(alterado.getBytes()) + token.substring(token.indexOf('.'));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            correctas++;
            System.out.println("  [OK]    " + descripcion);
        } else {
            fallidas++;
            System.out.println("  [FALLA] " + descripcion);
        }
    }
}
