package repository;

import model.Restaurante;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestauranteRepository {
    private final List<Restaurante> restaurantes = new ArrayList<>();

    public Restaurante save(Restaurante restaurante) {
        restaurantes.add(restaurante);
        return restaurante;
    }

    public boolean existsByNit(String nit) {
        return restaurantes.stream().anyMatch(r -> r.getNit().equals(nit));
    }

    public Optional<Restaurante> findByNit(String nit) {
        return restaurantes.stream().filter(r -> r.getNit().equals(nit)).findFirst();
    }

    public List<Restaurante> findAll() {
        return new ArrayList<>(restaurantes);
    }
}
