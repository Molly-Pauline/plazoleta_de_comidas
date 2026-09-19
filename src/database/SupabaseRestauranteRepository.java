package database;

import model.Restaurante;
import repository.RestauranteRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseRestauranteRepository extends RestauranteRepository {
    private final SupabaseConfig config;
    private final HttpClient httpClient;

    public SupabaseRestauranteRepository(SupabaseConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public Restaurante save(Restaurante restaurante) {
        try {
            String json = "{"
                    + "\"nombre\":\"" + restaurante.getNombre() + "\","
                    + "\"nit\":\"" + restaurante.getNit() + "\","
                    + "\"direccion\":\"" + restaurante.getDireccion() + "\","
                    + "\"telefono\":\"" + restaurante.getTelefono() + "\","
                    + "\"url_logo\":\"" + restaurante.getUrlLogo() + "\","
                    + "\"id_propietario\":" + restaurante.getIdPropietario()
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl() + "/restaurantes"))
                    .header("apikey", config.getServiceRoleKey())
                    .header("Authorization", "Bearer " + config.getServiceRoleKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Supabase no respondió correctamente: " + response.statusCode() + " - " + response.body());
            }
            return restaurante;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo guardar el restaurante en Supabase", e);
        }
    }
}
