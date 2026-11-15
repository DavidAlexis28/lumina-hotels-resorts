package com.lumina.backend.models;

import java.math.BigDecimal;

public class Habitacion {
    private Integer habitacionID;
    private String numero;
    private Integer tipoHabitacionID;
    private String nombreTipo;
    private Integer piso;
    private String estado;
    private Integer capacidad;
    private BigDecimal precioPorNoche;

    public Habitacion() {
    }

    public Habitacion(Integer habitacionID, String numero, Integer tipoHabitacionID, String nombreTipo,
                      Integer piso, String estado, Integer capacidad, BigDecimal precioPorNoche) {
        this.habitacionID = habitacionID;
        this.numero = numero;
        this.tipoHabitacionID = tipoHabitacionID;
        this.nombreTipo = nombreTipo;
        this.piso = piso;
        this.estado = estado;
        this.capacidad = capacidad;
        this.precioPorNoche = precioPorNoche;
    }

    public Integer getHabitacionID() { return habitacionID; }
    public void setHabitacionID(Integer habitacionID) { this.habitacionID = habitacionID; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public Integer getTipoHabitacionID() { return tipoHabitacionID; }
    public void setTipoHabitacionID(Integer tipoHabitacionID) { this.tipoHabitacionID = tipoHabitacionID; }
    public String getNombreTipo() { return nombreTipo; }
    public void setNombreTipo(String nombreTipo) { this.nombreTipo = nombreTipo; }
    public Integer getPiso() { return piso; }
    public void setPiso(Integer piso) { this.piso = piso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }
}
