# Cómo explicar las etapas del proyecto

Este historial registra una reconstrucción e integración por etapas realizada
en este repositorio. Se reutiliza código válido del proyecto y se incorporan
pruebas adicionales. Las fechas y autorías de Git no se alteran. Las ramas son
temas de trabajo, no evidencia de que una persona distinta programó cada una.

## Mapa de ramas

| Rama | Contenido que incorpora | Qué mostrar en la sustentación |
|---|---|---|
| `main` | Versión integrada | Aplicación completa y pruebas |
| `codex/base-datos` | Entidades, relaciones, repositorios y SQL | `ClassEnrollment`, sus dos `ManyToOne` y el script SQL |
| `codex/seguridad` | Login, roles, BCrypt y navegación | `SecurityConfig` y pruebas de acceso |
| `codex/mantenimientos` | Cinco mantenimientos administrativos | Un formulario, su controlador y su repositorio |
| `codex/clases-inscripciones` | Sexto mantenimiento, cupos e inscripciones | `EnrollmentService` y sus pruebas unitarias/transaccionales |
| `codex/consultas` | Filtros de miembros y pagos | Parámetros, consulta JPA y resultados en pantalla |
| `codex/pruebas-permisos` | Pruebas de cambios de estado y CSRF | Diferencia entre un rechazo por rol y uno por token CSRF |
| `codex/entrega-local` | Ejecución, importación al IDE y guía | Maven, JAR, perfil local y archivo launch |

Cada rama nace de la versión integrada disponible en ese momento. Las etapas
dependen de las anteriores: no son aplicaciones independientes. Se
conservan las ramas ya fusionadas para poder consultar sus commits. Solo `main`
representa la entrega final completa.

## Recorrido seguro, sin cambiar de rama

```powershell
git status
git branch -a
git log --graph --oneline --decorate --all
git log --no-merges --oneline main
git log --merges --oneline main
git rev-list --count main
git rev-list --count --no-merges main
```

El total incluye commits de contenido y commits de integración. Un merge no
equivale a una funcionalidad nueva; une el trabajo de una rama con `main`.
Para contar ramas remotas sin confundirlas con sus copias locales, revisar
la sección **Branches** de GitHub o usar `git branch -r`.

Para examinar un commit de la lista:

```powershell
git show --stat HASH
git show HASH -- src/main/java/pe/cibertec/gym/service/EnrollmentService.java
```

Sustituir `HASH` por un identificador real del historial. La segunda orden
muestra únicamente los cambios de ese archivo dentro de ese commit; no
necesariamente su contenido completo. Para ver el archivo en esa versión:

```powershell
git show HASH:src/main/java/pe/cibertec/gym/service/EnrollmentService.java
```

Para comparar un merge con la versión de `main` que lo precedía, usar el hash
de ese merge y su primer padre:

```powershell
git diff 'HASH^1' HASH --stat
```

## Qué responder al profesor

**¿Para qué utilizaron ramas?** Para separar cambios relacionados y comprobar
cada etapa antes de integrarla. Por ejemplo, la rama de clases agrupa la lógica
de cupos, sus formularios y las pruebas. `main` conserva la versión integrada.

**¿Qué diferencia hay entre commit, rama, merge y push?** Un commit guarda una
instantánea de cambios; una rama señala una línea de trabajo; un merge integra
líneas de trabajo; un push envía los commits y referencias al repositorio remoto.

**¿Qué comprueba exactamente la prueba del duplicado?** Prepara una clase y un
miembro activos, simula una inscripción previa y verifica el mensaje concreto
de rechazo y que no se guarden otra inscripción ni otro contador. No se limita
a comprobar que se lanzó cualquier excepción.

**¿Para qué una prueba transaccional adicional?** La prueba con Mockito aísla
las decisiones del servicio, pero no usa una base real ni el proxy transaccional
de Spring. La prueba con H2 usa JPA y Spring, verifica filas y ocupación, y
fuerza un fallo para comprobar el rollback. Sigue pendiente ensayar concurrencia
específica de SQL Server; H2 no reemplaza esa comprobación.

**¿Qué ocurre si RECEPCION envía INACTIVO manualmente?** Los guardados de
miembros, membresías y clases consultan `RecordStatePolicy`; si intenta cambiar
el estado, el servidor responde 403. Las pruebas usan un token CSRF válido
para demostrar que el rechazo se debe al permiso y no a la ausencia del token.

**¿Por qué tantos commits?** Porque se separaron piezas que se pueden explicar
y revisar: entidades, repositorios, cada mantenimiento, servicios, vistas y
pruebas. La cantidad no demuestra por sí sola la calidad o participación;
hay que mostrar los cambios reales y comprender su funcionamiento.

## Ensayo de demostración

1. Mostrar `main`, el gráfico de commits y una rama temática en GitHub.
2. Abrir un commit y explicar qué archivos cambió y por qué van juntos.
3. Ejecutar `mvn -B clean verify` y explicar una prueba con preparación,
   operación y verificación, no solo leer el número de casos.
4. Iniciar sesión, consultar miembros y registrar una operación con datos de
   demostración. Los datos de demostración no representan clientes reales.
5. Mostrar una clase y su inscripción/cancelación; relacionar la pantalla con
   el controlador, servicio, repositorio y entidades.
6. No presentar commits de reconstrucción como un desarrollo realizado en
   fechas anteriores ni atribuirlos a integrantes sin evidencia.
