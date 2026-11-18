package com.lumina.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {
    private Integer habitacionID;
    private String numero;
    private Integer tipoHabitacionID;
    private String nombreTipo;
    private Integer piso;
    private String estado;
    private Integer capacidad;
    private BigDecimal precioPorNoche;
}
