package service;

import model.Propietario;
import org.junit.jupiter.api.Test;
import repository.PropietarioRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PropietarioServiceTest {

    private final PropietarioRepository repository = new PropietarioRepository();
    private final PropietarioService service = new PropietarioService(repository);

    @Test
    void crearPropietario_conDatosValidos_yRegistraAlPropietario() {
        Propietario propietario = new Propietario(
                "Ana",
                "García",
                "12345678",
                "+573001112233",
                LocalDate.of(1990, 5, 20),
                "ana@correo.com",
                "ClaveSegura123"
        );

        Propietario guardado = service.crearPropietario(propietario, "ADMINISTRADOR");

        assertNotNull(guardado);
        assertEquals("PROPIETARIO", guardado.getRol());
        assertNotEquals("ClaveSegura123", guardado.getClave());
        assertTrue(guardado.getClave().startsWith("$2a$") || guardado.getClave().startsWith("$2b$") || guardado.getClave().startsWith("$2y$"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void crearPropietario_sinPermisoAdministrador_lanzaSecurityException() {
        Propietario propietario = new Propietario(
                "Luis",
                "Pérez",
                "87654321",
                "+573004445566",
                LocalDate.of(1995, 1, 15),
                "luis@correo.com",
                "ClaveSegura123"
        );

        SecurityException exception = assertThrows(SecurityException.class,
                () -> service.crearPropietario(propietario, "CLIENTE"));

        assertEquals("Solo ADMINISTRADOR puede crear propietario", exception.getMessage());
    }

    @Test
    void crearPropietario_conEmailInvalido_lanzaIllegalArgumentException() {
        Propietario propietario = new Propietario(
                "Maria",
                "Lopez",
                "11111111",
                "+573002223334",
                LocalDate.of(1992, 2, 10),
                "correo_invalido",
                "ClaveSegura123"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearPropietario(propietario, "ADMINISTRADOR"));

        assertEquals("Estructura de email no válida", exception.getMessage());
    }

    @Test
    void crearPropietario_conDocumentoNoNumerico_lanzaIllegalArgumentException() {
        Propietario propietario = new Propietario(
                "Carlos",
                "Rojas",
                "ABC123",
                "+573003334445",
                LocalDate.of(1991, 3, 12),
                "carlos@correo.com",
                "ClaveSegura123"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearPropietario(propietario, "ADMINISTRADOR"));

        assertEquals("Documento de identidad debe ser únicamente numérico", exception.getMessage());
    }

    @Test
    void crearPropietario_conMenorDeEdad_lanzaIllegalArgumentException() {
        Propietario propietario = new Propietario(
                "Nina",
                "Mora",
                "55555555",
                "+573005556667",
                LocalDate.now().minusYears(17),
                "nina@correo.com",
                "ClaveSegura123"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearPropietario(propietario, "ADMINISTRADOR"));

        assertEquals("Usuario debe ser mayor de edad (18 años o más)", exception.getMessage());
    }

    @Test
    void crearPropietario_conCorreoDuplicado_lanzaIllegalArgumentException() {
        Propietario primero = new Propietario(
                "Pedro",
                "Restrepo",
                "10101010",
                "+573009999999",
                LocalDate.of(1988, 7, 25),
                "pedro@correo.com",
                "ClaveSegura123"
        );
        service.crearPropietario(primero, "ADMINISTRADOR");

        Propietario duplicado = new Propietario(
                "Pepe",
                "Restrepo",
                "20202020",
                "+573008888888",
                LocalDate.of(1990, 7, 25),
                "pedro@correo.com",
                "ClaveSegura123"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.crearPropietario(duplicado, "ADMINISTRADOR"));

        assertEquals("Correo ya registrado", exception.getMessage());
    }
}
