package main.java.com.plazoleta.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

/** Verifica tokens Bearer compatibles con el formato firmado por el microservicio Usuarios. */
@Component
public class HmacTokenVerifier {

    /** Algoritmo interoperable que ambos servicios usan para firmar el contenido del token. */
    private static final String ALGORITHM = "HmacSHA256";
    /** Decodificador Base64 URL, que permite transportar el token sin caracteres problemáticos en HTTP. */
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    /** Lista cerrada de roles aceptables para evitar autorizar valores arbitrarios firmados. */
    private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "PROPIETARIO", "EMPLEADO", "CLIENTE");
    /** Clave binaria compartida con Usuarios; nunca se registra ni se expone en mensajes de error. */
    private final byte[] secret;

    /** Prepara la clave compartida y evita operar con una configuración vacía o demasiado corta. */
    public HmacTokenVerifier(@Value("${app.auth.secret:}") String configuredSecret) {
        // Con secreto ausente se crea uno efímero: la app puede arrancar, pero no aceptará tokens de otra instancia.
        String effectiveSecret = configuredSecret;
        if (effectiveSecret == null || effectiveSecret.length() < 16) {
            // Se generan 32 bytes aleatorios y se codifican sin padding para mantener una cadena transportable.
            byte[] randomSecret = new byte[32];
            new SecureRandom().nextBytes(randomSecret);
            effectiveSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomSecret);
        }
        // Usuarios firma los bytes UTF-8 del secreto configurado, por lo que aquí se usan los mismos bytes.
        this.secret = effectiveSecret.getBytes(StandardCharsets.UTF_8);
    }

    /** Valida estructura, firma, vencimiento, rol e identidad del token antes de crear el principal. */
    public AuthenticatedUser verify(String token) {
        // Un encabezado Bearer vacío no identifica a ningún usuario y se rechaza antes de decodificar.
        if (token == null || token.isBlank()) {
            throw new InvalidBearerTokenException();
        }

        // El punto separa el cuerpo codificado de la firma; ambas partes deben estar presentes.
        int separator = token.lastIndexOf('.');
        if (separator <= 0 || separator == token.length() - 1) {
            throw new InvalidBearerTokenException();
        }

        // Se firma el cuerpo codificado exacto para que productor y consumidor calculen la misma HMAC.
        String encodedBody = token.substring(0, separator);
        try {
            // El orden de las partes coincide con Usuarios: Base64URL(cuerpo).Base64URL(HMAC(cuerpo codificado)).
            byte[] suppliedSignature = DECODER.decode(token.substring(separator + 1));
            byte[] body = DECODER.decode(encodedBody);
            // Comparación de tiempo constante para no filtrar información sobre prefijos correctos de la firma.
            if (!MessageDigest.isEqual(suppliedSignature, sign(encodedBody))) {
                throw new InvalidBearerTokenException();
            }

            // Formato compartido: id:rol:vencimientoEpochSegundos y, opcionalmente, idRestaurante.
            String[] fields = new String(body, StandardCharsets.UTF_8).split(":", -1);
            // Tres campos mantienen compatibilidad con tokens anteriores; cuatro incluyen la asociación del empleado.
            if (fields.length != 3 && fields.length != 4) {
                throw new InvalidBearerTokenException();
            }
            // Se convierten los valores solo después de autenticar la firma, de modo que la identidad no es editable.
            long userId = Long.parseLong(fields[0]);
            long expiresAt = Long.parseLong(fields[2]);
            // Rechaza identificadores inválidos, tokens vencidos y roles fuera del conjunto reconocido.
            if (userId <= 0 || Instant.now().getEpochSecond() >= expiresAt || !ROLES.contains(fields[1])) {
                throw new InvalidBearerTokenException();
            }
            // El guion representa ausencia de restaurante; un ID presente queda protegido por la firma HMAC.
            Long restaurantId = fields.length == 4 && !"-".equals(fields[3]) ? Long.valueOf(fields[3]) : null;
            return new AuthenticatedUser(userId, fields[1], restaurantId);
        } catch (IllegalArgumentException exception) {
            // Errores de Base64 o de conversión numérica se presentan uniformemente como token inválido.
            throw new InvalidBearerTokenException();
        }
    }

    /** Calcula la firma sobre el cuerpo codificado con la clave común de Usuarios y Plazoleta. */
    private byte[] sign(String encodedBody) {
        try {
            // Mac se crea por operación porque no es seguro compartir una instancia mutable entre hilos.
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            // La entrada UTF-8 exacta debe coincidir con la entrada utilizada por el emisor del token.
            return mac.doFinal(encodedBody.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            // Un fallo criptográfico interno no se confunde con una firma simplemente inválida.
            throw new IllegalStateException("No se pudo verificar la credencial", exception);
        }
    }
}