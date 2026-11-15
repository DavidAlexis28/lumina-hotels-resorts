package com.lumina.backend.dto;

public class EstadoHabitacionRequestDTO {
    private String estado;

    public EstadoHabitacionRequestDTO() {
    }

    public EstadoHabitacionRequestDTO(String estado) {
        this.estado = estado;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
