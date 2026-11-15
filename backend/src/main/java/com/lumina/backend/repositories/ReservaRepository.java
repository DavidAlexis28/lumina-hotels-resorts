package com.lumina.backend.repositories;

import com.lumina.backend.models.Reserva;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservaRepository {
    private static final String RESERVATION_SELECT = """
            SELECT ReservaID, ClienteID, HabitacionID, UsuarioID, FechaEntrada, FechaSalida,
                   CantidadHuespedes, Estado, PrecioPorNoche, Observaciones
            FROM dbo.Reserva
            """;

    private final DataSource dataSource;

    public ReservaRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean existeConflicto(int habitacionID, LocalDate fechaInicio, LocalDate fechaFin,
                                   Integer reservaExcluidaID) {
        String sql = """
                SELECT COUNT(1)
                FROM dbo.Reserva WITH (UPDLOCK, HOLDLOCK)
                WHERE HabitacionID = ?
                  AND COALESCE(UPPER(LTRIM(RTRIM(Estado))), '') NOT IN ('CANCELADA', 'CANCELADO')
                  AND FechaEntrada < ? AND FechaSalida > ?
                """ + (reservaExcluidaID == null ? "" : " AND ReservaID <> ?");
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, habitacionID);
            statement.setDate(2, java.sql.Date.valueOf(fechaFin));
            statement.setDate(3, java.sql.Date.valueOf(fechaInicio));
            if (reservaExcluidaID != null) {
                statement.setInt(4, reservaExcluidaID);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessResourceFailureException("No se pudo verificar la disponibilidad.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public Reserva save(Reserva reserva) {
        String sql = """
                INSERT INTO dbo.Reserva
                    (ClienteID, HabitacionID, UsuarioID, FechaEntrada, FechaSalida,
                     CantidadHuespedes, Estado, PrecioPorNoche, Observaciones)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindReservation(statement, reserva);
            if (statement.executeUpdate() != 1) {
                throw new DataAccessResourceFailureException("No se pudo crear la reserva.");
            }
            int reservaID;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessResourceFailureException("La base de datos no devolvió el ID de la reserva.");
                }
                reservaID = keys.getInt(1);
            }
            return findById(reservaID)
                    .orElseThrow(() -> new DataAccessResourceFailureException(
                            "La reserva creada no pudo recuperarse."));
        } catch (SQLException e) {
            throw new DataAccessResourceFailureException("No se pudo crear la reserva.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public Optional<Reserva> findById(int reservaID) {
        return queryReservations(RESERVATION_SELECT + " WHERE ReservaID = ?",
                statement -> statement.setInt(1, reservaID)).stream().findFirst();
    }

    public List<Reserva> findByClienteId(int clienteID) {
        return queryReservations(RESERVATION_SELECT + " WHERE ClienteID = ? ORDER BY FechaEntrada DESC",
                statement -> statement.setInt(1, clienteID));
    }

    public boolean update(int reservaID, Reserva reserva) {
        String sql = """
                UPDATE dbo.Reserva
                SET ClienteID = ?, HabitacionID = ?, UsuarioID = ?, FechaEntrada = ?, FechaSalida = ?,
                    CantidadHuespedes = ?, Estado = ?, PrecioPorNoche = ?, Observaciones = ?
                WHERE ReservaID = ?
                """;
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindReservation(statement, reserva);
            statement.setInt(10, reservaID);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessResourceFailureException("No se pudo modificar la reserva.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public boolean cancel(int reservaID, int habitacionID) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement cancel = connection.prepareStatement(
                "UPDATE dbo.Reserva SET Estado = 'CANCELADA' WHERE ReservaID = ? " +
                        "AND UPPER(LTRIM(RTRIM(Estado))) <> 'CANCELADA'")) {
            cancel.setInt(1, reservaID);
            if (cancel.executeUpdate() != 1) {
                return false;
            }
            try (PreparedStatement release = connection.prepareStatement(
                    "UPDATE dbo.Habitacion SET Estado = 'DISPONIBLE' " +
                            "WHERE HabitacionID = ? AND UPPER(LTRIM(RTRIM(Estado))) = 'OCUPADA'")) {
                release.setInt(1, habitacionID);
                release.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            throw new DataAccessResourceFailureException("No se pudo cancelar la reserva.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private List<Reserva> queryReservations(String sql, StatementBinder binder) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Reserva> reservations = new ArrayList<>();
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
                return reservations;
            }
        } catch (SQLException e) {
            throw new DataAccessResourceFailureException("No se pudieron consultar las reservas.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private Reserva mapReservation(ResultSet resultSet) throws SQLException {
        LocalDate start = resultSet.getDate("FechaEntrada").toLocalDate();
        LocalDate end = resultSet.getDate("FechaSalida").toLocalDate();
        Reserva reserva = new Reserva();
        reserva.setReservaID(resultSet.getInt("ReservaID"));
        reserva.setClienteID(resultSet.getInt("ClienteID"));
        reserva.setHabitacionID(resultSet.getInt("HabitacionID"));
        reserva.setUsuarioID(resultSet.getInt("UsuarioID"));
        reserva.setFechaEntrada(start);
        reserva.setFechaSalida(end);
        reserva.setCantidadHuespedes(resultSet.getInt("CantidadHuespedes"));
        reserva.setEstado(resultSet.getString("Estado"));
        reserva.setPrecioPorNoche(resultSet.getBigDecimal("PrecioPorNoche"));
        reserva.setCostoTotal(reserva.getPrecioPorNoche().multiply(
                java.math.BigDecimal.valueOf(ChronoUnit.DAYS.between(start, end))));
        reserva.setObservaciones(resultSet.getString("Observaciones"));
        return reserva;
    }

    private void bindReservation(PreparedStatement statement, Reserva reserva) throws SQLException {
        statement.setInt(1, reserva.getClienteID());
        statement.setInt(2, reserva.getHabitacionID());
        statement.setInt(3, reserva.getUsuarioID());
        statement.setDate(4, java.sql.Date.valueOf(reserva.getFechaEntrada()));
        statement.setDate(5, java.sql.Date.valueOf(reserva.getFechaSalida()));
        statement.setInt(6, reserva.getCantidadHuespedes());
        statement.setString(7, reserva.getEstado());
        statement.setBigDecimal(8, reserva.getPrecioPorNoche());
        statement.setString(9, reserva.getObservaciones());
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
