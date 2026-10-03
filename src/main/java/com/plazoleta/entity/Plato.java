package main.java.com.plazoleta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidad de catálogo: cada plato pertenece a un restaurante y puede ocultarse sin eliminarse. */
@Entity
@Table(name = "platos")
public class Plato {

    /** Clave primaria asignada por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre mostrado en la carta. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Precio entero positivo validado en la solicitud antes de persistirlo. */
    @Column(nullable = false)
    private Integer precio;

    /** Texto descriptivo que HU4 permite actualizar junto con el precio. */
    @Column(nullable = false, length = 500)
    private String descripcion;

    /** URL de imagen que permanece sin cambios en la actualización HU4. */
    @Column(name = "url_imagen", nullable = false, length = 500)
    private String urlImagen;

    /** Categoría usada para filtrar el menú; también queda protegida frente a cambios HU4. */
    @Column(nullable = false, length = 80)
    private String categoria;

    /** Los platos nuevos están disponibles por defecto; false los excluye del menú y de nuevos pedidos. */
    @Column(nullable = false)
    private boolean activo = true;

    /** Relación obligatoria cargada bajo demanda para evitar traer el restaurante en consultas no relacionadas. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_restaurante", nullable = false)
    private Restaurante restaurante;

    /** Constructor sin argumentos que JPA necesita para materializar la fila. */
    protected Plato() {
    }

    /** Inicializa todos los datos de catálogo y activa el plato al momento de crearlo. */
    public Plato(String nombre, Integer precio, String descripcion, String urlImagen, String categoria,
                 Restaurante restaurante) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.urlImagen = urlImagen;
        this.categoria = categoria;
        this.restaurante = restaurante;
        this.activo = true;
    }

    /** Regla HU4: muta solo los atributos que la historia permite actualizar. */
    public void actualizarPrecioYDescripcion(Integer precio, String descripcion) {
        this.precio = precio;
        this.descripcion = descripcion;
    }

    /** Cambia la visibilidad comercial sin borrar el registro ni perder su historial. */
    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Integer getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public String getUrlImagen() { return urlImagen; }
    public String getCategoria() { return categoria; }
    public boolean isActivo() { return activo; }
    public Restaurante getRestaurante() { return restaurante; }
}