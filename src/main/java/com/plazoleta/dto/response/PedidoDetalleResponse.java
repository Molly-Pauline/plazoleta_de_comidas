package main.java.com.plazoleta.dto.response;

import com.plazoleta.entity.DetallePedido;

/**
 * Proyección pública de una línea: identifica el plato y conserva cantidad y precio histórico.
 * @param id identificador de la línea persistida
 * @param idPlato identificador del plato comprado
 * @param nombrePlato nombre informativo del plato
 * @param cantidad unidades incluidas en la línea
 * @param precioUnitario precio capturado al crear el pedido, no el precio actual del catálogo
 */
public record PedidoDetalleResponse(Long id, Long idPlato, String nombrePlato, Integer cantidad,
                                   Integer precioUnitario) {
    /** Convierte la entidad de detalle a datos de respuesta sin exponer asociaciones JPA. */
    public static PedidoDetalleResponse from(DetallePedido detalle) {
        // Se extraen los IDs y el nombre del plato asociado junto con el valor capturado al comprarlo.
        return new PedidoDetalleResponse(detalle.getId(), detalle.getPlato().getId(), detalle.getPlato().getNombre(),
                detalle.getCantidad(), detalle.getPrecioUnitario());
    }
}