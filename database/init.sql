-- =============================================================================
-- DATOS SEMILLA INICIALES (FICTICIOS / ÉTICOS) - LUMINA HOTELS & RESORTS
-- Archivo: database/init.sql
-- =============================================================================

USE [lumina_hotel_and_resorts];
GO

INSERT INTO dbo.TipoHabitacion (Nombre, Descripcion, Capacidad, PrecioPorNoche, Activo)
VALUES
    (N'Estándar Queen', N'Habitación ejecutiva con cama Queen, escritorio y vista al jardín.', 2, 115.00, 1),
    (N'Doble Superior', N'Dos camas matrimoniales, balcón privado y sala de estar.', 4, 175.00, 1),
    (N'Suite Presidencial', N'Suite panorámica de lujo con jacuzzi, sala de reuniones y terraza.', 4, 310.00, 1);
GO

INSERT INTO dbo.Habitacion (TipoHabitacionID, Numero, Piso, Estado)
VALUES
    (1, N'101', 1, N'DISPONIBLE'),
    (1, N'102', 1, N'DISPONIBLE'),
    (2, N'201', 2, N'DISPONIBLE'),
    (2, N'202', 2, N'MANTENIMIENTO'),
    (3, N'301', 3, N'DISPONIBLE'),
    (3, N'302', 3, N'DISPONIBLE');
GO

INSERT INTO dbo.Cliente (Nombres, Apellidos, DocumentoIdentidad, Correo, Telefono)
VALUES
    (N'Carlos Alberto', N'Mendoza Ruiz', N'05123456-7', N'carlos.mendoza@ejemplo.com', N'7100-1122'),
    (N'Sofía Elena', N'Ramírez Castro', N'04987654-1', N'sofia.ramirez@ejemplo.com', N'7200-3344'),
    (N'Alejandro José', N'Vance Torres', N'03456789-0', N'alejandro.vance@ejemplo.com', N'7300-5566');
GO

EXEC dbo.sp_RegistrarReserva
    @ClienteID = 1,
    @HabitacionID = 1,
    @FechaEntrada = '2026-11-20',
    @FechaSalida = '2026-11-23',
    @Huespedes = 2,
    @Estado = N'CONFIRMADA';
GO
