package controller;

import model.Plato;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import repository.PlatoRepository;
import repository.RestauranteRepository;
import service.RolAutenticado;

import static org.junit.jupiter.api.Assertions.*;

class PlatoControllerTest {
    @Test
    void crearYModificarPlato_conPropietarioPropio_funcionaSinCambiarCamposRestringidos() {
        RestauranteRepository restaurantes = new RestauranteRepository();
        Restaurante restaurante = restaurantes.save(new Restaurante(
                "La Casona", "123456789", "Calle 123", "+573001234567",
                "https://example.com/logo.png", 10L));
        PlatoController controller = new PlatoController(new PlatoRepository(), restaurantes);

        PlatoResponseDTO creado = controller.crearPlato(
                new PlatoRequestDTO("Pizza", 18000, "Margarita",
                        "https://example.com/pizza.png", "Pizzas", restaurante.getId()),
                new RolAutenticado(10L, "PROPIETARIO"));

        Plato original = creado.getPlato();
        PlatoResponseDTO actualizado = controller.modificarPlato(
                original.getId(),
                new PlatoUpdateRequestDTO(20000, "Margarita familiar"),
                new RolAutenticado(10L, "PROPIETARIO"));

        assertEquals("Plato modificado", actualizado.getMessage());
        assertEquals(20000, actualizado.getPlato().getPrecio());
        assertEquals("Margarita familiar", actualizado.getPlato().getDescripcion());
        assertEquals("Pizza", actualizado.getPlato().getNombre());
        assertEquals("Pizzas", actualizado.getPlato().getCategoria());
    }

    @Test
    void crearPlato_sinAutenticacion_rechazaElAcceso() {
        RestauranteRepository restaurantes = new RestauranteRepository();
        Restaurante restaurante = restaurantes.save(new Restaurante(
                "La Casona", "123456789", "Calle 123", "+573001234567",
                "https://example.com/logo.png", 10L));
        PlatoController controller = new PlatoController(new PlatoRepository(), restaurantes);

        assertThrows(SecurityException.class, () -> controller.crearPlato(
                new PlatoRequestDTO("Pizza", 18000, "Margarita", "imagen", "Pizzas", restaurante.getId()),
                null));
    }
}
