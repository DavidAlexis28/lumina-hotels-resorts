package com.lumina.backend.controllers;

import com.lumina.backend.dto.ReservaRequestDTO;
import com.lumina.backend.models.Reserva;
import com.lumina.backend.services.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<?> crearReserva(@RequestBody ReservaRequestDTO solicitud) {
        Reserva creada = reservaService.crearReserva(solicitud);
        return ResponseEntity.created(URI.create("/api/reservas/" + creada.getReservaID())).body(creada);
    }

    @GetMapping("/{reservaID}")
    public ResponseEntity<?> buscarPorId(@PathVariable int reservaID) {
        return ResponseEntity.ok(reservaService.buscarPorId(reservaID));
    }

    @GetMapping("/cliente/{clienteID}")
    public ResponseEntity<?> buscarPorCliente(@PathVariable int clienteID) {
        return ResponseEntity.ok(reservaService.buscarPorCliente(clienteID));
    }

    @PutMapping("/{reservaID}")
    public ResponseEntity<?> modificarReserva(@PathVariable int reservaID,
                                               @RequestBody ReservaRequestDTO solicitud) {
        return ResponseEntity.ok(reservaService.modificarReserva(reservaID, solicitud));
    }

    @DeleteMapping("/{reservaID}")
    public ResponseEntity<?> cancelarReserva(@PathVariable int reservaID) {
        reservaService.cancelarReserva(reservaID);
        return ResponseEntity.noContent().build();
    }
}
