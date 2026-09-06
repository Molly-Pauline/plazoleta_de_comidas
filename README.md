# Plazoleta de comidas

## HU 2: Crear restaurante

### Requisitos principales
- Un usuario con rol `ADMINISTRADOR` puede crear restaurantes.
- Los campos obligatorios son: nombre, NIT, dirección, teléfono, URL del logo e id del propietario.
- El NIT debe contener únicamente dígitos.
- El teléfono debe tener máximo 13 caracteres y puede incluir `+`.
- El nombre no puede estar compuesto solo por números.
- El propietario asociado debe existir y tener rol `PROPIETARIO`.

### Contrato de creación
Request DTO de ejemplo:

```json
{
  "nombre": "La Casona",
  "nit": "123456789",
  "direccion": "Calle 123 #45-67",
  "telefono": "+573001234567",
  "urlLogo": "https://example.com/logo.png",
  "idPropietario": 10
}
```

Autenticación esperada:

```json
{
  "idUsuario": 10,
  "rol": "ADMINISTRADOR"
}
```

Respuesta esperada:

```json
{
  "success": true,
  "message": "Restaurante creado",
  "restaurante": {
    "nombre": "La Casona",
    "nit": "123456789",
    "direccion": "Calle 123 #45-67",
    "telefono": "+573001234567",
    "urlLogo": "https://example.com/logo.png",
    "idPropietario": 10
  }
}
```

### Matriz de autorización
| Actor | Resultado |
| --- | --- |
| ADMINISTRADOR autenticado | Permitido |
| PROPIETARIO | Denegado |
| EMPLEADO | Denegado |
| CLIENTE | Denegado |
| Sin autenticación | Denegado |

### Evidencia de prueba
- Pruebas unitarias de validación.
- Pruebas del caso de uso con autorización.
- Pruebas de control del controlador.

## Modelo relacional inicial

```text
Usuario(id, nombre, correo, rol)
Rol(id, nombre)
Restaurante(id, nombre, nit, direccion, telefono, url_logo, id_propietario)
```

### Relación
- Un propietario se asocia a muchos restaurantes.
- El restaurante conserva el `id_propietario` y valida la existencia y rol del usuario en la capa de negocio.

## Configuración Supabase

La implementación Java usa la API REST de Supabase y lee estas variables de entorno:

```text
SUPABASE_URL=https://<proyecto>.supabase.co/rest/v1
SUPABASE_ANON_KEY=<clave-publicable-opcional>
SUPABASE_SERVICE_ROLE_KEY=<clave-de-servidor>
```

`SUPABASE_SERVICE_ROLE_KEY` solo debe configurarse en el servidor. No debe enviarse al frontend ni confirmarse en el repositorio.
