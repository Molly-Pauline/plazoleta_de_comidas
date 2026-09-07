package seguridad.aplicacion;

import seguridad.dominio.Credencial;
import java.util.Optional;

/**
 * Puerto de salida: como obtiene el caso de uso las credenciales.
 * La implementacion vive en infraestructura (memoria hoy, Supabase manana).
 */
public interface RepositorioDeCredenciales {
    Optional<Credencial> buscarPorCorreo(String correo);
}
