Guía para ejecutar Gym Fitness Center en Windows
Esta guía explica cómo abrir y ejecutar el proyecto en Eclipse con SQL Server Express.
Requisitos
JDK 21
Eclipse con soporte para Maven
SQL Server Express con TCP/IP habilitado y el servicio SQL Server Browser en ejecución
Conexión a Internet (Maven descarga las dependencias la primera vez)
1. Crear la base de datos
Desde PowerShell, en la carpeta del proyecto:
```
sqlcmd -S '.\\SQLEXPRESS' -E -b -i '.\\sql\\gymfitnessdb.sql'
```
El script crea la base `gymfitnessdb`, sus tablas y los datos de ejemplo.
2. Importar el proyecto en Eclipse
`File > Import > Maven > Existing Maven Projects`.
Elegir la carpeta que contiene el archivo `pom.xml`.
Esperar a que Maven termine de descargar las dependencias.
Verificar que el proyecto se llame `gym-fitness-center`, porque el archivo `GymFitnessCenter.launch` lo busca por ese nombre.
3. Ejecutar
Clic derecho sobre `GymFitnessCenter.launch` y elegir `Run As > GymFitnessCenter`.
Cuando la consola muestre `Started GymFitnessCenterApplication`, abrir `http://localhost:8080/login`.
Usuarios de demostración
Usuario	Rol
admin	ADMIN
recepcion	RECEPCION
Las contraseñas iniciales están en el archivo `README.md`.
Problemas frecuentes
Mensaje	Causa y solución
`references non-existing project gym-fitness-center`	El proyecto se importó con otro nombre. Renombrarlo a `gym-fitness-center` (F2) o importarlo como proyecto Maven.
`instancia con nombre SQLEXPRESS... SQL Server Browser`	El servicio SQL Server o SQL Server Browser está detenido, o falta habilitar TCP/IP. Iniciarlos y reiniciar el servicio.
`Port 8080 already in use`	Otro programa usa el puerto 8080. Cerrarlo o ejecutar con `--server.port=8081`.
