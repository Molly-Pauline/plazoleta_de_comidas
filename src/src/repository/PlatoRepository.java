package repository;

import model.Plato;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlatoRepository {
    private final List<Plato> platos = new ArrayList<>();
    private long nextId = 1L;

    public Plato save(Plato plato) {
        if (plato.getId() == null) {
            plato.setId(nextId++);
        }
        platos.add(plato);
        return plato;
    }

    public Optional<Plato> findById(Long id) {
        return platos.stream().filter(plato -> plato.getId().equals(id)).findFirst();
    }

    public Plato update(Plato plato) {
        if (plato == null || plato.getId() == null) {
            throw new IllegalArgumentException("El plato a actualizar es inválido");
        }

        for (int i = 0; i < platos.size(); i++) {
            if (platos.get(i).getId().equals(plato.getId())) {
                platos.set(i, plato);
                return plato;
            }
        }

        throw new IllegalArgumentException("El plato no existe");
    }

    public List<Plato> findAll() {
        return new ArrayList<>(platos);
    }
}
