package controller;

import model.Plato;

public class PlatoResponseDTO {
    private final boolean success;
    private final String message;
    private final Plato plato;

    public PlatoResponseDTO(boolean success, String message, Plato plato) {
        this.success = success;
        this.message = message;
        this.plato = plato;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Plato getPlato() { return plato; }
}
