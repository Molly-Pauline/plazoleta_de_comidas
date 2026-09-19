package seguridad.aplicacion;

import seguridad.dominio.AccesoDenegadoException;
import seguridad.dominio.Rol;
import service.RolAutenticado;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Matriz de autorizacion del Sprint 1, en UN solo lugar.
 *
 * Antes de HU-05 cada servicio comparaba cadenas ("ADMINISTRADOR".equals(...))
 * por su cuenta. Centralizarlo evita que una HU nueva olvide una regla.
 *
 *   Operacion            | ADMIN | PROPIETARIO | EMPLEADO | CLIENTE
 *   ---------------------|-------|-------------|----------|--------
 *   CREAR_PROPIETARIO    |  si   |     no      |    no    |   no
 *   CREAR_RESTAURANTE    |  si   |     no      |    no    |   no
 *   CREAR_PLATO          |  no   |    si (*)   |    no    |   no
 *   MODIFICAR_PLATO      |  no   |    si (*)   |    no    |   no
 *
 * (*) El rol PROPIETARIO NO basta: ademas hay que comprobar que el restaurante
 *     pertenece al propietario autenticado. Esa segunda comprobacion es de
 *     negocio y vive en PlatoService.verificarPertenencia().
 */
public class PoliticaDeAutorizacion {

    private static final Map<Operacion, Set<Rol>> MATRIZ = new EnumMap<>(Operacion.class);

    static {
        MATRIZ.put(Operacion.CREAR_PROPIETARIO, EnumSet.of(Rol.ADMINISTRADOR));
        MATRIZ.put(Operacion.CREAR_RESTAURANTE, EnumSet.of(Rol.ADMINISTRADOR));
        MATRIZ.put(Operacion.CREAR_PLATO, EnumSet.of(Rol.PROPIETARIO));
        MATRIZ.put(Operacion.MODIFICAR_PLATO, EnumSet.of(Rol.PROPIETARIO));
    }

    /** @return true si el rol puede ejecutar la operacion. */
    public boolean permite(Rol rol, Operacion operacion) {
        if (rol == null || operacion == null) {
            return false;
        }
        return MATRIZ.getOrDefault(operacion, EnumSet.noneOf(Rol.class)).contains(rol);
    }

    /**
     * Version imperativa para usar en controladores.
     * @throws AccesoDenegadoException si el rol no alcanza.
     */
    public void exigir(RolAutenticado usuario, Operacion operacion) {
        if (usuario == null || usuario.getRol() == null) {
            throw new AccesoDenegadoException("Acceso denegado: no autenticado");
        }
        Rol rol;
        try {
            rol = Rol.desdeTexto(usuario.getRol());
        } catch (RuntimeException ex) {
            throw new AccesoDenegadoException("Acceso denegado: rol no reconocido");
        }
        if (!permite(rol, operacion)) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: el rol " + rol + " no puede ejecutar " + operacion);
        }
    }
}
