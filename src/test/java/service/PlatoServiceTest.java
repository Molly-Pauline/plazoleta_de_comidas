package service;

import model.Plato;
import model.Restaurante;
import org.junit.jupiter.api.Test;
import repository.PlatoRepository;
import repository.RestauranteRepository;

import static org.junit.jupiter.api.Assertions.*;

class PlatoServiceTest {
    private RestauranteRepository restaurantes;
    private PlatoRepository platos;
    private PlatoService service;

    private void preparar() {
        restaurantes = new RestauranteRepository();
        platos = new PlatoRepository();
        restaurantes.save(new Restaurante(
                "La Casona", "123456789", "Calle 123", "+573001234567",
                "https://example.com/logo.png", 10L));
        service = new PlatoService(platos, restaurantes);
    }

    private Plato plato(Long idRestaurante) {
        return new Plato("Hamburguesa clásica", 25000, "Con queso",
                "https://example.com/burger.png", "Hamburguesas", idRestaurante);
    }

    @Test
    void crearPlato_valido_quedaActivoYAsociado() {
        preparar();

        Plato creado = service.crearPlato(plato(1L), new RolAutenticado(10L, "PROPIETARIO"));

        assertTrue(creado.isActivo());
        assertEquals(1L, creado.getIdRestaurante());
        assertNotNull(creado.getId());
    }

    @Test
    void crearPlato_conPrecioCero_rechazaLaSolicitud() {
        preparar();
        Plato invalido = new Plato("Hamburguesa", 0, "Descripción", "imagen", "Comida", 1L);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.crearPlato(invalido, new RolAutenticado(10L, "PROPIETARIO")));

        assertEquals("El precio debe ser un entero mayor que cero", error.getMessage());
    }

    @Test
    void crearPlato_enRestauranteAjeno_rechazaLaOperacion() {
        preparar();
        restaurantes.save(new Restaurante(
                "Otro", "987654321", "Calle 456", "+573001234568",
                "https://example.com/otro.png", 20L));

        SecurityException error = assertThrows(SecurityException.class,
                () -> service.crearPlato(plato(2L), new RolAutenticado(10L, "PROPIETARIO")));

        assertEquals("Acceso denegado: el restaurante no pertenece al propietario", error.getMessage());
    }

    @Test
    void crearPlato_conRolIncorrecto_rechazaElAcceso() {
        preparar();

        SecurityException error = assertThrows(SecurityException.class,
                () -> service.crearPlato(plato(1L), new RolAutenticado(10L, "ADMINISTRADOR")));

        assertEquals("Acceso denegado: solo PROPIETARIO puede gestionar platos", error.getMessage());
    }

    @Test
    void modificarPlato_soloCambiaPrecioYDescripcion() {
        preparar();
        Plato creado = service.crearPlato(plato(1L), new RolAutenticado(10L, "PROPIETARIO"));

        Plato modificado = service.modificarPlato(
                creado.getId(), 30000, "Con queso y tocineta",
                new RolAutenticado(10L, "PROPIETARIO"));

        assertEquals(30000, modificado.getPrecio());
        assertEquals("Con queso y tocineta", modificado.getDescripcion());
        assertEquals("Hamburguesa clásica", modificado.getNombre());
        assertEquals("Hamburguesas", modificado.getCategoria());
        assertEquals("https://example.com/burger.png", modificado.getUrlImagen());
        assertTrue(modificado.isActivo());
        assertEquals(1L, modificado.getIdRestaurante());
    }

    @Test
    void modificarPlato_ajeno_rechazaLaOperacion() {
        preparar();
        Plato creado = service.crearPlato(plato(1L), new RolAutenticado(10L, "PROPIETARIO"));

        SecurityException error = assertThrows(SecurityException.class,
                () -> service.modificarPlato(
                        creado.getId(), 30000, "Otra descripción",
                        new RolAutenticado(20L, "PROPIETARIO")));

        assertEquals("Acceso denegado: el restaurante no pertenece al propietario", error.getMessage());
    }

    @Test
    void repositorioActualizaElPlatoSinDuplicarlo() {
        preparar();
        Plato creado = service.crearPlato(plato(1L), new RolAutenticado(10L, "PROPIETARIO"));

        service.modificarPlato(
                creado.getId(), 30000, "Actualizada",
                new RolAutenticado(10L, "PROPIETARIO"));

        assertEquals(1, platos.findAll().size());
        assertEquals("Actualizada", platos.findById(creado.getId()).orElseThrow().getDescripcion());
    }
}
