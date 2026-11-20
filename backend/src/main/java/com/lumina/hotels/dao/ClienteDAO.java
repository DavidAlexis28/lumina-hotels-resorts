package com.lumina.hotels.dao;

import com.lumina.hotels.config.ConexionDB;
import com.lumina.hotels.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ClienteDAO {

    // Caché en memoria sincronizada tolerante a fallos si la DB está offline
    private static final Map<String, Cliente> cacheClientes = new ConcurrentHashMap<>();

    static {
        Cliente def = new Cliente(1, "Carlos Alberto", "Mendoza Ruiz", "A12345678", "carlos.mendoza@ejemplo.com", "+503 7123-4567");
        def.setGenero("Prefiero no decirlo");
        def.setFechaNacimiento("15/06/1992");
        def.setPais("El Salvador");
        def.setDireccion("Calle La Mascota #14, Colonia San Benito, San Salvador");
        def.setNumeroPasaporte("A12345678");
        def.setPaisEmisorPasaporte("El Salvador");
        def.setFechaVencimientoPasaporte("20/12/2030");
        cacheClientes.put(def.getCorreo().toLowerCase(), def);
    }

    public Cliente obtenerPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return cacheClientes.values().stream().findFirst().orElse(null);
        }
        String cleanCorreo = correo.trim().toLowerCase();

        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sql = "SELECT ClienteID, Nombres, Apellidos, DocumentoIdentidad, Correo, Telefono, Activo, " +
                         "Genero, FechaNacimiento, Pais, Direccion, NumeroPasaporte, PaisEmisorPasaporte, FechaVencimientoPasaporte " +
                         "FROM dbo.Cliente WHERE LOWER(Correo) = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cleanCorreo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Cliente c = mapearCliente(rs);
                        cacheClientes.put(cleanCorreo, c);
                        return c;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ClienteDAO] Advertencia al consultar DB: " + e.getMessage() + ". Utilizando caché.");
        }

        return cacheClientes.getOrDefault(cleanCorreo, cacheClientes.values().stream().findFirst().orElse(null));
    }

    public Cliente guardarOActualizarPerfil(Cliente c) {
        if (c == null) return null;
        String cleanCorreo = c.getCorreo() != null ? c.getCorreo().trim().toLowerCase() : "carlos.mendoza@ejemplo.com";
        c.setCorreo(cleanCorreo);

        try (Connection conn = ConexionDB.obtenerConexion()) {
            // Verificar si el cliente existe por correo o documento
            String checkSql = "SELECT ClienteID FROM dbo.Cliente WHERE LOWER(Correo) = ? OR DocumentoIdentidad = ?";
            Integer id = null;
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, cleanCorreo);
                checkPs.setString(2, c.getDocumentoIdentidad() != null ? c.getDocumentoIdentidad() : "");
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getInt("ClienteID");
                    }
                }
            }

            if (id != null) {
                // Actualizar
                String updateSql = "UPDATE dbo.Cliente SET Nombres = ?, Apellidos = ?, Telefono = ?, " +
                                   "Genero = ?, FechaNacimiento = ?, Pais = ?, Direccion = ?, " +
                                   "NumeroPasaporte = ?, PaisEmisorPasaporte = ?, FechaVencimientoPasaporte = ?, " +
                                   "DocumentoIdentidad = COALESCE(?, DocumentoIdentidad) WHERE ClienteID = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, c.getNombres());
                    ps.setString(2, c.getApellidos() != null ? c.getApellidos() : "");
                    ps.setString(3, c.getTelefono());
                    ps.setString(4, c.getGenero());
                    ps.setString(5, c.getFechaNacimiento());
                    ps.setString(6, c.getPais());
                    ps.setString(7, c.getDireccion());
                    ps.setString(8, c.getNumeroPasaporte());
                    ps.setString(9, c.getPaisEmisorPasaporte());
                    ps.setString(10, c.getFechaVencimientoPasaporte());
                    ps.setString(11, c.getNumeroPasaporte());
                    ps.setInt(12, id);
                    ps.executeUpdate();
                    c.setClienteId(id);
                }
            } else {
                // Insertar nuevo
                String insertSql = "INSERT INTO dbo.Cliente (Nombres, Apellidos, DocumentoIdentidad, Correo, Telefono, " +
                                   "Genero, FechaNacimiento, Pais, Direccion, NumeroPasaporte, PaisEmisorPasaporte, FechaVencimientoPasaporte) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, c.getNombres());
                    ps.setString(2, c.getApellidos() != null ? c.getApellidos() : "");
                    ps.setString(3, c.getNumeroPasaporte() != null ? c.getNumeroPasaporte() : "DOC-" + System.currentTimeMillis());
                    ps.setString(4, cleanCorreo);
                    ps.setString(5, c.getTelefono());
                    ps.setString(6, c.getGenero());
                    ps.setString(7, c.getFechaNacimiento());
                    ps.setString(8, c.getPais());
                    ps.setString(9, c.getDireccion());
                    ps.setString(10, c.getNumeroPasaporte());
                    ps.setString(11, c.getPaisEmisorPasaporte());
                    ps.setString(12, c.getFechaVencimientoPasaporte());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) c.setClienteId(keys.getInt(1));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ClienteDAO] Advertencia al guardar en DB: " + e.getMessage() + ". Persistiendo en memoria.");
        }

        cacheClientes.put(cleanCorreo, c);
        return c;
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setClienteId(rs.getInt("ClienteID"));
        c.setNombres(rs.getString("Nombres"));
        c.setApellidos(rs.getString("Apellidos"));
        c.setDocumentoIdentidad(rs.getString("DocumentoIdentidad"));
        c.setCorreo(rs.getString("Correo"));
        c.setTelefono(rs.getString("Telefono"));
        c.setActivo(rs.getBoolean("Activo"));

        try { c.setGenero(rs.getString("Genero")); } catch (Exception ignored) {}
        try { c.setFechaNacimiento(rs.getString("FechaNacimiento")); } catch (Exception ignored) {}
        try { c.setPais(rs.getString("Pais")); } catch (Exception ignored) {}
        try { c.setDireccion(rs.getString("Direccion")); } catch (Exception ignored) {}
        try { c.setNumeroPasaporte(rs.getString("NumeroPasaporte")); } catch (Exception ignored) {}
        try { c.setPaisEmisorPasaporte(rs.getString("PaisEmisorPasaporte")); } catch (Exception ignored) {}
        try { c.setFechaVencimientoPasaporte(rs.getString("FechaVencimientoPasaporte")); } catch (Exception ignored) {}

        return c;
    }
}
