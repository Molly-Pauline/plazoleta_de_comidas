package main.java.com.plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** Datos de creación de un plato; los identificadores se validan antes de consultar el servicio. */
public record PlatoRequest(
        // El nombre no puede estar vacío ni compuesto exclusivamente por dígitos.
        @NotBlank String nombre,
        // El precio debe estar presente y ser mayor que cero.
        @NotNull @Positive Integer precio,
        // La descripción es obligatoria para presentar el plato en el menú.
        @NotBlank String descripcion,
        // La imagen requiere una URL absoluta con esquema HTTP o HTTPS.
        @NotBlank @Pattern(regexp = "https?://.+", message = "La URL de imagen debe ser HTTP o HTTPS") String urlImagen,
        // La categoría es obligatoria y se usa como filtro del menú.
        @NotBlank String categoria,
        // El restaurante debe existir; la autorización por propiedad se verifica en el servicio.
        @NotNull @Positive Long idRestaurante) {
}