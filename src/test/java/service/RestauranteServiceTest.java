package service;

import model.Restaurante;
import org.junit.jupiter.api.Test;
import repository.RestauranteRepository;

import static org.junit.jupiter.api.Assertions.*;

class RestauranteServiceTest {

    private static class UsuarioValidationStub implements UsuarioValidationPort {
        private final boolean propietarioValido;

        private UsuarioValidationStub(boolean propietarioValido) {
            this.propietarioValido = propietarioValido;
        }

        @Override
        public boolean existePropietarioValido(Long idPropietario) {
            return propietarioValido;
        }
    }

    @Test
    void crearRestaurante_conDatosValidos_yRegistraElRestaurante() {
        RestauranteRepository repository = new RestauranteRepository();
        RestauranteService service = new RestauranteService(repository, new UsuarioValidationStub(true));

        Restaurante restaurante = new Restaurante(
                "La Casona",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        Restaurante guardado = service.crearRestaurante(restaurante, "ADMINISTRADOR");

        assertEquals("La Casona", guardado.getNombre());
        assertEquals("123456789", guardado.getNit());
        assertEquals(10L, guardado.getIdPropietario());
        assertTrue(repository.existsByNit("123456789"));
    }

    @Test
    void crearRestaurante_sinPermisoAdministrador_lanzaSecurityException() {
        RestauranteRepository repository = new RestauranteRepository();
        RestauranteService service = new RestauranteService(repository, new UsuarioValidationStub(true));

        Restaurante restaurante = new Restaurante(
                "La Casona",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        SecurityException exception = assertThrows(SecurityException.class,
                () -> service.crearRestaurante(restaurante, "CLIENTE"));

        assertEquals("Solo ADMINISTRADOR puede crear restaurante", exception.getMessage());
    }

    @Test
    void crearRestaurante_conNitInvalido_lanzaIllegalArgumentException() {
        RestauranteRepository repository = new RestauranteRepository();
        RestauranteService service = new RestauranteService(repository, new UsuarioValidationStub(true));

        Restaurante restaurante = new Restaurante(
                "La Casona",
                "12A456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearRestaurante(restaurante, "ADMINISTRADOR"));

        assertEquals("NIT debe contener únicamente números", exception.getMessage());
    }

    @Test
    void crearRestaurante_conTelefonoLargo_lanzaIllegalArgumentException() {
        RestauranteRepository repository = new RestauranteRepository();
        RestauranteService service = new RestauranteService(repository, new UsuarioValidationStub(true));

        Restaurante restaurante = new Restaurante(
                "La Casona",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567890",
                "https://example.com/logo.png",
                10L
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearRestaurante(restaurante, "ADMINISTRADOR"));

        assertEquals("Teléfono máximo 13 caracteres y puede contener +", exception.getMessage());
    }

    @Test
    void crearRestaurante_conNombreSoloNumeros_lanzaIllegalArgumentException() {
        RestauranteRepository repository = new RestauranteRepository();
        RestauranteService service = new RestauranteService(repository, new UsuarioValidationStub(true));

        Restaurante restaurante = new Restaurante(
                "12345",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearRestaurante(restaurante, "ADMINISTRADOR"));

        assertEquals("El nombre no puede estar compuesto solo por números", exception.getMessage());
    }

    @Test
    void crearRestaurante_conPropietarioInvalido_lanzaIllegalArgumentException() {
        RestauranteRepository repository = new RestauranteRepository();
        RestauranteService service = new RestauranteService(repository, new UsuarioValidationStub(false));

        Restaurante restaurante = new Restaurante(
                "La Casona",
                "123456789",
                "Calle 123 #45-67",
                "+573001234567",
                "https://example.com/logo.png",
                10L
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearRestaurante(restaurante, "ADMINISTRADOR"));

        assertEquals("El propietario asociado no existe o no tiene rol PROPIETARIO", exception.getMessage());
    }
}
