package controller;

import model.Restaurante;

public class RestauranteResponseDTO {
    private boolean success;
    private String message;
    private Restaurante restaurante;

    public RestauranteResponseDTO(boolean success, String message, Restaurante restaurante) {
        this.success = success;
        this.message = message;
        this.restaurante = restaurante;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Restaurante getRestaurante() { return restaurante; }
}
