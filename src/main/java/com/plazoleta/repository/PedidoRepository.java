package main.java.com.plazoleta.repository;

import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/** Consultas JPA necesarias para impedir pedidos activos duplicados y listar trabajo por restaurante. */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    /** Comprueba si el cliente ya tiene alguno de los estados que bloquean otro pedido. */
    boolean existsByIdClienteAndEstadoIn(Long idCliente, Collection<EstadoPedido> estados);

    /** Filtra por ID del restaurante relacionado y estado, devolviendo resultados paginados. */
    Page<Pedido> findByRestaurante_IdAndEstado(Long idRestaurante, EstadoPedido estado, Pageable pageable);
}