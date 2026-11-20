package com.lumina.hotels.dao;

import com.lumina.hotels.config.ConexionDB;
import com.lumina.hotels.model.Habitacion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitacionDAO {

    public List<Habitacion> consultarDisponibles(LocalDate fechaEntrada, LocalDate fechaSalida, int huespedes) throws SQLException {
        List<Habitacion> disponibles = new ArrayList<>();
        String sql = "{call dbo.sp_HabitacionesDisponibles(?, ?, ?)}";

        try (Connection conn = ConexionDB.obtenerConexion();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setDate(1, Date.valueOf(fechaEntrada));
            stmt.setDate(2, Date.valueOf(fechaSalida));
            stmt.setInt(3, huespedes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    disponibles.add(mapearHabitacion(rs));
                }
            }
        }
        return disponibles;
    }

    public List<Habitacion> listarTodas() throws SQLException {
        List<Habitacion> lista = new ArrayList<>();
        String sql = "SELECT h.HabitacionID, h.Numero, h.Piso, h.Estado, " +
                     "th.TipoHabitacionID, th.Nombre, th.Capacidad, th.PrecioPorNoche " +
                     "FROM dbo.Habitacion h " +
                     "INNER JOIN dbo.TipoHabitacion th ON th.TipoHabitacionID = h.TipoHabitacionID " +
                     "ORDER BY h.Numero";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearHabitacion(rs));
            }
        }
        return lista;
    }

    public int crearHabitacion(int tipoHabitacionId, String numero, int piso) throws SQLException {
        String sql = "INSERT INTO dbo.Habitacion (TipoHabitacionID, Numero, Piso, Estado) VALUES (?, ?, ?, N'DISPONIBLE')";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tipoHabitacionId);
            ps.setString(2, numero);
            ps.setInt(3, piso);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean actualizarHabitacion(int habitacionId, String numero, int piso, String estado, int tipoHabitacionId) throws SQLException {
        String sql = "UPDATE dbo.Habitacion SET Numero = ?, Piso = ?, Estado = ?, TipoHabitacionID = ? WHERE HabitacionID = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numero);
            ps.setInt(2, piso);
            ps.setString(3, estado);
            ps.setInt(4, tipoHabitacionId);
            ps.setInt(5, habitacionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarHabitacion(int habitacionId) throws SQLException {
        String sql = "DELETE FROM dbo.Habitacion WHERE HabitacionID = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitacionId);
            return ps.executeUpdate() > 0;
        }
    }

    private Habitacion mapearHabitacion(ResultSet rs) throws SQLException {
        return new Habitacion(
                rs.getInt("HabitacionID"),
                rs.getString("Numero"),
                rs.getInt("Piso"),
                rs.getString("Estado"),
                rs.getInt("TipoHabitacionID"),
                rs.getString("Nombre"),
                rs.getInt("Capacidad"),
                rs.getBigDecimal("PrecioPorNoche")
        );
    }
}
