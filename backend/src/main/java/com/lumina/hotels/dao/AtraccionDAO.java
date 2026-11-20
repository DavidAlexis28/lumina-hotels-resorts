package com.lumina.hotels.dao;

import com.lumina.hotels.config.ConexionDB;
import com.lumina.hotels.model.AtraccionEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AtraccionDAO {

    private static final List<AtraccionEntity> cacheAtracciones = new CopyOnWriteArrayList<>();

    static {
        // Inicializar atracciones semilla en memoria con microbús pequeño de 14 spots por defecto
        AtraccionEntity a1 = new AtraccionEntity(
                "attr-1", "Ascenso Guiado al Cráter del Volcán de Santa Ana (Ilamatepec)", "Santa Ana",
                "Naturaleza & Aventura", 4.9, 520, "5 horas", 55.0,
                "Camina hasta el borde del cráter activo más alto de El Salvador y contempla su impresionante laguna sulfurosa color turquesa con vistas al Lago de Coatepeque.",
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1000&q=80",
                Arrays.asList("Guía bilingüe certificado", "Equipo de bastones de trekking", "Almuerzo tipo picnic en la cumbre"),
                Arrays.asList("Entrada al Parque Nacional Los Volcanes", "Transporte: Microbús pequeño", "Botella de agua térmica"),
                14, 10
        );

        AtraccionEntity a2 = new AtraccionEntity(
                "attr-2", "Tour Histórico Colonial & Ruta del Café en Suchitoto", "Suchitoto",
                "Cultural & Tours", 4.8, 380, "4 horas", 45.0,
                "Recorre las calles empedradas de la capital cultural de El Salvador, visita talleres de añil orgánico y disfruta de un paseo en lancha en el Lago Suchitlán.",
                "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=1000&q=80",
                Arrays.asList("Taller interactivo de teñido con añil", "Paseo en lancha privada", "Degustación de café gourmet"),
                Arrays.asList("Transporte: Microbús pequeño", "Guía historiador", "Refrigerio artesanal"),
                14, 12
        );

        AtraccionEntity a3 = new AtraccionEntity(
                "attr-3", "Paseo Exclusivo en Catamarán al Atardecer por Bahía de Jiquilisco", "Usulután",
                "Playas & Deportes", 5.0, 210, "3.5 horas", 75.0,
                "Navega entre manglares protegidos por la UNESCO, observa tortugas marinas carey en su hábitat y degusta coctelería marina y vino espumoso al caer el sol.",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=1000&q=80",
                Arrays.asList("Barra libre de ceviches y mariscos", "Avistamiento de aves migratorias", "Música acústica en vivo a bordo"),
                Arrays.asList("Transporte: Microbús pequeño", "Bebidas premium", "Toallas y protector solar"),
                14, 8
        );

        AtraccionEntity a4 = new AtraccionEntity(
                "attr-4", "Experiencia Gastronómica de Degustación en Finca de Montaña", "Juayúa",
                "Gastronomía", 4.7, 430, "3 horas", 40.0,
                "Sumérgete en la famosa gastronomía de la Ruta de Las Flores con un menú degustación de 5 tiempos elaborado con ingredientes orgánicos de la cordillera.",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1000&q=80",
                Arrays.asList("Menú maridaje con vinos latinoamericanos", "Tour por el huerto biodinámico", "Postre de café cosechado en el sitio"),
                Arrays.asList("Transporte: Microbús pequeño", "Cena completa de 5 tiempos", "Souvenir de café de altura"),
                14, 14
        );

        cacheAtracciones.add(a1);
        cacheAtracciones.add(a2);
        cacheAtracciones.add(a3);
        cacheAtracciones.add(a4);
    }

    public List<AtraccionEntity> listarTodas() {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sql = "SELECT AtraccionID, Titulo, Ciudad, Categoria, Puntuacion, ResenasCount, Duracion, PrecioPorPersona, Descripcion, ImagenUrl, CapacidadMicrobus, LugaresDisponibles FROM dbo.Atraccion";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<AtraccionEntity> listFromDb = new ArrayList<>();
                while (rs.next()) {
                    AtraccionEntity a = new AtraccionEntity();
                    a.setId(rs.getString("AtraccionID"));
                    a.setTitle(rs.getString("Titulo"));
                    a.setCity(rs.getString("Ciudad"));
                    a.setCategory(rs.getString("Categoria"));
                    a.setRating(rs.getDouble("Puntuacion"));
                    a.setReviewCount(rs.getInt("ResenasCount"));
                    a.setDuration(rs.getString("Duracion"));
                    a.setPricePerPerson(rs.getDouble("PrecioPorPersona"));
                    a.setDescription(rs.getString("Descripcion"));
                    a.setImageUrl(rs.getString("ImagenUrl"));
                    a.setMinibusCapacity(rs.getInt("CapacidadMicrobus"));
                    a.setAvailableSpots(rs.getInt("LugaresDisponibles"));
                    a.setHighlights(Arrays.asList("Guía bilingüe certificado", "Transporte: Microbús pequeño", "Seguro de viaje"));
                    a.setInclusions(Arrays.asList("Transporte: Microbús pequeño (" + a.getMinibusCapacity() + " asientos)", "Entradas incluidas"));
                    listFromDb.add(a);
                }

                if (!listFromDb.isEmpty()) {
                    cacheAtracciones.clear();
                    cacheAtracciones.addAll(listFromDb);
                    return listFromDb;
                }
            }
        } catch (Exception e) {
            System.err.println("[AtraccionDAO] Aviso consulta DB: " + e.getMessage() + ". Utilizando inventario en memoria.");
        }
        return new ArrayList<>(cacheAtracciones);
    }

    public AtraccionEntity crearAtraccion(AtraccionEntity atraccion) {
        if (atraccion.getId() == null || atraccion.getId().isBlank()) {
            atraccion.setId("attr-" + (cacheAtracciones.size() + 1) + "-" + System.currentTimeMillis() % 1000);
        }
        if (atraccion.getMinibusCapacity() <= 0) {
            atraccion.setMinibusCapacity(14);
        }
        if (atraccion.getAvailableSpots() <= 0) {
            atraccion.setAvailableSpots(atraccion.getMinibusCapacity());
        }

        try (Connection conn = ConexionDB.obtenerConexion()) {
            String ins = "INSERT INTO dbo.Atraccion (AtraccionID, Titulo, Ciudad, Categoria, Puntuacion, ResenasCount, Duracion, PrecioPorPersona, Descripcion, ImagenUrl, CapacidadMicrobus, LugaresDisponibles) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                ps.setString(1, atraccion.getId());
                ps.setString(2, atraccion.getTitle());
                ps.setString(3, atraccion.getCity());
                ps.setString(4, atraccion.getCategory());
                ps.setDouble(5, atraccion.getRating() > 0 ? atraccion.getRating() : 4.9);
                ps.setInt(6, atraccion.getReviewCount() > 0 ? atraccion.getReviewCount() : 50);
                ps.setString(7, atraccion.getDuration());
                ps.setDouble(8, atraccion.getPricePerPerson());
                ps.setString(9, atraccion.getDescription());
                ps.setString(10, atraccion.getImageUrl());
                ps.setInt(11, atraccion.getMinibusCapacity());
                ps.setInt(12, atraccion.getAvailableSpots());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("[AtraccionDAO] Aviso al insertar en DB: " + e.getMessage() + ". Guardando en memoria.");
        }

        cacheAtracciones.add(0, atraccion);
        return atraccion;
    }

    public boolean decrementarCupos(String atraccionId, int tickets) {
        for (AtraccionEntity a : cacheAtracciones) {
            if (a.getId().equalsIgnoreCase(atraccionId)) {
                if (a.getAvailableSpots() >= tickets) {
                    a.setAvailableSpots(a.getAvailableSpots() - tickets);
                    sincronizarCuposDB(a.getId(), a.getAvailableSpots());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean incrementarCupos(String atraccionId, int tickets) {
        for (AtraccionEntity a : cacheAtracciones) {
            if (a.getId().equalsIgnoreCase(atraccionId)) {
                int nuevo = Math.min(a.getMinibusCapacity(), a.getAvailableSpots() + tickets);
                a.setAvailableSpots(nuevo);
                sincronizarCuposDB(a.getId(), a.getAvailableSpots());
                return true;
            }
        }
        return false;
    }

    private void sincronizarCuposDB(String atraccionId, int nuevosDisponibles) {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sql = "UPDATE dbo.Atraccion SET LugaresDisponibles = ? WHERE AtraccionID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nuevosDisponibles);
                ps.setString(2, atraccionId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    public boolean actualizarAtraccion(String atraccionId, AtraccionEntity atraccion) {
        for (int i = 0; i < cacheAtracciones.size(); i++) {
            AtraccionEntity a = cacheAtracciones.get(i);
            if (a.getId().equalsIgnoreCase(atraccionId)) {
                atraccion.setId(atraccionId);
                if (atraccion.getMinibusCapacity() <= 0) atraccion.setMinibusCapacity(a.getMinibusCapacity());
                if (atraccion.getAvailableSpots() < 0) atraccion.setAvailableSpots(a.getAvailableSpots());
                cacheAtracciones.set(i, atraccion);
                break;
            }
        }

        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sql = "UPDATE dbo.Atraccion SET Titulo = ?, Ciudad = ?, Categoria = ?, Puntuacion = ?, ResenasCount = ?, Duracion = ?, PrecioPorPersona = ?, Descripcion = ?, ImagenUrl = ?, CapacidadMicrobus = ?, LugaresDisponibles = ? WHERE AtraccionID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, atraccion.getTitle());
                ps.setString(2, atraccion.getCity());
                ps.setString(3, atraccion.getCategory());
                ps.setDouble(4, atraccion.getRating() > 0 ? atraccion.getRating() : 4.9);
                ps.setInt(5, atraccion.getReviewCount() > 0 ? atraccion.getReviewCount() : 50);
                ps.setString(6, atraccion.getDuration());
                ps.setDouble(7, atraccion.getPricePerPerson());
                ps.setString(8, atraccion.getDescription());
                ps.setString(9, atraccion.getImageUrl());
                ps.setInt(10, atraccion.getMinibusCapacity());
                ps.setInt(11, atraccion.getAvailableSpots());
                ps.setString(12, atraccionId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (Exception e) {
            System.err.println("[AtraccionDAO] Aviso al actualizar en DB: " + e.getMessage());
        }
        return true;
    }

    public boolean eliminarAtraccion(String atraccionId) {
        cacheAtracciones.removeIf(a -> a.getId().equalsIgnoreCase(atraccionId));

        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sql = "DELETE FROM dbo.Atraccion WHERE AtraccionID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, atraccionId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (Exception e) {
            System.err.println("[AtraccionDAO] Aviso al eliminar en DB: " + e.getMessage());
        }
        return true;
    }
}
