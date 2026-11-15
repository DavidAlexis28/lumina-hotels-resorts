package com.lumina.backend.services;

import com.lumina.backend.dto.ReservaRequestDTO;
import com.lumina.backend.exceptions.ReservationConflictException;
import com.lumina.backend.exceptions.ResourceNotFoundException;
import com.lumina.backend.models.Habitacion;
import com.lumina.backend.models.Reserva;
import com.lumina.backend.repositories.HabitacionRepository;
import com.lumina.backend.repositories.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;

    public ReservaService(ReservaRepository reservaRepository, HabitacionRepository habitacionRepository) {
        this.reservaRepository = reservaRepository;
        this.habitacionRepository = habitacionRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reserva crearReserva(ReservaRequestDTO dto) {
        validarSolicitud(dto);
        Habitacion habitacion = obtenerHabitacion(dto.getHabitacionID());
        validarHabitacion(habitacion, dto.getCantidadHuespedes());
        validarSinConflicto(dto, null);
        return reservaRepository.save(mapear(dto, habitacion.getPrecioPorNoche(), "PENDIENTE"));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reserva modificarReserva(int reservaID, ReservaRequestDTO dto) {
        validarSolicitud(dto);
        Reserva existente = obtenerReserva(reservaID);
        Habitacion habitacion = obtenerHabitacion(dto.getHabitacionID());
        validarHabitacion(habitacion, dto.getCantidadHuespedes());
        validarSinConflicto(dto, reservaID);
        Reserva modificada = mapear(dto, habitacion.getPrecioPorNoche(), existente.getEstado());
        if (!reservaRepository.update(reservaID, modificada)) {
            throw new ResourceNotFoundException("No existe la reserva " + reservaID + ".");
        }
        return reservaRepository.findById(reservaID)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la reserva " + reservaID + "."));
    }

    @Transactional(readOnly = true)
    public Reserva buscarPorId(int reservaID) {
        return obtenerReserva(reservaID);
    }

    @Transactional(readOnly = true)
    public List<Reserva> buscarPorCliente(int clienteID) {
        if (clienteID < 1) {
            throw new IllegalArgumentException("El ID del huésped debe ser mayor que cero.");
        }
        return reservaRepository.findByClienteId(clienteID);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void cancelarReserva(int reservaID) {
        Reserva reserva = obtenerReserva(reservaID);
        if ("CANCELADA".equalsIgnoreCase(reserva.getEstado())) {
            throw new ReservationConflictException("La reserva ya está cancelada.");
        }
        if (!reservaRepository.cancel(reservaID, reserva.getHabitacionID())) {
            throw new ReservationConflictException("No se pudo cancelar la reserva.");
        }
    }

    private void validarSolicitud(ReservaRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        }
        if (dto.getClienteID() == null || dto.getClienteID() < 1
                || dto.getHabitacionID() == null || dto.getHabitacionID() < 1
                || dto.getUsuarioID() == null || dto.getUsuarioID() < 1
                || dto.getCantidadHuespedes() == null || dto.getCantidadHuespedes() < 1) {
            throw new IllegalArgumentException("Huésped, habitación, usuario y huéspedes deben ser válidos.");
        }
        if (dto.getFechaEntrada() == null || dto.getFechaSalida() == null
                || !dto.getFechaSalida().isAfter(dto.getFechaEntrada())) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
    }

    private void validarHabitacion(Habitacion habitacion, int huespedes) {
        if (habitacion.getEstado() == null
                || !"DISPONIBLE".equalsIgnoreCase(habitacion.getEstado().trim())) {
            throw new ReservationConflictException("La habitación no está disponible.");
        }
        if (habitacion.getCapacidad() == null || huespedes > habitacion.getCapacidad()) {
            throw new IllegalArgumentException("La cantidad de huéspedes supera la capacidad de la habitación.");
        }
        if (habitacion.getPrecioPorNoche() == null || habitacion.getPrecioPorNoche().signum() < 0) {
            throw new IllegalStateException("La habitación no tiene una tarifa válida configurada.");
        }
    }

    private void validarSinConflicto(ReservaRequestDTO dto, Integer reservaExcluidaID) {
        if (reservaRepository.existeConflicto(dto.getHabitacionID(), dto.getFechaEntrada(),
                dto.getFechaSalida(), reservaExcluidaID)) {
            throw new ReservationConflictException("La habitación ya está reservada en esas fechas.");
        }
    }

    private Habitacion obtenerHabitacion(int habitacionID) {
        return habitacionRepository.findById(habitacionID)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la habitación " + habitacionID + "."));
    }

    private Reserva obtenerReserva(int reservaID) {
        if (reservaID < 1) {
            throw new IllegalArgumentException("El ID de reserva debe ser mayor que cero.");
        }
        return reservaRepository.findById(reservaID)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la reserva " + reservaID + "."));
    }

    private Reserva mapear(ReservaRequestDTO dto, BigDecimal tarifa, String estado) {
        Reserva reserva = new Reserva();
        reserva.setClienteID(dto.getClienteID());
        reserva.setHabitacionID(dto.getHabitacionID());
        reserva.setUsuarioID(dto.getUsuarioID());
        reserva.setFechaEntrada(dto.getFechaEntrada());
        reserva.setFechaSalida(dto.getFechaSalida());
        reserva.setCantidadHuespedes(dto.getCantidadHuespedes());
        reserva.setEstado(estado.toUpperCase(Locale.ROOT));
        reserva.setPrecioPorNoche(tarifa);
        reserva.setCostoTotal(tarifa.multiply(BigDecimal.valueOf(
                ChronoUnit.DAYS.between(dto.getFechaEntrada(), dto.getFechaSalida()))));
        reserva.setObservaciones(dto.getObservaciones());
        return reserva;
    }
}
