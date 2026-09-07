package seguridad.infraestructura;

import seguridad.aplicacion.ProveedorDeToken;
import seguridad.aplicacion.ProveedorDeTokenVigencia;
import seguridad.dominio.Credencial;
import seguridad.dominio.CredencialesInvalidasException;
import seguridad.dominio.Rol;
import service.RolAutenticado;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Token de sesion firmado con HMAC-SHA256, implementado solo con el JDK.
 *
 * DECISION TECNICA DEL EQUIPO: los anexos exigen login, bcrypt y roles, pero no
 * imponen JWT. Se eligio un token propio firmado porque:
 *   - No agrega dependencias al pom (el proyecto es Java plano, sin Spring).
 *   - Cumple lo que si se exige: que el cliente no pueda declarar su propio rol.
 *   - Es verificable y explicable en la sustentacion.
 *
 * Formato:  base64url(idUsuario:ROL:vencimientoEpochSegundos) + "." + base64url(HMAC)
 *
 * Lo importante: el rol viaja DENTRO de la parte firmada. Si el cliente lo
 * cambia, la firma deja de coincidir y el token se rechaza. Ese es exactamente
 * el agujero que tenia el sistema antes de HU-05, cuando el rol llegaba en un
 * encabezado X-Rol que cualquiera podia escribir.
 */
public class ProveedorDeTokenHmac implements ProveedorDeToken {

    private static final String ALGORITMO = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final byte[] secreto;
    private final long vigenciaEnSegundos;

    public ProveedorDeTokenHmac(String secreto) {
        this(secreto, ProveedorDeTokenVigencia.SEGUNDOS);
    }

    public ProveedorDeTokenHmac(String secreto, long vigenciaEnSegundos) {
        if (secreto == null || secreto.length() < 16) {
            throw new IllegalArgumentException(
                    "El secreto de firma debe tener al menos 16 caracteres");
        }
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
        this.vigenciaEnSegundos = vigenciaEnSegundos;
    }

    /**
     * Lee el secreto de la variable de entorno AUTH_SECRET.
     *
     * NUNCA se versiona un secreto en Git (checklist de PR: "sin secretos").
     * Si la variable no esta definida se genera uno aleatorio en memoria: el
     * servidor arranca para la demo, pero los tokens dejan de ser validos al
     * reiniciar, que es justo lo que se quiere en desarrollo.
     */
    public static ProveedorDeTokenHmac desdeEntorno() {
        String secreto = System.getenv("AUTH_SECRET");
        if (secreto == null || secreto.length() < 16) {
            byte[] aleatorio = new byte[32];
            new SecureRandom().nextBytes(aleatorio);
            secreto = ENCODER.encodeToString(aleatorio);
            System.err.println("[seguridad] AUTH_SECRET no definido; se genera un secreto efimero. "
                    + "Definalo antes de desplegar.");
        }
        return new ProveedorDeTokenHmac(secreto);
    }

    @Override
    public String emitir(Credencial credencial) {
        long vencimiento = Instant.now().getEpochSecond() + vigenciaEnSegundos;
        String cuerpo = credencial.getIdUsuario() + ":" + credencial.getRol().name() + ":" + vencimiento;
        String cuerpoCodificado = ENCODER.encodeToString(cuerpo.getBytes(StandardCharsets.UTF_8));
        return cuerpoCodificado + "." + ENCODER.encodeToString(firmar(cuerpoCodificado));
    }

    @Override
    public RolAutenticado verificar(String token) {
        if (token == null || token.isBlank()) {
            throw new CredencialesInvalidasException("No autenticado");
        }

        int separador = token.lastIndexOf('.');
        if (separador <= 0 || separador == token.length() - 1) {
            throw new CredencialesInvalidasException("Credencial invalida");
        }

        String cuerpoCodificado = token.substring(0, separador);
        byte[] firmaRecibida;
        byte[] cuerpo;
        try {
            firmaRecibida = DECODER.decode(token.substring(separador + 1));
            cuerpo = DECODER.decode(cuerpoCodificado);
        } catch (IllegalArgumentException ex) {
            throw new CredencialesInvalidasException("Credencial invalida");
        }

        // Comparacion en tiempo constante: evita distinguir firmas por el tiempo de respuesta.
        if (!MessageDigest.isEqual(firmaRecibida, firmar(cuerpoCodificado))) {
            throw new CredencialesInvalidasException("Credencial invalida");
        }

        String[] partes = new String(cuerpo, StandardCharsets.UTF_8).split(":");
        if (partes.length != 3) {
            throw new CredencialesInvalidasException("Credencial invalida");
        }

        long vencimiento;
        Long idUsuario;
        Rol rol;
        try {
            idUsuario = Long.valueOf(partes[0]);
            rol = Rol.valueOf(partes[1]);
            vencimiento = Long.parseLong(partes[2]);
        } catch (RuntimeException ex) {
            throw new CredencialesInvalidasException("Credencial invalida");
        }

        if (Instant.now().getEpochSecond() >= vencimiento) {
            throw new CredencialesInvalidasException("Credencial vencida");
        }

        return new RolAutenticado(idUsuario, rol.name());
    }

    private byte[] firmar(String cuerpoCodificado) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(secreto, ALGORITMO));
            return mac.doFinal(cuerpoCodificado.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el token", ex);
        }
    }
}
