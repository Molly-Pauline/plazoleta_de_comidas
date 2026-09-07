package controller;

/**
 * Datos de entrada para crear un propietario (HU-01).
 *
 * No incluye 'rol': el rol lo asigna el servidor. Si el cliente pudiera
 * mandarlo, podria crearse a si mismo como ADMINISTRADOR.
 */
public class PropietarioRequestDTO {
    private String nombre;
    private String apellido;
    private String documentoDeIdentidad;
    private String celular;
    private String fechaNacimiento; // ISO-8601: aaaa-mm-dd
    private String correo;
    private String clave;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDocumentoDeIdentidad() { return documentoDeIdentidad; }
    public void setDocumentoDeIdentidad(String d) { this.documentoDeIdentidad = d; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
