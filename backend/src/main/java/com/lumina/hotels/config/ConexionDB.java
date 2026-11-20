package com.lumina.hotels.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "1433";
    private static final String DEFAULT_DB = "lumina_hotel_and_resorts";
    private static final String DEFAULT_USER = "sa";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró el controlador JDBC de SQL Server.", e);
        }
    }

    public static String buildJdbcUrl() {
        String customUrl = System.getenv("LUMINA_DB_URL");
        if (customUrl != null && !customUrl.isBlank()) {
            return customUrl;
        }
        String host = System.getenv().getOrDefault("LUMINA_DB_HOST", DEFAULT_HOST);
        String port = System.getenv().getOrDefault("LUMINA_DB_PORT", DEFAULT_PORT);
        String dbName = System.getenv().getOrDefault("LUMINA_DB_NAME", DEFAULT_DB);
        return String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;",
                host, port, dbName
        );
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = buildJdbcUrl();
        String user = System.getenv().getOrDefault("LUMINA_DB_USER", DEFAULT_USER);
        String password = System.getenv("LUMINA_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new SQLException("Variable de entorno LUMINA_DB_PASSWORD no configurada.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
