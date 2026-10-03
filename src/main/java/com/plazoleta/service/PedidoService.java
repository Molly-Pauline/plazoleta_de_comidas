package main.java.com.plazoleta.service;

import com.plazoleta.dto.request.PedidoRequest;
import com.plazoleta.dto.response.PedidoResponse;
import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.security.AuthenticatedUser;
import org.springframework.data.domain.Page;

/** Contrato de creación y consulta paginada de pedidos. */
public interface PedidoService {
    /** Crea un pedido para el cliente autenticado, aplicando reglas de activo y pertenencia de platos. */
    PedidoResponse crear(PedidoRequest request, AuthenticatedUser user);

    /** Lista pedidos del restaurante firmado en la identidad de un empleado por estado. */
    Page<PedidoResponse> listarPorEstado(EstadoPedido estado, int page, int size, AuthenticatedUser empleado);
}