package seguridad.infraestructura;

import org.mindrot.BCrypt;
import seguridad.aplicacion.VerificadorDeClave;

/**
 * Adaptador bcrypt. Reutiliza la misma libreria con la que HU-01 cifra la
 * clave al crear el propietario, de modo que el hash generado alli es el que
 * se verifica aqui.
 */
public class VerificadorDeClaveBCrypt implements VerificadorDeClave {

    @Override
    public boolean coincide(String claveEnTextoPlano, String hashAlmacenado) {
        if (claveEnTextoPlano == null || hashAlmacenado == null || hashAlmacenado.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(claveEnTextoPlano, hashAlmacenado);
        } catch (IllegalArgumentException ex) {
            // Hash con formato invalido: se trata como credencial incorrecta,
            // nunca como error tecnico expuesto al cliente.
            return false;
        }
    }
}
