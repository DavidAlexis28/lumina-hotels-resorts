package com.lumina.hotels.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = ConexionDB.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            System.out.println("[DatabaseInitializer] Conexión establecida. Verificando esquema de base de datos...");

            // 1. Columnas adicionales para Cliente (Perfil y Pasaporte)
            String[] clienteCols = {
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.Cliente') AND name = 'Genero') ALTER TABLE dbo.Cliente ADD Genero NVARCHAR(50) NULL;",
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.Cliente') AND name = 'FechaNacimiento') ALTER TABLE dbo.Cliente ADD FechaNacimiento NVARCHAR(20) NULL;",
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.Cliente') AND name = 'Pais') ALTER TABLE dbo.Cliente ADD Pais NVARCHAR(100) NULL;",
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.Cliente') AND name = 'Direccion') ALTER TABLE dbo.Cliente ADD Direccion NVARCHAR(250) NULL;",
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.Cliente') AND name = 'NumeroPasaporte') ALTER TABLE dbo.Cliente ADD NumeroPasaporte NVARCHAR(30) NULL;",
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.Cliente') AND name = 'PaisEmisorPasaporte') ALTER TABLE dbo.Cliente ADD PaisEmisorPasaporte NVARCHAR(100) NULL;",
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.Cliente') AND name = 'FechaVencimientoPasaporte') ALTER TABLE dbo.Cliente ADD FechaVencimientoPasaporte NVARCHAR(20) NULL;"
            };
            for (String sql : clienteCols) {
                stmt.execute(sql);
            }

            // 2. Tabla Hotel
            String sqlCreateHotel =
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Hotel') " +
                "BEGIN " +
                "    CREATE TABLE dbo.Hotel ( " +
                "        HotelID NVARCHAR(50) NOT NULL PRIMARY KEY, " +
                "        Nombre NVARCHAR(150) NOT NULL, " +
                "        Ciudad NVARCHAR(100) NOT NULL, " +
                "        Direccion NVARCHAR(250) NULL, " +
                "        Estrellas INT NOT NULL DEFAULT 5, " +
                "        Puntuacion DECIMAL(3,1) NOT NULL DEFAULT 9.4, " +
                "        ResenasCount INT NOT NULL DEFAULT 100, " +
                "        ResenasLabel NVARCHAR(50) NOT NULL DEFAULT N'Excepcional', " +
                "        Descripcion NVARCHAR(MAX) NULL, " +
                "        Amenidades NVARCHAR(500) NULL, " +
                "        PrecioBase DECIMAL(10,2) NOT NULL DEFAULT 100.0, " +
                "        FeaturedTag NVARCHAR(50) NULL, " +
                "        ImagenUrl NVARCHAR(500) NULL " +
                "    ); " +
                "END";
            stmt.execute(sqlCreateHotel);

            // 3. Tabla HotelRoom
            String sqlCreateRoom =
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'HotelRoom') " +
                "BEGIN " +
                "    CREATE TABLE dbo.HotelRoom ( " +
                "        RoomID INT IDENTITY(1,1) PRIMARY KEY, " +
                "        HotelID NVARCHAR(50) NOT NULL, " +
                "        Nombre NVARCHAR(100) NOT NULL, " +
                "        ConfiguracionCama NVARCHAR(100) NULL, " +
                "        Capacidad INT NOT NULL DEFAULT 2, " +
                "        TamanoM2 INT NOT NULL DEFAULT 35, " +
                "        PrecioPorNoche DECIMAL(10,2) NOT NULL, " +
                "        Amenidades NVARCHAR(500) NULL, " +
                "        ImagenUrl NVARCHAR(500) NULL, " +
                "        CuartosDisponibles INT NOT NULL DEFAULT 5, " +
                "        CuartosTotales INT NOT NULL DEFAULT 5 " +
                "    ); " +
                "END";
            stmt.execute(sqlCreateRoom);

            // 4. Tabla Atraccion
            String sqlCreateAtraccion =
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Atraccion') " +
                "BEGIN " +
                "    CREATE TABLE dbo.Atraccion ( " +
                "        AtraccionID NVARCHAR(50) NOT NULL PRIMARY KEY, " +
                "        Titulo NVARCHAR(150) NOT NULL, " +
                "        Ciudad NVARCHAR(100) NOT NULL, " +
                "        Categoria NVARCHAR(100) NOT NULL, " +
                "        Puntuacion DECIMAL(3,1) NOT NULL DEFAULT 4.9, " +
                "        ResenasCount INT NOT NULL DEFAULT 50, " +
                "        Duracion NVARCHAR(50) NOT NULL, " +
                "        PrecioPorPersona DECIMAL(10,2) NOT NULL, " +
                "        Descripcion NVARCHAR(MAX) NULL, " +
                "        ImagenUrl NVARCHAR(500) NULL, " +
                "        CapacidadMicrobus INT NOT NULL DEFAULT 14, " +
                "        LugaresDisponibles INT NOT NULL DEFAULT 14 " +
                "    ); " +
                "END";
            stmt.execute(sqlCreateAtraccion);

            System.out.println("[DatabaseInitializer] Esquema verificado y actualizado exitosamente.");

        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Conexión a base de datos omitida (" + e.getMessage() + "). El servidor operará con almacenamiento resiliente en memoria.");
        }
    }
}
