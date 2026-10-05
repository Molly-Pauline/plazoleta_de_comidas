package test.java.com.plazoleta.repository;

import com.plazoleta.dto.response.RestauranteListadoResponse;
import com.plazoleta.entity.Plato;
import com.plazoleta.entity.Restaurante;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Prueba con H2 las consultas JPA de catálogo, orden, filtrado y proyección pública. */
@DataJpaTest
class CatalogRepositoryTest {

        /** Repositorio real conectado a la base temporal de prueba. */
    @Autowired
    private RestauranteRepository restauranteRepository;

        /** Repositorio real para verificar filtrado y paginación del menú. */
    @Autowired
    private PlatoRepository platoRepository;

        /** Comprueba que HU9 pagina en orden alfabético y expone solo nombre y logo. */
    @Test
    void restaurantPagesAreAlphabeticalAndExposeOnlyNameAndLogo() {
                // Se guardan nombres fuera de orden para distinguir la ordenación de la secuencia de inserción.
        Restaurante zulu = restauranteRepository.save(new Restaurante("Zulu", "9001", "Calle 1", "3001234567",
                "https://example.com/zulu.png", 1L));
        Restaurante alfa = restauranteRepository.save(new Restaurante("Alfa", "9002", "Calle 2", "3001234568",
                "https://example.com/alfa.png", 2L));
        // flush envía las inserciones a H2 antes de ejecutar la consulta paginada.
        restauranteRepository.flush();

        // Una sola fila por página permite comprobar simultáneamente orden y paginación.
        var page = restauranteRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "nombre")));
        RestauranteListadoResponse response = RestauranteListadoResponse.from(page.getContent().get(0));

                // El total incluye resultados de páginas posteriores, aunque esta página contenga solo una fila.
        assertEquals(2, page.getTotalElements());
                // El primer restaurante es Alfa por la ordenación ascendente solicitada.
        assertEquals("Alfa", response.nombre());
                // El logo corresponde al restaurante de esa primera fila.
        assertEquals("https://example.com/alfa.png", response.urlLogo());
                // Dos componentes confirman que el listado no filtra NIT, teléfono, dirección ni propietario.
        assertEquals(2, RestauranteListadoResponse.class.getRecordComponents().length);
    }

        /** Verifica comparación de categoría sin distinguir mayúsculas y exclusión de platos inactivos. */
    @Test
    void menuPagesFilterByCategoryAndExcludeDisabledDishes() {
                // El menú contiene un plato activo de la categoría buscada y otro activo de una categoría diferente.
        Restaurante restaurant = restauranteRepository.save(new Restaurante("La Casona", "9010", "Calle 3",
                "3001234569", "https://example.com/logo.png", 10L));
        Plato burger = platoRepository.save(new Plato("Hamburguesa", 25000, "Queso", "https://example.com/a.png",
                "Hamburguesas", restaurant));
        platoRepository.save(new Plato("Gaseosa", 5000, "Fria", "https://example.com/b.png",
                "Bebidas", restaurant));
        // Este plato coincide con la categoría, pero debe desaparecer por estar desactivado.
        Plato disabledBurger = new Plato("Burger desactivada", 20000, "Queso", "https://example.com/c.png",
                "Hamburguesas", restaurant);
        disabledBurger.cambiarEstado(false);
        platoRepository.save(disabledBurger);
        platoRepository.flush();

        // Se consulta con categoría en mayúsculas para probar la comparación insensible al caso.
        var page = platoRepository.listarMenu(restaurant.getId(), "HAMBURGUESAS", PageRequest.of(0, 1));

        // El conteo confirma que ni la otra categoría ni el plato desactivado forman parte del resultado.
        assertEquals(1, page.getTotalElements());
        // El único elemento de la página es el plato activo que coincide con el filtro.
        assertEquals(burger.getId(), page.getContent().get(0).getId());
    }
}
