package main.java.com.plazoleta.dto.response;

import com.plazoleta.entity.Restaurante;

/**
 * Proyección reducida del catálogo HU9: publica solo el nombre y el logo del restaurante.
 * @param nombre nombre que identifica al restaurante en el catálogo
 * @param urlLogo imagen de marca que acompaña el nombre
 */
public record RestauranteListadoResponse(String nombre, String urlLogo) {
    /** Evita incluir NIT, teléfono, dirección y propietario en el listado de restaurantes para clientes. */
    public static RestauranteListadoResponse from(Restaurante restaurante) {
        return new RestauranteListadoResponse(restaurante.getNombre(), restaurante.getUrlLogo());
    }
}