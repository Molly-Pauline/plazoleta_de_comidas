package controller;

import model.Plato;
import repository.PlatoRepository;
import repository.RestauranteRepository;
import service.PlatoService;
import service.RolAutenticado;

public class PlatoController {
    private final PlatoService service;

    public PlatoController(PlatoRepository platoRepository, RestauranteRepository restauranteRepository) {
        this.service = new PlatoService(platoRepository, restauranteRepository);
    }

    public PlatoResponseDTO crearPlato(PlatoRequestDTO request, RolAutenticado usuarioAutenticado) {
        if (request == null) {
            throw new IllegalArgumentException("Solicitud de plato requerida");
        }
        Plato plato = new Plato(
                request.getNombre(),
                request.getPrecio(),
                request.getDescripcion(),
                request.getUrlImagen(),
                request.getCategoria(),
                request.getIdRestaurante()
        );
        return new PlatoResponseDTO(true, "Plato creado", service.crearPlato(plato, usuarioAutenticado));
    }

    public PlatoResponseDTO modificarPlato(Long idPlato, PlatoUpdateRequestDTO request,
                                           RolAutenticado usuarioAutenticado) {
        if (request == null) {
            throw new IllegalArgumentException("Solicitud de modificaci??n requerida");
        }
        Plato actualizado = service.modificarPlato(
                idPlato, request.getPrecio(), request.getDescripcion(), usuarioAutenticado);
        return new PlatoResponseDTO(true, "Plato modificado", actualizado);
    }
}
