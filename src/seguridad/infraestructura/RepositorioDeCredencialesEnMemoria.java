package seguridad.infraestructura;

import model.Propietario;
import org.mindrot.BCrypt;
import repository.PropietarioRepository;
import seguridad.aplicacion.RepositorioDeCredenciales;
import seguridad.dominio.Credencial;
import seguridad.dominio.Rol;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia para credenciales.
 *
 * Une dos fuentes:
 *   1. Los propietarios creados por HU-01 (su clave ya viene cifrada con bcrypt).
 *   2. Las cuentas ADMINISTRADOR, que HU-01 no crea porque el enunciado dice que
 *      el administrador ya existe en el sistema.
 *
 * Cuando el equipo conecte Supabase, se reemplaza esta clase por un adaptador
 * SQL sin tocar el caso de uso: el puerto RepositorioDeCredenciales no cambia.
 */
public class RepositorioDeCredencialesEnMemoria implements RepositorioDeCredenciales {

    private final PropietarioRepository propietarioRepository;
    private final List<Credencial> credencialesAdicionales = new ArrayList<>();

    public RepositorioDeCredencialesEnMemoria(PropietarioRepository propietarioRepository) {
        this.propietarioRepository = propietarioRepository;
    }

    /**
     * Registra una cuenta que no proviene de HU-01 (tipicamente el ADMINISTRADOR).
     * Recibe la clave en texto plano y la cifra aqui: en memoria solo queda el hash.
     */
    public RepositorioDeCredencialesEnMemoria registrar(Long id, String correo, String clave, Rol rol) {
        credencialesAdicionales.add(
                new Credencial(id, correo, BCrypt.hashpw(clave, BCrypt.gensalt()), rol));
        return this;
    }

    /**
     * Siembra el ADMINISTRADOR para la demo.
     *
     * Lee ADMIN_CORREO y ADMIN_CLAVE del entorno. Si no estan definidas genera
     * una clave aleatoria y la imprime por consola: asi el servidor arranca para
     * la sustentacion sin que quede NINGUNA clave escrita en el repositorio.
     */
    public RepositorioDeCredencialesEnMemoria sembrarAdministrador() {
        String correo = System.getenv("ADMIN_CORREO");
        if (correo == null || correo.isBlank()) {
            correo = "admin@plazoleta.com";
        }
        String clave = System.getenv("ADMIN_CLAVE");
        if (clave == null || clave.isBlank()) {
            byte[] aleatorio = new byte[12];
            new SecureRandom().nextBytes(aleatorio);
            clave = Base64.getUrlEncoder().withoutPadding().encodeToString(aleatorio);
            System.out.println("[seguridad] Administrador de demo -> correo: " + correo
                    + " | clave generada: " + clave);
            System.out.println("[seguridad] Defina ADMIN_CORREO y ADMIN_CLAVE para fijarlas.");
        }
        return registrar(1L, correo, clave, Rol.ADMINISTRADOR);
    }

    @Override
    public Optional<Credencial> buscarPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return Optional.empty();
        }

        Optional<Credencial> adicional = credencialesAdicionales.stream()
                .filter(c -> c.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
        if (adicional.isPresent()) {
            return adicional;
        }

        return propietarioRepository.findByCorreo(correo).map(this::aCredencial);
    }

    private Credencial aCredencial(Propietario propietario) {
        return new Credencial(
                propietario.getId(),
                propietario.getCorreo(),
                propietario.getClave(),   // ya es el hash bcrypt que guardo HU-01
                Rol.desdeTexto(propietario.getRol()));
    }
}
