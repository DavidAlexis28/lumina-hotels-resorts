package com.lumina.backend.controllers;

import com.lumina.backend.dto.EstadoHabitacionRequestDTO;
import com.lumina.backend.models.Habitacion;
import com.lumina.backend.services.HabitacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionController {
    private final HabitacionService habitacionService;

    public HabitacionController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @GetMapping
    public ResponseEntity<?> listarTodas() {
        return ResponseEntity.ok(habitacionService.listarTodas());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<?> listarDisponibles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam(required = false) Integer tipoHabitacionID,
            @RequestParam(defaultValue = "1") Integer huespedes) {
        LocalDate inicio = fechaInicio != null ? fechaInicio : fechaEntrada;
        LocalDate fin = fechaFin != null ? fechaFin : fechaSalida;
        List<Habitacion> disponibles = habitacionService.listarDisponibles(
                inicio, fin, tipoHabitacionID, huespedes);
        return ResponseEntity.ok(disponibles);
    }

    @PutMapping("/{habitacionID}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable int habitacionID,
                                               @RequestBody EstadoHabitacionRequestDTO solicitud) {
        String estado = solicitud == null ? null : solicitud.getEstado();
        return ResponseEntity.ok(habitacionService.actualizarEstado(habitacionID, estado));
    }
}
