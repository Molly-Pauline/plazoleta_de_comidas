# Modelo relacional

```text
Restaurante(
  id PK,
  nombre,
  nit UNIQUE,
  direccion,
  telefono,
  url_logo,
  id_propietario
)

Plato(
  id PK,
  nombre,
  precio,
  descripcion,
  url_imagen,
  categoria,
  activo,
  id_restaurante FK -> Restaurante.id
)

Pedido(
  id PK,
  id_cliente,
  id_empleado NULL,
  id_restaurante FK -> Restaurante.id,
  estado,
  fecha_creacion,
  total
)

PedidoDetalle(
  id PK,
  id_pedido FK -> Pedido.id,
  id_plato FK -> Plato.id,
  cantidad,
  precio_unitario
)
```

Un restaurante conserva el identificador del propietario en Usuarios. Un restaurante contiene cero o más platos; cada plato pertenece a un único restaurante. `activo` se fuerza a `true` al crear un plato y no forma parte del DTO de actualización HU4. Un pedido agrupa detalles de un único restaurante y conserva el precio unitario al momento de compra.