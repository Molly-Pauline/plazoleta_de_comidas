package main.java.com.plazoleta.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Cabecera del pedido con estado inicial, cliente, restaurante, total y líneas asociadas. */
@Entity
@Table(name = "pedidos")
public class Pedido {

    /** Clave primaria del pedido. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del cliente autenticado; se conserva como referencia, no como dato controlado por la petición. */
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    /** ID del empleado que eventualmente atienda el pedido; puede ser nulo al crearlo. */
    @Column(name = "id_empleado")
    private Long idEmpleado;

    /** Asociación obligatoria al restaurante que prepara el pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_restaurante", nullable = false)
    private Restaurante restaurante;

    /** Enum persistido por nombre para que el significado no dependa de ordinales numéricos. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EstadoPedido estado;

    /** Instante local de creación asignado al construir el pedido. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /** Total calculado por el servicio usando precios persistidos y aritmética con detección de overflow. */
    @Column(nullable = false)
    private Long total;

    /** Las líneas se guardan y eliminan junto con la cabecera; la lista refleja el lado inverso de la relación. */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Constructor vacío reservado para JPA. */
    protected Pedido() {
    }

    /** Crea el pedido con estado PENDIENTE y fija la fecha inicial del ciclo de vida. */
    public Pedido(Long idCliente, Restaurante restaurante, Long total) {
        this.idCliente = idCliente;
        this.restaurante = restaurante;
        this.total = total;
        this.estado = EstadoPedido.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    /** Mantiene ambos lados de la relación en memoria para que JPA persista cada detalle con su pedido. */
    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.asociarPedido(this);
    }

    public Long getId() { return id; }
    public Long getIdCliente() { return idCliente; }
    public Long getIdEmpleado() { return idEmpleado; }
    public Restaurante getRestaurante() { return restaurante; }
    public EstadoPedido getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public Long getTotal() { return total; }
    /** Devuelve una copia no modificable para evitar que consumidores externos alteren la colección interna. */
    public List<DetallePedido> getDetalles() { return List.copyOf(detalles); }
}