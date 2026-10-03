package main.java.com.plazoleta.service;

import com.plazoleta.dto.request.PlatoRequest;
import com.plazoleta.dto.request.PlatoUpdateRequest;
import com.plazoleta.dto.request.PlatoEstadoRequest;
import com.plazoleta.entity.Plato;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.dto.response.PlatoResponse;
import org.springframework.data.domain.Page;

/** Contrato de creación, actualización, disponibilidad y consulta de platos. */
public interface PlatoService {
    /** Crea un plato dentro de un restaurante que pertenezca al usuario autenticado. */
    Plato crear(PlatoRequest request, AuthenticatedUser user);

    /** Cambia exclusivamente precio y descripción de un plato autorizado. */
    Plato modificar(Long idPlato, PlatoUpdateRequest request, AuthenticatedUser user);

    /** Cambia la disponibilidad de un plato autorizado. */
    Plato cambiarEstado(Long idPlato, PlatoEstadoRequest request, AuthenticatedUser user);

    /** Lista el menú activo con filtro opcional de categoría y paginación. */
    Page<PlatoResponse> listarMenu(Long idRestaurante, String categoria, int page, int size);
}