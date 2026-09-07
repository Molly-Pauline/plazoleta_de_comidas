package controller;

import model.Restaurante;
import repository.RestauranteRepository;
import service.RestauranteService;
import service.RolAutenticado;
import service.UsuarioValidationPort;

public class RestauranteController {
    private final RestauranteRepository repository = new RestauranteRepository();
    private final UsuarioValidationPort usuarioValidationPort = idPropietario -> idPropietario != null && idPropietario > 0;
    private final RestauranteService service = new RestauranteService(repository, usuarioValidationPort);

    public RestauranteRepository getRepository() {
        return repository;
    }

    public String crearRestaurante(Restaurante restaurante, RolAutenticado usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getRol() == null) {
            throw new SecurityException("Acceso denegado: no autenticado");
        }

        if (!"ADMINISTRADOR".equals(usuarioAutenticado.getRol())) {
            throw new SecurityException("Acceso denegado: solo ADMINISTRADOR puede crear restaurantes");
        }

        Restaurante creado = service.crearRestaurante(restaurante, usuarioAutenticado.getRol());
        return "Restaurante creado: " + creado.getNombre() + " - NIT: " + creado.getNit();
    }

    public RestauranteResponseDTO crearRestaurante(RestauranteRequestDTO request, RolAutenticado usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getRol() == null) {
            throw new SecurityException("Acceso denegado: no autenticado");
        }

        if (!"ADMINISTRADOR".equals(usuarioAutenticado.getRol())) {
            throw new SecurityException("Acceso denegado: solo ADMINISTRADOR puede crear restaurantes");
        }

        Restaurante restaurante = new Restaurante(
                request.getNombre(),
                request.getNit(),
                request.getDireccion(),
                request.getTelefono(),
                request.getUrlLogo(),
                request.getIdPropietario()
        );

        Restaurante creado = service.crearRestaurante(restaurante, usuarioAutenticado.getRol());
        return new RestauranteResponseDTO(true, "Restaurante creado", creado);
    }
}
