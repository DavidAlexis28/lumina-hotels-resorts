package com.lumina.backend.repositories;

import com.lumina.backend.models.Habitacion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class HabitacionRepository {

    private final JdbcTemplate jdbcTemplate;

    public HabitacionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Habitacion> habitacionRowMapper = (rs, rowNum) -> new Habitacion(
            rs.getInt("HabitacionID"),
            rs.getString("Numero"),
            rs.getInt("TipoHabitacionID"),
            rs.getString("Nombre"),
            rs.getInt("Piso"),
            rs.getString("Estado"),
            rs.getInt("Capacidad"),
            rs.getBigDecimal("PrecioPorNoche")
    );

    public List<Habitacion> findDisponibles(LocalDate fechaEntrada, LocalDate fechaSalida, int huespedes) {
        String sql = "EXEC dbo.sp_HabitacionesDisponibles @FechaEntrada = ?, @FechaSalida = ?, @Huespedes = ?";
        return jdbcTemplate.query(sql, habitacionRowMapper,
                Date.valueOf(fechaEntrada),
                Date.valueOf(fechaSalida),
                huespedes);
    }
}
