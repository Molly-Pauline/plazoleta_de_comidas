package main.java.com.plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Limita la actualización HU4 a precio y descripción, protegiendo los demás atributos del plato. */
public record PlatoUpdateRequest(@NotNull @Positive Integer precio, @NotBlank String descripcion) {
}