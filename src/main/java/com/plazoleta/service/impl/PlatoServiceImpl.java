package main.java.com.plazoleta.service.impl;

import com.plazoleta.entity.Plato;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.dto.request.PlatoRequest;
import com.plazoleta.dto.request.PlatoUpdateRequest;
import com.plazoleta.dto.request.PlatoEstadoRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PlatoService;
import com.plazoleta.dto.response.PlatoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aplica reglas de propiedad, actualización limitada y consulta del menú de platos. */
/** Aplica reglas de propiedad, actualización limitada y consulta del menú de platos. */
@Service
public class PlatoServiceImpl implements PlatoService {

    /** Persiste platos y ejecuta consultas paginadas del menú. */
    private final PlatoRepository platoRepository;
    /** Comprueba la existencia del restaurante antes de operar sobre él. */
    private final RestauranteRepository restauranteRepository;

    /** Inyecta los repositorios necesarios para aplicar las reglas de platos. */
    public PlatoServiceImpl(PlatoRepository platoRepository, RestauranteRepository restauranteRepository) {
        this.platoRepository = platoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    /** Crea un plato únicamente cuando el propietario autenticado controla el restaurante (HU3). */
    @Transactional
    public Plato crear(PlatoRequest request, AuthenticatedUser user) {
        // Se carga la entidad para no confiar en un identificador de restaurante inexistente.
        Restaurante restaurante = restauranteRepository.findById(request.idRestaurante())
                .orElseThrow(() -> new DomainException(HttpStatus.BAD_REQUEST, "El restaurante no existe"));
        // La regla de pertenencia se evalúa en el servicio, además del rol exigido por la ruta HTTP.
        verificarPertenencia(restaurante, user);
        // Los datos permitidos por HU3 se aplican a una entidad nueva ligada al restaurante autorizado.
        return platoRepository.save(new Plato(request.nombre(), request.precio(), request.descripcion(),
                request.urlImagen(), request.categoria(), restaurante));
    }

    /** Actualiza solo precio y descripción; nombre, categoría e imagen permanecen intactos (HU4). */
    @Transactional
    public Plato modificar(Long idPlato, PlatoUpdateRequest request, AuthenticatedUser user) {
        // La propiedad se obtiene desde la relación persistida del plato, no desde datos enviados por el cliente.
        Plato plato = platoRepository.findById(idPlato)
                .orElseThrow(() -> new DomainException(HttpStatus.BAD_REQUEST, "El plato no existe"));
        verificarPertenencia(plato.getRestaurante(), user);
        // La entidad limita explícitamente la mutación a los dos campos autorizados por HU4.
        plato.actualizarPrecioYDescripcion(request.precio(), request.descripcion());
        return platoRepository.save(plato);
    }

    /** Activa o desactiva un plato del restaurante del propietario autenticado (HU7). */
    @Transactional
    public Plato cambiarEstado(Long idPlato, PlatoEstadoRequest request, AuthenticatedUser user) {
        // Resolver el plato permite derivar su restaurante y proteger la pertenencia del recurso concreto.
        Plato plato = platoRepository.findById(idPlato)
                .orElseThrow(() -> new DomainException(HttpStatus.BAD_REQUEST, "El plato no existe"));
        verificarPertenencia(plato.getRestaurante(), user);
        // Se conserva el plato y se cambia solo su disponibilidad mediante la regla de dominio.
        plato.cambiarEstado(request.activo());
        return platoRepository.save(plato);
    }

    /** Devuelve el menú visible, opcionalmente filtrado por categoría y dividido en páginas (HU10). */
    @Transactional(readOnly = true)
    public Page<PlatoResponse> listarMenu(Long idRestaurante, String categoria, int page, int size) {
        // La cota superior y el mínimo positivo evitan parámetros de paginación inválidos o costosos.
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Paginacion invalida: size debe estar entre 1 y 100");
        }
        // Diferencia un restaurante inexistente de un menú existente que simplemente no tiene platos.
        if (!restauranteRepository.existsById(idRestaurante)) {
            throw new DomainException(HttpStatus.NOT_FOUND, "El restaurante no existe");
        }
        // El filtro vacío se normaliza a null para que la consulta omita la condición de categoría.
        String categoryFilter = categoria == null || categoria.isBlank() ? null : categoria.trim();
        // El repositorio filtra activos y por categoría; la capa de servicio transforma entidades en respuestas.
        return platoRepository.listarMenu(idRestaurante, categoryFilter, PageRequest.of(page, size))
                .map(PlatoResponse::from);
    }

    /** Impide que un propietario modifique platos pertenecientes a otro propietario. */
    private void verificarPertenencia(Restaurante restaurante, AuthenticatedUser user) {
        // Se compara con la identidad firmada; un ID de propietario recibido en el cuerpo no concede acceso.
        if (!restaurante.getIdPropietario().equals(user.idUsuario())) {
            throw new DomainException(HttpStatus.FORBIDDEN,
                    "El restaurante no pertenece al propietario autenticado");
        }
    }
}