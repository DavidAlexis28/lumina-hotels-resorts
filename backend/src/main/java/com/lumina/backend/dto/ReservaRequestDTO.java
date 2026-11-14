package com.lumina.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaRequestDTO {
    private Integer clienteID;
    private Integer habitacionID;
    private Integer usuarioID;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private Integer cantidadHuespedes;
    private BigDecimal precioPorNoche;
    private String observaciones;
}
