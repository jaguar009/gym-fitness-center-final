# Gym Fitness Center

Aplicación académica para administrar miembros, membresías, pagos, asistencias,
empleados, clases e inscripciones de un gimnasio.

Java 21, Maven, Spring Boot, Spring MVC, Thymeleaf, Spring Security, JPA/Hibernate
y SQL Server. El alcance de entrega comprende los puntos 7.1 a 7.6 del informe.

Este repositorio es una reconstrucción por etapas del código del proyecto.
Los commits registran la incorporación y verificación de cada parte en este
repositorio, con sus fechas reales; no representan fechas de desarrollo anteriores.

## Funcionalidades

- Inicio y cierre de sesión con perfiles ADMIN y RECEPCION.
- Mantenimientos de miembros, membresías, pagos, asistencias, empleados y clases.
- Consultas de miembros por DNI, nombre y estado; pagos por periodo y método.
- Inscripción, cancelación y reactivación con control transaccional de cupos.
- Validación de formularios, DNI único, montos positivos y rangos de fechas.

ADMIN administra empleados y puede cambiar el estado de los registros.
RECEPCION realiza las operaciones permitidas y conserva el estado al editar
miembros, membresías y clases. Las validaciones se aplican en el servidor;
ocultar un botón no sustituye una autorización.

## Ejecutar en Windows con SQL Server Express

Requisitos: JDK 21 o posterior compatible, Maven 3.9, SQL Server Express con
TCP/IP disponible y acceso de la cuenta de Windows a la base `gymfitnessdb`.

1. Para una base nueva, ejecutar `sql/gymfitnessdb.sql` desde SQL Server Management
   Studio o con el siguiente comando si `sqlcmd` está instalado:

   ```powershell
   sqlcmd -S '.\SQLEXPRESS' -E -b -i '.\sql\gymfitnessdb.sql'
   ```

   El script incorpora datos académicos de ejemplo y el contador de cupos. No
   eliminar una base existente. Para actualizar únicamente el contador de una
   instalación anterior, revisar y ejecutar `sql/002_cupos_transaccionales.sql`.

2. Desde la carpeta que contiene `pom.xml`:

   ```powershell
   mvn -B clean verify
   .\ejecutar-local.ps1
   ```

3. Abrir [el inicio de sesión local](http://localhost:8080/login).
   Si otro proyecto ocupa ese puerto: `./ejecutar-local.ps1 -Port 8081`.

El perfil `local-windows` utiliza la DLL oficial x64 incluida en
`native/windows-x64`; conserva su licencia. No requiere instalar Tomcat por
separado: Spring Boot lo incorpora.

| Usuario de demostración | Contraseña inicial | Rol |
|---|---|---|
| admin | Admin123! | ADMIN |
| recepcion | Recepcion123! | RECEPCION |

Estas son cuentas académicas, no credenciales para un sistema público. Las
variables `APP_ADMIN_PASSWORD` y `APP_RECEPTION_PASSWORD` se utilizan al crear
usuarios nuevos; no cambian contraseñas ya guardadas. No publicar claves reales.

Para autenticación SQL, utilizar el perfil predeterminado y definir `DB_URL`,
`DB_USER` y `DB_PASSWORD` en el entorno antes de ejecutar el JAR. La aplicación
no carga automáticamente archivos `.env`. `ddl-auto=validate` comprueba el
esquema, no crea las tablas. La configuración local confía en el certificado
del servidor de desarrollo; no es una configuración de producción.

## Eclipse o Spring Tools

Importar **File > Import > Maven > Existing Maven Projects** y seleccionar
esta carpeta. El nombre del proyecto es `gym-fitness-center-final`, para que
no choque con una copia anterior. Seleccionar un JDK compatible con Java 21,
actualizar Maven y ejecutar `GymFitnessCenterFinal.launch` después de preparar
la base. La importación puede generar `.classpath` y `.settings` locales.

## Arquitectura y pruebas

`controller` recibe los formularios y prepara las vistas; `model` contiene las
ocho entidades JPA; `repository` permite consultar y guardar datos; `service`
concentra las reglas transaccionales de clases e inscripciones; `security`
valida cambios de estado. Los CRUD sencillos también utilizan repositorios
directamente desde sus controladores.

`EnrollmentService` bloquea la fila de la clase con `PESSIMISTIC_WRITE`, revisa
estado, duplicados y disponibilidad, y actualiza inscripción y ocupación en
una misma transacción. Al cancelar, hace `flush` y vuelve a contar las
inscripciones activas; `flush` no significa `commit`.

Ejecutar todas las pruebas con `mvn -B clean verify`. La suite utiliza H2 en
memoria y Mockito; no modifica SQL Server. Comprueba acceso, CSRF, permisos de
estado, consultas, duplicados, cancelaciones y rollback. Las pruebas de H2 no
demuestran por sí solas el comportamiento concurrente específico de SQL Server.

## Ramas y explicación de los commits

Consultar [la guía de Git](docs/Guia_Git.md) para recorrer las etapas, comparar
los cambios y preparar su explicación. Cada rama se integra en `main` mediante
un commit de merge; las ramas se conservan para estudiar lo que incorporan.
