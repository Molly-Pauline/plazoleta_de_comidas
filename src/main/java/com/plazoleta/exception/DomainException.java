package main.java.com.plazoleta.exception;

import org.springframework.http.HttpStatus;

/** Excepción de negocio que transporta tanto el estado HTTP como el mensaje que debe recibir el cliente. */
public class DomainException extends RuntimeException {

    /** Estado HTTP asociado a la regla de negocio que no se pudo cumplir. */
    private final HttpStatus status;

    /** Construye el error con el estado y detalle que traducirá el advice REST. */
    public DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /** Permite al manejador REST preservar el estado elegido por la capa de servicio. */
    public HttpStatus getStatus() {
        return status;
    }
}