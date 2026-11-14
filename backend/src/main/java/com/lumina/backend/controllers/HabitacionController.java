package com.lumina.backend.controllers;

import com.lumina.backend.models.Habitacion;
import com.lumina.backend.repositories.HabitacionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionController {

    private final HabitacionRepository habitacionRepository;

    public HabitacionController(HabitacionRepository habitacionRepository) {
        this.habitacionRepository = habitacionRepository;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Habitacion>> getDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam(defaultValue = "1") int huespedes) {

        List<Habitacion> disponibles = habitacionRepository.findDisponibles(fechaEntrada, fechaSalida, huespedes);
        return ResponseEntity.ok(disponibles);
    }
}
