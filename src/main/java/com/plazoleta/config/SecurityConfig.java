package main.java.com.plazoleta.config;

import com.plazoleta.security.BearerTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Define autenticación sin estado y la matriz de roles permitidos para cada ruta HTTP. */
/** Define autenticación sin estado y la matriz de roles permitidos para cada ruta HTTP. */
@Configuration
public class SecurityConfig {

    /** Desactiva la autenticación de usuario/contraseña: la aplicación solo confía en Bearer verificado. */
    @Bean
    UserDetailsService disabledUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("La autenticacion se realiza mediante Bearer token");
        };
    }

    /** Construye la cadena de seguridad que valida credenciales y autoriza cada operación de negocio. */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter bearerTokenFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // API stateless: el cliente debe presentar la identidad en cada solicitud, sin sesión de servidor.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Si una ruta protegida llega sin principal, se responde 401 en lugar de iniciar un login web.
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) ->
                    response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests(authorize -> authorize
                        // El endpoint de salud es el único acceso público de esta lista.
                        .requestMatchers("/actuator/health").permitAll()
                        // Usuarios solo puede validar una relación de propiedad con identidad de propietario.
                        .requestMatchers(HttpMethod.GET, "/restaurantes/*/propietario/*").hasRole("PROPIETARIO")
                        // El catálogo público para clientes autenticados incluye listado y menú del restaurante.
                        .requestMatchers(HttpMethod.GET, "/restaurantes", "/restaurantes/*/platos").hasRole("CLIENTE")
                        // HU11 reserva la creación de pedidos a clientes.
                        .requestMatchers(HttpMethod.POST, "/pedidos").hasRole("CLIENTE")
                        // HU12 reserva la consulta de pedidos a empleados; el servicio limita además por restaurante firmado.
                        .requestMatchers(HttpMethod.GET, "/pedidos").hasRole("EMPLEADO")
                        // HU2 permite que solo administración registre restaurantes.
                        .requestMatchers(HttpMethod.POST, "/restaurantes").hasRole("ADMINISTRADOR")
                        // HU3, HU4 y HU7 exigen rol de propietario; el servicio confirma la propiedad del recurso.
                        .requestMatchers(HttpMethod.POST, "/platos").hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.PATCH, "/platos/*/estado").hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.PUT, "/platos/**").hasRole("PROPIETARIO")
                        // Todo endpoint no listado sigue requiriendo autenticación, aunque no tenga regla específica.
                        .anyRequest().authenticated())
                    // Ejecuta la verificación HMAC antes del filtro estándar de usuario y contraseña.
                .addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}