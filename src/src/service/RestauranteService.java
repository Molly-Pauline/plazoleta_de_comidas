package service;

import model.Restaurante;
import repository.RestauranteRepository;

public class RestauranteService {
    private final RestauranteRepository repository;
    private final UsuarioValidationPort usuarioValidationPort;

    public RestauranteService(RestauranteRepository repository, UsuarioValidationPort usuarioValidationPort) {
        this.repository = repository;
        this.usuarioValidationPort = usuarioValidationPort;
    }

    public Restaurante crearRestaurante(Restaurante restaurante, String rolAutenticado) {
        if (!"ADMINISTRADOR".equals(rolAutenticado)) {
            throw new SecurityException("Solo ADMINISTRADOR puede crear restaurante");
        }

        if (restaurante == null ||
                restaurante.getNombre() == null || restaurante.getNombre().isBlank() ||
                restaurante.getNit() == null || restaurante.getNit().isBlank() ||
                restaurante.getDireccion() == null || restaurante.getDireccion().isBlank() ||
                restaurante.getTelefono() == null || restaurante.getTelefono().isBlank() ||
                restaurante.getUrlLogo() == null || restaurante.getUrlLogo().isBlank() ||
                restaurante.getIdPropietario() == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios: nombre, NIT, dirección, teléfono, URL del logo e id del propietario");
        }

        if (!restaurante.getNit().matches("^\\d+$")) {
            throw new IllegalArgumentException("NIT debe contener únicamente números");
        }

        if (!restaurante.getTelefono().matches("^\\+?\\d{1,13}$") || restaurante.getTelefono().length() > 13) {
            throw new IllegalArgumentException("Teléfono máximo 13 caracteres y puede contener +");
        }

        if (restaurante.getNombre().matches("^\\d+$")) {
            throw new IllegalArgumentException("El nombre no puede estar compuesto solo por números");
        }

        if (repository.existsByNit(restaurante.getNit())) {
            throw new IllegalArgumentException("Ya existe un restaurante con ese NIT");
        }

        if (!usuarioValidationPort.existePropietarioValido(restaurante.getIdPropietario())) {
            throw new IllegalArgumentException("El propietario asociado no existe o no tiene rol PROPIETARIO");
        }

        return repository.save(restaurante);
    }
}
