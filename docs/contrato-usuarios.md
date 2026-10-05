# Contrato de integración con Usuarios

HU2 exige comprobar que `idPropietario` existe y su rol es `PROPIETARIO`. El servicio Usuarios implementa el endpoint de validación descrito abajo. HU6 valida propiedad del restaurante mediante una consulta autenticada desde Usuarios hacia Plazoleta.

## Endpoint requerido

`GET /api/v1/usuarios/{id}/validacion-propietario`

- `200` con cuerpo JSON `true`: el usuario existe y su rol es `PROPIETARIO`.
- `200` con cuerpo JSON `false` o `404`: no es un propietario válido.
- Errores de red/servidor: Plazoleta responde `503` y no crea el restaurante.

Plazoleta configura host y ruta mediante `USUARIOS_BASE_URL` y `USUARIOS_OWNER_VALIDATION_PATH`. Las pruebas unitarias sustituyen el puerto con un mock. Ningún servicio lee directamente la base de datos privada del otro.

## Validar restaurante para crear empleado (HU6)

Usuarios reenvía el Bearer del propietario a `GET /restaurantes/{idRestaurante}/propietario/{idPropietario}` en Plazoleta. Plazoleta exige rol `PROPIETARIO`, compara el propietario del token con el identificador solicitado y confirma la relación persistida. Solo con respuesta `true` Usuarios crea la cuenta del empleado asociada al restaurante.

El login de Usuarios firma el identificador del empleado y su `idRestaurante` dentro del token HMAC. Plazoleta usa esa afirmación firmada para limitar HU12 al restaurante del empleado, sin aceptar un ID de restaurante desde el query string.