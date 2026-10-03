package main.java.com.plazoleta.service;

/** Puerto de dominio para validar propietarios sin acoplar el servicio al cliente HTTP concreto. */
public interface UsuarioValidationPort {
    /** Informa si Usuarios reconoce el ID como cuenta con rol PROPIETARIO. */
    boolean existePropietarioValido(Long idPropietario);
}