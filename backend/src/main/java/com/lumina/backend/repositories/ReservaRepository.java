package com.lumina.backend.repositories;

import com.lumina.backend.models.Reserva;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;

@Repository
public class ReservaRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Reserva reserva) {
        String sql = "INSERT INTO dbo.Reserva " +
                "(ClienteID, HabitacionID, UsuarioID, FechaEntrada, FechaSalida, CantidadHuespedes, Estado, PrecioPorNoche, Observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                reserva.getClienteID(),
                reserva.getHabitacionID(),
                reserva.getUsuarioID(),
                Date.valueOf(reserva.getFechaEntrada()),
                Date.valueOf(reserva.getFechaSalida()),
                reserva.getCantidadHuespedes(),
                reserva.getEstado(),
                reserva.getPrecioPorNoche(),
                reserva.getObservaciones()
        );
    }
}