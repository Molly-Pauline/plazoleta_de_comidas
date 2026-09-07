# HU 3 y HU 4 - Platos y menu

## Alcance

El modulo implementa exclusivamente:

- **HU 3:** un propietario crea un plato en un restaurante propio.
- **HU 4:** un propietario modifica solo el precio y la descripcion de un plato propio.

No se implementan listados, habilitar/deshabilitar platos, pedidos ni notificaciones.

## Modelo

```mermaid
classDiagram
    Restaurante "1" --> "0..*" Plato : contiene
    Restaurante {
        Long id
        String nombre
        String nit
        Long idPropietario
    }
    Plato {
        Long id
        String nombre
        Integer precio
        String descripcion
        String urlImagen
        String categoria
        Long idRestaurante
        boolean activo
    }
```

`categoria` se representa como texto no vacio porque la guia no define un catalogo oficial. La decision permite integrar el catalogo del equipo posteriormente sin cambiar el contrato de HU 3.

## Contratos

### Crear plato

`POST /platos`

Headers: `X-Rol: PROPIETARIO`, `X-User-Id: 10`

```json
{
  "nombre": "Hamburguesa clasica",
  "precio": 25000,
  "descripcion": "Con queso",
  "urlImagen": "https://example.com/burger.png",
  "categoria": "Hamburguesas",
  "idRestaurante": 1
}
```

El servidor fuerza `activo: true`; el cliente no puede elegirlo.

### Modificar plato

`PUT /platos/{id}`

```json
{
  "precio": 30000,
  "descripcion": "Con queso y tocineta"
}
```

El DTO de actualizacion no contiene nombre, imagen, categoria, estado ni restaurante. Por tanto, esos campos conservan su valor.

## Autorizacion y errores

| Condicion | Resultado |
| --- | --- |
| Sin autenticacion | HTTP 401 |
| Rol distinto de `PROPIETARIO` | HTTP 403 |
| Restaurante ajeno | HTTP 403 |
| Restaurante o plato inexistente | HTTP 400 |
| Precio ausente, cero, negativo o no entero | HTTP 400 |
| Campos obligatorios ausentes | HTTP 400 |

La pertenencia se verifica comparando `X-User-Id` con `Restaurante.idPropietario` en cada operacion.

## Evidencia automatizada

- `PlatoServiceTest`: creacion, precio, restaurante inexistente/ajeno, rol, activo por defecto y modificacion restringida.
- `PlatoControllerTest`: contratos de aplicacion, autenticacion y preservacion de campos.
- `PlatoHttpServerTest`: flujo HTTP completo de HU 4 y preservacion de los campos restringidos.
- `RestauranteHttpServerTest`: regresion del endpoint existente.

La persistencia actual sigue el patron en memoria utilizado por el proyecto. `RestauranteRepository` y `PlatoRepository` asignan identificadores y encapsulan el almacenamiento para permitir sustituirlo por Supabase sin mover las reglas de negocio.
