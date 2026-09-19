package service;

import model.Plato;
import model.Restaurante;
import repository.PlatoRepository;
import repository.RestauranteRepository;

public class PlatoService {
    private final PlatoRepository platoRepository;
    private final RestauranteRepository restauranteRepository;

    public PlatoService(PlatoRepository platoRepository, RestauranteRepository restauranteRepository) {
        this.platoRepository = platoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    public Plato crearPlato(Plato plato, RolAutenticado usuarioAutenticado) {
        validarPropietario(usuarioAutenticado);
        validarDatosCreacion(plato);
        verificarPertenencia(plato.getIdRestaurante(), usuarioAutenticado.getIdUsuario());
        return platoRepository.save(plato);
    }

    public Plato modificarPlato(Long idPlato, Integer precio, String descripcion,
                                RolAutenticado usuarioAutenticado) {
        validarPropietario(usuarioAutenticado);
        validarPrecio(precio);
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripci??n es obligatoria");
        }

        Plato plato = platoRepository.findById(idPlato)
                .orElseThrow(() -> new IllegalArgumentException("El plato no existe"));
        verificarPertenencia(plato.getIdRestaurante(), usuarioAutenticado.getIdUsuario());
        plato.actualizarPrecioYDescripcion(precio, descripcion);
        return platoRepository.update(plato);
    }

    private void validarPropietario(RolAutenticado usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getRol() == null) {
            throw new SecurityException("Acceso denegado: no autenticado");
        }
        if (!"PROPIETARIO".equals(usuarioAutenticado.getRol())) {
            throw new SecurityException("Acceso denegado: solo PROPIETARIO puede gestionar platos");
        }
        if (usuarioAutenticado.getIdUsuario() == null || usuarioAutenticado.getIdUsuario() <= 0) {
            throw new SecurityException("Acceso denegado: identidad inv??lida");
        }
    }

    private void validarDatosCreacion(Plato plato) {
        if (plato == null || esVacio(plato.getNombre()) || plato.getPrecio() == null
                || esVacio(plato.getDescripcion()) || esVacio(plato.getUrlImagen())
                || esVacio(plato.getCategoria()) || plato.getIdRestaurante() == null) {
            throw new IllegalArgumentException(
                    "Todos los campos son obligatorios: nombre, precio, descripci??n, URL de imagen, categor??a e id del restaurante");
        }
        validarPrecio(plato.getPrecio());
        if (!restauranteRepository.findById(plato.getIdRestaurante()).isPresent()) {
            throw new IllegalArgumentException("El restaurante no existe");
        }
    }

    private void validarPrecio(Integer precio) {
        if (precio == null || precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser un entero mayor que cero");
        }
    }

    private void verificarPertenencia(Long idRestaurante, Long idPropietario) {
        Restaurante restaurante = restauranteRepository.findById(idRestaurante)
                .orElseThrow(() -> new IllegalArgumentException("El restaurante no existe"));
        if (!idPropietario.equals(restaurante.getIdPropietario())) {
            throw new SecurityException("Acceso denegado: el restaurante no pertenece al propietario");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
