package test.java.com.plazoleta.service;

import com.plazoleta.entity.Restaurante;
import com.plazoleta.dto.request.RestauranteRequest;
import com.plazoleta.exception.DomainException;
import com.plazoleta.repository.RestauranteRepository;
import com.plazoleta.service.impl.RestauranteServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas unitarias de unicidad del NIT y validación externa de propietario requerida por HU2. */
class RestauranteServiceTest {

    /** Repositorio falso para verificar consultas e inserciones sin acceder a una base real. */
    private final RestauranteRepository repository = mock(RestauranteRepository.class);
    /** Puerto falso de Usuarios, que aísla la regla de red del comportamiento de negocio. */
    private final UsuarioValidationPort usuarioValidation = mock(UsuarioValidationPort.class);
    private final RestauranteServiceImpl service = new RestauranteServiceImpl(repository, usuarioValidation);
    /** Solicitud representativa reutilizada por los escenarios positivos y negativos. */
    private final RestauranteRequest request = new RestauranteRequest(
            "La Casona", "123456789", "Calle 123", "+573001234567", "https://example.com/logo.png", 10L);

    /** Guarda solo después de verificar que Usuarios reconoce al ID como propietario válido. */
    @Test
    void createsRestaurantOnlyAfterValidatingOwner() {
        // El NIT está disponible y la dependencia externa confirma el rol PROPIETARIO.
        when(repository.existsByNit(request.nit())).thenReturn(false);
        when(usuarioValidation.existePropietarioValido(10L)).thenReturn(true);
        when(repository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Restaurante created = service.crear(request);

        // Se comprueban datos guardados y la interacción que demuestra la llamada de validación.
        assertEquals("La Casona", created.getNombre());
        assertEquals(10L, created.getIdPropietario());
        verify(usuarioValidation).existePropietarioValido(10L);
    }

    /** Un NIT duplicado se rechaza antes de realizar la llamada de red a Usuarios. */
    @Test
    void rejectsDuplicateNitBeforeCallingUsersService() {
        // Una coincidencia en base local basta para detener la operación.
        when(repository.existsByNit(request.nit())).thenReturn(true);

        assertThrows(DomainException.class, () -> service.crear(request));

        // Nunca se consulta el puerto externo cuando el NIT ya es inválido.
        verify(usuarioValidation, never()).existePropietarioValido(any());
    }

    /** Un ID inexistente o con rol distinto de propietario no puede crear un restaurante. */
    @Test
    void rejectsUnknownOrNonOwnerUser() {
        // NIT disponible pero respuesta negativa de Usuarios debe impedir guardar.
        when(repository.existsByNit(request.nit())).thenReturn(false);
        when(usuarioValidation.existePropietarioValido(10L)).thenReturn(false);

        // El rechazo se representa como error de dominio y no debe generar inserción alguna.
        assertThrows(DomainException.class, () -> service.crear(request));

        verify(repository, never()).save(any());
    }
}