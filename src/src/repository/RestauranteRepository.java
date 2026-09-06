package repository;

import model.Restaurante;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestauranteRepository {
    private final List<Restaurante> restaurantes = new ArrayList<>();
    private long nextId = 1L;

    public Restaurante save(Restaurante restaurante) {
        if (restaurante.getId() == null) {
            restaurante.setId(nextId++);
        }
        restaurantes.add(restaurante);
        return restaurante;
    }

    public boolean existsByNit(String nit) {
        return restaurantes.stream().anyMatch(r -> r.getNit().equals(nit));
    }

    public Optional<Restaurante> findByNit(String nit) {
        return restaurantes.stream().filter(r -> r.getNit().equals(nit)).findFirst();
    }

    public Optional<Restaurante> findById(Long id) {
        return restaurantes.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public List<Restaurante> findAll() {
        return new ArrayList<>(restaurantes);
    }
}
