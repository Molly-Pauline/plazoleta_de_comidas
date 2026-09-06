package model;

public class Plato {
    private Long id;
    private final String nombre;
    private Integer precio;
    private String descripcion;
    private final String urlImagen;
    private final String categoria;
    private final Long idRestaurante;
    private boolean activo;

    public Plato(String nombre, Integer precio, String descripcion, String urlImagen,
                 String categoria, Long idRestaurante) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.urlImagen = urlImagen;
        this.categoria = categoria;
        this.idRestaurante = idRestaurante;
        this.activo = true;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Integer getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public String getUrlImagen() { return urlImagen; }
    public String getCategoria() { return categoria; }
    public Long getIdRestaurante() { return idRestaurante; }
    public boolean isActivo() { return activo; }

    public void setId(Long id) { this.id = id; }
    public void actualizarPrecioYDescripcion(Integer precio, String descripcion) {
        this.precio = precio;
        this.descripcion = descripcion;
    }
}
