package seguridad;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import seguridad.aplicacion.Operacion;
import seguridad.aplicacion.PoliticaDeAutorizacion;
import seguridad.dominio.AccesoDenegadoException;
import seguridad.dominio.Rol;
import service.RolAutenticado;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba parametrizada de la matriz de autorizacion completa del Sprint 1
 * (documento maestro, seccion 13 y guia del Roll 2, seccion 10).
 */
class PoliticaDeAutorizacionTest {

    private final PoliticaDeAutorizacion politica = new PoliticaDeAutorizacion();

    @ParameterizedTest(name = "{0} sobre {1} -> permitido={2}")
    @CsvSource({
            "ADMINISTRADOR, CREAR_PROPIETARIO, true",
            "PROPIETARIO,   CREAR_PROPIETARIO, false",
            "EMPLEADO,      CREAR_PROPIETARIO, false",
            "CLIENTE,       CREAR_PROPIETARIO, false",

            "ADMINISTRADOR, CREAR_RESTAURANTE, true",
            "PROPIETARIO,   CREAR_RESTAURANTE, false",
            "EMPLEADO,      CREAR_RESTAURANTE, false",
            "CLIENTE,       CREAR_RESTAURANTE, false",

            "ADMINISTRADOR, CREAR_PLATO,       false",
            "PROPIETARIO,   CREAR_PLATO,       true",
            "EMPLEADO,      CREAR_PLATO,       false",
            "CLIENTE,       CREAR_PLATO,       false",

            "ADMINISTRADOR, MODIFICAR_PLATO,   false",
            "PROPIETARIO,   MODIFICAR_PLATO,   true",
            "EMPLEADO,      MODIFICAR_PLATO,   false",
            "CLIENTE,       MODIFICAR_PLATO,   false"
    })
    void matrizDeAutorizacion(Rol rol, Operacion operacion, boolean esperado) {
        assertEquals(esperado, politica.permite(rol, operacion));
    }

    @Test
    void sinUsuarioAutenticado_seDeniega() {
        assertThrows(AccesoDenegadoException.class,
                () -> politica.exigir(null, Operacion.CREAR_RESTAURANTE));
    }

    @Test
    void rolNoReconocido_seDeniega() {
        assertThrows(AccesoDenegadoException.class,
                () -> politica.exigir(new RolAutenticado(1L, "SUPERUSUARIO"), Operacion.CREAR_RESTAURANTE));
    }

    @Test
    void rolCorrecto_noLanza() {
        assertDoesNotThrow(() ->
                politica.exigir(new RolAutenticado(1L, "ADMINISTRADOR"), Operacion.CREAR_RESTAURANTE));
    }
}
