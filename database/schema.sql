-- =============================================================================
-- SISTEMA DE GESTIÓN HOTELERA: LUMINA HOTELS & RESORTS
-- Archivo: database/schema.sql
-- Motor: Microsoft SQL Server 2022 Developer Edition
-- Incluye: Tablas, llaves, restricciones, índices, vista vw_ReservasDetalle y
--          procedimientos almacenados CRUD completos (Create, Read, Update, Delete)
-- =============================================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'lumina_hotel_and_resorts')
BEGIN
    CREATE DATABASE [lumina_hotel_and_resorts];
END;
GO

USE [lumina_hotel_and_resorts];
GO

IF OBJECT_ID('dbo.vw_ReservasDetalle', 'V') IS NOT NULL DROP VIEW dbo.vw_ReservasDetalle;
IF OBJECT_ID('dbo.sp_HabitacionesDisponibles', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_HabitacionesDisponibles;
IF OBJECT_ID('dbo.sp_RegistrarReserva', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_RegistrarReserva;
IF OBJECT_ID('dbo.sp_ActualizarReserva', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_ActualizarReserva;
IF OBJECT_ID('dbo.sp_CancelarReserva', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_CancelarReserva;
IF OBJECT_ID('dbo.sp_EliminarReserva', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_EliminarReserva;
IF OBJECT_ID('dbo.Reserva', 'U') IS NOT NULL DROP TABLE dbo.Reserva;
IF OBJECT_ID('dbo.Habitacion', 'U') IS NOT NULL DROP TABLE dbo.Habitacion;
IF OBJECT_ID('dbo.TipoHabitacion', 'U') IS NOT NULL DROP TABLE dbo.TipoHabitacion;
IF OBJECT_ID('dbo.Cliente', 'U') IS NOT NULL DROP TABLE dbo.Cliente;
GO

CREATE TABLE dbo.Cliente (
    ClienteID INT IDENTITY(1,1) NOT NULL,
    Nombres NVARCHAR(100) NOT NULL,
    Apellidos NVARCHAR(100) NOT NULL,
    DocumentoIdentidad NVARCHAR(30) NOT NULL,
    Correo NVARCHAR(150) NOT NULL,
    Telefono NVARCHAR(25) NULL,
    Genero NVARCHAR(50) NULL,
    FechaNacimiento NVARCHAR(20) NULL,
    Pais NVARCHAR(100) NULL,
    Direccion NVARCHAR(250) NULL,
    NumeroPasaporte NVARCHAR(30) NULL,
    PaisEmisorPasaporte NVARCHAR(100) NULL,
    FechaVencimientoPasaporte NVARCHAR(20) NULL,
    FechaRegistro DATETIME2 NOT NULL CONSTRAINT DF_Cliente_FechaRegistro DEFAULT SYSDATETIME(),
    Activo BIT NOT NULL CONSTRAINT DF_Cliente_Activo DEFAULT (1),
    CONSTRAINT PK_Cliente PRIMARY KEY CLUSTERED (ClienteID),
    CONSTRAINT UQ_Cliente_Documento UNIQUE (DocumentoIdentidad),
    CONSTRAINT UQ_Cliente_Correo UNIQUE (Correo)
);
GO

CREATE TABLE dbo.Hotel (
    HotelID NVARCHAR(50) NOT NULL,
    Nombre NVARCHAR(150) NOT NULL,
    Ciudad NVARCHAR(100) NOT NULL,
    Direccion NVARCHAR(250) NULL,
    Estrellas INT NOT NULL DEFAULT 5,
    Puntuacion DECIMAL(3,1) NOT NULL DEFAULT 9.4,
    ResenasCount INT NOT NULL DEFAULT 100,
    ResenasLabel NVARCHAR(50) NOT NULL DEFAULT N'Excepcional',
    Descripcion NVARCHAR(MAX) NULL,
    Amenidades NVARCHAR(500) NULL,
    PrecioBase DECIMAL(10,2) NOT NULL DEFAULT 100.0,
    FeaturedTag NVARCHAR(50) NULL,
    ImagenUrl NVARCHAR(500) NULL,
    CONSTRAINT PK_Hotel PRIMARY KEY CLUSTERED (HotelID)
);
GO

CREATE TABLE dbo.HotelRoom (
    RoomID INT IDENTITY(1,1) NOT NULL,
    HotelID NVARCHAR(50) NOT NULL,
    Nombre NVARCHAR(100) NOT NULL,
    ConfiguracionCama NVARCHAR(100) NULL,
    Capacidad INT NOT NULL DEFAULT 2,
    TamanoM2 INT NOT NULL DEFAULT 35,
    PrecioPorNoche DECIMAL(10,2) NOT NULL,
    Amenidades NVARCHAR(500) NULL,
    ImagenUrl NVARCHAR(500) NULL,
    CuartosDisponibles INT NOT NULL DEFAULT 5,
    CuartosTotales INT NOT NULL DEFAULT 5,
    CONSTRAINT PK_HotelRoom PRIMARY KEY CLUSTERED (RoomID),
    CONSTRAINT FK_HotelRoom_Hotel FOREIGN KEY (HotelID) REFERENCES dbo.Hotel (HotelID)
);
GO

CREATE TABLE dbo.Atraccion (
    AtraccionID NVARCHAR(50) NOT NULL,
    Titulo NVARCHAR(150) NOT NULL,
    Ciudad NVARCHAR(100) NOT NULL,
    Categoria NVARCHAR(100) NOT NULL,
    Puntuacion DECIMAL(3,1) NOT NULL DEFAULT 4.9,
    ResenasCount INT NOT NULL DEFAULT 50,
    Duracion NVARCHAR(50) NOT NULL,
    PrecioPorPersona DECIMAL(10,2) NOT NULL,
    Descripcion NVARCHAR(MAX) NULL,
    ImagenUrl NVARCHAR(500) NULL,
    CapacidadMicrobus INT NOT NULL DEFAULT 14,
    LugaresDisponibles INT NOT NULL DEFAULT 14,
    CONSTRAINT PK_Atraccion PRIMARY KEY CLUSTERED (AtraccionID)
);
GO

CREATE TABLE dbo.TipoHabitacion (
    TipoHabitacionID INT IDENTITY(1,1) NOT NULL,
    Nombre NVARCHAR(80) NOT NULL,
    Descripcion NVARCHAR(250) NULL,
    Capacidad INT NOT NULL,
    PrecioPorNoche DECIMAL(10, 2) NOT NULL,
    Activo BIT NOT NULL CONSTRAINT DF_TipoHabitacion_Activo DEFAULT (1),
    CONSTRAINT PK_TipoHabitacion PRIMARY KEY CLUSTERED (TipoHabitacionID),
    CONSTRAINT CK_TipoHabitacion_Capacidad CHECK (Capacidad > 0),
    CONSTRAINT CK_TipoHabitacion_Precio CHECK (PrecioPorNoche > 0)
);
GO

CREATE TABLE dbo.Habitacion (
    HabitacionID INT IDENTITY(1,1) NOT NULL,
    TipoHabitacionID INT NOT NULL,
    Numero NVARCHAR(15) NOT NULL,
    Piso INT NOT NULL,
    Estado NVARCHAR(20) NOT NULL CONSTRAINT DF_Habitacion_Estado DEFAULT (N'DISPONIBLE'),
    CONSTRAINT PK_Habitacion PRIMARY KEY CLUSTERED (HabitacionID),
    CONSTRAINT UQ_Habitacion_Numero UNIQUE (Numero),
    CONSTRAINT FK_Habitacion_TipoHabitacion FOREIGN KEY (TipoHabitacionID)
        REFERENCES dbo.TipoHabitacion (TipoHabitacionID),
    CONSTRAINT CK_Habitacion_Estado CHECK (Estado IN (N'DISPONIBLE', N'OCUPADA', N'MANTENIMIENTO'))
);
GO

CREATE TABLE dbo.Reserva (
    ReservaID INT IDENTITY(1,1) NOT NULL,
    ClienteID INT NOT NULL,
    HabitacionID INT NOT NULL,
    FechaEntrada DATE NOT NULL,
    FechaSalida DATE NOT NULL,
    Huespedes INT NOT NULL,
    Estado NVARCHAR(20) NOT NULL CONSTRAINT DF_Reserva_Estado DEFAULT (N'CONFIRMADA'),
    TotalEstimado DECIMAL(12, 2) NOT NULL,
    FechaCreacion DATETIME2 NOT NULL CONSTRAINT DF_Reserva_FechaCreacion DEFAULT SYSDATETIME(),
    CONSTRAINT PK_Reserva PRIMARY KEY CLUSTERED (ReservaID),
    CONSTRAINT FK_Reserva_Cliente FOREIGN KEY (ClienteID)
        REFERENCES dbo.Cliente (ClienteID),
    CONSTRAINT FK_Reserva_Habitacion FOREIGN KEY (HabitacionID)
        REFERENCES dbo.Habitacion (HabitacionID),
    CONSTRAINT CK_Reserva_Fechas CHECK (FechaSalida > FechaEntrada),
    CONSTRAINT CK_Reserva_Huespedes CHECK (Huespedes > 0),
    CONSTRAINT CK_Reserva_Estado CHECK (Estado IN (N'PENDIENTE', N'CONFIRMADA', N'CANCELADA', N'FINALIZADA'))
);
GO

CREATE NONCLUSTERED INDEX IX_Habitacion_Estado_Tipo
    ON dbo.Habitacion (Estado, TipoHabitacionID)
    INCLUDE (Numero, Piso);

CREATE NONCLUSTERED INDEX IX_Reserva_Habitacion_Fechas_Estado
    ON dbo.Reserva (HabitacionID, FechaEntrada, FechaSalida)
    INCLUDE (Estado, ClienteID, TotalEstimado);

CREATE NONCLUSTERED INDEX IX_Reserva_Cliente
    ON dbo.Reserva (ClienteID, FechaCreacion DESC);
GO

CREATE VIEW dbo.vw_ReservasDetalle
AS
SELECT
    r.ReservaID,
    r.ClienteID,
    CONCAT(c.Nombres, N' ', c.Apellidos) AS NombreCompletoCliente,
    c.DocumentoIdentidad,
    c.Correo AS CorreoCliente,
    h.HabitacionID,
    h.Numero AS NumeroHabitacion,
    h.Piso,
    th.TipoHabitacionID,
    th.Nombre AS TipoHabitacion,
    th.PrecioPorNoche,
    r.FechaEntrada,
    r.FechaSalida,
    DATEDIFF(DAY, r.FechaEntrada, r.FechaSalida) AS CantidadNoches,
    r.Huespedes,
    r.Estado AS EstadoReserva,
    (DATEDIFF(DAY, r.FechaEntrada, r.FechaSalida) * th.PrecioPorNoche) AS TotalEstimadoCobro,
    r.FechaCreacion
FROM dbo.Reserva r
INNER JOIN dbo.Cliente c ON c.ClienteID = r.ClienteID
INNER JOIN dbo.Habitacion h ON h.HabitacionID = r.HabitacionID
INNER JOIN dbo.TipoHabitacion th ON th.TipoHabitacionID = h.TipoHabitacionID;
GO

CREATE PROCEDURE [dbo].[sp_HabitacionesDisponibles]
(
    @FechaEntrada DATE,
    @FechaSalida DATE,
    @Huespedes INT = 1
)
AS
BEGIN
    SET NOCOUNT ON;

    IF @FechaEntrada IS NULL OR @FechaSalida IS NULL OR @FechaSalida <= @FechaEntrada
        THROW 50010, 'Las fechas de entrada y salida no son validas.', 1;

    IF @Huespedes IS NULL OR @Huespedes <= 0
        THROW 50011, 'La cantidad de huespedes debe ser mayor que cero.', 1;

    SELECT
        h.HabitacionID,
        h.Numero,
        h.Piso,
        h.Estado,
        th.TipoHabitacionID,
        th.Nombre,
        th.Capacidad,
        th.PrecioPorNoche
    FROM dbo.Habitacion h
    INNER JOIN dbo.TipoHabitacion th ON th.TipoHabitacionID = h.TipoHabitacionID
    WHERE h.Estado = N'DISPONIBLE'
      AND th.Activo = 1
      AND th.Capacidad >= @Huespedes
      AND NOT EXISTS (
          SELECT 1
          FROM dbo.Reserva r
          WHERE r.HabitacionID = h.HabitacionID
            AND r.Estado IN (N'PENDIENTE', N'CONFIRMADA')
            AND r.FechaEntrada < @FechaSalida
            AND r.FechaSalida > @FechaEntrada
      )
    ORDER BY th.PrecioPorNoche;
END;
GO

CREATE PROCEDURE [dbo].[sp_RegistrarReserva]
(
    @ClienteID INT,
    @HabitacionID INT,
    @FechaEntrada DATE,
    @FechaSalida DATE,
    @Huespedes INT,
    @Estado NVARCHAR(20) = N'CONFIRMADA'
)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @FechaEntrada IS NULL OR @FechaSalida IS NULL OR @FechaSalida <= @FechaEntrada
        THROW 50010, 'Las fechas de entrada y salida no son validas.', 1;

    IF @Huespedes IS NULL OR @Huespedes <= 0
        THROW 50011, 'La cantidad de huespedes debe ser mayor que cero.', 1;

    BEGIN TRANSACTION;

    DECLARE @PrecioPorNoche DECIMAL(10, 2);
    DECLARE @Capacidad INT;
    DECLARE @EstadoHab NVARCHAR(20);

    SELECT
        @PrecioPorNoche = th.PrecioPorNoche,
        @Capacidad = th.Capacidad,
        @EstadoHab = h.Estado
    FROM dbo.Habitacion h WITH (UPDLOCK, HOLDLOCK)
    INNER JOIN dbo.TipoHabitacion th ON th.TipoHabitacionID = h.TipoHabitacionID
    WHERE h.HabitacionID = @HabitacionID AND th.Activo = 1;

    IF @PrecioPorNoche IS NULL
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50012, 'La habitacion indicada no existe o no esta activa.', 1;
    END;

    IF @EstadoHab <> N'DISPONIBLE'
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50013, 'La habitacion no se encuentra disponible operativamente.', 1;
    END;

    IF @Huespedes > @Capacidad
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50014, 'La cantidad de huespedes excede la capacidad de la habitacion.', 1;
    END;

    IF EXISTS (
        SELECT 1
        FROM dbo.Reserva r WITH (UPDLOCK, HOLDLOCK)
        WHERE r.HabitacionID = @HabitacionID
          AND r.Estado IN (N'PENDIENTE', N'CONFIRMADA')
          AND r.FechaEntrada < @FechaSalida
          AND r.FechaSalida > @FechaEntrada
    )
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50015, 'La habitacion ya posee una reserva en el rango de fechas solicitado.', 1;
    END;

    DECLARE @Noches INT = DATEDIFF(DAY, @FechaEntrada, @FechaSalida);
    DECLARE @Total DECIMAL(12, 2) = @Noches * @PrecioPorNoche;

    INSERT INTO dbo.Reserva (ClienteID, HabitacionID, FechaEntrada, FechaSalida, Huespedes, Estado, TotalEstimado)
    VALUES (@ClienteID, @HabitacionID, @FechaEntrada, @FechaSalida, @Huespedes, @Estado, @Total);

    DECLARE @NuevoReservaID INT = SCOPE_IDENTITY();

    COMMIT TRANSACTION;

    SELECT * FROM dbo.vw_ReservasDetalle WHERE ReservaID = @NuevoReservaID;
END;
GO

CREATE PROCEDURE [dbo].[sp_ActualizarReserva]
(
    @ReservaID INT,
    @FechaEntrada DATE,
    @FechaSalida DATE,
    @Huespedes INT,
    @Estado NVARCHAR(20)
)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @FechaEntrada IS NULL OR @FechaSalida IS NULL OR @FechaSalida <= @FechaEntrada
        THROW 50010, 'Las fechas de entrada y salida no son validas.', 1;

    IF @Huespedes IS NULL OR @Huespedes <= 0
        THROW 50011, 'La cantidad de huespedes debe ser mayor que cero.', 1;

    BEGIN TRANSACTION;

    DECLARE @HabitacionID INT;
    DECLARE @PrecioPorNoche DECIMAL(10, 2);
    DECLARE @Capacidad INT;

    SELECT
        @HabitacionID = r.HabitacionID,
        @PrecioPorNoche = th.PrecioPorNoche,
        @Capacidad = th.Capacidad
    FROM dbo.Reserva r WITH (UPDLOCK, HOLDLOCK)
    INNER JOIN dbo.Habitacion h ON h.HabitacionID = r.HabitacionID
    INNER JOIN dbo.TipoHabitacion th ON th.TipoHabitacionID = h.TipoHabitacionID
    WHERE r.ReservaID = @ReservaID;

    IF @HabitacionID IS NULL
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50016, 'La reserva especificada no existe.', 1;
    END;

    IF @Huespedes > @Capacidad
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50014, 'La cantidad de huespedes excede la capacidad de la habitacion.', 1;
    END;

    IF @Estado IN (N'PENDIENTE', N'CONFIRMADA') AND EXISTS (
        SELECT 1
        FROM dbo.Reserva r2 WITH (UPDLOCK, HOLDLOCK)
        WHERE r2.HabitacionID = @HabitacionID
          AND r2.ReservaID <> @ReservaID
          AND r2.Estado IN (N'PENDIENTE', N'CONFIRMADA')
          AND r2.FechaEntrada < @FechaSalida
          AND r2.FechaSalida > @FechaEntrada
    )
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50015, 'Conflicto de fechas con otra reserva activa en la misma habitacion.', 1;
    END;

    DECLARE @Total DECIMAL(12, 2) = DATEDIFF(DAY, @FechaEntrada, @FechaSalida) * @PrecioPorNoche;

    UPDATE dbo.Reserva
    SET FechaEntrada = @FechaEntrada,
        FechaSalida = @FechaSalida,
        Huespedes = @Huespedes,
        Estado = @Estado,
        TotalEstimado = @Total
    WHERE ReservaID = @ReservaID;

    COMMIT TRANSACTION;

    SELECT * FROM dbo.vw_ReservasDetalle WHERE ReservaID = @ReservaID;
END;
GO

CREATE PROCEDURE [dbo].[sp_CancelarReserva]
(
    @ReservaID INT
)
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.Reserva WHERE ReservaID = @ReservaID)
        THROW 50016, 'La reserva especificada no existe.', 1;

    UPDATE dbo.Reserva
    SET Estado = N'CANCELADA'
    WHERE ReservaID = @ReservaID
      AND Estado IN (N'PENDIENTE', N'CONFIRMADA');

    SELECT * FROM dbo.vw_ReservasDetalle WHERE ReservaID = @ReservaID;
END;
GO

CREATE PROCEDURE [dbo].[sp_EliminarReserva]
(
    @ReservaID INT
)
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.Reserva WHERE ReservaID = @ReservaID)
        THROW 50016, 'La reserva especificada no existe.', 1;

    DELETE FROM dbo.Reserva WHERE ReservaID = @ReservaID;
END;
GO
