package seguridad.aplicacion;

import seguridad.dominio.Rol;

/**
 * Respuesta del login. No contiene la clave ni el hash: solo lo necesario
 * para que el cliente use el token.
 */
public class ResultadoLogin {
    private final String token;
    private final Long idUsuario;
    private final Rol rol;
    private final long expiraEnSegundos;

    public ResultadoLogin(String token, Long idUsuario, Rol rol, long expiraEnSegundos) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.expiraEnSegundos = expiraEnSegundos;
    }

    public String getToken() { return token; }
    public Long getIdUsuario() { return idUsuario; }
    public Rol getRol() { return rol; }
    public long getExpiraEnSegundos() { return expiraEnSegundos; }
}
