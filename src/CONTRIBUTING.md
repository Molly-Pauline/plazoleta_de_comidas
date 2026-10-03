# Contribuir

1. Crear cada rama `feature/HU-XX-*` desde `develop`.
2. Mantener cada cambio limitado a su historia de usuario.
3. Ejecutar `mvn test` antes de abrir un pull request.
4. No confirmar secretos, `.env`, `target/` ni archivos de IDE.
5. Integrar cambios a `develop` mediante revisión; crear `release/sprint-1` desde `develop` al cerrar el Sprint 1 y fusionar la release a `main`.

Formato de commit: `tipo(ambito): descripcion [HU-XX]`, por ejemplo `feat(restaurante): validar propietario [HU-02]`.