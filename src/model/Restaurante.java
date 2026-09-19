package model;

public class Restaurante {
    private Long id;
    private String nombre;
    private String nit;
    private String direccion;
    private String telefono;
    private String urlLogo;
    private Long idPropietario;

    public Restaurante(String nombre, String nit, String direccion, String telefono, String urlLogo, Long idPropietario) {
        this.nombre = nombre;
        this.nit = nit;
        this.direccion = direccion;
        this.telefono = telefono;
        this.urlLogo = urlLogo;
        this.idPropietario = idPropietario;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNit() { return nit; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getUrlLogo() { return urlLogo; }
    public Long getIdPropietario() { return idPropietario; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setNit(String nit) { this.nit = nit; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setUrlLogo(String urlLogo) { this.urlLogo = urlLogo; }
    public void setIdPropietario(Long idPropietario) { this.idPropietario = idPropietario; }
}
