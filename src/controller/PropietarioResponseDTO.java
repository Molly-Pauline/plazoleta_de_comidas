package controller;

import model.Propietario;

/**
 * Respuesta de creacion de propietario.
 *
 * IMPORTANTE: no expone la clave ni el hash. Es un criterio de aceptacion
 * explicito de HU-01 y se demuestra en la sustentacion.
 */
public class PropietarioResponseDTO {
    private final boolean success;
    private final String message;
    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String documentoDeIdentidad;
    private final String celular;
    private final String correo;
    private final String rol;

    public PropietarioResponseDTO(boolean success, String message, Propietario propietario) {
        this.success = success;
        this.message = message;
        this.id = propietario.getId();
        this.nombre = propietario.getNombre();
        this.apellido = propietario.getApellido();
        this.documentoDeIdentidad = propietario.getDocumentoDeIdentidad();
        this.celular = propietario.getCelular();
        this.correo = propietario.getCorreo();
        this.rol = propietario.getRol();
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumentoDeIdentidad() { return documentoDeIdentidad; }
    public String getCelular() { return celular; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
}
