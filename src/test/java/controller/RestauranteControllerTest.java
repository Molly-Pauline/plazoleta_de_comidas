package controller;

import model.Restaurante;
import org.junit.jupiter.api.Test;
import service.RolAutenticado;

import static org.junit.jupiter.api.Assertions.*;

class RestauranteControllerTest {

    @Test
    void crearRestaurante_conAdministradorAutenticado_creaElRestaurante() {
        RestauranteController controller = new RestauranteController();

        Restaurante restaurante = new Restaurante(
                "La Casona",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        String resultado = controller.crearRestaurante(restaurante, new RolAutenticado(10L, "ADMINISTRADOR"));

        assertTrue(resultado.contains("Restaurante creado"));
    }

    @Test
    void crearRestaurante_conPropietarioAutenticado_deniegaElAcceso() {
        RestauranteController controller = new RestauranteController();

        Restaurante restaurante = new Restaurante(
                "La Casona",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        SecurityException exception = assertThrows(SecurityException.class,
                () -> controller.crearRestaurante(restaurante, new RolAutenticado(10L, "PROPIETARIO")));

        assertEquals("Acceso denegado: solo ADMINISTRADOR puede crear restaurantes", exception.getMessage());
    }
}
