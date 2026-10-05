package main.java.com.plazoleta.dto.response;

import com.plazoleta.entity.Restaurante;

/**
 * Datos completos del restaurante que devuelve el endpoint de creación.
 * @param id identificador asignado al restaurante
 * @param nombre nombre del establecimiento
 * @param nit identificador tributario
 * @param direccion ubicación física
 * @param telefono contacto telefónico
 * @param urlLogo URL de la imagen del restaurante
 * @param idPropietario referencia al propietario validado en Usuarios
 */
public record RestauranteResponse(Long id, String nombre, String nit, String direccion, String telefono,
                                  String urlLogo, Long idPropietario) {
    /** Proyecta los campos escalares de la entidad sin exponer detalles internos de persistencia. */
    public static RestauranteResponse from(Restaurante restaurante) {
        return new RestauranteResponse(restaurante.getId(), restaurante.getNombre(), restaurante.getNit(),
                restaurante.getDireccion(), restaurante.getTelefono(), restaurante.getUrlLogo(),
                restaurante.getIdPropietario());
    }
}