package com.lumina.hotels.dao;

import com.lumina.hotels.config.ConexionDB;
import com.lumina.hotels.model.Reserva;
import com.lumina.hotels.model.ReservaDetalleView;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    public ReservaDetalleView registrarReserva(String nombres, String apellidos, String documento,
                                               String correo, String telefono,
                                               Reserva reserva) throws SQLException {
        if (reserva == null || !reserva.esRangoFechasValido()) {
            throw new IllegalArgumentException("Las fechas de entrada y salida de la reserva no son válidas.");
        }
        if (reserva.getHuespedes() <= 0) {
            throw new IllegalArgumentException("La cantidad de huéspedes debe ser mayor que cero.");
        }

        try (Connection conn = ConexionDB.obtenerConexion()) {
            int clienteId = obtenerOCrearCliente(conn, nombres, apellidos, documento, correo, telefono);

            String callSql = "{call dbo.sp_RegistrarReserva(?, ?, ?, ?, ?, ?)}";
            try (CallableStatement cs = conn.prepareCall(callSql)) {
                cs.setInt(1, clienteId);
                cs.setInt(2, reserva.getHabitacionId());
                cs.setDate(3, Date.valueOf(reserva.getFechaEntrada()));
                cs.setDate(4, Date.valueOf(reserva.getFechaSalida()));
                cs.setInt(5, reserva.getHuespedes());
                cs.setString(6, reserva.getEstado() != null ? reserva.getEstado() : "CONFIRMADA");

                try (ResultSet rs = cs.executeQuery()) {
                    if (rs.next()) {
                        return mapearDetalle(rs);
                    }
                }
            }
        }
        throw new SQLException("No se pudo obtener el detalle de la reserva registrada.");
    }

    public List<ReservaDetalleView> listarReservasDetalle() throws SQLException {
        List<ReservaDetalleView> lista = new ArrayList<>();
        String sql = "SELECT * FROM dbo.vw_ReservasDetalle ORDER BY ReservaID DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearDetalle(rs));
            }
        }
        return lista;
    }

    public ReservaDetalleView actualizarReserva(int reservaId, LocalDate nuevaEntrada, LocalDate nuevaSalida,
                                                int nuevosHuespedes, String nuevoEstado) throws SQLException {
        String callSql = "{call dbo.sp_ActualizarReserva(?, ?, ?, ?, ?)}";
        try (Connection conn = ConexionDB.obtenerConexion();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, reservaId);
            cs.setDate(2, Date.valueOf(nuevaEntrada));
            cs.setDate(3, Date.valueOf(nuevaSalida));
            cs.setInt(4, nuevosHuespedes);
            cs.setString(5, nuevoEstado);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return mapearDetalle(rs);
                }
            }
        }
        throw new SQLException("No se pudo actualizar la reserva ID: " + reservaId);
    }

    public ReservaDetalleView cancelarReserva(int reservaId) throws SQLException {
        String callSql = "{call dbo.sp_CancelarReserva(?)}";
        try (Connection conn = ConexionDB.obtenerConexion();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, reservaId);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return mapearDetalle(rs);
                }
            }
        }
        throw new SQLException("No se encontró la reserva con ID: " + reservaId);
    }

    public void eliminarReserva(int reservaId) throws SQLException {
        String callSql = "{call dbo.sp_EliminarReserva(?)}";
        try (Connection conn = ConexionDB.obtenerConexion();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, reservaId);
            cs.execute();
        }
    }

    private int obtenerOCrearCliente(Connection conn, String nombres, String apellidos,
                                     String documento, String correo, String telefono) throws SQLException {
        String selectSql = "SELECT ClienteID FROM dbo.Cliente WHERE Correo = ? OR DocumentoIdentidad = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, correo);
            ps.setString(2, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ClienteID");
                }
            }
        }

        String insertSql = "INSERT INTO dbo.Cliente (Nombres, Apellidos, DocumentoIdentidad, Correo, Telefono) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setString(3, documento);
            ps.setString(4, correo);
            ps.setString(5, telefono);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo registrar el cliente.");
    }

    private ReservaDetalleView mapearDetalle(ResultSet rs) throws SQLException {
        ReservaDetalleView dto = new ReservaDetalleView();
        dto.setReservaId(rs.getInt("ReservaID"));
        dto.setClienteId(rs.getInt("ClienteID"));
        dto.setNombreCompletoCliente(rs.getString("NombreCompletoCliente"));
        dto.setDocumentoIdentidad(rs.getString("DocumentoIdentidad"));
        dto.setCorreoCliente(rs.getString("CorreoCliente"));
        dto.setHabitacionId(rs.getInt("HabitacionID"));
        dto.setNumeroHabitacion(rs.getString("NumeroHabitacion"));
        dto.setPiso(rs.getInt("Piso"));
        dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
        dto.setPrecioPorNoche(rs.getBigDecimal("PrecioPorNoche"));
        dto.setFechaEntrada(rs.getString("FechaEntrada"));
        dto.setFechaSalida(rs.getString("FechaSalida"));
        dto.setCantidadNoches(rs.getInt("CantidadNoches"));
        dto.setHuespedes(rs.getInt("Huespedes"));
        dto.setEstadoReserva(rs.getString("EstadoReserva"));
        dto.setTotalEstimadoCobro(rs.getBigDecimal("TotalEstimadoCobro"));
        return dto;
    }
}
