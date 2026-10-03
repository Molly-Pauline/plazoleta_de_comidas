package main.java.com.plazoleta.controller;

import com.plazoleta.entity.Restaurante;
import com.plazoleta.exception.DomainException;
import com.plazoleta.dto.response.RestauranteApiResponse;
import com.plazoleta.dto.request.RestauranteRequest;
import com.plazoleta.dto.response.RestauranteResponse;
import com.plazoleta.dto.response.RestauranteListadoResponse;
import com.plazoleta.dto.response.PlatoResponse;
import com.plazoleta.service.RestauranteService;
import com.plazoleta.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import com.plazoleta.security.InternalServiceTokenVerifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.security.InternalServiceTokenVerifier;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Publica operaciones de restaurantes y consulta del menú de platos. */
@RestController
@RequestMapping("/restaurantes")
public class RestauranteController {

    /** Reglas de creación, listado y comprobación de propiedad de restaurantes. */
    private final RestauranteService restauranteService;
    /** Consulta el menú de platos que pertenece a un restaurante. */
    private final PlatoService platoService;
    /** Valida el secreto de servicio para el endpoint interno de comprobación de propiedad. */
    private final InternalServiceTokenVerifier internalServiceTokenVerifier;

    /** Inyecta los servicios de dominio y el verificador interno. */
    public RestauranteController(RestauranteService restauranteService, PlatoService platoService,
                                 InternalServiceTokenVerifier internalServiceTokenVerifier) {
        this.restauranteService = restauranteService;
        this.platoService = platoService;
        this.internalServiceTokenVerifier = internalServiceTokenVerifier;
    }

    /** Registra un restaurante después de validar datos y propietario en el servicio. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestauranteApiResponse crear(@Valid @RequestBody RestauranteRequest request) {
        Restaurante restaurante = restauranteService.crear(request);
        // Se transforma la entidad persistida a una respuesta estable para el contrato HTTP.
        return new RestauranteApiResponse(true, "Restaurante creado", RestauranteResponse.from(restaurante));
    }

    /** Lista restaurantes por páginas; la ordenación ascendente se define en el servicio. */
    @GetMapping
    public Page<RestauranteListadoResponse> listar(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return restauranteService.listar(page, size);
    }

    /** Devuelve el menú paginado de un restaurante, con filtro opcional por categoría. */
    @GetMapping("/{idRestaurante}/platos")
    public Page<PlatoResponse> listarMenu(@PathVariable Long idRestaurante,
                                         @RequestParam(required = false) String categoria,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return platoService.listarMenu(idRestaurante, categoria, page, size);
    }

    /** Confirma a Usuarios que el propietario del token coincide con la relación persistida del restaurante. */
    @GetMapping("/{idRestaurante}/propietario/{idPropietario}")
    public boolean perteneceAPropietario(@PathVariable Long idRestaurante, @PathVariable Long idPropietario,
                                         @AuthenticationPrincipal AuthenticatedUser user,
                                         @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {
        // Este endpoint entre servicios requiere una credencial interna además del Bearer de propietario.
        if (!internalServiceTokenVerifier.isValid(serviceToken)) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Autenticacion de servicio requerida");
        }
        // Se comprueban las tres condiciones: principal presente, ID firmado coincidente y relación persistida.
        return user != null && user.idUsuario().equals(idPropietario)
                && restauranteService.perteneceAPropietario(idRestaurante, idPropietario);
    }
}