package controller;

import model.Propietario;
import seguridad.aplicacion.Operacion;
import seguridad.aplicacion.PoliticaDeAutorizacion;
import service.PropietarioService;
import service.RolAutenticado;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Endpoint de creacion de propietario (HU-01), protegido por HU-05.
 *
 * La guia del Roll 2 exige que la creacion de propietario sea accesible solo
 * para ADMINISTRADOR. Antes de HU-05 esa regla existia dentro del servicio pero
 * no habia endpoint que la expusiera.
 */
public class PropietarioController {

    private final PropietarioService propietarioService;
    private final PoliticaDeAutorizacion politicaDeAutorizacion;

    public PropietarioController(PropietarioService propietarioService,
                                 PoliticaDeAutorizacion politicaDeAutorizacion) {
        this.propietarioService = propietarioService;
        this.politicaDeAutorizacion = politicaDeAutorizacion;
    }

    public PropietarioResponseDTO crearPropietario(PropietarioRequestDTO request,
                                                   RolAutenticado usuarioAutenticado) {
        // 1. Autorizacion por rol: lanza AccesoDenegadoException -> 403.
        politicaDeAutorizacion.exigir(usuarioAutenticado, Operacion.CREAR_PROPIETARIO);

        if (request == null) {
            throw new IllegalArgumentException("Body requerido");
        }

        // 2. Construccion del dominio. El rol NO se lee del request.
        Propietario propietario = new Propietario(
                request.getNombre(),
                request.getApellido(),
                request.getDocumentoDeIdentidad(),
                request.getCelular(),
                fecha(request.getFechaNacimiento()),
                request.getCorreo(),
                request.getClave());

        // 3. El servicio valida, cifra con bcrypt y persiste.
        Propietario creado = propietarioService.crearPropietario(
                propietario, usuarioAutenticado.getRol());

        return new PropietarioResponseDTO(true, "Propietario creado", creado);
    }

    private LocalDate fecha(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento debe tener el formato aaaa-mm-dd");
        }
    }
}
