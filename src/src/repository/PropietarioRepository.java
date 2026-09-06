package repository;
import model.Propietario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Guarda propietarios en memoria (solo guarda, no valida)
public class PropietarioRepository {
    private List<Propietario> propietarios = new ArrayList<>();

    public Propietario save(Propietario p) {
        propietarios.add(p);
        return p;
    }

    public Optional<Propietario> findByCorreo(String correo) {
        return propietarios.stream().filter(x -> x.getCorreo().equalsIgnoreCase(correo)).findFirst();
    }

    public Optional<Propietario> findByDocumento(String documento) {
        return propietarios.stream().filter(x -> x.getDocumentoDeIdentidad().equals(documento)).findFirst();
    }

    public boolean existsByCorreo(String correo) { return findByCorreo(correo).isPresent(); }
    public boolean existsByDocumento(String documento) { return findByDocumento(documento).isPresent(); }
    public List<Propietario> findAll() { return new ArrayList<>(propietarios); }
}
