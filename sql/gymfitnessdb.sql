/*
   Gym Fitness Center - SQL Server
   Compatible with SQL Server Express and SQL Server 2019+.
   Run with sqlcmd or SQL Server Management Studio using Windows authentication.
*/

IF DB_ID(N'gymfitnessdb') IS NULL
BEGIN
    CREATE DATABASE [gymfitnessdb];
END
GO

USE [gymfitnessdb];
GO

IF OBJECT_ID(N'dbo.miembro', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.miembro (
        idMiembro BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_miembro PRIMARY KEY,
        dni VARCHAR(15) NOT NULL CONSTRAINT UQ_miembro_dni UNIQUE,
        nombres VARCHAR(100) NOT NULL,
        apellidos VARCHAR(100) NOT NULL,
        telefono VARCHAR(20) NULL,
        correo VARCHAR(100) NULL,
        direccion VARCHAR(150) NULL,
        fechaRegistro DATE NULL,
        estado VARCHAR(20) NOT NULL CONSTRAINT DF_miembro_estado DEFAULT ('ACTIVO')
    );
END
GO

IF OBJECT_ID(N'dbo.empleado', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.empleado (
        idEmpleado BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_empleado PRIMARY KEY,
        dni VARCHAR(15) NOT NULL CONSTRAINT UQ_empleado_dni UNIQUE,
        nombres VARCHAR(100) NOT NULL,
        apellidos VARCHAR(100) NOT NULL,
        cargo VARCHAR(50) NULL,
        telefono VARCHAR(20) NULL,
        correo VARCHAR(100) NULL,
        estado VARCHAR(20) NOT NULL CONSTRAINT DF_empleado_estado DEFAULT ('ACTIVO')
    );
END
GO

IF OBJECT_ID(N'dbo.usuario', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.usuario (
        idUsuario BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_usuario PRIMARY KEY,
        username VARCHAR(50) NOT NULL CONSTRAINT UQ_usuario_username UNIQUE,
        password VARCHAR(255) NOT NULL,
        rol VARCHAR(30) NOT NULL,
        estado VARCHAR(20) NOT NULL CONSTRAINT DF_usuario_estado DEFAULT ('ACTIVO')
    );
END
GO

IF OBJECT_ID(N'dbo.membresia', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.membresia (
        idMembresia BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_membresia PRIMARY KEY,
        idMiembro BIGINT NOT NULL,
        tipo VARCHAR(50) NOT NULL,
        costo DECIMAL(10,2) NOT NULL,
        fechaInicio DATE NULL,
        fechaFin DATE NULL,
        estado VARCHAR(20) NOT NULL CONSTRAINT DF_membresia_estado DEFAULT ('ACTIVO'),
        CONSTRAINT FK_membresia_miembro FOREIGN KEY (idMiembro) REFERENCES dbo.miembro(idMiembro) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'dbo.pago', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pago (
        idPago BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_pago PRIMARY KEY,
        idMiembro BIGINT NOT NULL,
        monto DECIMAL(10,2) NOT NULL,
        fechaPago DATE NULL,
        metodoPago VARCHAR(50) NULL,
        concepto VARCHAR(100) NULL,
        CONSTRAINT FK_pago_miembro FOREIGN KEY (idMiembro) REFERENCES dbo.miembro(idMiembro) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'dbo.asistencia', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.asistencia (
        idAsistencia BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_asistencia PRIMARY KEY,
        idMiembro BIGINT NOT NULL,
        fecha DATE NOT NULL,
        horaEntrada VARCHAR(10) NULL,
        observacion VARCHAR(150) NULL,
        CONSTRAINT FK_asistencia_miembro FOREIGN KEY (idMiembro) REFERENCES dbo.miembro(idMiembro) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'dbo.clase', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.clase (
        idClase BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_clase PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL,
        descripcion VARCHAR(150) NULL,
        horario VARCHAR(100) NULL,
        cupos INT NOT NULL CONSTRAINT DF_clase_cupos DEFAULT (0),
        idEmpleado BIGINT NULL,
        estado VARCHAR(20) NOT NULL CONSTRAINT DF_clase_estado DEFAULT ('ACTIVO'),
        CONSTRAINT FK_clase_empleado FOREIGN KEY (idEmpleado) REFERENCES dbo.empleado(idEmpleado) ON DELETE SET NULL
    );
END
GO

IF OBJECT_ID(N'dbo.inscripcion_clase', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.inscripcion_clase (
        idInscripcion BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_inscripcion_clase PRIMARY KEY,
        idClase BIGINT NOT NULL,
        idMiembro BIGINT NOT NULL,
        fechaInscripcion DATE NOT NULL,
        estado VARCHAR(20) NOT NULL CONSTRAINT DF_inscripcion_estado DEFAULT ('ACTIVO'),
        CONSTRAINT UQ_inscripcion_clase_miembro UNIQUE (idClase, idMiembro),
        CONSTRAINT FK_inscripcion_clase FOREIGN KEY (idClase) REFERENCES dbo.clase(idClase) ON DELETE CASCADE,
        CONSTRAINT FK_inscripcion_miembro FOREIGN KEY (idMiembro) REFERENCES dbo.miembro(idMiembro) ON DELETE CASCADE
    );
END
GO

/* Datos de prueba idempotentes: no elimina registros existentes. */
IF NOT EXISTS (SELECT 1 FROM dbo.miembro WHERE dni = '70000001')
    INSERT dbo.miembro (dni, nombres, apellidos, telefono, correo, direccion, fechaRegistro, estado)
    VALUES ('70000001', 'Carlos', 'Ramirez', '999111222', 'carlos@gmail.com', 'Av. Lima 123', '2025-01-10', 'ACTIVO');
IF NOT EXISTS (SELECT 1 FROM dbo.miembro WHERE dni = '70000002')
    INSERT dbo.miembro (dni, nombres, apellidos, telefono, correo, direccion, fechaRegistro, estado)
    VALUES ('70000002', 'Lucia', 'Fernandez', '999222333', 'lucia@gmail.com', 'Jr. Sol 456', '2025-01-12', 'ACTIVO');
IF NOT EXISTS (SELECT 1 FROM dbo.miembro WHERE dni = '70000003')
    INSERT dbo.miembro (dni, nombres, apellidos, telefono, correo, direccion, fechaRegistro, estado)
    VALUES ('70000003', 'Jorge', 'Vasquez', '999333444', 'jorge@gmail.com', 'Av. Norte 789', '2025-01-15', 'INACTIVO');

IF NOT EXISTS (SELECT 1 FROM dbo.empleado WHERE dni = '80000001')
    INSERT dbo.empleado (dni, nombres, apellidos, cargo, telefono, correo, estado)
    VALUES ('80000001', 'Ana', 'Torres', 'Entrenador', '988111222', 'ana@gym.com', 'ACTIVO');
IF NOT EXISTS (SELECT 1 FROM dbo.empleado WHERE dni = '80000002')
    INSERT dbo.empleado (dni, nombres, apellidos, cargo, telefono, correo, estado)
    VALUES ('80000002', 'Luis', 'Castro', 'Recepcionista', '988222333', 'luis@gym.com', 'ACTIVO');
IF NOT EXISTS (SELECT 1 FROM dbo.empleado WHERE dni = '80000003')
    INSERT dbo.empleado (dni, nombres, apellidos, cargo, telefono, correo, estado)
    VALUES ('80000003', 'Marta', 'Lopez', 'Instructor Zumba', '988333444', 'marta@gym.com', 'ACTIVO');

IF NOT EXISTS (
    SELECT 1 FROM dbo.membresia m INNER JOIN dbo.miembro mi ON mi.idMiembro = m.idMiembro
    WHERE mi.dni = '70000001' AND m.tipo = 'Mensual' AND m.fechaInicio = '2025-02-01'
)
    INSERT dbo.membresia (idMiembro, tipo, costo, fechaInicio, fechaFin, estado)
    SELECT idMiembro, 'Mensual', 120.00, '2025-02-01', '2025-02-28', 'ACTIVO'
    FROM dbo.miembro WHERE dni = '70000001';
IF NOT EXISTS (
    SELECT 1 FROM dbo.membresia m INNER JOIN dbo.miembro mi ON mi.idMiembro = m.idMiembro
    WHERE mi.dni = '70000002' AND m.tipo = 'Trimestral' AND m.fechaInicio = '2025-02-01'
)
    INSERT dbo.membresia (idMiembro, tipo, costo, fechaInicio, fechaFin, estado)
    SELECT idMiembro, 'Trimestral', 300.00, '2025-02-01', '2025-04-30', 'ACTIVO'
    FROM dbo.miembro WHERE dni = '70000002';
IF NOT EXISTS (
    SELECT 1 FROM dbo.membresia m INNER JOIN dbo.miembro mi ON mi.idMiembro = m.idMiembro
    WHERE mi.dni = '70000003' AND m.tipo = 'Mensual' AND m.fechaInicio = '2025-01-01'
)
    INSERT dbo.membresia (idMiembro, tipo, costo, fechaInicio, fechaFin, estado)
    SELECT idMiembro, 'Mensual', 120.00, '2025-01-01', '2025-01-31', 'VENCIDO'
    FROM dbo.miembro WHERE dni = '70000003';

IF NOT EXISTS (SELECT 1 FROM dbo.pago p INNER JOIN dbo.miembro mi ON mi.idMiembro = p.idMiembro WHERE mi.dni = '70000001' AND p.monto = 120.00 AND p.fechaPago = '2025-02-01')
    INSERT dbo.pago (idMiembro, monto, fechaPago, metodoPago, concepto)
    SELECT idMiembro, 120.00, '2025-02-01', 'EFECTIVO', 'Pago membresia mensual' FROM dbo.miembro WHERE dni = '70000001';
IF NOT EXISTS (SELECT 1 FROM dbo.pago p INNER JOIN dbo.miembro mi ON mi.idMiembro = p.idMiembro WHERE mi.dni = '70000002' AND p.monto = 300.00 AND p.fechaPago = '2025-02-01')
    INSERT dbo.pago (idMiembro, monto, fechaPago, metodoPago, concepto)
    SELECT idMiembro, 300.00, '2025-02-01', 'YAPE', 'Pago membresia trimestral' FROM dbo.miembro WHERE dni = '70000002';
IF NOT EXISTS (SELECT 1 FROM dbo.pago p INNER JOIN dbo.miembro mi ON mi.idMiembro = p.idMiembro WHERE mi.dni = '70000001' AND p.monto = 50.00 AND p.fechaPago = '2025-02-10')
    INSERT dbo.pago (idMiembro, monto, fechaPago, metodoPago, concepto)
    SELECT idMiembro, 50.00, '2025-02-10', 'TARJETA', 'Pago entrenamiento personal' FROM dbo.miembro WHERE dni = '70000001';

IF NOT EXISTS (SELECT 1 FROM dbo.asistencia a INNER JOIN dbo.miembro mi ON mi.idMiembro = a.idMiembro WHERE mi.dni = '70000001' AND a.fecha = '2025-02-05')
    INSERT dbo.asistencia (idMiembro, fecha, horaEntrada, observacion)
    SELECT idMiembro, '2025-02-05', '07:00', 'Ingreso normal' FROM dbo.miembro WHERE dni = '70000001';
IF NOT EXISTS (SELECT 1 FROM dbo.asistencia a INNER JOIN dbo.miembro mi ON mi.idMiembro = a.idMiembro WHERE mi.dni = '70000002' AND a.fecha = '2025-02-05')
    INSERT dbo.asistencia (idMiembro, fecha, horaEntrada, observacion)
    SELECT idMiembro, '2025-02-05', '08:15', 'Clase funcional' FROM dbo.miembro WHERE dni = '70000002';
IF NOT EXISTS (SELECT 1 FROM dbo.asistencia a INNER JOIN dbo.miembro mi ON mi.idMiembro = a.idMiembro WHERE mi.dni = '70000001' AND a.fecha = '2025-02-06')
    INSERT dbo.asistencia (idMiembro, fecha, horaEntrada, observacion)
    SELECT idMiembro, '2025-02-06', '07:05', 'Ingreso normal' FROM dbo.miembro WHERE dni = '70000001';

IF NOT EXISTS (SELECT 1 FROM dbo.clase WHERE nombre = 'Zumba')
    INSERT dbo.clase (nombre, descripcion, horario, cupos, idEmpleado, estado)
    SELECT 'Zumba', 'Clase de baile y cardio', 'Lunes-Miercoles-Viernes 18:00', 25, idEmpleado, 'ACTIVO' FROM dbo.empleado WHERE dni = '80000003';
IF NOT EXISTS (SELECT 1 FROM dbo.clase WHERE nombre = 'Spinning')
    INSERT dbo.clase (nombre, descripcion, horario, cupos, idEmpleado, estado)
    SELECT 'Spinning', 'Clase de resistencia y cardio', 'Martes-Jueves 19:00', 20, idEmpleado, 'ACTIVO' FROM dbo.empleado WHERE dni = '80000001';
IF NOT EXISTS (SELECT 1 FROM dbo.clase WHERE nombre = 'Funcional')
    INSERT dbo.clase (nombre, descripcion, horario, cupos, idEmpleado, estado)
    SELECT 'Funcional', 'Entrenamiento funcional integral', 'Sabado 09:00', 30, idEmpleado, 'ACTIVO' FROM dbo.empleado WHERE dni = '80000001';

IF NOT EXISTS (
    SELECT 1 FROM dbo.inscripcion_clase i INNER JOIN dbo.clase c ON c.idClase = i.idClase INNER JOIN dbo.miembro m ON m.idMiembro = i.idMiembro
    WHERE c.nombre = 'Zumba' AND m.dni = '70000001'
)
    INSERT dbo.inscripcion_clase (idClase, idMiembro, fechaInscripcion, estado)
    SELECT c.idClase, m.idMiembro, '2025-02-03', 'ACTIVO' FROM dbo.clase c CROSS JOIN dbo.miembro m WHERE c.nombre = 'Zumba' AND m.dni = '70000001';
IF NOT EXISTS (
    SELECT 1 FROM dbo.inscripcion_clase i INNER JOIN dbo.clase c ON c.idClase = i.idClase INNER JOIN dbo.miembro m ON m.idMiembro = i.idMiembro
    WHERE c.nombre = 'Spinning' AND m.dni = '70000002'
)
    INSERT dbo.inscripcion_clase (idClase, idMiembro, fechaInscripcion, estado)
    SELECT c.idClase, m.idMiembro, '2025-02-03', 'ACTIVO' FROM dbo.clase c CROSS JOIN dbo.miembro m WHERE c.nombre = 'Spinning' AND m.dni = '70000002';
IF NOT EXISTS (
    SELECT 1 FROM dbo.inscripcion_clase i INNER JOIN dbo.clase c ON c.idClase = i.idClase INNER JOIN dbo.miembro m ON m.idMiembro = i.idMiembro
    WHERE c.nombre = 'Funcional' AND m.dni = '70000001'
)
    INSERT dbo.inscripcion_clase (idClase, idMiembro, fechaInscripcion, estado)
    SELECT c.idClase, m.idMiembro, '2025-02-04', 'ACTIVO' FROM dbo.clase c CROSS JOIN dbo.miembro m WHERE c.nombre = 'Funcional' AND m.dni = '70000001';
GO

PRINT 'gymfitnessdb listo para Gym Fitness Center.';

/* Etapa 2: ocupación persistida para inscripción y cancelación atómicas. */
USE [gymfitnessdb];
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH('dbo.clase', 'cuposOcupados') IS NULL
    ALTER TABLE dbo.clase ADD cuposOcupados INT NOT NULL
        CONSTRAINT DF_clase_cuposOcupados DEFAULT (0) WITH VALUES;
COMMIT;
GO
BEGIN TRANSACTION;
UPDATE c SET cuposOcupados = (
    SELECT COUNT(*) FROM dbo.inscripcion_clase i WITH (HOLDLOCK)
    WHERE i.idClase = c.idClase AND i.estado = 'ACTIVO'
) FROM dbo.clase c WITH (UPDLOCK, HOLDLOCK);
IF EXISTS (SELECT 1 FROM dbo.clase WHERE cuposOcupados > cupos OR cupos < 0)
BEGIN
    ROLLBACK;
    THROW 51000, 'Hay clases con capacidad menor que sus inscripciones. Revisar antes de iniciar.', 1;
END;
COMMIT;
GO
