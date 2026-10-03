package main.java.com.plazoleta.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Contrato de entrada de HU11: un restaurante y al menos una línea válida de platos. */
public record PedidoRequest(@NotNull @Positive Long idRestaurante,
                            // Cada elemento también se valida para aplicar las restricciones de PedidoDetalleRequest.
                            @NotEmpty List<@Valid PedidoDetalleRequest> platos) {
}