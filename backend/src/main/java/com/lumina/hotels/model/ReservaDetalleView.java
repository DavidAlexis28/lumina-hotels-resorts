package com.lumina.hotels.model;

import java.math.BigDecimal;

public class ReservaDetalleView {
    private int reservaId;
    private int clienteId;
    private String nombreCompletoCliente;
    private String documentoIdentidad;
    private String correoCliente;
    private int habitacionId;
    private String numeroHabitacion;
    private int piso;
    private String tipoHabitacion;
    private BigDecimal precioPorNoche;
    private String fechaEntrada;
    private String fechaSalida;
    private int cantidadNoches;
    private int huespedes;
    private String estadoReserva;
    private BigDecimal totalEstimadoCobro;

    public int getReservaId() { return reservaId; }
    public void setReservaId(int reservaId) { this.reservaId = reservaId; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getNombreCompletoCliente() { return nombreCompletoCliente; }
    public void setNombreCompletoCliente(String nombreCompletoCliente) { this.nombreCompletoCliente = nombreCompletoCliente; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }
    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }
    public int getHabitacionId() { return habitacionId; }
    public void setHabitacionId(int habitacionId) { this.habitacionId = habitacionId; }
    public String getNumeroHabitacion() { return numeroHabitacion; }
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }
    public int getPiso() { return piso; }
    public void setPiso(int piso) { this.piso = piso; }
    public String getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }
    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }
    public String getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(String fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public String getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(String fechaSalida) { this.fechaSalida = fechaSalida; }
    public int getCantidadNoches() { return cantidadNoches; }
    public void setCantidadNoches(int cantidadNoches) { this.cantidadNoches = cantidadNoches; }
    public int getHuespedes() { return huespedes; }
    public void setHuespedes(int huespedes) { this.huespedes = huespedes; }
    public String getEstadoReserva() { return estadoReserva; }
    public void setEstadoReserva(String estadoReserva) { this.estadoReserva = estadoReserva; }
    public BigDecimal getTotalEstimadoCobro() { return totalEstimadoCobro; }
    public void setTotalEstimadoCobro(BigDecimal totalEstimadoCobro) { this.totalEstimadoCobro = totalEstimadoCobro; }
}
