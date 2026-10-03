package main.java.com.plazoleta.dto.response;

import com.plazoleta.entity.Plato;

/**
 * Contrato de salida de catálogo con datos del plato y el ID de su restaurante.
 * @param id identificador del plato
 * @param nombre nombre visible en el menú
 * @param precio precio vigente por unidad
 * @param descripcion descripción presentada al cliente
 * @param urlImagen ubicación de la imagen del plato
 * @param categoria categoría usada para agrupar o filtrar el menú
 * @param idRestaurante identificador del restaurante propietario del plato
 * @param activo disponibilidad actual para el menú y nuevos pedidos
 */
public record PlatoResponse(Long id, String nombre, Integer precio, String descripcion, String urlImagen,
                            String categoria, Long idRestaurante, boolean activo) {
    /** Proyecta la entidad a valores simples adecuados para serializar en HTTP. */
    public static PlatoResponse from(Plato plato) {
        // Se expone el ID de la relación en lugar del objeto Restaurante completo para mantener el contrato acotado.
        return new PlatoResponse(plato.getId(), plato.getNombre(), plato.getPrecio(), plato.getDescripcion(),
                plato.getUrlImagen(), plato.getCategoria(), plato.getRestaurante().getId(), plato.isActivo());
    }
}