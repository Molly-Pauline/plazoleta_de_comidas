import model.Propietario;
import model.Restaurante;
import repository.PropietarioRepository;
import repository.RestauranteRepository;
import service.PropietarioService;
import service.RestauranteService;
import service.UsuarioValidationPort;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        PropietarioRepository propietarioRepository = new PropietarioRepository();
        PropietarioService propietarioService = new PropietarioService(propietarioRepository);

        Propietario propietario = new Propietario(
                "Ana",
                "García",
                "12345678",
                "+573001112233",
                LocalDate.of(1990, 5, 20),
                "ana@correo.com",
                "ClaveSegura123"
        );

        try {
            Propietario creado = propietarioService.crearPropietario(propietario, "ADMINISTRADOR");
            System.out.println("Propietario creado: " + creado);

            RestauranteRepository restauranteRepository = new RestauranteRepository();
            UsuarioValidationPort usuarioValidationPort = idPropietario -> idPropietario.equals(1L) || idPropietario.equals(12345678L);
            RestauranteService restauranteService = new RestauranteService(restauranteRepository, usuarioValidationPort);

            Restaurante restaurante = new Restaurante(
                    "La Casona",
                    "123456789",
                    "Calle 123 #45-67",
                    "+573001234567",
                    "https://example.com/logo.png",
                    12345678L
            );

            Restaurante guardado = restauranteService.crearRestaurante(restaurante, "ADMINISTRADOR");
            System.out.println("Restaurante creado: " + guardado.getNombre() + " - NIT: " + guardado.getNit());
        } catch (Exception e) {
            System.err.println("No se pudo completar la operación: " + e.getMessage());
        }
    }
}
