package main.java.com.plazoleta.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Valida el token compartido de autenticación entre servicios internos. */
/** Valida el token compartido de autenticación entre servicios internos. */
@Component
public class InternalServiceTokenVerifier {

    /** Forma binaria del valor configurado para compararlo sin filtrar coincidencias parciales. */
    private final byte[] expectedToken;

    /** Convierte la configuración en bytes UTF-8, sin imprimir ni devolver el token. */
    public InternalServiceTokenVerifier(@Value("${app.internal.service-token:}") String configuredToken) {
        this.expectedToken = configuredToken.getBytes(StandardCharsets.UTF_8);
    }

    /** Acepta únicamente secretos configurados con longitud mínima y comparación de tiempo constante. */
    public boolean isValid(String suppliedToken) {
        // La longitud mínima reduce el riesgo de usar accidentalmente una credencial interna débil.
        if (expectedToken.length < 32 || suppliedToken == null || suppliedToken.isBlank()) {
            return false;
        }
        // La comparación constante limita la información temporal sobre cuántos bytes coinciden.
        return MessageDigest.isEqual(expectedToken, suppliedToken.getBytes(StandardCharsets.UTF_8));
    }
}
