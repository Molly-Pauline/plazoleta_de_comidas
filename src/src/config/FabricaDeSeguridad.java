package config;

import repository.PropietarioRepository;
import seguridad.aplicacion.PoliticaDeAutorizacion;
import seguridad.aplicacion.ServicioDeAutenticacion;
import seguridad.dominio.Rol;
import seguridad.infraestructura.ProveedorDeTokenHmac;
import seguridad.infraestructura.RepositorioDeCredencialesEnMemoria;
import seguridad.infraestructura.VerificadorDeClaveBCrypt;

/**
 * Capa de configuracion: arma el grafo de objetos de seguridad en un solo sitio.
 *
 * Sin framework de inyeccion de dependencias, esta clase cumple el papel que en
 * Spring harian las anotaciones de configuracion. Tenerla separada permite que
 * las pruebas construyan la misma seguridad con credenciales conocidas.
 */
public class FabricaDeSeguridad {

    private final PropietarioRepository propietarioRepository;
    private final RepositorioDeCredencialesEnMemoria repositorioDeCredenciales;
    private final ServicioDeAutenticacion servicioDeAutenticacion;
    private final PoliticaDeAutorizacion politicaDeAutorizacion = new PoliticaDeAutorizacion();

    private FabricaDeSeguridad(PropietarioRepository propietarioRepository, String secreto) {
        this.propietarioRepository = propietarioRepository;
        this.repositorioDeCredenciales = new RepositorioDeCredencialesEnMemoria(propietarioRepository);
        this.servicioDeAutenticacion = new ServicioDeAutenticacion(
                repositorioDeCredenciales,
                new VerificadorDeClaveBCrypt(),
                secreto == null ? ProveedorDeTokenHmac.desdeEntorno() : new ProveedorDeTokenHmac(secreto));
    }

    /** Uso normal: secreto y administrador vienen del entorno. */
    public static FabricaDeSeguridad porDefecto() {
        FabricaDeSeguridad fabrica = new FabricaDeSeguridad(new PropietarioRepository(), null);
        fabrica.repositorioDeCredenciales.sembrarAdministrador();
        return fabrica;
    }

    /**
     * Uso en pruebas y demo: secreto fijo y sin administrador sembrado, para que
     * cada prueba registre las cuentas que necesita.
     */
    public static FabricaDeSeguridad paraPruebas(String secreto) {
        return new FabricaDeSeguridad(new PropietarioRepository(), secreto);
    }

    /** Registra una cuenta (por ejemplo el ADMINISTRADOR) con clave conocida. */
    public FabricaDeSeguridad conCuenta(Long id, String correo, String clave, Rol rol) {
        repositorioDeCredenciales.registrar(id, correo, clave, rol);
        return this;
    }

    public PropietarioRepository getPropietarioRepository() { return propietarioRepository; }
    public RepositorioDeCredencialesEnMemoria getRepositorioDeCredenciales() { return repositorioDeCredenciales; }
    public ServicioDeAutenticacion getServicioDeAutenticacion() { return servicioDeAutenticacion; }
    public PoliticaDeAutorizacion getPoliticaDeAutorizacion() { return politicaDeAutorizacion; }
}
