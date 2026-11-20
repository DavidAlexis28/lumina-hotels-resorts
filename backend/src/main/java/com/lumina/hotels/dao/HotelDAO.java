package com.lumina.hotels.dao;

import com.lumina.hotels.config.ConexionDB;
import com.lumina.hotels.model.HotelEntity;
import com.lumina.hotels.model.HotelRoomEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HotelDAO {

    private static final List<HotelEntity> cacheHoteles = new CopyOnWriteArrayList<>();

    static {
        // Inicializar hoteles semilla en memoria
        HotelEntity h1 = new HotelEntity(
                "hotel-1", "Lumina Grand Palace Resort & Spa", "San Salvador",
                "Av. Las Magnolias 102, Colonia San Benito", 5, 9.4, 1240, "Excepcional",
                "Un oasis de lujo en el corazón de la zona más exclusiva. Cuenta con spa de clase mundial, piscina infinita climatizada, jardines tropicales y tres restaurantes de alta cocina internacional.",
                Arrays.asList("Piscina Infinita", "Spa & Wellness", "Wi-Fi de Alta Velocidad", "Desayuno Buffet", "Gimnasio 24/7", "Valet Parking"),
                115.0, "Más Vendido",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
                new ArrayList<>()
        );
        h1.getRooms().add(new HotelRoomEntity(1, "hotel-1", "Habitación Deluxe Queen Superior", "1 cama Queen extragrande", 2, 38, 115.0,
                Arrays.asList("Vista a jardines", "Smart TV 55\"", "Cafetera Nespresso", "Bata de baño de lujo"),
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1000&q=80", 5, 5));
        h1.getRooms().add(new HotelRoomEntity(3, "hotel-1", "Suite Junior Ejecutiva con Balcón", "1 cama King + 1 sofá cama", 3, 54, 175.0,
                Arrays.asList("Balcón privado", "Área de estar", "Tina de hidromasaje", "Acceso al Executive Lounge"),
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1000&q=80", 4, 4));
        h1.getRooms().add(new HotelRoomEntity(5, "hotel-1", "Villa Presidencial Lumina", "2 camas King en dormitorios independientes", 4, 98, 310.0,
                Arrays.asList("Piscina privada tipo plunge", "Mayordomo dedicado", "Cava de vinos", "Terraza panorámica"),
                "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1000&q=80", 2, 2));

        HotelEntity h2 = new HotelEntity(
                "hotel-2", "Lumina Royal Ocean Beach Resort", "Costa del Sol",
                "Km 72 Bulevar Costa del Sol, La Paz", 5, 9.6, 980, "Excepcional",
                "Resort de lujo frente al mar pacífico con acceso exclusivo a playa de arena dorada, club de playa privado, paseos en catamarán y gastronomía marina contemporánea.",
                Arrays.asList("Acceso a Playa Privada", "3 Piscinas frente al mar", "Bar en la piscina", "Deportes acuáticos", "Restaurante de Mariscos", "Spa Oceanview"),
                185.0, "Frente a la Playa",
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
                new ArrayList<>()
        );
        h2.getRooms().add(new HotelRoomEntity(201, "hotel-2", "Oceanfront Deluxe King", "1 cama King extragrande con dosel", 2, 45, 185.0,
                Arrays.asList("Vista panorámica al mar", "Terraza con hamaca", "Minibar premium", "Ducha exterior estilo Bali"),
                "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1000&q=80", 6, 6));

        HotelEntity h3 = new HotelEntity(
                "hotel-3", "Lumina Boutique & Coffee Estate", "Apaneca",
                "Ruta de Las Flores Km 88, Apaneca, Ahuachapán", 4, 9.2, 650, "Fantástico",
                "Exclusivo hotel boutique inmerso en una finca de café de altura con arquitectura colonial moderna, degustaciones privadas de café Geisha y clima fresco de montaña.",
                Arrays.asList("Cata de Café Especialidad", "Chimenea en Habitación", "Restaurante Farm-to-Table", "Senderismo Entre Cafetales", "Mirador Panorámico"),
                95.0, "Montaña",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
                new ArrayList<>()
        );
        h3.getRooms().add(new HotelRoomEntity(301, "hotel-3", "Cabaña Suite con Chimenea", "1 cama King rústica artesanal", 2, 42, 95.0,
                Arrays.asList("Chimenea de leña natural", "Balcón con vista a cafetales", "Cafetera Pour-Over manual", "Techos de madera noble"),
                "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1000&q=80", 4, 4));

        cacheHoteles.add(h1);
        cacheHoteles.add(h2);
        cacheHoteles.add(h3);
    }

    public List<HotelEntity> listarTodas() {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sqlHotels = "SELECT HotelID, Nombre, Ciudad, Direccion, Estrellas, Puntuacion, ResenasCount, ResenasLabel, Descripcion, Amenidades, PrecioBase, FeaturedTag, ImagenUrl FROM dbo.Hotel";
            try (PreparedStatement psH = conn.prepareStatement(sqlHotels);
                 ResultSet rsH = psH.executeQuery()) {
                List<HotelEntity> listFromDb = new ArrayList<>();
                while (rsH.next()) {
                    HotelEntity h = new HotelEntity();
                    h.setId(rsH.getString("HotelID"));
                    h.setName(rsH.getString("Nombre"));
                    h.setCity(rsH.getString("Ciudad"));
                    h.setAddress(rsH.getString("Direccion"));
                    h.setStarRating(rsH.getInt("Estrellas"));
                    h.setReviewScore(rsH.getDouble("Puntuacion"));
                    h.setReviewCount(rsH.getInt("ResenasCount"));
                    h.setReviewLabel(rsH.getString("ResenasLabel"));
                    h.setDescription(rsH.getString("Descripcion"));
                    String am = rsH.getString("Amenidades");
                    h.setAmenities(am != null && !am.isBlank() ? Arrays.asList(am.split(",")) : new ArrayList<>());
                    h.setPricePerNightStarting(rsH.getDouble("PrecioBase"));
                    h.setFeaturedTag(rsH.getString("FeaturedTag"));
                    h.setImageUrl(rsH.getString("ImagenUrl"));
                    listFromDb.add(h);
                }

                if (!listFromDb.isEmpty()) {
                    for (HotelEntity h : listFromDb) {
                        String sqlRooms = "SELECT RoomID, HotelID, Nombre, ConfiguracionCama, Capacidad, TamanoM2, PrecioPorNoche, Amenidades, ImagenUrl, CuartosDisponibles, CuartosTotales FROM dbo.HotelRoom WHERE HotelID = ?";
                        try (PreparedStatement psR = conn.prepareStatement(sqlRooms)) {
                            psR.setString(1, h.getId());
                            try (ResultSet rsR = psR.executeQuery()) {
                                while (rsR.next()) {
                                    HotelRoomEntity r = new HotelRoomEntity();
                                    r.setRoomId(rsR.getInt("RoomID"));
                                    r.setHotelId(rsR.getString("HotelID"));
                                    r.setName(rsR.getString("Nombre"));
                                    r.setBedConfiguration(rsR.getString("ConfiguracionCama"));
                                    r.setCapacity(rsR.getInt("Capacidad"));
                                    r.setSizeM2(rsR.getInt("TamanoM2"));
                                    r.setPricePerNight(rsR.getDouble("PrecioPorNoche"));
                                    String ram = rsR.getString("Amenidades");
                                    r.setAmenities(ram != null && !ram.isBlank() ? Arrays.asList(ram.split(",")) : new ArrayList<>());
                                    r.setImageUrl(rsR.getString("ImagenUrl"));
                                    r.setAvailableRooms(rsR.getInt("CuartosDisponibles"));
                                    r.setTotalRooms(rsR.getInt("CuartosTotales"));
                                    h.getRooms().add(r);
                                }
                            }
                        }
                    }
                    cacheHoteles.clear();
                    cacheHoteles.addAll(listFromDb);
                    return listFromDb;
                }
            }
        } catch (Exception e) {
            System.err.println("[HotelDAO] Aviso consulta DB: " + e.getMessage() + ". Utilizando inventario en memoria.");
        }
        return new ArrayList<>(cacheHoteles);
    }

    public HotelEntity crearHotelYHabitacion(HotelEntity hotel, HotelRoomEntity room) {
        if (hotel.getId() == null || hotel.getId().isBlank()) {
            hotel.setId("hotel-" + (cacheHoteles.size() + 1) + "-" + System.currentTimeMillis() % 1000);
        }
        room.setHotelId(hotel.getId());
        if (room.getRoomId() <= 0) {
            room.setRoomId(100 + (int)(System.currentTimeMillis() % 900));
        }
        hotel.getRooms().add(room);

        try (Connection conn = ConexionDB.obtenerConexion()) {
            String insHotel = "INSERT INTO dbo.Hotel (HotelID, Nombre, Ciudad, Direccion, Estrellas, Puntuacion, ResenasCount, ResenasLabel, Descripcion, Amenidades, PrecioBase, FeaturedTag, ImagenUrl) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psH = conn.prepareStatement(insHotel)) {
                psH.setString(1, hotel.getId());
                psH.setString(2, hotel.getName());
                psH.setString(3, hotel.getCity());
                psH.setString(4, hotel.getAddress() != null ? hotel.getAddress() : hotel.getCity());
                psH.setInt(5, hotel.getStarRating());
                psH.setDouble(6, hotel.getReviewScore() > 0 ? hotel.getReviewScore() : 9.5);
                psH.setInt(7, hotel.getReviewCount() > 0 ? hotel.getReviewCount() : 50);
                psH.setString(8, hotel.getReviewLabel() != null ? hotel.getReviewLabel() : "Excepcional");
                psH.setString(9, hotel.getDescription());
                psH.setString(10, hotel.getAmenities() != null ? String.join(",", hotel.getAmenities()) : "");
                psH.setDouble(11, hotel.getPricePerNightStarting());
                psH.setString(12, hotel.getFeaturedTag() != null ? hotel.getFeaturedTag() : "Nuevo");
                psH.setString(13, hotel.getImageUrl());
                psH.executeUpdate();
            }

            String insRoom = "INSERT INTO dbo.HotelRoom (HotelID, Nombre, ConfiguracionCama, Capacidad, TamanoM2, PrecioPorNoche, Amenidades, ImagenUrl, CuartosDisponibles, CuartosTotales) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psR = conn.prepareStatement(insRoom)) {
                psR.setString(1, hotel.getId());
                psR.setString(2, room.getName());
                psR.setString(3, room.getBedConfiguration() != null ? room.getBedConfiguration() : "1 cama King");
                psR.setInt(4, room.getCapacity());
                psR.setInt(5, room.getSizeM2() > 0 ? room.getSizeM2() : 40);
                psR.setDouble(6, room.getPricePerNight());
                psR.setString(7, room.getAmenities() != null ? String.join(",", room.getAmenities()) : "");
                psR.setString(8, room.getImageUrl());
                psR.setInt(9, room.getAvailableRooms());
                psR.setInt(10, room.getTotalRooms());
                psR.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("[HotelDAO] Aviso al insertar en DB: " + e.getMessage() + ". Guardando en memoria.");
        }

        cacheHoteles.add(0, hotel);
        return hotel;
    }

    public boolean decrementarCuartoDisponible(String hotelId, int habitacionId) {
        for (HotelEntity h : cacheHoteles) {
            if (hotelId == null || hotelId.isBlank() || h.getId().equalsIgnoreCase(hotelId)) {
                for (HotelRoomEntity r : h.getRooms()) {
                    if (r.getRoomId() == habitacionId || r.getBackendHabitacionId() == habitacionId || habitacionId <= 0) {
                        if (r.getAvailableRooms() > 0) {
                            r.setAvailableRooms(r.getAvailableRooms() - 1);
                            sincronizarDisponibilidadDB(h.getId(), r.getRoomId(), r.getAvailableRooms());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean incrementarCuartoDisponible(String hotelId, int habitacionId) {
        for (HotelEntity h : cacheHoteles) {
            if (hotelId == null || hotelId.isBlank() || h.getId().equalsIgnoreCase(hotelId)) {
                for (HotelRoomEntity r : h.getRooms()) {
                    if (r.getRoomId() == habitacionId || r.getBackendHabitacionId() == habitacionId) {
                        if (r.getAvailableRooms() < r.getTotalRooms()) {
                            r.setAvailableRooms(r.getAvailableRooms() + 1);
                            sincronizarDisponibilidadDB(h.getId(), r.getRoomId(), r.getAvailableRooms());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void sincronizarDisponibilidadDB(String hotelId, int roomId, int nuevosDisponibles) {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sql = "UPDATE dbo.HotelRoom SET CuartosDisponibles = ? WHERE HotelID = ? AND RoomID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nuevosDisponibles);
                ps.setString(2, hotelId);
                ps.setInt(3, roomId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    public boolean actualizarHotel(String hotelId, HotelEntity hotel) {
        for (int i = 0; i < cacheHoteles.size(); i++) {
            HotelEntity h = cacheHoteles.get(i);
            if (h.getId().equalsIgnoreCase(hotelId)) {
                hotel.setId(hotelId);
                if (hotel.getRooms() == null || hotel.getRooms().isEmpty()) {
                    hotel.setRooms(h.getRooms());
                }
                cacheHoteles.set(i, hotel);
                break;
            }
        }

        try (Connection conn = ConexionDB.obtenerConexion()) {
            String sql = "UPDATE dbo.Hotel SET Nombre = ?, Ciudad = ?, Direccion = ?, Estrellas = ?, Puntuacion = ?, ResenasCount = ?, ResenasLabel = ?, Descripcion = ?, Amenidades = ?, PrecioBase = ?, FeaturedTag = ?, ImagenUrl = ? WHERE HotelID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hotel.getName());
                ps.setString(2, hotel.getCity());
                ps.setString(3, hotel.getAddress() != null ? hotel.getAddress() : hotel.getCity());
                ps.setInt(4, hotel.getStarRating());
                ps.setDouble(5, hotel.getReviewScore() > 0 ? hotel.getReviewScore() : 9.5);
                ps.setInt(6, hotel.getReviewCount() > 0 ? hotel.getReviewCount() : 50);
                ps.setString(7, hotel.getReviewLabel() != null ? hotel.getReviewLabel() : "Excepcional");
                ps.setString(8, hotel.getDescription());
                ps.setString(9, hotel.getAmenities() != null ? String.join(",", hotel.getAmenities()) : "");
                ps.setDouble(10, hotel.getPricePerNightStarting());
                ps.setString(11, hotel.getFeaturedTag() != null ? hotel.getFeaturedTag() : "Destacado");
                ps.setString(12, hotel.getImageUrl());
                ps.setString(13, hotelId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (Exception e) {
            System.err.println("[HotelDAO] Aviso al actualizar en DB: " + e.getMessage());
        }
        return true;
    }

    public boolean eliminarHotel(String hotelId) {
        cacheHoteles.removeIf(h -> h.getId().equalsIgnoreCase(hotelId));

        try (Connection conn = ConexionDB.obtenerConexion()) {
            String delRooms = "DELETE FROM dbo.HotelRoom WHERE HotelID = ?";
            try (PreparedStatement psR = conn.prepareStatement(delRooms)) {
                psR.setString(1, hotelId);
                psR.executeUpdate();
            }
            String delHotel = "DELETE FROM dbo.Hotel WHERE HotelID = ?";
            try (PreparedStatement psH = conn.prepareStatement(delHotel)) {
                psH.setString(1, hotelId);
                int rows = psH.executeUpdate();
                return rows > 0;
            }
        } catch (Exception e) {
            System.err.println("[HotelDAO] Aviso al eliminar en DB: " + e.getMessage());
        }
        return true;
    }
}
