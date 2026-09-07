# Reporte de control Git — Sprint 1

Responsable: Molly Lecompte (Scrum Master) · Documento maestro, secciones 7.2 y 15

## 1. Ramas del sprint

| Rama | Estado | Observacion |
| --- | --- | --- |
| `main` | | Solo recibe `release/sprint-1` |
| `develop` | | Integracion de las cinco HU |
| `feature/HU-01-crear-propietario` | | Renombrada: nacio sin el prefijo `feature/` |
| `feature/HU-02-crear-restaurante` | | |
| `feature/HU-03-crear-plato` | | Rehecha desde `develop`: colgaba de HU-02 |
| `feature/HU-04-modificar-plato` | | Rehecha desde `develop`: colgaba de HU-03 |
| `feature/HU-05-autenticacion` | | |
| `release/sprint-1` | | |

## 2. Pull Requests

| PR | HU | Autor | Revisor | Estado |
| --- | --- | --- | --- | --- |
| #1 | HU-01 | | | |
| #2 | HU-02 | | | |
| #3 | HU-03 | | | |
| #4 | HU-04 | | | |
| #5 | HU-05 | | | |
| #6 | release | | | |

## 3. Checklist por integrante

| Control | Molly | Simon | Melina |
| --- | --- | --- | --- |
| Mensajes normalizados | | | |
| Sin cambios directos a `main` | | | |
| PR asociado a HU | | | |
| Revision cruzada | | | |
| Documentacion versionada | | | |
| 3 commits semanales en dias distintos | | | |

## 4. Incidencias detectadas y correcciones

| # | Incidencia | Correccion aplicada |
| --- | --- | --- |
| 1 | La rama de HU-01 se creo como `HU-01-crear-propietario`, sin el prefijo `feature/` | Renombrada al formato del documento maestro |
| 2 | HU-03 y HU-04 se crearon a partir de la rama anterior en vez de `develop` | Rehechas desde `develop`; cada PR contiene solo su HU |
| 3 | HU-02, HU-03 y HU-04 nunca se fusionaron a `develop` ni a `main` | Integradas por PR en orden |
| 4 | Mensajes de commit sin el formato acordado (`HU2: ...`) | Normalizados a `tipo(ambito): descripcion [HU-0X]` |
| 5 | No existia `CONTRIBUTING.md` ni plantilla de PR | Creados e incorporados al repositorio |
| 6 | Archivos de IDE (`.idea/`) versionados | Excluidos por `.gitignore` |
| 7 | La distribucion de commits no cumplio "3 semanales en dias distintos" | **No corregida.** Ver seccion 5 |
| 8 | Un integrante (John Lopez) se retiro; sus HU quedaron sin responsable original | Reasignadas; registrado como riesgo materializado |

## 5. Desviacion declarada

El control "3 commits semanales en dias distintos por integrante" **no se
cumplio en el Sprint 1**. El trabajo se concentro en pocas sesiones en lugar de
distribuirse a lo largo de la semana.

Se deja registrado en vez de disimularse, y entra a la retrospectiva como
accion correctiva para el Sprint 2:

- Tope maximo de tareas en curso por integrante.
- Revision del tablero en cada Daily con evidencia de commit del dia anterior.
- Molly verifica el historial dos veces por semana, no solo al cierre.

## 6. Riesgo materializado

La salida de John Lopez a mitad de sprint dejo HU-03 y HU-04 sin su responsable
original. Impacto: reasignacion de carga y perdida del reparto previsto en la
matriz de responsabilidades. Accion para el Sprint 2: definir un segundo
conocedor por modulo desde el Planning.
