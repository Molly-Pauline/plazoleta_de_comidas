package main.java.com.plazoleta.repository;

import com.plazoleta.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

/** Operaciones CRUD de restaurantes con búsqueda derivada por el NIT único. */
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    /** Consulta existencia para rechazar un NIT duplicado antes de insertar. */
    boolean existsByNit(String nit);
}