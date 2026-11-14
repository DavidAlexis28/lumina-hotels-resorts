package com.lumina.backend.controllers;

import com.lumina.backend.dto.ReservaRequestDTO;
import com.lumina.backend.services.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<String> crearReserva(@RequestBody ReservaRequestDTO dto) {
        try {
            reservaService.crearReserva(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Reserva creada con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la reserva.");
        }
    }
}
