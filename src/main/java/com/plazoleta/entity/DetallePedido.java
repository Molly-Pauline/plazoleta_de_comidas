package main.java.com.plazoleta.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

/** Línea de pedido que registra cantidad y precio unitario vigente al momento de la compra. */
@Entity
@Table(name = "pedido_detalles")
public class DetallePedido {

    /** Clave primaria generada para cada línea del pedido. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relación obligatoria con la cabecera, establecida al agregar este detalle al pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    /** Plato que se compró; se conserva la relación para identificar el producto. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plato", nullable = false)
    private Plato plato;

    /** Unidades solicitadas, cuya positividad se valida en el DTO de entrada. */
    @Column(nullable = false)
    private Integer cantidad;

    /** Copia del precio del plato al crear la línea; cambios de precio futuros no alteran el pedido histórico. */
    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;

    /** Constructor vacío requerido por JPA. */
    protected DetallePedido() {
    }

    /** Captura el precio actual del plato para mantener estable el valor histórico de la compra. */
    public DetallePedido(Plato plato, Integer cantidad) {
        this.plato = plato;
        this.cantidad = cantidad;
        this.precioUnitario = plato.getPrecio();
    }

    /** Vincula la línea con su cabecera; se invoca desde Pedido.agregarDetalle. */
    void asociarPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Long getId() { return id; }
    public Plato getPlato() { return plato; }
    public Integer getCantidad() { return cantidad; }
    public Integer getPrecioUnitario() { return precioUnitario; }
}