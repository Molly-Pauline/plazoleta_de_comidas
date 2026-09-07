package database;

public class SupabaseConfig {
    private final String url;
    private final String anonKey;
    private final String serviceRoleKey;

    public SupabaseConfig(String url, String anonKey, String serviceRoleKey) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("SUPABASE_URL es obligatorio");
        }
        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            throw new IllegalArgumentException("SUPABASE_SERVICE_ROLE_KEY es obligatorio");
        }
        this.url = url.replaceAll("/+$", "");
        this.anonKey = anonKey;
        this.serviceRoleKey = serviceRoleKey;
    }

    public static SupabaseConfig fromEnvironment() {
        return new SupabaseConfig(
                System.getenv("SUPABASE_URL"),
                System.getenv("SUPABASE_ANON_KEY"),
                System.getenv("SUPABASE_SERVICE_ROLE_KEY")
        );
    }

    public String getUrl() {
        return url;
    }

    public String getAnonKey() {
        return anonKey;
    }

    public String getServiceRoleKey() {
        return serviceRoleKey;
    }
}
