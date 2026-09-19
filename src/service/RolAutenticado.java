package service;

public class RolAutenticado {
    private final Long idUsuario;
    private final String rol;

    public RolAutenticado(Long idUsuario, String rol) {
        this.idUsuario = idUsuario;
        this.rol = rol;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getRol() {
        return rol;
    }
}
