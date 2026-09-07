package seguridad.aplicacion;

import seguridad.dominio.Credencial;
import service.RolAutenticado;

/**
 * Puerto de salida: emision y verificacion de la credencial de sesion.
 *
 * Decision tecnica del equipo (los anexos no exigen JWT): token propio firmado
 * con HMAC-SHA256 usando solo el JDK, para no agregar dependencias al pom.
 */
public interface ProveedorDeToken {

    /** Emite un token firmado que transporta id de usuario, rol y vencimiento. */
    String emitir(Credencial credencial);

    /**
     * Verifica firma y vigencia y devuelve la identidad autenticada.
     * @throws seguridad.dominio.CredencialesInvalidasException si el token esta
     *         ausente, alterado o vencido.
     */
    RolAutenticado verificar(String token);
}
