package com.lumina.luminahotels.model

import com.google.gson.annotations.SerializedName

/**
 * Estado genérico reactivo para la interfaz de usuario en Lumina Hotels.
 */
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    object Empty : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/**
 * Eventos de un solo disparo para Feedback al usuario (Snackbars, navegación, alertas).
 */
sealed interface UiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : UiEvent
}

// =========================================================================
// MODELOS B2C (CLIENTE FINAL / HUÉSPED - ESTILO BOOKING.COM)
// =========================================================================

/**
 * Representa un Hotel / Resort de lujo disponible para reserva de huéspedes.
 */
data class Hotel(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val starRating: Int = 5,
    val reviewScore: Double = 9.4,
    val reviewCount: Int = 840,
    val reviewLabel: String = "Excepcional",
    val description: String,
    val amenities: List<String>,
    val pricePerNightStarting: Double,
    val featuredTag: String? = null,
    val imageUrl: String = "",
    val hasGeniusDiscount: Boolean = true,
    val rooms: List<HotelRoom>
)

/**
 * Tipo de habitación dentro de un hotel con diseño interior e imagen única.
 */
data class HotelRoom(
    val roomId: Int,
    val hotelId: String,
    val name: String,
    val bedConfiguration: String,
    val capacity: Int,
    val sizeM2: Int,
    val pricePerNight: Double,
    val amenities: List<String>,
    val imageUrl: String = "",
    val freeCancellation: Boolean = true,
    val breakfastIncluded: Boolean = true,
    val backendHabitacionId: Int = roomId,
    val backendTipoId: Int = 1,
    val availableRooms: Int = 5,
    val totalRooms: Int = 5
)

/**
 * Atracción turística, tour o experiencia guiada local.
 */
data class TouristAttraction(
    val id: String,
    val title: String,
    val city: String,
    val category: String,
    val rating: Double = 4.9,
    val reviewCount: Int = 310,
    val duration: String,
    val pricePerPerson: Double,
    val description: String,
    val imageUrl: String = "",
    val highlights: List<String>,
    val inclusions: List<String>,
    val cancellationPolicy: String = "Cancelación gratuita hasta 24 horas antes",
    val minibusCapacity: Int = 14,
    val availableSpots: Int = 14
)

/**
 * Código de marcación telefónica internacional para el selector inteligente.
 */
data class CountryDialCode(
    val code: String,
    val dialCode: String,
    val name: String,
    val flagEmoji: String
)

/**
 * Tarjeta de crédito/débito guardada del usuario.
 */
data class SavedPaymentCard(
    val id: String,
    val cardNumberMasked: String,
    val cardHolder: String,
    val expiry: String,
    val cardBrand: String = "Visa",
    val isDefault: Boolean = false
)

/**
 * Historial de transacciones pasadas completadas del cliente.
 */
data class PastTransaction(
    val id: String,
    val title: String,
    val type: String, // "Estadía" o "Atracción"
    val date: String,
    val bookingReference: String,
    val paymentMethod: String,
    val amountPaid: Double,
    val status: String = "Completada"
)

/**
 * Perfil de cuenta del usuario cliente con datos de pasaporte y demográficos.
 */
data class UserProfile(
    val fullName: String,
    val email: String,
    val dialCode: String = "+503",
    val phoneNumber: String = "7123-4567",
    val isGeniusMember: Boolean = true,
    val gender: String = "Prefiero no decirlo",
    val birthDate: String = "15/06/1992",
    val country: String = "El Salvador",
    val address: String = "Calle La Mascota #14, Colonia San Benito, San Salvador",
    val passportNumber: String = "A12345678",
    val passportCountry: String = "El Salvador",
    val passportExpiryDate: String = "20/12/2030",
    val isAdmin: Boolean = false
)

/**
 * Reserva confirmada de estadía en hotel del cliente.
 */
data class CustomerHotelBooking(
    val bookingReference: String,
    val hotelId: String,
    val hotelName: String,
    val hotelCity: String,
    val roomName: String,
    val guestFullName: String,
    val guestEmail: String,
    val guestPhone: String,
    val checkInDate: String,
    val checkOutDate: String,
    val nights: Int,
    val guestsCount: Int,
    val roomSubtotal: Double,
    val taxesAndFees: Double,
    val totalPrice: Double,
    val paymentMethod: String,
    val specialRequests: String? = null,
    val status: String = "CONFIRMADA",
    val createdAt: String = java.time.LocalDate.now().toString(),
    val backendReservaId: Int? = null
)

/**
 * Reserva confirmada de entradas a atracciones o tours.
 */
data class CustomerAttractionBooking(
    val ticketReference: String,
    val attractionId: String,
    val attractionTitle: String,
    val city: String,
    val date: String,
    val ticketsCount: Int,
    val leadGuestName: String,
    val leadGuestEmail: String,
    val leadGuestPhone: String,
    val totalPrice: Double,
    val paymentMethod: String,
    val status: String = "CONFIRMADA",
    val createdAt: String = java.time.LocalDate.now().toString()
)

// =========================================================================
// DTOs Y CONTRATOS REST CON /backend (COMPATIBILIDAD CON SQL SERVER 2022)
// =========================================================================

data class HabitacionDto(
    @SerializedName("habitacionId") val habitacionId: Int,
    @SerializedName("numero") val numero: String,
    @SerializedName("piso") val piso: Int,
    @SerializedName("estado") val estado: String,
    @SerializedName("tipoHabitacionId") val tipoHabitacionId: Int,
    @SerializedName("nombreTipo") val nombreTipo: String,
    @SerializedName("capacidad") val capacidad: Int,
    @SerializedName("precioPorNoche") val precioPorNoche: Double
)

data class HabitacionRequest(
    @SerializedName("tipoHabitacionId") val tipoHabitacionId: Int,
    @SerializedName("numero") val numero: String,
    @SerializedName("piso") val piso: Int,
    @SerializedName("estado") val estado: String = "DISPONIBLE"
)

data class CrearReservaRequest(
    @SerializedName("nombres") val nombres: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("documentoIdentidad") val documentoIdentidad: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("habitacionId") val habitacionId: Int,
    @SerializedName("fechaEntrada") val fechaEntrada: String,
    @SerializedName("fechaSalida") val fechaSalida: String,
    @SerializedName("huespedes") val huespedes: Int,
    @SerializedName("estado") val estado: String = "CONFIRMADA"
)

data class ActualizarReservaRequest(
    @SerializedName("fechaEntrada") val fechaEntrada: String,
    @SerializedName("fechaSalida") val fechaSalida: String,
    @SerializedName("huespedes") val huespedes: Int,
    @SerializedName("estado") val estado: String
)

data class ReservaDetalleDto(
    @SerializedName("reservaId") val reservaId: Int,
    @SerializedName("clienteId") val clienteId: Int,
    @SerializedName("nombreCompletoCliente") val nombreCompletoCliente: String,
    @SerializedName("documentoIdentidad") val documentoIdentidad: String,
    @SerializedName("correoCliente") val correoCliente: String,
    @SerializedName("habitacionId") val habitacionId: Int,
    @SerializedName("numeroHabitacion") val numeroHabitacion: String,
    @SerializedName("piso") val piso: Int,
    @SerializedName("tipoHabitacion") val tipoHabitacion: String,
    @SerializedName("precioPorNoche") val precioPorNoche: Double,
    @SerializedName("fechaEntrada") val fechaEntrada: String,
    @SerializedName("fechaSalida") val fechaSalida: String,
    @SerializedName("cantidadNoches") val cantidadNoches: Int,
    @SerializedName("huespedes") val huespedes: Int,
    @SerializedName("estadoReserva") val estadoReserva: String,
    @SerializedName("totalEstimadoCobro") val totalEstimadoCobro: Double
)

data class UsuarioSesion(
    val uid: String,
    val correo: String,
    val nombreMostrado: String
)

data class ClientePerfilDto(
    @SerializedName("clienteId") val clienteId: Int,
    @SerializedName("nombres") val nombres: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("genero") val genero: String?,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerializedName("pais") val pais: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("numeroPasaporte") val numeroPasaporte: String?,
    @SerializedName("paisEmisorPasaporte") val paisEmisorPasaporte: String?,
    @SerializedName("fechaVencimientoPasaporte") val fechaVencimientoPasaporte: String?
)

data class ClientePerfilRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("dialCode") val dialCode: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("country") val country: String,
    @SerializedName("address") val address: String,
    @SerializedName("passportNumber") val passportNumber: String,
    @SerializedName("passportCountry") val passportCountry: String,
    @SerializedName("passportExpiryDate") val passportExpiryDate: String
)

data class CrearHotelRequest(
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String,
    @SerializedName("address") val address: String,
    @SerializedName("starRating") val starRating: Int,
    @SerializedName("description") val description: String,
    @SerializedName("amenities") val amenities: List<String>,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("roomName") val roomName: String,
    @SerializedName("pricePerNight") val pricePerNight: Double,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("availableRooms") val availableRooms: Int
)

data class CrearAtraccionRequest(
    @SerializedName("title") val title: String,
    @SerializedName("city") val city: String,
    @SerializedName("category") val category: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("pricePerPerson") val pricePerPerson: Double,
    @SerializedName("description") val description: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("minibusCapacity") val minibusCapacity: Int
)

data class AtraccionReservaRequest(
    @SerializedName("attractionId") val attractionId: String,
    @SerializedName("ticketsCount") val ticketsCount: Int,
    @SerializedName("leadGuestName") val leadGuestName: String,
    @SerializedName("leadGuestEmail") val leadGuestEmail: String,
    @SerializedName("leadGuestPhone") val leadGuestPhone: String,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("paymentMethod") val paymentMethod: String
)

