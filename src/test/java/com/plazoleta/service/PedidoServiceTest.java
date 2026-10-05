package test.java.com.plazoleta.service;

import com.plazoleta.dto.request.PedidoDetalleRequest;
import com.plazoleta.dto.request.PedidoRequest;
import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.entity.Pedido;
import com.plazoleta.entity.Plato;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PedidoRepository;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PedidoCreationLock;
import com.plazoleta.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas unitarias de validación, cálculo y autorización de HU11-HU12. */
class PedidoServiceTest {

    /** Dobles de repositorio que permiten fijar las respuestas consultadas por el servicio. */
    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final PlatoRepository platoRepository = mock(PlatoRepository.class);
    private final RestauranteRepository restauranteRepository = mock(RestauranteRepository.class);
    /** El candado se sustituye para aislar aquí las reglas de dominio del mecanismo de concurrencia. */
        private final PedidoCreationLock orderCreationLock = mock(PedidoCreationLock.class);
        private final PedidoServiceImpl service = new PedidoServiceImpl(
            pedidoRepository, platoRepository, restauranteRepository, orderCreationLock);
    /** Principal autenticado usado para representar al cliente que solicita el pedido. */
    private final AuthenticatedUser customer = new AuthenticatedUser(25L, "CLIENTE");

    /** Calcula el total usando el precio persistido y crea una orden pendiente de un solo restaurante. */
    @Test
    void createsPendingOrderForSingleRestaurant() {
        // Los mocks representan las entidades y respuestas que los repositorios cargarían desde la base.
        Restaurante restaurant = mock(Restaurante.class);
        Plato dish = mock(Plato.class);
        when(restaurant.getId()).thenReturn(1L);
        when(dish.getId()).thenReturn(8L);
        when(dish.getPrecio()).thenReturn(25000);
        when(dish.isActivo()).thenReturn(true);
        when(dish.getRestaurante()).thenReturn(restaurant);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(pedidoRepository.existsByIdClienteAndEstadoIn(any(), any())).thenReturn(false);
        when(platoRepository.findById(8L)).thenReturn(Optional.of(dish));
        // Se simula la persistencia devolviendo el mismo objeto completo construido por el servicio.
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.crear(new PedidoRequest(1L, List.of(new PedidoDetalleRequest(8L, 2))), customer);

        // Estado PENDIENTE demuestra el ciclo inicial; precio 25000 por cantidad 2 produce total 50000.
        assertEquals("PENDIENTE", created.estado());
        // La igualdad con 50000 verifica precio unitario persistido multiplicado por dos unidades.
        assertEquals(50000L, created.total());
        // El pedido resultante contiene exactamente la línea incluida en la solicitud.
        assertEquals(1, created.platos().size());
    }

    /** Un cliente con pedido activo recibe conflicto y no genera una segunda escritura. */
    @Test
    void refusesSecondInProgressOrder() {
        // Se fuerza la condición de existencia de pedido en estado bloqueante.
        when(pedidoRepository.existsByIdClienteAndEstadoIn(any(), any())).thenReturn(true);

        // La regla produce una excepción de dominio antes de resolver o guardar platos.
        assertThrows(DomainException.class, () -> service.crear(
                new PedidoRequest(1L, List.of(new PedidoDetalleRequest(8L, 1))), customer));

        // Ninguna entidad Pedido debe persistirse después del rechazo.
        verify(pedidoRepository, never()).save(any());
    }

    /** Una línea con plato de restaurante distinto se rechaza y no persiste el pedido. */
    @Test
    void refusesDishFromDifferentRestaurant() {
        Restaurante requestedRestaurant = mock(Restaurante.class);
        Restaurante otherRestaurant = mock(Restaurante.class);
        Plato dish = mock(Plato.class);
        // Los IDs distintos modelan restaurante solicitado y restaurante dueño del plato.
        when(requestedRestaurant.getId()).thenReturn(1L);
        when(otherRestaurant.getId()).thenReturn(2L);
        when(dish.getRestaurante()).thenReturn(otherRestaurant);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(requestedRestaurant));
        when(pedidoRepository.existsByIdClienteAndEstadoIn(any(), any())).thenReturn(false);
        when(platoRepository.findById(8L)).thenReturn(Optional.of(dish));

        // Tras detectar la pertenencia cruzada, el servicio debe lanzar error de negocio.
        assertThrows(DomainException.class, () -> service.crear(
                new PedidoRequest(1L, List.of(new PedidoDetalleRequest(8L, 1))), customer));

        // La verificación asegura que una solicitud inválida nunca queda parcialmente guardada.
        verify(pedidoRepository, never()).save(any());
    }

    /** La consulta del empleado queda limitada al ID de restaurante autenticado y firmado (HU12). */
    @Test
    void employeeOnlyListsOrdersForRestaurantInSignedIdentity() {
        // Se prepara una página vacía para comprobar la consulta sin depender de datos de persistencia.
        when(pedidoRepository.findByRestaurante_IdAndEstado(77L, EstadoPedido.PENDIENTE, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of()));

        // El ID 77 procede del principal del empleado, no de un parámetro controlable de la solicitud.
        service.listarPorEstado(EstadoPedido.PENDIENTE, 0, 10,
                new AuthenticatedUser(30L, "EMPLEADO", 77L));

        // La interacción exacta comprueba filtro por restaurante, estado y paginación esperados.
        verify(pedidoRepository).findByRestaurante_IdAndEstado(77L, EstadoPedido.PENDIENTE, PageRequest.of(0, 10));
    }
}