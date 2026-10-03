package main.java.com.plazoleta.service;

import com.plazoleta.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Implementa la consulta HTTP de validación de propietario hacia el microservicio Usuarios. */
@Component
public class RestClientUsuarioValidationAdapter implements UsuarioValidationPort {

    /** Cliente HTTP configurado con la URL base del servicio Usuarios. */
    private final RestClient restClient;
    /** Ruta parametrizada que Usuarios expone para validar el ID de propietario. */
    private final String validationPath;
    /** Credencial interna para que Usuarios autentique esta llamada entre servicios. */
    private final String serviceToken;

    /** Construye el cliente y recibe configuración externa sin incluir secretos en el código. */
    public RestClientUsuarioValidationAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${app.usuarios.base-url}") String baseUrl,
            @Value("${app.usuarios.owner-validation-path}") String validationPath,
            @Value("${app.usuarios.service-token:}") String serviceToken) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.validationPath = validationPath;
        this.serviceToken = serviceToken;
    }

    /** Traduce la respuesta de Usuarios al puerto booleano que consume la regla HU2. */
    @Override
    public boolean existePropietarioValido(Long idPropietario) {
        try {
            // Se sustituye el parámetro de ruta con el ID consultado y se adjunta la credencial interna configurada.
            Boolean valido = restClient.get()
                    .uri(validationPath, idPropietario)
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .body(Boolean.class);
            // Un cuerpo nulo no cuenta como confirmación positiva de propiedad.
            return Boolean.TRUE.equals(valido);
        } catch (HttpClientErrorException.NotFound exception) {
            // Usuarios puede representar una cuenta inexistente como 404; para HU2 equivale a propietario inválido.
            return false;
        } catch (RestClientException exception) {
            // Un fallo de red no se interpreta como respuesta negativa: la dependencia no está disponible.
            throw new DomainException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible validar el propietario en el servicio de Usuarios");
        }
    }
}