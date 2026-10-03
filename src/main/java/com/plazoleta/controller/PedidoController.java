package main.java.com.plazoleta.controller;

import com.plazoleta.dto.request.PedidoRequest;
import com.plazoleta.dto.response.PedidoApiResponse;
import com.plazoleta.dto.response.PedidoResponse;
import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Expone los endpoints HTTP de creación y consulta de pedidos. */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    /** Servicio donde se aplican las reglas de negocio y autorización por recurso. */
    private final PedidoService pedidoService;

    /** Inyecta el servicio de pedidos. */
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Recibe un pedido validado, toma al cliente del principal autenticado y responde con estado HTTP 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoApiResponse crear(@Valid @RequestBody PedidoRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser user) {
        // @Valid ejecuta las restricciones declaradas en el DTO antes de llamar al servicio.
        return new PedidoApiResponse(true, "Pedido creado", pedidoService.crear(request, user));
    }

    /** Consulta una página de pedidos por estado; el servicio deriva el restaurante del empleado autenticado. */
    @GetMapping
    public Page<PedidoResponse> listar(@RequestParam EstadoPedido estado,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return pedidoService.listarPorEstado(estado, page, size, user);
    }
}