# Modelo relacional - Plazoleta de Comidas

**Sprint:** 1  
**Alcance:** HU-01 a HU-05  
**Responsable:** Melina Velasquez  
**Equipo:** 4

Este documento describe el modelo relacional de las funcionalidades implementadas en el Sprint 1.

## 1. Entidades

### Usuario

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | `BIGSERIAL` | Clave primaria |
| `nombre` | `VARCHAR(80)` | Obligatorio |
| `apellido` | `VARCHAR(80)` | Obligatorio |
| `documento_de_identidad` | `VARCHAR(20)` | Obligatorio, unico, solo digitos |
| `celular` | `VARCHAR(13)` | Obligatorio, maximo 13 caracteres, admite `+` |
| `fecha_nacimiento` | `DATE` | Obligatorio, usuario mayor de edad |
| `correo` | `VARCHAR(120)` | Obligatorio, unico, formato valido |
| `clave` | `VARCHAR(72)` | Obligatorio, hash BCrypt |
| `rol` | `VARCHAR(15)` | `ADMINISTRADOR`, `PROPIETARIO`, `EMPLEADO` o `CLIENTE` |

En HU-01 el servidor asigna el rol `PROPIETARIO`. La clave nunca se guarda en texto plano ni se devuelve en respuestas.

### Restaurante

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | `BIGSERIAL` | Clave primaria |
| `nombre` | `VARCHAR(120)` | Obligatorio, no puede ser solo numerico |
| `nit` | `VARCHAR(20)` | Obligatorio, unico, solo digitos |
| `direccion` | `VARCHAR(160)` | Obligatorio |
| `telefono` | `VARCHAR(13)` | Obligatorio, maximo 13 caracteres, admite `+` |
| `url_logo` | `VARCHAR(255)` | Obligatorio |
| `id_propietario` | `BIGINT` | Obligatorio, FK a `usuario(id)` |

El propietario debe existir y tener rol `PROPIETARIO`. Esta validacion se realiza en la capa de aplicacion mediante `UsuarioValidationPort`.

### Plato

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | `BIGSERIAL` | Clave primaria |
| `nombre` | `VARCHAR(120)` | Obligatorio, inmutable |
| `precio` | `INTEGER` | Obligatorio, mayor que cero |
| `descripcion` | `TEXT` | Obligatorio, modificable en HU-04 |
| `url_imagen` | `VARCHAR(255)` | Obligatorio, inmutable |
| `categoria` | `VARCHAR(60)` | Obligatorio, inmutable |
| `id_restaurante` | `BIGINT` | Obligatorio, FK a `restaurante(id)` |
| `activo` | `BOOLEAN` | Obligatorio, valor predeterminado `TRUE` |

En HU-04 solo se pueden modificar `precio` y `descripcion`. La propiedad se valida navegando desde el plato hacia su restaurante y propietario.

## 2. Cardinalidades

| Relacion | Cardinalidad | Regla |
| --- | --- | --- |
| Usuario -> Restaurante | 1 a N | Un propietario puede tener varios restaurantes; cada restaurante tiene un propietario |
| Restaurante -> Plato | 1 a N | Un restaurante puede tener varios platos; cada plato pertenece a un restaurante |
| Usuario -> Plato | Indirecta | La pertenencia se determina mediante `plato -> restaurante -> id_propietario` |

No existe una FK directa entre `usuario` y `plato`. La relacion se deduce por el restaurante al que pertenece el plato.

## 3. Script de creacion PostgreSQL / Supabase

```sql
create table public.usuario (
  id bigserial primary key,
  nombre varchar(80) not null,
  apellido varchar(80) not null,
  documento_de_identidad varchar(20) not null unique,
  celular varchar(13) not null,
  fecha_nacimiento date not null,
  correo varchar(120) not null unique,
  clave varchar(72) not null,
  rol varchar(15) not null,
  constraint ck_usuario_rol
    check (rol in ('ADMINISTRADOR', 'PROPIETARIO', 'EMPLEADO', 'CLIENTE')),
  constraint ck_usuario_documento_numerico
    check (documento_de_identidad ~ '^[0-9]+$'),
  constraint ck_usuario_mayor_de_edad
    check (fecha_nacimiento <= current_date - interval '18 years')
);

create table public.restaurante (
  id bigserial primary key,
  nombre varchar(120) not null,
  nit varchar(20) not null unique,
  direccion varchar(160) not null,
  telefono varchar(13) not null,
  url_logo varchar(255) not null,
  id_propietario bigint not null,
  constraint fk_restaurante_propietario
    foreign key (id_propietario) references public.usuario (id),
  constraint ck_restaurante_nit_numerico
    check (nit ~ '^[0-9]+$'),
  constraint ck_restaurante_nombre_no_solo_numeros
    check (nombre !~ '^[0-9]+$')
);

create table public.plato (
  id bigserial primary key,
  nombre varchar(120) not null,
  precio integer not null,
  descripcion text not null,
  url_imagen varchar(255) not null,
  categoria varchar(60) not null,
  id_restaurante bigint not null,
  activo boolean not null default true,
  constraint fk_plato_restaurante
    foreign key (id_restaurante) references public.restaurante (id),
  constraint ck_plato_precio_positivo
    check (precio > 0)
);

create index idx_restaurante_propietario
  on public.restaurante (id_propietario);

create index idx_plato_restaurante
  on public.plato (id_restaurante);

create index idx_usuario_correo
  on public.usuario (correo);
```

## 4. Decisiones de modelado

- `documento_de_identidad` y `nit` son texto para conservar ceros a la izquierda.
- `precio` es entero positivo, de acuerdo con la HU.
- El rol vive en `usuario` porque en este Sprint cada usuario tiene un unico rol.
- El rol `PROPIETARIO` se valida en la aplicacion, no mediante una FK compuesta.
- `plato.activo` permite una futura baja logica sin borrado fisico.

## 5. Deuda tecnica: Categoria

Durante el Sprint 1, `categoria` permanece como texto dentro de `plato` porque no existe una HU para administrar o consultar categorias. Cuando sea necesario normalizarla, se puede crear `categoria(id, nombre, descripcion)` y reemplazar `plato.categoria` por `plato.id_categoria` con una migracion de datos.

## 6. Persistencia actual

Los repositorios del Sprint 1 funcionan en memoria. `SupabaseRestauranteRepository` y `SupabaseConfig` preparan el adaptador REST para Supabase mediante variables de entorno, sin guardar credenciales en el repositorio.