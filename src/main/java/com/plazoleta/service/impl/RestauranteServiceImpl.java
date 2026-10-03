package main.java.com.plazoleta.service.impl;

import com.plazoleta.entity.Restaurante;
import com.plazoleta.dto.request.RestauranteRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.service.RestauranteService;
import com.plazoleta.service.UsuarioValidationPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.plazoleta.dto.response.RestauranteListadoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** Implementa creación, listado y validación de pertenencia de restaurantes. */
@Service
public class RestauranteServiceImpl implements RestauranteService {

    /** Persistencia local de las entidades Restaurante. */
    private final RestauranteRepository restauranteRepository;
    /** Puerto que valida en Usuarios que el propietario exista y tenga el rol requerido por HU2. */
    private final UsuarioValidationPort usuarioValidationPort;

    /** Inyecta la persistencia y el puerto hacia el servicio Usuarios. */
    public RestauranteServiceImpl(RestauranteRepository restauranteRepository, UsuarioValidationPort usuarioValidationPort) {
        this.restauranteRepository = restauranteRepository;
        this.usuarioValidationPort = usuarioValidationPort;
    }

    /** Crea un restaurante solo después de descartar NIT duplicado y validar al propietario en Usuarios (HU2). */
    @Transactional
    public Restaurante crear(RestauranteRequest request) {
        // El NIT es identificador único; se rechaza antes de hacer una llamada de red innecesaria.
        if (restauranteRepository.existsByNit(request.nit())) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Ya existe un restaurante con ese NIT");
        }
        // La base de usuarios es propiedad del otro microservicio, por eso la comprobación cruza su API.
        if (!usuarioValidationPort.existePropietarioValido(request.idPropietario())) {
            throw new DomainException(HttpStatus.BAD_REQUEST,
                    "El propietario asociado no existe o no tiene rol PROPIETARIO");
        }
        // Se guardan solo los datos validados; el ID propietario se conserva como referencia entre servicios.
        Restaurante restaurante = new Restaurante(request.nombre(), request.nit(), request.direccion(),
                request.telefono(), request.urlLogo(), request.idPropietario());
        return restauranteRepository.save(restaurante);
    }

    /** Lista restaurantes ordenados por nombre y con tamaño de página limitado. */
    @Transactional(readOnly = true)
    public Page<RestauranteListadoResponse> listar(int page, int size) {
        // La paginación inválida o con más de 100 elementos se rechaza antes de consultar la base.
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Paginacion invalida: size debe estar entre 1 y 100");
        }
        // Orden estable por nombre y transformación a una respuesta reducida de nombre y logo (HU9).
        return restauranteRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre")))
                .map(RestauranteListadoResponse::from);
    }

    /** Comprueba la relación restaurante-propietario sin filtrar excepciones por ID inexistente. */
    @Transactional(readOnly = true)
    public boolean perteneceAPropietario(Long idRestaurante, Long idPropietario) {
        // Si existe, compara la referencia guardada; si no, responde false para no afirmar propiedad.
        return restauranteRepository.findById(idRestaurante)
                .map(restaurante -> restaurante.getIdPropietario().equals(idPropietario))
                .orElse(false);
    }
}