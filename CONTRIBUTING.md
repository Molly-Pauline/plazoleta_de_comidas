# Guia de contribucion — Plazoleta de Comidas

Equipo 4 · Sprint 1. Este documento fija las reglas de Git que exige el
documento maestro (seccion 13) y la guia de cada rol.

## 1. Ramas

| Rama | Proposito | Nace de |
| --- | --- | --- |
| `main` | Codigo estable entregado | `release/*` |
| `develop` | Integracion continua del sprint | — |
| `feature/HU-0X-descripcion` | Una historia de usuario | `develop` actualizada |
| `release/sprint-N` | Preparacion de la entrega | `develop` |

Reglas:

- **Nunca se hace commit directo a `main` ni a `develop`.** Todo entra por Pull Request.
- Una rama por HU. El nombre lleva **siempre** el prefijo `feature/` y el codigo de la historia.
- Una rama feature nace de `develop` actualizada, no de otra rama feature. Si una
  feature cuelga de otra, su PR arrastra commits que no le pertenecen y deja de
  poder revisarse por separado.

```bash
git checkout develop
git pull origin develop
git checkout -b feature/HU-05-autenticacion
```

## 2. Mensajes de commit

Formato obligatorio:

```
tipo(ambito): descripcion en minuscula y en imperativo [HU-0X]
```

Tipos permitidos: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`.

Ambitos del proyecto: `usuarios`, `seguridad`, `restaurantes`, `platos`, `git`, `arquitectura`.

Ejemplos validos:

```
feat(usuarios): crear propietario con validaciones [HU-01]
fix(usuarios): cifrar clave con bcrypt antes de persistir [HU-01]
feat(restaurantes): validar NIT numerico [HU-02]
test(platos): rechazar modificacion de plato ajeno [HU-04]
feat(seguridad): implementar login con correo y clave [HU-05]
docs(seguridad): documentar matriz de autorizacion [HU-05]
```

No validos: `HU2: cosas`, `avance`, `cambios`, `arreglos varios`.

Un commit = un cambio con sentido propio. Si el mensaje necesita un "y" para
describir lo que hace, probablemente son dos commits.

## 3. Pull Requests

- Titulo: `HU-0X · Descripcion corta`.
- Descripcion: usa la plantilla de `.github/pull_request_template.md`.
- Destino: siempre `develop` (nunca `main` directamente).
- **Revision cruzada obligatoria**: quien abre el PR no lo aprueba. Se solicita
  revision a otro integrante y se resuelven las observaciones antes de fusionar.
- No se fusiona con pruebas en rojo.

## 4. Reglas de contenido

- Sin secretos, claves, tokens ni cadenas de conexion en el repositorio. Todo por
  variable de entorno (ver `.env.example`).
- Sin `System.out.println` de depuracion en codigo de produccion.
- La documentacion cambia junto con el codigo, en el mismo PR.
- Los binarios de las guias (`.docx`, `.pdf`) no se versionan en la raiz: van a
  `docs/` o fuera del repositorio.

## 5. Antes de pedir revision

```bash
mvn -q test          # todas las pruebas en verde
git log --oneline -5 # mensajes con formato
git diff develop...  # revisar que no se coló nada de mas
```
