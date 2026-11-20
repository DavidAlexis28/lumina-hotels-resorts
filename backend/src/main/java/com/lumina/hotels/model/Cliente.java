package com.lumina.hotels.model;

public class Cliente {
    private int clienteId;
    private String nombres;
    private String apellidos;
    private String documentoIdentidad;
    private String correo;
    private String telefono;
    private boolean activo;
    private String genero;
    private String fechaNacimiento;
    private String pais;
    private String direccion;
    private String numeroPasaporte;
    private String paisEmisorPasaporte;
    private String fechaVencimientoPasaporte;

    public Cliente() {
        this.activo = true;
    }

    public Cliente(int clienteId, String nombres, String apellidos, String documentoIdentidad, String correo, String telefono) {
        this.clienteId = clienteId;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.documentoIdentidad = documentoIdentidad;
        this.correo = correo;
        this.telefono = telefono;
        this.activo = true;
    }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getNumeroPasaporte() { return numeroPasaporte; }
    public void setNumeroPasaporte(String numeroPasaporte) { this.numeroPasaporte = numeroPasaporte; }
    public String getPaisEmisorPasaporte() { return paisEmisorPasaporte; }
    public void setPaisEmisorPasaporte(String paisEmisorPasaporte) { this.paisEmisorPasaporte = paisEmisorPasaporte; }
    public String getFechaVencimientoPasaporte() { return fechaVencimientoPasaporte; }
    public void setFechaVencimientoPasaporte(String fechaVencimientoPasaporte) { this.fechaVencimientoPasaporte = fechaVencimientoPasaporte; }
}
