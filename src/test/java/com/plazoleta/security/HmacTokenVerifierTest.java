package test.java.com.plazoleta.security;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de formato HMAC, identidad firmada y rechazo de tokens alterados o vencidos. */
class HmacTokenVerifierTest {

    /** Clave sintética de prueba, nunca destinada a configuración de despliegue. */
    private static final String SECRET = "local-test-secret-with-32-chars";
    /** El verificador comparte la clave con el emisor auxiliar para probar interoperabilidad. */
    private final HmacTokenVerifier verifier = new HmacTokenVerifier(SECRET);

    /** Acepta el formato histórico de tres campos sin asociación de restaurante. */
     @Test
    void acceptsValidTokenAndReturnsSignedIdentity() {
        // El vencimiento futuro aísla el caso de credencial válida.
        AuthenticatedUser user = verifier.verify(token("42:PROPIETARIO:" + (Instant.now().getEpochSecond() + 60)));

        // La identidad resultante debe reproducir el ID y rol que protegía la firma.
        assertEquals(new AuthenticatedUser(42L, "PROPIETARIO"), user);
    }

    /** Acepta el cuarto campo firmado que vincula a un empleado con su restaurante para HU12. */
     @Test
    void acceptsEmployeeTokenWithSignedRestaurantId() {
        AuthenticatedUser user = verifier.verify(token("42:EMPLEADO:"
                + (Instant.now().getEpochSecond() + 60) + ":9"));

        // El ID de restaurante autenticado queda disponible en el principal para limitar consultas.
        assertEquals(new AuthenticatedUser(42L, "EMPLEADO", 9L), user);
    }

    /** Rechaza una firma manipulada aunque el cuerpo conserve una estructura válida. */
     @Test
    void rejectsTokenWithModifiedSignature() {
        String validToken = token("42:PROPIETARIO:" + (Instant.now().getEpochSecond() + 60));
        int separator = validToken.lastIndexOf('.');
        char firstSignatureCharacter = validToken.charAt(separator + 1);
        char changedCharacter = firstSignatureCharacter == 'A' ? 'B' : 'A';
        // Se cambia un carácter de la firma conservando el cuerpo original.
        String tamperedToken = validToken.substring(0, separator + 1) + changedCharacter
            + validToken.substring(separator + 2);

        // El payload no puede seguir autenticando una firma distinta de la calculada por HMAC.
        assertThrows(InvalidBearerTokenException.class,
            () -> verifier.verify(tamperedToken));
    }

    /** Rechaza una credencial cuyo vencimiento ya quedó en el pasado. */
     @Test
    void rejectsExpiredToken() {
        // El vencimiento anterior al segundo actual debe invalidar el token.
        assertThrows(InvalidBearerTokenException.class,
                () -> verifier.verify(token("42:PROPIETARIO:" + (Instant.now().getEpochSecond() - 1))));
    }

    /** Emula el emisor Usuarios para verificar el mismo algoritmo, codificación y bytes firmados. */
    private String token(String body) {
        // El cuerpo se codifica con Base64 URL sin padding antes de calcular la HMAC.
        String encodedBody = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(body.getBytes(StandardCharsets.UTF_8));
        try {
            // Ambos servicios deben usar el mismo nombre de algoritmo y secreto en bytes UTF-8.
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            // Se firma el cuerpo codificado y se vuelve a representar la firma en Base64 URL sin padding.
            String signature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(encodedBody.getBytes(StandardCharsets.UTF_8)));
            return encodedBody + "." + signature;
        } catch (Exception exception) {
            // Un error criptográfico invalida la preparación del test y no debe quedar oculto.
            throw new IllegalStateException(exception);
        }
    }
}