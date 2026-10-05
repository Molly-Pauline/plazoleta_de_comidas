package main.java.com.plazoleta.dto.response;

import com.plazoleta.entity.Pedido;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista serializable de una orden, incluida su identificación, ciclo de vida, total y líneas de compra.
 * @param id identificador persistido del pedido
 * @param idCliente cliente que creó el pedido
 * @param idEmpleado empleado que eventualmente atiende el pedido
 * @param idRestaurante restaurante responsable de prepararlo
 * @param estado estado del ciclo de vida representado por nombre
 * @param fechaCreacion fecha y hora de creación
 * @param total importe calculado con precios persistidos y cantidades solicitadas
 * @param platos líneas del pedido con sus precios unitarios históricos
 */
public record PedidoResponse(Long id, Long idCliente, Long idEmpleado, Long idRestaurante, String estado,
                             LocalDateTime fechaCreacion, Long total, List<PedidoDetalleResponse> platos) {
    /** Mapea la cabecera y transforma cada detalle al DTO que se expone por la API. */
    public static PedidoResponse from(Pedido pedido) {
        // Enum.name fija una representación textual estable y cada detalle se convierte sin devolver entidades JPA.
        return new PedidoResponse(pedido.getId(), pedido.getIdCliente(), pedido.getIdEmpleado(),
                pedido.getRestaurante().getId(), pedido.getEstado().name(), pedido.getFechaCreacion(),
                pedido.getTotal(), pedido.getDetalles().stream().map(PedidoDetalleResponse::from).toList());
    }
}