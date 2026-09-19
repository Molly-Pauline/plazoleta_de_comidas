package seguridad.aplicacion;

/**
 * Puerto de salida: verificacion de clave contra su hash.
 * Aisla bcrypt del caso de uso para poder probarlo sin costo de CPU.
 */
public interface VerificadorDeClave {
    boolean coincide(String claveEnTextoPlano, String hashAlmacenado);
}
