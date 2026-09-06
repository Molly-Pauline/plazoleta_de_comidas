package controller;

import model.Restaurante;
import org.junit.jupiter.api.Test;
import service.RolAutenticado;

import static org.junit.jupiter.api.Assertions.*;

class RestauranteControllerContractTest {

    @Test
    void crearRestauranteDesdeDto_conAdministradorAutenticado_devuelveRespuestaDeContrato() {
        RestauranteController controller = new RestauranteController();

        RestauranteRequestDTO request = new RestauranteRequestDTO(
                "La Casona",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        RestauranteResponseDTO response = controller.crearRestaurante(request, new RolAutenticado(10L, "ADMINISTRADOR"));

        assertTrue(response.isSuccess());
        assertEquals("La Casona", response.getRestaurante().getNombre());
        assertEquals("123456789", response.getRestaurante().getNit());
    }
}
