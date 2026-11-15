package com.lumina.backend.repositories;

import com.lumina.backend.models.Habitacion;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class HabitacionRepository {
    private static final String ROOM_SELECT = """
            SELECT h.HabitacionID, h.Numero, h.TipoHabitacionID, t.Nombre AS NombreTipo,
                   h.Piso, h.Estado, h.Capacidad, h.PrecioPorNoche
            FROM dbo.Habitacion h
            LEFT JOIN dbo.TipoHabitacion t ON t.TipoHabitacionID = h.TipoHabitacionID
            """;

    private final DataSource dataSource;

    public HabitacionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Habitacion> findAll() {
        return queryRooms(ROOM_SELECT + " ORDER BY h.HabitacionID", statement -> { });
    }

    public Optional<Habitacion> findById(int habitacionID) {
        List<Habitacion> rooms = queryRooms(ROOM_SELECT + " WHERE h.HabitacionID = ?",
                statement -> statement.setInt(1, habitacionID));
        return rooms.stream().findFirst();
    }

    public List<Habitacion> findDisponibles(LocalDate fechaInicio, LocalDate fechaFin,
                                             Integer tipoHabitacionID, Integer huespedes) {
        String sql = ROOM_SELECT + """
                 WHERE UPPER(LTRIM(RTRIM(h.Estado))) = 'DISPONIBLE'
                   AND h.Capacidad >= ?
                   AND (? IS NULL OR h.TipoHabitacionID = ?)
                   AND NOT EXISTS (
                       SELECT 1 FROM dbo.Reserva r
                       WHERE r.HabitacionID = h.HabitacionID
                         AND COALESCE(UPPER(LTRIM(RTRIM(r.Estado))), '') NOT IN ('CANCELADA', 'CANCELADO')
                         AND r.FechaEntrada < ? AND r.FechaSalida > ?
                   )
                 ORDER BY h.HabitacionID
                """;
        return queryRooms(sql, statement -> {
            statement.setInt(1, huespedes);
            if (tipoHabitacionID == null) {
                statement.setNull(2, Types.INTEGER);
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(2, tipoHabitacionID);
                statement.setInt(3, tipoHabitacionID);
            }
            statement.setDate(4, java.sql.Date.valueOf(fechaFin));
            statement.setDate(5, java.sql.Date.valueOf(fechaInicio));
        });
    }

    public boolean actualizarEstado(int habitacionID, String estado) {
        String sql = "UPDATE dbo.Habitacion SET Estado = ? WHERE HabitacionID = ?";
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estado);
            statement.setInt(2, habitacionID);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessResourceFailureException("No se pudo actualizar el estado de la habitación.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private List<Habitacion> queryRooms(String sql, StatementBinder binder) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Habitacion> rooms = new ArrayList<>();
                while (resultSet.next()) {
                    rooms.add(new Habitacion(
                            resultSet.getInt("HabitacionID"),
                            resultSet.getString("Numero"),
                            resultSet.getInt("TipoHabitacionID"),
                            resultSet.getString("NombreTipo"),
                            resultSet.getInt("Piso"),
                            resultSet.getString("Estado"),
                            resultSet.getInt("Capacidad"),
                            resultSet.getBigDecimal("PrecioPorNoche")
                    ));
                }
                return rooms;
            }
        } catch (SQLException e) {
            throw new DataAccessResourceFailureException("No se pudieron consultar las habitaciones.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
