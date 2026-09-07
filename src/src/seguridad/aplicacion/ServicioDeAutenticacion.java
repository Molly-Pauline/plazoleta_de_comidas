package seguridad.aplicacion;

import seguridad.dominio.Credencial;
import seguridad.dominio.CredencialesInvalidasException;
import service.RolAutenticado;

import java.util.Optional;

/**
 * Caso de uso de HU-05: iniciar sesion y resolver la identidad de una peticion.
 *
 * Reglas que implementa:
 *  - Login con correo y clave.
 *  - La clave se verifica contra el hash bcrypt, nunca en texto plano.
 *  - Intentos ilimitados (la HU no pide bloqueo por intentos).
 *  - Mensaje UNICO para correo inexistente y clave incorrecta.
 *  - La respuesta jamas incluye clave ni hash.
 */
public class ServicioDeAutenticacion {

    /** Mensaje unico: no revela si el correo existe. */
    private static final String MENSAJE_UNICO = "Credenciales invalidas";

    private final RepositorioDeCredenciales repositorio;
    private final VerificadorDeClave verificadorDeClave;
    private final ProveedorDeToken proveedorDeToken;

    public ServicioDeAutenticacion(RepositorioDeCredenciales repositorio,
                                   VerificadorDeClave verificadorDeClave,
                                   ProveedorDeToken proveedorDeToken) {
        this.repositorio = repositorio;
        this.verificadorDeClave = verificadorDeClave;
        this.proveedorDeToken = proveedorDeToken;
    }

    public ResultadoLogin login(String correo, String clave) {
        if (correo == null || correo.isBlank() || clave == null || clave.isBlank()) {
            throw new CredencialesInvalidasException(MENSAJE_UNICO);
        }

        Optional<Credencial> encontrada = repositorio.buscarPorCorreo(correo.trim());
        if (encontrada.isEmpty()) {
            throw new CredencialesInvalidasException(MENSAJE_UNICO);
        }

        Credencial credencial = encontrada.get();
        if (!verificadorDeClave.coincide(clave, credencial.getClaveHash())) {
            throw new CredencialesInvalidasException(MENSAJE_UNICO);
        }

        String token = proveedorDeToken.emitir(credencial);
        return new ResultadoLogin(
                token,
                credencial.getIdUsuario(),
                credencial.getRol(),
                ProveedorDeTokenVigencia.SEGUNDOS);
    }

    /**
     * Resuelve la identidad de una peticion a partir del encabezado
     * "Authorization: Bearer &lt;token&gt;".
     */
    public RolAutenticado autenticar(String encabezadoAuthorization) {
        if (encabezadoAuthorization == null || encabezadoAuthorization.isBlank()) {
            throw new CredencialesInvalidasException("No autenticado");
        }
        String valor = encabezadoAuthorization.trim();
        if (!valor.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new CredencialesInvalidasException("No autenticado");
        }
        return proveedorDeToken.verificar(valor.substring(7).trim());
    }
}
