# Plazoleta de comidas

Microservicio REST para restaurantes y platos, migrado a Spring Boot 3.5.5 con Java 17. Este repositorio corresponde al Repo B; el microservicio de Usuarios continúa en su repositorio separado.

## Requisitos

- JDK 17
- Maven 3.9 o Maven Wrapper

## Ejecutar

```powershell
mvn test
$env:SPRING_DATASOURCE_USERNAME = "root"
$securePassword = Read-Host "MySQL password" -AsSecureString
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
mvn spring-boot:run
```

La aplicación escucha en `http://localhost:8080` y se conecta a MySQL en `jdbc:mysql://localhost:3306/plazoleta_bd`. Configura `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` como variables del proceso antes de ejecutarla. `.env.example` documenta los valores esperados, pero Spring no carga archivos `.env` automáticamente. Para probar localmente sin MySQL, los tests usan H2.

El esquema inicial compatible con las entidades JPA está en [`database/plazoleta_schema.sql`](database/plazoleta_schema.sql). Ejecútalo en MySQL Workbench conectado al servidor local antes de arrancar los servicios. Para validar el esquema contra las entidades, inicia ambos servicios con `DDL_AUTO=validate`; `update` puede quedar para una base local descartable, no como estrategia de migración.

El script usa `CREATE TABLE IF NOT EXISTS`: no modifica tablas antiguas ya existentes. Si ejecutaste una versión previa del esquema, respáldala y migra las tablas/columnas antes de continuar; no elimines datos necesarios para la entrega.

## API

Todas las rutas de negocio reciben `Authorization: Bearer <token>` emitido por Usuarios. Plazoleta valida la firma HMAC y obtiene el identificador y rol desde el token firmado; no confía en `X-Rol` ni `X-User-Id`.

| Método | Ruta | Permiso | Historia |
| --- | --- | --- | --- |
| `POST` | `/restaurantes` | `ADMINISTRADOR` | HU2 |
| `POST` | `/platos` | `PROPIETARIO` del restaurante | HU3 |
| `PUT` | `/platos/{idPlato}` | `PROPIETARIO` del restaurante | HU4 |
| `PATCH` | `/platos/{idPlato}/estado` | `PROPIETARIO` del restaurante | HU7 |
| `GET` | `/restaurantes?page=0&size=10` | `CLIENTE` | HU9 |
| `GET` | `/restaurantes/{id}/platos?page=0&size=10&categoria=...` | `CLIENTE` | HU10 |
| `POST` | `/pedidos` | `CLIENTE` | HU11 |
| `GET` | `/pedidos?estado=PENDIENTE&page=0&size=10` | `EMPLEADO` del restaurante | HU12 |
| `GET` | `/actuator/health` | Público | Salud |

HU4 solo admite `precio` y `descripcion`; los demás datos del plato se conservan. HU2 comprueba que el usuario asociado exista y tenga rol `PROPIETARIO` mediante `GET /api/v1/usuarios/{id}/validacion-propietario` de Usuarios. HU6 valida la asociación empleado/restaurante mediante un token Bearer reenviado a Plazoleta; el token del empleado incluye `idRestaurante` firmado. HU11 impide otro pedido mientras exista uno `PENDIENTE`, `EN_PREPARACION` o `LISTO`; HU12 filtra por estado y limita la consulta al restaurante firmado.

La colección Postman está en [`postman/Plazoleta.postman_collection.json`](postman/Plazoleta.postman_collection.json), con environment importable en [`postman/Plazoleta.local.postman_environment.json`](postman/Plazoleta.local.postman_environment.json). Para flujo completo, importa también la colección y environment de Usuarios, corre Usuarios en `8081` y Plazoleta en `8080`, e inicia sesión para llenar `token`. Ambos procesos deben compartir el mismo `AUTH_SECRET`.

## Configuración

- `AUTH_SECRET`: secreto compartido con Usuarios; ambos servicios deben usar el mismo valor.
- `INTERNAL_SERVICE_TOKEN`: token aleatorio de al menos 32 caracteres, compartido para las llamadas internas Usuarios↔Plazoleta.
- `USUARIOS_BASE_URL`: URL base del servicio Usuarios (por defecto `http://localhost:8081`).
- `SPRING_DATASOURCE_URL`: por defecto `jdbc:mysql://localhost:3306/plazoleta_bd`.
- `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`: credenciales MySQL; no guardes la contraseña en Git.
- `DDL_AUTO`: estrategia Hibernate, por defecto `update` para desarrollo.
- `SQL_LOGGING`: activa el SQL de Hibernate cuando se establece en `true`.

No confirmes secretos ni archivos `.env`. En despliegue usa un gestor de secretos y `DDL_AUTO=validate` junto con migraciones controladas.

## Ramas locales

Las historias 1–12 se distribuyen entre los dos repositorios independientes; la tabla completa de ramas está en [`docs/reporte-git.md`](docs/reporte-git.md).

## Estructura

El código Java está en `src/main/java/com/plazoleta` y se organiza en `config`, `controller`, `entity`, `dto/request`, `dto/response`, `exception`, `repository`, `security`, `service` y `service/impl`. Las pruebas están en `src/test/java/com/plazoleta`.