package main.java.com.plazoleta.security;

/**
 * Principal inmutable construido únicamente después de verificar la firma del Bearer.
 * @param idUsuario identificador de usuario incluido en el contenido firmado
 * @param rol rol autenticado que Spring convierte en autoridad
 * @param idRestaurante asociación firmada del empleado, nula para identidades sin restaurante
 */
public record AuthenticatedUser(Long idUsuario, String rol, Long idRestaurante) {
	/** Compatibilidad para identidades sin asociación de restaurante, como propietario o cliente. */
	public AuthenticatedUser(Long idUsuario, String rol) {
		this(idUsuario, rol, null);
	}
}