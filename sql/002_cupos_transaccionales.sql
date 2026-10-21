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
