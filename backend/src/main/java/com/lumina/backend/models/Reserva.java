package com.lumina.backend.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public Reserva() {
    }

    public Reserva(Integer reservaID, String codigoReserva, Integer clienteID, Integer habitacionID,
                   Integer usuarioID, LocalDate fechaEntrada, LocalDate fechaSalida, Integer cantidadHuespedes,
                   String estado, BigDecimal precioPorNoche, BigDecimal costoTotal, String observaciones,
                   LocalDateTime fechaCreacion) {
        this.reservaID = reservaID;
        this.codigoReserva = codigoReserva;
        this.clienteID = clienteID;
        this.habitacionID = habitacionID;
        this.usuarioID = usuarioID;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.cantidadHuespedes = cantidadHuespedes;
        this.estado = estado;
        this.precioPorNoche = precioPorNoche;
        this.costoTotal = costoTotal;
        this.observaciones = observaciones;
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getReservaID() { return reservaID; }
    public void setReservaID(Integer reservaID) { this.reservaID = reservaID; }
    public String getCodigoReserva() { return codigoReserva; }
    public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }
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
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
