package service;
import model.Propietario;
import repository.PropietarioRepository;
import org.mindrot.BCrypt;
import java.time.LocalDate;
import java.time.Period;

// Valida y crea propietario - HU1 + autorizacion HU5
public class PropietarioService {
    private PropietarioRepository repository;

    public PropietarioService(PropietarioRepository repository) {
        this.repository = repository;
    }

    // Solo ADMINISTRADOR puede crear propietario
    public Propietario crearPropietario(Propietario p, String rolAutenticado) {
        if (!"ADMINISTRADOR".equals(rolAutenticado)) {
            throw new SecurityException("Solo ADMINISTRADOR puede crear propietario");
        }
        if (p.getNombre() == null || p.getNombre().isBlank() ||
                p.getApellido() == null || p.getApellido().isBlank() ||
                p.getDocumentoDeIdentidad() == null || p.getDocumentoDeIdentidad().isBlank() ||
                p.getCelular() == null || p.getCelular().isBlank() ||
                p.getFechaNacimiento() == null ||
                p.getCorreo() == null || p.getCorreo().isBlank() ||
                p.getClave() == null || p.getClave().isBlank()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios: Nombre, Apellido, DocumentoDeIdentidad, celular, fechaNacimiento, correo y clave");
        }
        if (!p.getCorreo().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Estructura de email no válida");
        }
        if (!p.getCelular().matches("^\\+?\\d{1,13}$") || p.getCelular().length() > 13) {
            throw new IllegalArgumentException("Teléfono máximo 13 caracteres y puede contener + Ej: +573005698325");
        }
        if (!p.getDocumentoDeIdentidad().matches("^\\d+$")) {
            throw new IllegalArgumentException("Documento de identidad debe ser únicamente numérico");
        }
        if (Period.between(p.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Usuario debe ser mayor de edad (18 años o más)");
        }
        if (repository.existsByCorreo(p.getCorreo())) throw new IllegalArgumentException("Correo ya registrado");
        if (repository.existsByDocumento(p.getDocumentoDeIdentidad())) throw new IllegalArgumentException("Documento ya registrado");

        p.setClave(BCrypt.hashpw(p.getClave(), BCrypt.gensalt())); // encriptada con bcrypt
        return repository.save(p);
    }
}
