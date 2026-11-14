package com.lumina.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    private Integer reservaID;
    private String codigoReserva;
    private Integer clienteID;
    private Integer habitacionID;
    private Integer usuarioID;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Integer cantidadHuespedes;
    private String estado;
    private BigDecimal precioPorNoche;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}