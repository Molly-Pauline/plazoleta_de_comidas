package seguridad;

import config.FabricaDeSeguridad;
import model.Propietario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seguridad.aplicacion.ResultadoLogin;
import seguridad.aplicacion.ServicioDeAutenticacion;
import seguridad.dominio.CredencialesInvalidasException;
import seguridad.dominio.Rol;
import service.PropietarioService;
import service.RolAutenticado;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** HU-05. Caso de uso de login. */
class ServicioDeAutenticacionTest {

    private FabricaDeSeguridad seguridad;
    private ServicioDeAutenticacion autenticacion;

    @BeforeEach
    void prepararEscenario() {
        seguridad = FabricaDeSeguridad.paraPruebas("secreto-de-pruebas-sprint-1")
                .conCuenta(1L, "admin@plazoleta.com", "Admin12345", Rol.ADMINISTRADOR);
        autenticacion = seguridad.getServicioDeAutenticacion();
    }

    @Test
    void loginConCredencialesValidas_devuelveTokenYRol() {
        ResultadoLogin resultado = autenticacion.login("admin@plazoleta.com", "Admin12345");

        assertNotNull(resultado.getToken());
        assertEquals(Rol.ADMINISTRADOR, resultado.getRol());
        assertEquals(1L, resultado.getIdUsuario());
        assertTrue(resultado.getExpiraEnSegundos() > 0);
    }

    @Test
    void loginNoDevuelveLaClaveNiElHash() {
        ResultadoLogin resultado = autenticacion.login("admin@plazoleta.com", "Admin12345");

        assertFalse(resultado.getToken().contains("Admin12345"));
        assertFalse(resultado.getToken().contains("$2a$"));
    }

    @Test
    void claveIncorrectaYCorreoInexistente_danElMismoMensaje() {
        CredencialesInvalidasException claveMala = assertThrows(
                CredencialesInvalidasException.class,
                () -> autenticacion.login("admin@plazoleta.com", "equivocada"));
        CredencialesInvalidasException correoMalo = assertThrows(
                CredencialesInvalidasException.class,
                () -> autenticacion.login("nadie@plazoleta.com", "Admin12345"));

        // No debe poderse deducir que correos estan registrados.
        assertEquals(claveMala.getMessage(), correoMalo.getMessage());
        assertEquals("Credenciales invalidas", claveMala.getMessage());
    }

    @Test
    void camposVacios_seRechazan() {
        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.login(null, "x"));
        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.login("a@b.com", null));
        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.login("  ", "  "));
    }

    @Test
    void intentosIlimitados_noSeBloqueaLaCuenta() {
        for (int i = 0; i < 10; i++) {
            assertThrows(CredencialesInvalidasException.class,
                    () -> autenticacion.login("admin@plazoleta.com", "equivocada"));
        }
        // La HU no pide bloqueo por intentos: la cuenta sigue sirviendo.
        assertNotNull(autenticacion.login("admin@plazoleta.com", "Admin12345").getToken());
    }

    @Test
    void propietarioCreadoEnHU01_puedeAutenticarse() {
        new PropietarioService(seguridad.getPropietarioRepository())
                .crearPropietario(new Propietario("Ana", "Garcia", "12345678", "+573001112233",
                        LocalDate.of(1990, 5, 20), "ana@correo.com", "ClaveSegura123"), "ADMINISTRADOR");

        ResultadoLogin resultado = autenticacion.login("ana@correo.com", "ClaveSegura123");

        assertEquals(Rol.PROPIETARIO, resultado.getRol());
    }

    @Test
    void autenticarResuelveLaIdentidadDesdeElEncabezado() {
        String token = autenticacion.login("admin@plazoleta.com", "Admin12345").getToken();

        RolAutenticado identidad = autenticacion.autenticar("Bearer " + token);

        assertEquals("ADMINISTRADOR", identidad.getRol());
        assertEquals(1L, identidad.getIdUsuario());
    }

    @Test
    void encabezadoAusenteOMalFormado_seRechaza() {
        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.autenticar(null));
        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.autenticar(""));
        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.autenticar("Basic abc"));
    }
}
