package seguridad.aplicacion;

/** Vigencia acordada por el equipo para el token de sesion. */
public final class ProveedorDeTokenVigencia {
    /** 8 horas: cubre una jornada sin obligar a reautenticar en la demo. */
    public static final long SEGUNDOS = 8 * 60 * 60;

    private ProveedorDeTokenVigencia() {
    }
}
