package test.java.com.plazoleta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración de contexto y autorización HTTP usando la configuración H2 de tests. */
@SpringBootTest
@AutoConfigureMockMvc
class PlazoletaApplicationTests {

        /** Clave sintética exclusiva de pruebas, configurada también como secreto de autenticación del perfil test. */
    private static final String AUTH_SECRET = "test-secret-with-at-least-16-chars";

        /** Cliente MVC que ejecuta solicitudes contra los filtros y controladores reales de Spring. */
    @Autowired
    private MockMvc mockMvc;

        /** Comprueba que el contexto Spring Boot y sus beans se puedan construir. */
         @Test
    void contextLoads() {
    }

        /** Distingue una solicitud anónima (401) de una identidad autenticada con rol incorrecto (403) en HU12. */
         @Test
    void orderRoutesRequireAuthenticationAndEmployeeRole() throws Exception {
                // Sin Bearer no existe principal y el endpoint protegido debe rechazar la solicitud.
        mockMvc.perform(get("/pedidos").param("estado", "PENDIENTE"))
                .andExpect(status().isUnauthorized());
                // El token identifica un propietario válido, pero la ruta de consulta requiere rol EMPLEADO.
        mockMvc.perform(get("/pedidos").param("estado", "PENDIENTE")
                        .header("Authorization", "Bearer " + token(7, "PROPIETARIO", null)))
                .andExpect(status().isForbidden());
    }

        /** Comprueba que el endpoint interno requiere tanto identidad propietaria como credencial entre servicios. */
         @Test
    void serviceOwnershipEndpointRequiresBothProprietorAndInternalToken() throws Exception {
                // El Bearer de propietario no sustituye al token interno requerido para la llamada de Usuarios.
        mockMvc.perform(get("/restaurantes/1/propietario/7")
                        .header("Authorization", "Bearer " + token(7, "PROPIETARIO", null)))
                .andExpect(status().isUnauthorized());
        // Un token interno correcto no concede acceso a un principal CLIENTE, pues la ruta exige PROPIETARIO.
        mockMvc.perform(get("/restaurantes/1/propietario/7")
                        .header("Authorization", "Bearer " + token(7, "CLIENTE", null))
                        .header("X-Service-Token", "test-internal-service-token-at-least-32-chars"))
                .andExpect(status().isForbidden());
    }

        /** Verifica que la creación de restaurantes está reservada al rol ADMINISTRADOR (HU2). */
         @Test
    void onlyAdministratorCanCreateRestaurant() throws Exception {
                // El cliente autenticado debe recibir 403 antes de procesar el cuerpo enviado.
        mockMvc.perform(post("/restaurantes")
                        .header("Authorization", "Bearer " + token(7, "CLIENTE", null))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

        /** Emula el emisor de Usuarios para probar la compatibilidad del filtro con el formato HMAC compartido. */
    private String token(long userId, String role, Long restaurantId) throws Exception {
                // Un vencimiento futuro evita que una prueba de autorización falle por expiración accidental.
        long expiresAt = Instant.now().getEpochSecond() + 60;
                // El guion representa ausencia de restaurante; para empleados se serializa el ID que quedará firmado.
        String restaurant = restaurantId == null ? "-" : restaurantId.toString();
                // Este orden de campos es el contrato que comparten Usuarios y HmacTokenVerifier.
        String payload = userId + ":" + role + ":" + expiresAt + ":" + restaurant;
                // Se usa Base64 URL sin padding para que el contenido viaje en un encabezado HTTP.
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        // La firma se calcula sobre el payload ya codificado, con HmacSHA256 y los mismos bytes UTF-8.
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(AUTH_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        // El punto separa cuerpo y firma en el formato aceptado por el verificador de Plazoleta.
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8)));
        return encodedPayload + "." + signature;
    }
}