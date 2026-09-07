package seguridad;

import org.junit.jupiter.api.Test;
import seguridad.dominio.Credencial;
import seguridad.dominio.CredencialesInvalidasException;
import seguridad.dominio.Rol;
import seguridad.infraestructura.ProveedorDeTokenHmac;
import service.RolAutenticado;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-05. Estas pruebas cubren el nucleo de la historia: que el cliente no pueda
 * decidir su propio rol.
 */
class ProveedorDeTokenHmacTest {

    private static final String SECRETO = "secreto-de-pruebas-sprint-1";

    private final ProveedorDeTokenHmac proveedor = new ProveedorDeTokenHmac(SECRETO);
    private final Credencial credencial =
            new Credencial(7L, "ana@correo.com", "hash-irrelevante", Rol.PROPIETARIO);

    @Test
    void tokenEmitido_seVerificaYDevuelveIdentidad() {
        RolAutenticado identidad = proveedor.verificar(proveedor.emitir(credencial));

        assertEquals(7L, identidad.getIdUsuario());
        assertEquals("PROPIETARIO", identidad.getRol());
    }

    @Test
    void tokenConFirmaAlterada_seRechaza() {
        String token = proveedor.emitir(credencial);

        assertThrows(CredencialesInvalidasException.class,
                () -> proveedor.verificar(token + "x"));
    }

    @Test
    void tokenConRolManipulado_seRechaza() {
        String token = proveedor.emitir(credencial);
        int punto = token.indexOf('.');
        String cuerpo = new String(Base64.getUrlDecoder().decode(token.substring(0, punto)));
        String alterado = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(cuerpo.replace("PROPIETARIO", "ADMINISTRADOR").getBytes());

        // La firma sigue siendo la original, pero ya no corresponde al cuerpo.
        assertThrows(CredencialesInvalidasException.class,
                () -> proveedor.verificar(alterado + token.substring(punto)));
    }

    @Test
    void tokenFirmadoConOtroSecreto_seRechaza() {
        String ajeno = new ProveedorDeTokenHmac("otro-secreto-completamente-distinto")
                .emitir(credencial);

        assertThrows(CredencialesInvalidasException.class, () -> proveedor.verificar(ajeno));
    }

    @Test
    void tokenVencido_seRechaza() {
        // Vigencia negativa: nace vencido.
        String vencido = new ProveedorDeTokenHmac(SECRETO, -1).emitir(credencial);

        CredencialesInvalidasException ex = assertThrows(
                CredencialesInvalidasException.class, () -> proveedor.verificar(vencido));
        assertEquals("Credencial vencida", ex.getMessage());
    }

    @Test
    void tokenAusenteOVacio_seRechaza() {
        assertThrows(CredencialesInvalidasException.class, () -> proveedor.verificar(null));
        assertThrows(CredencialesInvalidasException.class, () -> proveedor.verificar("   "));
        assertThrows(CredencialesInvalidasException.class, () -> proveedor.verificar("sin-punto"));
    }

    @Test
    void secretoDemasiadoCorto_noSePermite() {
        assertThrows(IllegalArgumentException.class, () -> new ProveedorDeTokenHmac("corto"));
    }
}
