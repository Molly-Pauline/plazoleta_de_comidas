package main.java.com.plazoleta.service.impl;

import com.plazoleta.dto.request.PedidoDetalleRequest;
import com.plazoleta.dto.request.PedidoRequest;
import com.plazoleta.dto.response.PedidoResponse;
import com.plazoleta.entity.DetallePedido;
import com.plazoleta.entity.EstadoPedido;
import com.plazoleta.entity.Pedido;
import com.plazoleta.entity.Plato;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PedidoRepository;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.PedidoService;
import com.plazoleta.service.PedidoCreationLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/** Implementa las reglas de negocio para crear pedidos y consultar los de un restaurante. */
/** Implementa las reglas de negocio para crear pedidos y consultar los de un restaurante. */
@Service
public class PedidoServiceImpl implements PedidoService {

    /** Estados que bloquean un segundo pedido del mismo cliente según HU11. */
    private static final Set<EstadoPedido> PEDIDOS_ACTIVOS = EnumSet.of(
            EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO);

    /** Persistencia de pedidos y consultas filtradas por cliente, estado y restaurante. */
    private final PedidoRepository pedidoRepository;
    /** Permite resolver los platos solicitados y comprobar su estado actual. */
    private final PlatoRepository platoRepository;
    /** Permite confirmar que existe el restaurante solicitado. */
    private final RestauranteRepository restauranteRepository;
    /** Evita carreras al crear pedidos concurrentes para el mismo cliente. */
    private final PedidoCreationLock pedidoCreationLock;

    /** Inyecta los repositorios y el mecanismo de exclusión por cliente. */
    public PedidoServiceImpl(PedidoRepository pedidoRepository, PlatoRepository platoRepository,
                             RestauranteRepository restauranteRepository, PedidoCreationLock pedidoCreationLock) {
        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
        this.restauranteRepository = restauranteRepository;
        this.pedidoCreationLock = pedidoCreationLock;
    }

    /** Crea un pedido en una sola transacción y restringe la operación a clientes autenticados (HU11). */
    @Override
    @Transactional
    public PedidoResponse crear(PedidoRequest request, AuthenticatedUser user) {
        // La identidad y el rol provienen del token validado; nunca se aceptan desde el cuerpo del pedido.
        if (user == null || !"CLIENTE".equals(user.rol())) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Solo un cliente autenticado puede crear pedidos");
        }
        // Se mantiene el bloqueo durante la transacción para que dos solicitudes no superen juntas la validación.
        try (PedidoCreationLock.Handle ignored = pedidoCreationLock.acquire(user.idUsuario())) {
            return crearConBloqueo(request, user);
        }
    }

    /** Ejecuta las validaciones y persistencia una vez adquirido el bloqueo del cliente. */
    private PedidoResponse crearConBloqueo(PedidoRequest request, AuthenticatedUser user) {
        // Solo los estados pendientes, en preparación o listos impiden que el cliente solicite otro pedido.
        if (pedidoRepository.existsByIdClienteAndEstadoIn(user.idUsuario(), PEDIDOS_ACTIVOS)) {
            throw new DomainException(HttpStatus.CONFLICT,
                    "El cliente ya tiene un pedido pendiente, en preparacion o listo");
        }
        // El pedido debe asociarse a un restaurante existente antes de validar sus platos.
        Restaurante restaurante = restauranteRepository.findById(request.idRestaurante())
                .orElseThrow(() -> new DomainException(HttpStatus.BAD_REQUEST, "El restaurante no existe"));

        // Se usa un conjunto para rechazar líneas repetidas y un long para acumular importes con rango ampliado.
        Set<Long> dishIds = new HashSet<>();
        long total = 0;
        Pedido pedido;
        // El try captura desbordamientos de la multiplicación o suma del total, sin guardar un pedido parcial.
        try {
            for (PedidoDetalleRequest item : request.platos()) {
                // Una sola línea por plato evita ambigüedad: la cantidad se expresa en esa línea.
                if (!dishIds.add(item.idPlato())) {
                    throw new DomainException(HttpStatus.BAD_REQUEST, "No repitas un plato; acumula su cantidad en una linea");
                }
                // Se resuelve cada identificador contra la base, sin confiar en precios enviados por el cliente.
                Plato plato = platoRepository.findById(item.idPlato())
                        .orElseThrow(() -> new DomainException(HttpStatus.BAD_REQUEST, "Uno de los platos no existe"));
                // Un plato desactivado no se puede incorporar a un pedido nuevo.
                if (!plato.isActivo()) {
                    throw new DomainException(HttpStatus.BAD_REQUEST, "No se puede pedir un plato inactivo");
                }
                // Todas las líneas deben provenir del restaurante seleccionado para el pedido.
                if (!plato.getRestaurante().getId().equals(restaurante.getId())) {
                    throw new DomainException(HttpStatus.BAD_REQUEST, "Todos los platos deben pertenecer al restaurante del pedido");
                }
                // El precio persistido y la cantidad validada forman el subtotal; las operaciones detectan overflow.
                total = Math.addExact(total, Math.multiplyExact((long) plato.getPrecio(), item.cantidad()));
            }
            // El cliente asociado se toma de la identidad firmada, y el estado inicial lo fija la entidad Pedido.
            pedido = new Pedido(user.idUsuario(), restaurante, total);
            for (PedidoDetalleRequest item : request.platos()) {
                // Se agregan detalles asociados a entidades resueltas, preservando cantidades y precios del dominio.
                Plato plato = platoRepository.findById(item.idPlato()).orElseThrow();
                pedido.agregarDetalle(new DetallePedido(plato, item.cantidad()));
            }
        } catch (ArithmeticException exception) {
            // El overflow se transforma en una respuesta de validación y no en un error interno de servidor.
            throw new DomainException(HttpStatus.BAD_REQUEST, "El total del pedido excede el limite permitido");
        }
        // Solo después de validar todas las líneas se persiste y se transforma a la respuesta pública.
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    /** Consulta pedidos de un empleado por estado y por el restaurante incluido en su identidad firmada (HU12). */
    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarPorEstado(EstadoPedido estado, int page, int size, AuthenticatedUser empleado) {
        // No se permite elegir el restaurante en la petición: debe venir firmado en el token del empleado.
        if (empleado == null || !"EMPLEADO".equals(empleado.rol()) || empleado.idRestaurante() == null) {
            throw new DomainException(HttpStatus.FORBIDDEN, "El empleado no tiene un restaurante asociado");
        }
        // Se acota el tamaño de página para evitar consultas excesivas y se exige un estado explícito.
        if (estado == null || page < 0 || size < 1 || size > 100) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Estado o paginacion invalida");
        }
        // La consulta filtra en base de datos por restaurante y estado, y convierte cada fila a DTO de salida.
        return pedidoRepository.findByRestaurante_IdAndEstado(empleado.idRestaurante(), estado,
                        PageRequest.of(page, size))
                .map(PedidoResponse::from);
    }
}