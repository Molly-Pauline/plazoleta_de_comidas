package main.java.com.plazoleta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Define una línea del pedido y evita identificadores o cantidades nulos/no positivos. */
public record PedidoDetalleRequest(@NotNull @Positive Long idPlato, @NotNull @Positive Integer cantidad) {
}