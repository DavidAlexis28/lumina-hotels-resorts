package com.lumina.hotels.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reserva {
    private int reservaId;
    private int clienteId;
    private int habitacionId;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private int huespedes;
    private String estado;
    private BigDecimal totalEstimado;

    public Reserva() {}

    public Reserva(int reservaId, int clienteId, int habitacionId,
                   LocalDate fechaEntrada, LocalDate fechaSalida,
                   int huespedes, String estado, BigDecimal totalEstimado) {
        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.habitacionId = habitacionId;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.huespedes = huespedes;
        this.estado = estado;
        this.totalEstimado = totalEstimado;
    }

    public long calcularNoches() {
        if (fechaEntrada == null || fechaSalida == null) return 0;
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    public boolean esRangoFechasValido() {
        return fechaEntrada != null && fechaSalida != null && fechaSalida.isAfter(fechaEntrada);
    }

    public int getReservaId() { return reservaId; }
    public void setReservaId(int reservaId) { this.reservaId = reservaId; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public int getHabitacionId() { return habitacionId; }
    public void setHabitacionId(int habitacionId) { this.habitacionId = habitacionId; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
    public int getHuespedes() { return huespedes; }
    public void setHuespedes(int huespedes) { this.huespedes = huespedes; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getTotalEstimado() { return totalEstimado; }
    public void setTotalEstimado(BigDecimal totalEstimado) { this.totalEstimado = totalEstimado; }
}
