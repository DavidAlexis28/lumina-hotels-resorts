package com.lumina.backend.services;

import com.lumina.backend.exceptions.ResourceNotFoundException;
import com.lumina.backend.models.Habitacion;
import com.lumina.backend.repositories.HabitacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class HabitacionService {
    private final HabitacionRepository habitacionRepository;

    public HabitacionService(HabitacionRepository habitacionRepository) {
        this.habitacionRepository = habitacionRepository;
    }

    public List<Habitacion> listarTodas() {
        return habitacionRepository.findAll();
    }

    public List<Habitacion> listarDisponibles(LocalDate fechaInicio, LocalDate fechaFin,
                                               Integer tipoHabitacionID, Integer huespedes) {
        validarRangoFechas(fechaInicio, fechaFin);
        if (huespedes == null || huespedes < 1) {
            throw new IllegalArgumentException("La cantidad de huéspedes debe ser mayor que cero.");
        }
        if (tipoHabitacionID != null && tipoHabitacionID < 1) {
            throw new IllegalArgumentException("El tipo de habitación debe ser un identificador válido.");
        }
        return habitacionRepository.findDisponibles(fechaInicio, fechaFin, tipoHabitacionID, huespedes);
    }

    public Habitacion actualizarEstado(int habitacionID, String estado) {
        if (habitacionID < 1) {
            throw new IllegalArgumentException("El ID de habitación debe ser mayor que cero.");
        }
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        String normalized = estado.trim().toUpperCase(Locale.ROOT);
        if (!List.of("DISPONIBLE", "OCUPADA", "MANTENIMIENTO").contains(normalized)) {
            throw new IllegalArgumentException("El estado debe ser DISPONIBLE, OCUPADA o MANTENIMIENTO.");
        }
        if (!habitacionRepository.actualizarEstado(habitacionID, normalized)) {
            throw new ResourceNotFoundException("No existe la habitación " + habitacionID + ".");
        }
        return habitacionRepository.findById(habitacionID)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la habitación " + habitacionID + "."));
    }

    private void validarRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha final debe ser posterior a la fecha inicial.");
        }
    }
}
