package database;

public class SupabaseConnectionExample {
    public static void main(String[] args) {
    SupabaseConfig config = SupabaseConfig.fromEnvironment();

        System.out.println("Supabase URL configurada: " + config.getUrl());
        System.out.println("Listo para conectar con la base de datos de Supabase");
    }
}
