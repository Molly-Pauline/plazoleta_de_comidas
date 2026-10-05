package main.java.com.plazoleta.entity;

/** Estados posibles del ciclo de vida que se persisten por nombre en la tabla de pedidos. */
public enum EstadoPedido {
    /** Pedido creado y todavía sin preparación. */
    PENDIENTE,
    /** El restaurante está preparando el pedido. */
    EN_PREPARACION,
    /** El pedido está listo para entrega o recogida. */
    LISTO,
    /** El cliente ya recibió el pedido; este estado no bloquea otro pedido. */
    ENTREGADO,
    /** Pedido cancelado; este estado tampoco cuenta como activo. */
    CANCELADO
}