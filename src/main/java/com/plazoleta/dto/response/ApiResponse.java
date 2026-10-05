package main.java.com.plazoleta.dto.response;

/**
 * Envoltorio genérico de éxito con mensaje y datos serializables para una respuesta HTTP.
 * @param <T> tipo del objeto específico que viaja en data
 * @param success indica si la operación terminó correctamente
 * @param message texto breve que describe el resultado
 * @param data resultado tipado de la operación
 */
public record ApiResponse<T>(boolean success, String message, T data) {
}