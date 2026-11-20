package com.lumina.luminahotels.data.repository

import com.lumina.luminahotels.model.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato del Repositorio para Lumina Hotels & Resorts.
 * Soporta la experiencia orientada al cliente B2C (Estilo Booking.com)
 * y mantiene modularidad con la infraestructura backend REST / SQL.
 */
interface HotelRepository {
    val modoOfflineActivo: StateFlow<Boolean>

    // Experiencia B2C - Alojamientos, Hoteles y Habitaciones
    suspend fun getHotels(): Result<List<Hotel>>
    suspend fun getHotelById(hotelId: String): Result<Hotel?>

    // Experiencia B2C - Atracciones Turísticas
    suspend fun getAttractions(): Result<List<TouristAttraction>>

    // Experiencia B2C - Reservas del Huésped (Mis Reservas)
    suspend fun getCustomerHotelBookings(): Result<List<CustomerHotelBooking>>
    suspend fun getCustomerAttractionBookings(): Result<List<CustomerAttractionBooking>>
    suspend fun bookHotel(booking: CustomerHotelBooking): Result<CustomerHotelBooking>
    suspend fun bookAttraction(booking: CustomerAttractionBooking): Result<CustomerAttractionBooking>
    suspend fun cancelHotelBooking(bookingReference: String): Result<Unit>
    suspend fun cancelAttractionBooking(ticketReference: String): Result<Unit>

    // Persistencia y Sincronización B2C / Panel Admin / Perfil
    suspend fun actualizarPerfil(profile: UserProfile): Result<UserProfile>
    suspend fun crearHotel(hotel: Hotel, room: HotelRoom): Result<Hotel>
    suspend fun updateHotel(hotelId: String, hotel: Hotel, room: HotelRoom): Result<Hotel>
    suspend fun deleteHotel(hotelId: String): Result<Unit>
    suspend fun crearAtraccion(attraction: TouristAttraction): Result<TouristAttraction>
    suspend fun updateAttraction(attrId: String, attraction: TouristAttraction): Result<TouristAttraction>
    suspend fun deleteAttraction(attrId: String): Result<Unit>

    // Soporte e integración con API REST / SQL Server 2022
    suspend fun getHabitacionesDisponibles(
        fechaEntrada: String,
        fechaSalida: String,
        huespedes: Int
    ): Result<List<HabitacionDto>>

    suspend fun getTodasHabitaciones(): Result<List<HabitacionDto>>

    suspend fun crearHabitacion(
        request: HabitacionRequest,
        nombreTipo: String,
        capacidad: Int,
        precioPorNoche: Double
    ): Result<HabitacionDto>

    suspend fun actualizarHabitacion(
        id: Int,
        request: HabitacionRequest,
        nombreTipo: String,
        capacidad: Int,
        precioPorNoche: Double
    ): Result<HabitacionDto>

    suspend fun eliminarHabitacion(id: Int): Result<Unit>

    suspend fun getReservas(): Result<List<ReservaDetalleDto>>

    suspend fun crearReserva(
        request: CrearReservaRequest,
        habitacion: HabitacionDto
    ): Result<ReservaDetalleDto>

    suspend fun actualizarReserva(
        id: Int,
        request: ActualizarReservaRequest,
        habitacion: HabitacionDto?
    ): Result<ReservaDetalleDto>

    suspend fun cancelarReserva(id: Int): Result<ReservaDetalleDto>

    suspend fun eliminarReserva(id: Int): Result<Unit>
}
