package controller;

public class PlatoUpdateRequestDTO {
    private Integer precio;
    private String descripcion;

    public PlatoUpdateRequestDTO() {
    }

    public PlatoUpdateRequestDTO(Integer precio, String descripcion) {
        this.precio = precio;
        this.descripcion = descripcion;
    }

    public Integer getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public void setPrecio(Integer precio) { this.precio = precio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
