package controller;

public class PlatoRequestDTO {
    private String nombre;
    private Integer precio;
    private String descripcion;
    private String urlImagen;
    private String categoria;
    private Long idRestaurante;

    public PlatoRequestDTO() {
    }

    public PlatoRequestDTO(String nombre, Integer precio, String descripcion, String urlImagen,
                           String categoria, Long idRestaurante) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.urlImagen = urlImagen;
        this.categoria = categoria;
        this.idRestaurante = idRestaurante;
    }

    public String getNombre() { return nombre; }
    public Integer getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public String getUrlImagen() { return urlImagen; }
    public String getCategoria() { return categoria; }
    public Long getIdRestaurante() { return idRestaurante; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(Integer precio) { this.precio = precio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setIdRestaurante(Long idRestaurante) { this.idRestaurante = idRestaurante; }
}
