package com.lumina.backend.services;

import com.lumina.backend.dto.ReservaRequestDTO;
import com.lumina.backend.models.Reserva;
import com.lumina.backend.repositories.ReservaRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public void crearReserva(ReservaRequestDTO dto) {
        if (dto.getFechaSalida().isBefore(dto.getFechaEntrada()) || dto.getFechaSalida().isEqual(dto.getFechaEntrada())) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de entrada.");
        }

        Reserva reserva = new Reserva();
        reserva.setClienteID(dto.getClienteID());
        reserva.setHabitacionID(dto.getHabitacionID());
        reserva.setUsuarioID(dto.getUsuarioID());
        reserva.setFechaEntrada(dto.getFechaEntrada());
        reserva.setFechaSalida(dto.getFechaSalida());
        reserva.setCantidadHuespedes(dto.getCantidadHuespedes());
        reserva.setEstado("PENDIENTE");
        reserva.setPrecioPorNoche(dto.getPrecioPorNoche());
        reserva.setObservaciones(dto.getObservaciones());

        reservaRepository.save(reserva);
    }
}