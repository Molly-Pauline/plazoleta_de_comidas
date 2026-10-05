package main.java.com.plazoleta.dto.response;

/**
 * Envoltorio de estado y mensaje para la creación de restaurantes.
 * @param success indica si el registro terminó correctamente
 * @param message texto del resultado
 * @param restaurante datos del restaurante creado
 */
public record RestauranteApiResponse(boolean success, String message, RestauranteResponse restaurante) {
}