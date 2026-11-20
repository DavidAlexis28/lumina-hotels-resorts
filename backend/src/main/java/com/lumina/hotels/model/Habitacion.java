package com.lumina.hotels.model;

import java.math.BigDecimal;

public class Habitacion {
    private int habitacionId;
    private String numero;
    private int piso;
    private String estado;
    private int tipoHabitacionId;
    private String nombreTipo;
    private int capacidad;
    private BigDecimal precioPorNoche;

    public Habitacion() {}

    public Habitacion(int habitacionId, String numero, int piso, String estado,
                      int tipoHabitacionId, String nombreTipo, int capacidad, BigDecimal precioPorNoche) {
        this.habitacionId = habitacionId;
        this.numero = numero;
        this.piso = piso;
        this.estado = estado;
        this.tipoHabitacionId = tipoHabitacionId;
        this.nombreTipo = nombreTipo;
        this.capacidad = capacidad;
        this.precioPorNoche = precioPorNoche;
    }

    public int getHabitacionId() { return habitacionId; }
    public void setHabitacionId(int habitacionId) { this.habitacionId = habitacionId; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public int getPiso() { return piso; }
    public void setPiso(int piso) { this.piso = piso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getTipoHabitacionId() { return tipoHabitacionId; }
    public void setTipoHabitacionId(int tipoHabitacionId) { this.tipoHabitacionId = tipoHabitacionId; }
    public String getNombreTipo() { return nombreTipo; }
    public void setNombreTipo(String nombreTipo) { this.nombreTipo = nombreTipo; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }
}
