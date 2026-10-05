package main.java.com.plazoleta.dto.response;

/**
 * Respuesta de creación de pedido con indicador de éxito, mensaje y pedido resultante.
 * @param success indica si la creación terminó correctamente
 * @param message mensaje legible del resultado
 * @param pedido pedido creado y convertido a su DTO público
 */
public record PedidoApiResponse(boolean success, String message, PedidoResponse pedido) {
}