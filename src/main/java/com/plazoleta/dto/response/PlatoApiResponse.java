package main.java.com.plazoleta.dto.response;

/**
 * Envoltorio de éxito para creación, modificación o cambio de estado de un plato.
 * @param success indica si la operación finalizó correctamente
 * @param message describe la operación realizada
 * @param plato plato resultante en su representación de API
 */
public record PlatoApiResponse(boolean success, String message, PlatoResponse plato) {
}