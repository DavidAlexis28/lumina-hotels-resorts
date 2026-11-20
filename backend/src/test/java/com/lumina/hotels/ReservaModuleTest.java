package com.lumina.hotels;

import com.lumina.hotels.config.ConexionDB;
import com.lumina.hotels.dao.HabitacionDAO;
import com.lumina.hotels.dao.ReservaDAO;
import com.lumina.hotels.model.Habitacion;
import com.lumina.hotels.model.Reserva;
import com.lumina.hotels.model.ReservaDetalleView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaModuleTest {

    @Test
    @DisplayName("1. Validar creación de entidad Reserva y cálculo de noches")
    void testCreacionEntidadReserva() {
        LocalDate entrada = LocalDate.of(2026, 12, 1);
        LocalDate salida = LocalDate.of(2026, 12, 5);
        Reserva reserva = new Reserva(1, 10, 2, entrada, salida, 2, "CONFIRMADA", new BigDecimal("460.00"));

        assertTrue(reserva.esRangoFechasValido());
        assertEquals(4, reserva.calcularNoches());
        assertEquals("CONFIRMADA", reserva.getEstado());
    }

    @Test
    @DisplayName("2. Rechazar reserva con fecha de salida anterior o igual a entrada")
    void testValidacionFechasInvalidas() {
        ReservaDAO dao = new ReservaDAO();
        Reserva invalida = new Reserva(
                0, 1, 1,
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 10),
                2, "CONFIRMADA", BigDecimal.ZERO
        );

        assertThrows(IllegalArgumentException.class, () ->
                dao.registrarReserva("Ana", "Gómez", "01234567-8", "ana@ejemplo.com", "7000-0000", invalida)
        );
    }

    @Test
    @DisplayName("3. Verificar construcción de cadena JDBC para SQL Server 2022")
    void testConfiguracionUrlJdbc() {
        String url = ConexionDB.buildJdbcUrl();
        assertTrue(url.startsWith("jdbc:sqlserver://"));
        assertTrue(url.contains("lumina_hotel_and_resorts"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "LUMINA_DB_PASSWORD", matches = ".+")
    @DisplayName("4. Prueba de integración: apertura de conexión, consulta SP y vw_ReservasDetalle")
    void testIntegracionSqlServer() throws Exception {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }

        HabitacionDAO habitacionDAO = new HabitacionDAO();
        List<Habitacion> disponibles = habitacionDAO.consultarDisponibles(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                2
        );
        assertNotNull(disponibles);

        ReservaDAO reservaDAO = new ReservaDAO();
        List<ReservaDetalleView> historial = reservaDAO.listarReservasDetalle();
        assertNotNull(historial);
    }
}
