package main.java.com.plazoleta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/** Traduce fallos de negocio y validación de entrada a respuestas HTTP Problem Details. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Conserva el estado y el mensaje definidos por la regla de negocio que lanzó la excepción. */
    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomainException(DomainException exception) {
        return ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
    }

    /** Reúne errores de campos distintos sin repetir mensajes y responde con estado 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
        // Cada error incluye el nombre del campo para que el consumidor identifique qué dato debe corregir.
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    }
}