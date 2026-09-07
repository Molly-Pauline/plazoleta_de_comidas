package repository;
import model.Propietario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

// Guarda propietarios en memoria (solo guarda, no valida)
public class PropietarioRepository {
    private List<Propietario> propietarios = new ArrayList<>();
    // HU-05: el id empieza en 2 porque el 1 queda reservado al ADMINISTRADOR sembrado.
    private final AtomicLong secuencia = new AtomicLong(1);

    public Propietario save(Propietario p) {
        if (p.getId() == null) {
            p.setId(secuencia.incrementAndGet());
        }
        propietarios.add(p);
        return p;
    }

    public Optional<Propietario> findById(Long id) {
        return propietarios.stream().filter(x -> id != null && id.equals(x.getId())).findFirst();
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
