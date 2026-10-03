package main.java.com.plazoleta.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/** Convierte una credencial Bearer válida en la identidad de Spring Security para la solicitud actual. */
/** Convierte una credencial Bearer válida en la identidad de Spring Security para la solicitud actual. */
@Component
public class BearerTokenFilter extends OncePerRequestFilter {

    /** Valida la firma y el contenido temporal del token recibido. */
    private final HmacTokenVerifier tokenVerifier;
    /** Serializa una respuesta JSON uniforme cuando el token presentado es inválido. */
    private final ObjectMapper objectMapper;

    /** Recibe el verificador y el serializador administrados por Spring. */
    public BearerTokenFilter(HmacTokenVerifier tokenVerifier, ObjectMapper objectMapper) {
        this.tokenVerifier = tokenVerifier;
        this.objectMapper = objectMapper;
    }

    /** Inspecciona el encabezado, establece el principal autenticado o rechaza una credencial inválida. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Solo los encabezados con el esquema Bearer se validan aquí; Spring decide luego si la ruta exige identidad.
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // Sin token se continúa como anónimo para que la cadena de seguridad aplique el 401 donde corresponda.
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Se quita el prefijo exacto "Bearer " y se verifica el contenido restante.
            AuthenticatedUser user = tokenVerifier.verify(authorization.substring(7));
            // El principal contiene solo identidad autenticada; la autoridad ROLE_ permite usar hasRole en las rutas.
            var authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.rol())));
            // El contexto limita esta autenticación al procesamiento de la solicitud actual.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (InvalidBearerTokenException exception) {
            // Se limpia cualquier identidad previa y se detiene la cadena para no ejecutar el controlador.
            SecurityContextHolder.clearContext();
            // El cliente recibe un estado 401 y un JSON, sin detalles criptográficos ni valores secretos.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), Map.of("success", false, "message", "Credencial invalida"));
        }
    }
}