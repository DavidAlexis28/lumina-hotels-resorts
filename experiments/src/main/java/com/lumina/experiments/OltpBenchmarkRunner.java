package com.lumina.experiments;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Banco Experimental OLTP - Lumina Hotels & Resorts
 * Diseño factorial:
 *   - 5 volúmenes: 100, 1_000, 10_000, 50_000, 100_000 registros
 *   - 4 operaciones: SELECT, INSERT, UPDATE, DELETE
 *   - 5 ejecuciones de calentamiento (warmup) descartadas por combinación
 *   - 30 repeticiones válidas por combinación = 600 mediciones registradas
 *   - Fórmula: Latencia (ms) = (T_final - T_inicial) / 1_000_000.0
 */
public class OltpBenchmarkRunner {

    private static final int[] VOLUMENES = {100, 1_000, 10_000, 50_000, 100_000};
    private static final String[] OPERACIONES = {"SELECT", "INSERT", "UPDATE", "DELETE"};
    private static final int WARMUP_RUNS = 5;
    private static final int MEASURED_RUNS = 30;

    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("LUMINA_DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("LUMINA_DB_PORT", "1433");
        String dbName = System.getenv().getOrDefault("LUMINA_DB_NAME", "lumina_hotel_and_resorts");
        String user = System.getenv().getOrDefault("LUMINA_DB_USER", "sa");
        String password = System.getenv("LUMINA_DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Debe configurar la variable de entorno LUMINA_DB_PASSWORD antes de ejecutar el benchmark.");
        }

        String url = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;",
                host, port, dbName
        );

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PrintWriter rawCsv = new PrintWriter(new FileWriter("mediciones_oltp_600.csv"));
             PrintWriter statsCsv = new PrintWriter(new FileWriter("resumen_estadistico_oltp.csv"))) {

            rawCsv.println("combinacion_id,volumen_registros,operacion_oltp,repeticion,latencia_ms");
            statsCsv.println("volumen_registros,operacion_oltp,n_muestras,media_ms,mediana_ms,minimo_ms,maximo_ms,desviacion_estandar_ms,percentil_95_ms");

            int combId = 0;
            for (int volumen : VOLUMENES) {
                prepararVolumenDatos(conn, volumen);

                for (String op : OPERACIONES) {
                    combId++;
                    // 1. Fase de calentamiento (5 iteraciones descartadas)
                    for (int w = 0; w < WARMUP_RUNS; w++) {
                        ejecutarOperacionControlada(conn, op, volumen, w);
                    }

                    // 2. Fase de medición (30 repeticiones válidas)
                    double[] muestras = new double[MEASURED_RUNS];
                    for (int r = 1; r <= MEASURED_RUNS; r++) {
                        double latenciaMs = ejecutarOperacionControlada(conn, op, volumen, r);
                        muestras[r - 1] = latenciaMs;
                        rawCsv.printf(Locale.US, "%d,%d,%s,%d,%.6f%n", combId, volumen, op, r, latenciaMs);
                    }

                    // 3. Procesamiento estadístico por combinación
                    double[] sorted = Arrays.copyOf(muestras, muestras.length);
                    Arrays.sort(sorted);

                    double media = Arrays.stream(muestras).average().orElse(0.0);
                    double mediana = (sorted[14] + sorted[15]) / 2.0;
                    double min = sorted[0];
                    double max = sorted[sorted.length - 1];
                    double p95 = sorted[(int) Math.ceil(0.95 * sorted.length) - 1];

                    double sumSq = 0.0;
                    for (double v : muestras) sumSq += Math.pow(v - media, 2);
                    double stdDev = Math.sqrt(sumSq / (muestras.length - 1));

                    statsCsv.printf(Locale.US, "%d,%s,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f%n",
                            volumen, op, MEASURED_RUNS, media, mediana, min, max, stdDev, p95);
                }
            }
        }
        System.out.println("Experimento completado: 600 mediciones exportadas a mediciones_oltp_600.csv");
    }

    private static void prepararVolumenDatos(Connection conn, int objetivo) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM dbo.Reserva;");
            st.executeUpdate("DBCC CHECKIDENT ('dbo.Reserva', RESEED, 0);");
        }

        String insertSql = "INSERT INTO dbo.Reserva (ClienteID, HabitacionID, FechaEntrada, FechaSalida, Huespedes, Estado, TotalEstimado) VALUES (1, 1, ?, ?, 2, N'FINALIZADA', 230.00)";
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            LocalDate base = LocalDate.of(2020, 1, 1);
            for (int i = 0; i < objetivo; i++) {
                LocalDate ent = base.plusDays(i * 3L);
                ps.setDate(1, java.sql.Date.valueOf(ent));
                ps.setDate(2, java.sql.Date.valueOf(ent.plusDays(2)));
                ps.addBatch();
                if (i % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static double ejecutarOperacionControlada(Connection conn, String op, int volumen, int iter) throws SQLException {
        long tInicio;
        long tFin;

        switch (op) {
            case "SELECT" -> {
                String sql = "SELECT TOP 50 * FROM dbo.vw_ReservasDetalle WHERE ReservaID >= ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Math.max(1, volumen / 2));
                    tInicio = System.nanoTime();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) { /* consumir cursor */ }
                    }
                    tFin = System.nanoTime();
                }
            }
            case "INSERT" -> {
                String ins = "INSERT INTO dbo.Reserva (ClienteID, HabitacionID, FechaEntrada, FechaSalida, Huespedes, Estado, TotalEstimado) VALUES (1, 2, '2027-05-01', '2027-05-03', 2, N'CONFIRMADA', 230.00)";
                try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                    tInicio = System.nanoTime();
                    ps.executeUpdate();
                    tFin = System.nanoTime();

                    // Restablecer estado original para mantener volumen constante
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            int id = keys.getInt(1);
                            try (PreparedStatement del = conn.prepareStatement("DELETE FROM dbo.Reserva WHERE ReservaID = ?")) {
                                del.setInt(1, id);
                                del.executeUpdate();
                            }
                        }
                    }
                }
            }
            case "UPDATE" -> {
                int targetId = Math.max(1, volumen / 2);
                String upd = "UPDATE dbo.Reserva SET Huespedes = ? WHERE ReservaID = ?";
                try (PreparedStatement ps = conn.prepareStatement(upd)) {
                    ps.setInt(1, (iter % 4) + 1);
                    ps.setInt(2, targetId);
                    tInicio = System.nanoTime();
                    ps.executeUpdate();
                    tFin = System.nanoTime();
                }
            }
            case "DELETE" -> {
                // Insertar registro temporal previo y medir exclusivamente el DELETE
                int tempId = -1;
                String preIns = "INSERT INTO dbo.Reserva (ClienteID, HabitacionID, FechaEntrada, FechaSalida, Huespedes, Estado, TotalEstimado) VALUES (1, 3, '2027-06-01', '2027-06-02', 1, N'CANCELADA', 175.00)";
                try (PreparedStatement psIns = conn.prepareStatement(preIns, Statement.RETURN_GENERATED_KEYS)) {
                    psIns.executeUpdate();
                    try (ResultSet keys = psIns.getGeneratedKeys()) {
                        if (keys.next()) tempId = keys.getInt(1);
                    }
                }
                String del = "DELETE FROM dbo.Reserva WHERE ReservaID = ?";
                try (PreparedStatement psDel = conn.prepareStatement(del)) {
                    psDel.setInt(1, tempId);
                    tInicio = System.nanoTime();
                    psDel.executeUpdate();
                    tFin = System.nanoTime();
                }
            }
            default -> throw new IllegalArgumentException("Operación no soportada: " + op);
        }

        return (tFin - tInicio) / 1_000_000.0;
    }
}
