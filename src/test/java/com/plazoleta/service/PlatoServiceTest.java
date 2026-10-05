package test.java.com.plazoleta.service;

import com.plazoleta.entity.Plato;
import com.plazoleta.entity.Restaurante;
import com.plazoleta.dto.request.PlatoRequest;
import com.plazoleta.dto.request.PlatoUpdateRequest;
import com.plazoleta.dto.request.PlatoEstadoRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.PlatoRepository;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.security.AuthenticatedUser;
import com.plazoleta.service.impl.PlatoServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas de propiedad, creación, actualización restringida y disponibilidad de platos. */
class PlatoServiceTest {

        /** Repositorios simulados para aislar las decisiones de la capa de servicio. */
    private final PlatoRepository platoRepository = mock(PlatoRepository.class);
    private final RestauranteRepository restauranteRepository = mock(RestauranteRepository.class);
    private final PlatoServiceImpl service = new PlatoServiceImpl(platoRepository, restauranteRepository);
        /** Restaurante cuyo propietario persistido es el usuario de prueba 10. */
    private final Restaurante restaurant = new Restaurante(
            "La Casona", "123456789", "Calle 123", "+573001234567", "https://example.com/logo.png", 10L);
        /** Identidad firmada que sí tiene autorización sobre el restaurante de prueba. */
    private final AuthenticatedUser owner = new AuthenticatedUser(10L, "PROPIETARIO");

        /** El propietario asociado puede crear un plato, que comienza activo y ligado a su restaurante. */
    @Test
    void createsActiveDishForOwningRestaurant() {
                // La búsqueda entrega el restaurante existente y el guardado devuelve el objeto construido.
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(platoRepository.save(any(Plato.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PlatoRequest request = new PlatoRequest("Hamburguesa", 25000, "Con queso",
                "https://example.com/burger.png", "Hamburguesas", 1L);

        Plato created = service.crear(request, owner);

        // La aserción cubre tanto el estado inicial por defecto como la asociación correcta.
        assertTrue(created.isActivo());
        assertEquals(restaurant, created.getRestaurante());
    }

        /** Un propietario diferente no puede crear platos ni siquiera si el restaurante existe. */
    @Test
    void refusesDishForAnotherOwnersRestaurant() {
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        PlatoRequest request = new PlatoRequest("Hamburguesa", 25000, "Con queso",
                "https://example.com/burger.png", "Hamburguesas", 1L);

                // El principal 11 no coincide con el propietario 10 del restaurante.
        assertThrows(DomainException.class, () -> service.crear(request, new AuthenticatedUser(11L, "PROPIETARIO")));

                // La negativa debe ocurrir antes de persistir cualquier Plato.
        verify(platoRepository, never()).save(any());
    }

        /** HU4 actualiza precio y descripción, dejando nombre, categoría e imagen sin cambios. */
    @Test
    void updateChangesOnlyPriceAndDescription() {
        Plato dish = new Plato("Hamburguesa", 25000, "Con queso", "https://example.com/burger.png",
                "Hamburguesas", restaurant);
        // El servicio recibe la entidad existente y el repositorio retorna esa misma instancia tras guardar.
        when(platoRepository.findById(4L)).thenReturn(Optional.of(dish));
        when(platoRepository.save(dish)).thenReturn(dish);

        Plato updated = service.modificar(4L, new PlatoUpdateRequest(30000, "Con queso y tocineta"), owner);

        // Comprueba los dos únicos campos mutables autorizados por el contrato HU4.
        assertEquals(30000, updated.getPrecio());
        assertEquals("Con queso y tocineta", updated.getDescripcion());
        // Estos atributos deben conservar sus valores originales después de la actualización.
        assertEquals("Hamburguesa", updated.getNombre());
        assertEquals("Hamburguesas", updated.getCategoria());
        assertEquals("https://example.com/burger.png", updated.getUrlImagen());
    }

        /** El propietario puede desactivar su propio plato, mientras que un tercero no puede cambiarlo. */
    @Test
    void ownerCanDisableOwnDishButAnotherOwnerCannot() {
        Plato dish = new Plato("Hamburguesa", 25000, "Con queso", "https://example.com/burger.png",
                "Hamburguesas", restaurant);
        when(platoRepository.findById(4L)).thenReturn(Optional.of(dish));
        when(platoRepository.save(dish)).thenReturn(dish);

        // El propietario correcto solicita desactivar el plato existente.
        Plato disabled = service.cambiarEstado(4L, new PlatoEstadoRequest(false), owner);

        // false confirma el cambio de disponibilidad sin eliminar la entidad.
        assertEquals(false, disabled.isActivo());
        // La misma operación con identidad de otro propietario se rechaza.
        assertThrows(DomainException.class, () -> service.cambiarEstado(4L, new PlatoEstadoRequest(true),
                new AuthenticatedUser(11L, "PROPIETARIO")));
    }
}