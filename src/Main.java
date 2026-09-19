import config.FabricaDeSeguridad;
import controller.RestauranteController;
import http.RestauranteHttpServer;

/**
 * Punto de entrada de la aplicacion — Sprint 1.
 *
 * Arranca el servidor HTTP con las rutas del sprint:
 *
 *   POST /auth/login     publica
 *   POST /propietarios   solo ADMINISTRADOR        (HU-01)
 *   POST /restaurantes   solo ADMINISTRADOR        (HU-02)
 *   POST /platos         solo PROPIETARIO duenio   (HU-03)
 *   PUT  /platos/{id}    solo PROPIETARIO duenio   (HU-04)
 *
 * Variables de entorno (ninguna se versiona; ver .env.example):
 *   AUTH_SECRET    firma del token, minimo 16 caracteres
 *   ADMIN_CORREO   correo del administrador inicial
 *   ADMIN_CLAVE    su clave; si falta, se genera una y se imprime
 *   PORT           puerto, por defecto 8080
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int puerto = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080"));

        // Siembra el ADMINISTRADOR: el enunciado dice que ya existe en el
        // sistema, asi que no lo crea HU-01.
        FabricaDeSeguridad seguridad = FabricaDeSeguridad.porDefecto();

        RestauranteHttpServer servidor =
                new RestauranteHttpServer(puerto, new RestauranteController(), seguridad);
        int puertoReal = servidor.start();

        System.out.println();
        System.out.println("===========================================================");
        System.out.println("  Plazoleta de Comidas - Sprint 1");
        System.out.println("  Servidor escuchando en http://localhost:" + puertoReal);
        System.out.println("===========================================================");
        System.out.println("  POST /auth/login      publica");
        System.out.println("  POST /propietarios    ADMINISTRADOR        [HU-01]");
        System.out.println("  POST /restaurantes    ADMINISTRADOR        [HU-02]");
        System.out.println("  POST /platos          PROPIETARIO duenio   [HU-03]");
        System.out.println("  PUT  /platos/{id}     PROPIETARIO duenio   [HU-04]");
        System.out.println("===========================================================");
        System.out.println("  Ctrl+C para detener.");
        System.out.println();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nDeteniendo el servidor...");
            servidor.stop();
        }));

        Thread.currentThread().join();
    }
}
