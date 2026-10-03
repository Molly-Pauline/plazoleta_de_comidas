package main.java.com.plazoleta.service;

import com.plazoleta.dto.request.RestauranteRequest;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.dto.response.RestauranteListadoResponse;
import org.springframework.data.domain.Page;

/** Contrato de operaciones disponibles para el dominio de restaurantes. */
public interface RestauranteService {
    /** Registra un restaurante luego de sus validaciones de unicidad y propietario. */
    Restaurante crear(RestauranteRequest request);

    /** Devuelve un listado paginado de restaurantes. */
    Page<RestauranteListadoResponse> listar(int page, int size);

    /** Comprueba si el ID de propietario coincide con el propietario persistido del restaurante. */
    boolean perteneceAPropietario(Long idRestaurante, Long idPropietario);
}