package com.lumina.backend.models;


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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEntrada;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaSalida;
    private Integer cantidadHuespedes;
    private String estado;
    private BigDecimal precioPorNoche;
    private BigDecimal costoTotal;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}