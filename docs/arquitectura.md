# Arquitectura

El repositorio contiene el microservicio Plazoleta como aplicación Spring Boot independiente. Usuarios sigue siendo otro servicio y emite credenciales firmadas; Plazoleta verifica esas credenciales localmente con HMAC-SHA256.

## Capas

- `controller`: API REST y conversión de contratos HTTP.
- `dto/request` y `dto/response`: contratos HTTP de entrada y salida.
- `service`: contratos y reglas de HU2-HU4 y control de pertenencia.
- `service/impl`: implementaciones de los contratos de servicio.
- `entity`: entidades JPA `Restaurante` y `Plato`.
- `repository`: persistencia mediante Spring Data JPA.
- `security`: verificación de token Bearer y creación del principal autenticado.
- `config`: autorización de rutas y configuración de seguridad.
- `exception`: traducción de errores de negocio y validación a respuestas HTTP.

## Persistencia

MySQL (`plazoleta_bd`) es la base de ejecución local; `SPRING_DATASOURCE_URL` y credenciales externas pueden cambiarse por entorno. H2 se utiliza solo en pruebas. `Restaurante` guarda `idPropietario` como referencia al servicio Usuarios; `Plato` tiene una relación muchos-a-uno con `Restaurante`. Las migraciones de producción deben sustituir `ddl-auto=update`.

## Seguridad

Se verifica el formato compartido con Usuarios: `base64url(idUsuario:ROL:vencimientoEpochSegundos).base64url(HMAC-SHA256)`. La firma se compara en tiempo constante, se exige vencimiento vigente y el rol procede únicamente del contenido firmado. `AUTH_SECRET` debe ser idéntico en ambos servicios; si falta, la app genera un secreto efímero y los tokens de Usuarios no serán aceptados.