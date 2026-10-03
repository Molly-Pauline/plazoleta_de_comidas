package main.java.com.plazoleta.controller;

import com.plazoleta.entity.Plato;
import com.plazoleta.dto.response.PlatoApiResponse;
import com.plazoleta.dto.request.PlatoRequest;
import com.plazoleta.dto.request.PlatoEstadoRequest;
import com.plazoleta.dto.response.PlatoResponse;
import com.plazoleta.dto.request.PlatoUpdateRequest;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adapta las operaciones HTTP de platos a los servicios de negocio. */
@RestController
@RequestMapping("/platos")
public class PlatoController {

    /** Servicio que aplica pertenencia del propietario y reglas HU3, HU4 y HU7. */
    private final PlatoService platoService;

    /** Recibe el servicio que ejecuta las reglas de platos. */
    public PlatoController(PlatoService platoService) {
        this.platoService = platoService;
    }

    /** Crea un plato del restaurante autorizado y responde con HTTP 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatoApiResponse crear(@Valid @RequestBody PlatoRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        // @Valid hace cumplir las restricciones de PlatoRequest antes de iniciar la operación de negocio.
        Plato plato = platoService.crear(request, user);
        // La respuesta usa un DTO para no exponer directamente la entidad JPA.
        return new PlatoApiResponse(true, "Plato creado", PlatoResponse.from(plato));
    }

    /** Modifica únicamente los campos aceptados por PlatoUpdateRequest (HU4). */
    @PutMapping("/{idPlato}")
    public PlatoApiResponse modificar(@PathVariable Long idPlato, @Valid @RequestBody PlatoUpdateRequest request,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        // El principal contiene la identidad autenticada que el servicio compara con el dueño del restaurante.
        Plato plato = platoService.modificar(idPlato, request, user);
        return new PlatoApiResponse(true, "Plato modificado", PlatoResponse.from(plato));
    }

    /** Cambia el indicador de disponibilidad del plato, sin alterar sus demás datos (HU7). */
    @PatchMapping("/{idPlato}/estado")
    public PlatoApiResponse cambiarEstado(@PathVariable Long idPlato, @Valid @RequestBody PlatoEstadoRequest request,
                                          @AuthenticationPrincipal AuthenticatedUser user) {
        Plato plato = platoService.cambiarEstado(idPlato, request, user);
        return new PlatoApiResponse(true, "Estado del plato actualizado", PlatoResponse.from(plato));
    }
}