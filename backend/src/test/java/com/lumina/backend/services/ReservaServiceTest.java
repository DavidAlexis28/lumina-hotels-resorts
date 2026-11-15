package com.lumina.backend.services;

import com.lumina.backend.dto.ReservaRequestDTO;
import com.lumina.backend.exceptions.ReservationConflictException;
import com.lumina.backend.models.Habitacion;
import com.lumina.backend.models.Reserva;
import com.lumina.backend.repositories.HabitacionRepository;
import com.lumina.backend.repositories.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private HabitacionRepository habitacionRepository;
    @InjectMocks
    private ReservaService reservaService;

    @Test
    void creaReservaCalculandoElCostoConLaTarifaDeLaHabitacion() {
        LocalDate entrada = LocalDate.of(2026, 12, 1);
        LocalDate salida = LocalDate.of(2026, 12, 4);
        Habitacion habitacion = new Habitacion(5, "105", 2, "Doble", 1,
                "DISPONIBLE", 2, new BigDecimal("125.50"));
        when(habitacionRepository.findById(5)).thenReturn(java.util.Optional.of(habitacion));
        when(reservaRepository.existeConflicto(5, entrada, salida, null)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva creada = reservaService.crearReserva(new ReservaRequestDTO(
                7, 5, 3, entrada, salida, 2, "Prueba"
        ));

        assertEquals(new BigDecimal("125.50"), creada.getPrecioPorNoche());
        assertEquals(new BigDecimal("376.50"), creada.getCostoTotal());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void rechazaFechasInvalidasAntesDeConsultarRepositorios() {
        ReservaRequestDTO solicitud = new ReservaRequestDTO(
                7, 5, 3, LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 4), 2, null
        );

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(solicitud));
        verifyNoInteractions(habitacionRepository, reservaRepository);
    }

    @Test
    void rechazaHabitacionConReservaSuperpuesta() {
        LocalDate entrada = LocalDate.of(2026, 12, 1);
        LocalDate salida = LocalDate.of(2026, 12, 4);
        when(habitacionRepository.findById(5)).thenReturn(java.util.Optional.of(
                new Habitacion(5, "105", 2, "Doble", 1, "DISPONIBLE", 2, new BigDecimal("125.50"))));
        when(reservaRepository.existeConflicto(5, entrada, salida, null)).thenReturn(true);

        assertThrows(ReservationConflictException.class, () -> reservaService.crearReserva(
                new ReservaRequestDTO(7, 5, 3, entrada, salida, 2, null)));
        verify(reservaRepository).existeConflicto(5, entrada, salida, null);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }
}
