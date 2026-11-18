package com.lumina.backend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaRequestDTO {
    private Integer clienteID;
    private Integer habitacionID;
    private Integer usuarioID;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEntrada;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaSalida;
    private Integer cantidadHuespedes;
    private BigDecimal precioPorNoche;
    private String observaciones;

    public ReservaRequestDTO() {
    }

    public ReservaRequestDTO(Integer clienteID, Integer habitacionID, Integer usuarioID, LocalDate fechaEntrada,
                             LocalDate fechaSalida, Integer cantidadHuespedes, String observaciones) {
        this.clienteID = clienteID;
        this.habitacionID = habitacionID;
        this.usuarioID = usuarioID;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.cantidadHuespedes = cantidadHuespedes;
        this.observaciones = observaciones;
    }

    public Integer getClienteID() { return clienteID; }
    public void setClienteID(Integer clienteID) { this.clienteID = clienteID; }
    public Integer getHabitacionID() { return habitacionID; }
    public void setHabitacionID(Integer habitacionID) { this.habitacionID = habitacionID; }
    public Integer getUsuarioID() { return usuarioID; }
    public void setUsuarioID(Integer usuarioID) { this.usuarioID = usuarioID; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
    public Integer getCantidadHuespedes() { return cantidadHuespedes; }
    public void setCantidadHuespedes(Integer cantidadHuespedes) { this.cantidadHuespedes = cantidadHuespedes; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
