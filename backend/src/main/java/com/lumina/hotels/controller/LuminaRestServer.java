package com.lumina.hotels.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lumina.hotels.config.DatabaseInitializer;
import com.lumina.hotels.dao.AtraccionDAO;
import com.lumina.hotels.dao.ClienteDAO;
import com.lumina.hotels.dao.HabitacionDAO;
import com.lumina.hotels.dao.HotelDAO;
import com.lumina.hotels.dao.ReservaDAO;
import com.lumina.hotels.model.AtraccionEntity;
import com.lumina.hotels.model.Cliente;
import com.lumina.hotels.model.HotelEntity;
import com.lumina.hotels.model.HotelRoomEntity;
import com.lumina.hotels.model.Reserva;
import com.lumina.hotels.model.ReservaDetalleView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuminaRestServer {

    private static final Gson gson = new Gson();
    private static final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private static final ReservaDAO reservaDAO = new ReservaDAO();
    private static final ClienteDAO clienteDAO = new ClienteDAO();
    private static final HotelDAO hotelDAO = new HotelDAO();
    private static final AtraccionDAO atraccionDAO = new AtraccionDAO();

    public static void main(String[] args) throws IOException {
        // Inicializar esquema de base de datos de manera segura y no destructiva
        DatabaseInitializer.initialize();

        int port = Integer.parseInt(System.getenv().getOrDefault("LUMINA_API_PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // =====================================================================
        // 1. ENDPOINT: PERFIL Y PASAPORTE DEL VIAJERO (/api/cliente/perfil)
        // =====================================================================
        server.createContext("/api/cliente/perfil", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
                    String correo = params.get("correo");
                    Cliente c = clienteDAO.obtenerPorCorreo(correo);
                    sendJson(exchange, 200, c);
                } else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    Cliente c = new Cliente();
                    if (body.has("fullName")) {
                        String full = body.get("fullName").getAsString().trim();
                        int space = full.indexOf(' ');
                        if (space > 0) {
                            c.setNombres(full.substring(0, space));
                            c.setApellidos(full.substring(space + 1));
                        } else {
                            c.setNombres(full);
                            c.setApellidos("");
                        }
                    } else {
                        c.setNombres(body.has("nombres") ? body.get("nombres").getAsString() : "");
                        c.setApellidos(body.has("apellidos") ? body.get("apellidos").getAsString() : "");
                    }

                    c.setCorreo(body.has("email") ? body.get("email").getAsString() :
                               (body.has("correo") ? body.get("correo").getAsString() : "carlos.mendoza@ejemplo.com"));

                    String dial = body.has("dialCode") ? body.get("dialCode").getAsString() : "+503";
                    String phone = body.has("phoneNumber") ? body.get("phoneNumber").getAsString() :
                                  (body.has("telefono") ? body.get("telefono").getAsString() : "");
                    c.setTelefono(phone.startsWith("+") ? phone : (dial + " " + phone).trim());

                    c.setGenero(body.has("gender") ? body.get("gender").getAsString() :
                               (body.has("genero") ? body.get("genero").getAsString() : null));
                    c.setFechaNacimiento(body.has("birthDate") ? body.get("birthDate").getAsString() :
                                        (body.has("fechaNacimiento") ? body.get("fechaNacimiento").getAsString() : null));
                    c.setPais(body.has("country") ? body.get("country").getAsString() :
                             (body.has("pais") ? body.get("pais").getAsString() : null));
                    c.setDireccion(body.has("address") ? body.get("address").getAsString() :
                                  (body.has("direccion") ? body.get("direccion").getAsString() : null));
                    c.setNumeroPasaporte(body.has("passportNumber") ? body.get("passportNumber").getAsString() :
                                        (body.has("numeroPasaporte") ? body.get("numeroPasaporte").getAsString() : null));
                    c.setPaisEmisorPasaporte(body.has("passportCountry") ? body.get("passportCountry").getAsString() :
                                            (body.has("paisEmisorPasaporte") ? body.get("paisEmisorPasaporte").getAsString() : null));
                    c.setFechaVencimientoPasaporte(body.has("passportExpiryDate") ? body.get("passportExpiryDate").getAsString() :
                                                  (body.has("fechaVencimientoPasaporte") ? body.get("fechaVencimientoPasaporte").getAsString() : null));

                    Cliente guardado = clienteDAO.guardarOActualizarPerfil(c);
                    sendJson(exchange, 200, Map.of(
                            "mensaje", "Perfil y pasaporte persistidos exitosamente en base de datos",
                            "cliente", guardado
                    ));
                } else {
                    sendJson(exchange, 405, Map.of("error", "Método no permitido"));
                }
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("error", e.getMessage()));
            }
        });

        // =====================================================================
        // 2. ENDPOINT: HOTELES Y HABITACIONES / INVENTARIO (/api/hoteles)
        // =====================================================================
        server.createContext("/api/hoteles", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                if ("GET".equalsIgnoreCase(method)) {
                    List<HotelEntity> lista = hotelDAO.listarTodas();
                    sendJson(exchange, 200, lista);
                } else if ("POST".equalsIgnoreCase(method)) {
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);

                    HotelEntity hotel = new HotelEntity();
                    hotel.setName(body.get("name").getAsString());
                    hotel.setCity(body.get("city").getAsString());
                    hotel.setAddress(body.has("address") ? body.get("address").getAsString() : body.get("city").getAsString());
                    hotel.setStarRating(body.has("starRating") ? body.get("starRating").getAsInt() : 5);
                    hotel.setDescription(body.get("description").getAsString());
                    hotel.setPricePerNightStarting(body.has("pricePerNight") ? body.get("pricePerNight").getAsDouble() : 100.0);
                    hotel.setImageUrl(body.has("imageUrl") ? body.get("imageUrl").getAsString() : "");

                    if (body.has("amenities")) {
                        if (body.get("amenities").isJsonArray()) {
                            List<String> amList = new ArrayList<>();
                            body.getAsJsonArray("amenities").forEach(el -> amList.add(el.getAsString()));
                            hotel.setAmenities(amList);
                        } else {
                            hotel.setAmenities(Arrays.asList(body.get("amenities").getAsString().split(",")));
                        }
                    }

                    int initialAvailableRooms = body.has("availableRooms") ? body.get("availableRooms").getAsInt() :
                                               (body.has("initialAvailableRooms") ? body.get("initialAvailableRooms").getAsInt() : 5);

                    HotelRoomEntity room = new HotelRoomEntity();
                    room.setName(body.has("roomName") ? body.get("roomName").getAsString() : "Habitación Suite Estándar");
                    room.setPricePerNight(hotel.getPricePerNightStarting());
                    room.setCapacity(body.has("capacity") ? body.get("capacity").getAsInt() : 2);
                    room.setAvailableRooms(initialAvailableRooms);
                    room.setTotalRooms(initialAvailableRooms);
                    room.setImageUrl(hotel.getImageUrl());
                    room.setAmenities(hotel.getAmenities());

                    HotelEntity creado = hotelDAO.crearHotelYHabitacion(hotel, room);
                    sendJson(exchange, 201, creado);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String hotelId = extraerIdString(path);
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    HotelEntity hotel = new HotelEntity();
                    hotel.setName(body.get("name").getAsString());
                    hotel.setCity(body.get("city").getAsString());
                    hotel.setAddress(body.has("address") ? body.get("address").getAsString() : body.get("city").getAsString());
                    hotel.setStarRating(body.has("starRating") ? body.get("starRating").getAsInt() : 5);
                    hotel.setDescription(body.get("description").getAsString());
                    hotel.setPricePerNightStarting(body.has("pricePerNight") ? body.get("pricePerNight").getAsDouble() : 100.0);
                    hotel.setImageUrl(body.has("imageUrl") ? body.get("imageUrl").getAsString() : "");
                    if (body.has("amenities")) {
                        if (body.get("amenities").isJsonArray()) {
                            List<String> amList = new ArrayList<>();
                            body.getAsJsonArray("amenities").forEach(el -> amList.add(el.getAsString()));
                            hotel.setAmenities(amList);
                        } else {
                            hotel.setAmenities(Arrays.asList(body.get("amenities").getAsString().split(",")));
                        }
                    }
                    boolean ok = hotelDAO.actualizarHotel(hotelId, hotel);
                    sendJson(exchange, ok ? 200 : 404, hotel);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    String hotelId = extraerIdString(path);
                    boolean ok = hotelDAO.eliminarHotel(hotelId);
                    sendJson(exchange, ok ? 200 : 404, Map.of("eliminado", ok));
                } else {
                    sendJson(exchange, 405, Map.of("error", "Método no permitido"));
                }
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("error", e.getMessage()));
            }
        });

        // =====================================================================
        // 3. ENDPOINT: ATRACCIONES TURÍSTICAS Y MICROBÚS (/api/atracciones)
        // =====================================================================
        server.createContext("/api/atracciones", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                if ("GET".equalsIgnoreCase(method)) {
                    List<AtraccionEntity> lista = atraccionDAO.listarTodas();
                    sendJson(exchange, 200, lista);
                } else if ("POST".equalsIgnoreCase(method)) {
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);

                    AtraccionEntity a = new AtraccionEntity();
                    a.setTitle(body.get("title").getAsString());
                    a.setCity(body.get("city").getAsString());
                    a.setCategory(body.has("category") ? body.get("category").getAsString() : "Naturaleza & Aventura");
                    a.setDuration(body.has("duration") ? body.get("duration").getAsString() : "4 horas");
                    a.setPricePerPerson(body.get("pricePerPerson").getAsDouble());
                    a.setDescription(body.get("description").getAsString());
                    a.setImageUrl(body.has("imageUrl") ? body.get("imageUrl").getAsString() : "");

                    int minibusCap = body.has("minibusCapacity") ? body.get("minibusCapacity").getAsInt() :
                                    (body.has("capacidadMicrobus") ? body.get("capacidadMicrobus").getAsInt() : 14);
                    if (minibusCap < 8) minibusCap = 8;
                    if (minibusCap > 15) minibusCap = 15;
                    a.setMinibusCapacity(minibusCap);
                    a.setAvailableSpots(minibusCap);

                    AtraccionEntity creada = atraccionDAO.crearAtraccion(a);
                    sendJson(exchange, 201, creada);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String attrId = extraerIdString(path);
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    AtraccionEntity a = new AtraccionEntity();
                    a.setTitle(body.get("title").getAsString());
                    a.setCity(body.get("city").getAsString());
                    a.setCategory(body.has("category") ? body.get("category").getAsString() : "Naturaleza & Aventura");
                    a.setDuration(body.has("duration") ? body.get("duration").getAsString() : "4 horas");
                    a.setPricePerPerson(body.get("pricePerPerson").getAsDouble());
                    a.setDescription(body.get("description").getAsString());
                    a.setImageUrl(body.has("imageUrl") ? body.get("imageUrl").getAsString() : "");
                    int minibusCap = body.has("minibusCapacity") ? body.get("minibusCapacity").getAsInt() : 14;
                    a.setMinibusCapacity(minibusCap);
                    a.setAvailableSpots(minibusCap);
                    boolean ok = atraccionDAO.actualizarAtraccion(attrId, a);
                    sendJson(exchange, ok ? 200 : 404, a);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    String attrId = extraerIdString(path);
                    boolean ok = atraccionDAO.eliminarAtraccion(attrId);
                    sendJson(exchange, ok ? 200 : 404, Map.of("eliminado", ok));
                } else {
                    sendJson(exchange, 405, Map.of("error", "Método no permitido"));
                }
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("error", e.getMessage()));
            }
        });

        // =====================================================================
        // 4. ENDPOINT: RESERVA DE ATRACCIÓN (DECREMENTA SPOTS MICROBÚS)
        // =====================================================================
        server.createContext("/api/reservas/atraccion", exchange -> {
            try {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    String attrId = body.get("attractionId").getAsString();
                    int tickets = body.has("ticketsCount") ? body.get("ticketsCount").getAsInt() : 1;

                    boolean decremented = atraccionDAO.decrementarCupos(attrId, tickets);
                    if (decremented) {
                        sendJson(exchange, 201, Map.of(
                                "mensaje", "Reserva de atracción confirmada y spots de microbús actualizados en base de datos",
                                "attractionId", attrId,
                                "tickets", tickets
                        ));
                    } else {
                        sendJson(exchange, 400, Map.of("error", "No hay suficientes cupos disponibles en el microbús."));
                    }
                } else {
                    sendJson(exchange, 405, Map.of("error", "Método no permitido"));
                }
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("error", e.getMessage()));
            }
        });

        // =====================================================================
        // 5. CRUD HABITACIONES EXISTENTES (PRESERVACIÓN TOTAL)
        // =====================================================================
        server.createContext("/api/habitaciones", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("GET".equalsIgnoreCase(method)) {
                    if (path.endsWith("/disponibles")) {
                        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
                        LocalDate entrada = LocalDate.parse(params.get("fechaEntrada"));
                        LocalDate salida = LocalDate.parse(params.get("fechaSalida"));
                        int huespedes = Integer.parseInt(params.getOrDefault("huespedes", "1"));
                        sendJson(exchange, 200, habitacionDAO.consultarDisponibles(entrada, salida, huespedes));
                    } else {
                        sendJson(exchange, 200, habitacionDAO.listarTodas());
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    int id = habitacionDAO.crearHabitacion(
                            body.get("tipoHabitacionId").getAsInt(),
                            body.get("numero").getAsString(),
                            body.get("piso").getAsInt()
                    );
                    sendJson(exchange, 201, Map.of("habitacionId", id, "mensaje", "Habitación creada"));
                } else if ("PUT".equalsIgnoreCase(method)) {
                    int id = extraerIdFinal(path);
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    boolean ok = habitacionDAO.actualizarHabitacion(
                            id,
                            body.get("numero").getAsString(),
                            body.get("piso").getAsInt(),
                            body.get("estado").getAsString(),
                            body.get("tipoHabitacionId").getAsInt()
                    );
                    sendJson(exchange, ok ? 200 : 404, Map.of("actualizado", ok));
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    int id = extraerIdFinal(path);
                    boolean ok = habitacionDAO.eliminarHabitacion(id);
                    sendJson(exchange, ok ? 200 : 404, Map.of("eliminado", ok));
                } else {
                    sendJson(exchange, 405, Map.of("error", "Método no permitido"));
                }
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("error", e.getMessage()));
            }
        });

        // =====================================================================
        // 6. CRUD RESERVAS EXISTENTES (CON ACTUALIZACIÓN DE INVENTARIO)
        // =====================================================================
        server.createContext("/api/reservas", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("GET".equalsIgnoreCase(method)) {
                    sendJson(exchange, 200, reservaDAO.listarReservasDetalle());
                } else if ("POST".equalsIgnoreCase(method)) {
                    JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                    Reserva r = new Reserva();
                    int habId = body.get("habitacionId").getAsInt();
                    r.setHabitacionId(habId);
                    r.setFechaEntrada(LocalDate.parse(body.get("fechaEntrada").getAsString()));
                    r.setFechaSalida(LocalDate.parse(body.get("fechaSalida").getAsString()));
                    r.setHuespedes(body.get("huespedes").getAsInt());
                    r.setEstado(body.has("estado") ? body.get("estado").getAsString() : "CONFIRMADA");

                    ReservaDetalleView creada = reservaDAO.registrarReserva(
                            body.get("nombres").getAsString(),
                            body.get("apellidos").getAsString(),
                            body.get("documentoIdentidad").getAsString(),
                            body.get("correo").getAsString(),
                            body.has("telefono") ? body.get("telefono").getAsString() : "",
                            r
                    );

                    // Sincronizar decremento de cuarto en inventario
                    String hotelId = body.has("hotelId") ? body.get("hotelId").getAsString() : null;
                    hotelDAO.decrementarCuartoDisponible(hotelId, habId);

                    sendJson(exchange, 201, creada);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    if (path.endsWith("/cancelar")) {
                        String[] segments = path.split("/");
                        int reservaId = Integer.parseInt(segments[segments.length - 2]);
                        ReservaDetalleView cancelada = reservaDAO.cancelarReserva(reservaId);
                        if (cancelada != null) {
                            hotelDAO.incrementarCuartoDisponible(null, cancelada.getHabitacionId());
                        }
                        sendJson(exchange, 200, cancelada);
                    } else {
                        int reservaId = extraerIdFinal(path);
                        JsonObject body = gson.fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
                        ReservaDetalleView actualizada = reservaDAO.actualizarReserva(
                                reservaId,
                                LocalDate.parse(body.get("fechaEntrada").getAsString()),
                                LocalDate.parse(body.get("fechaSalida").getAsString()),
                                body.get("huespedes").getAsInt(),
                                body.get("estado").getAsString()
                        );
                        sendJson(exchange, 200, actualizada);
                    }
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    int reservaId = extraerIdFinal(path);
                    reservaDAO.eliminarReserva(reservaId);
                    sendJson(exchange, 200, Map.of("eliminado", true, "reservaId", reservaId));
                } else {
                    sendJson(exchange, 405, Map.of("error", "Operación no soportada"));
                }
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("error", e.getMessage()));
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Lumina Hotels API REST CRUD iniciada en puerto " + port);
    }

    private static int extraerIdFinal(String path) {
        String clean = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        String[] parts = clean.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private static String extraerIdString(String path) {
        String clean = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        String[] parts = clean.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return map;
        for (String pair : rawQuery.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return map;
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] bytes = gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
