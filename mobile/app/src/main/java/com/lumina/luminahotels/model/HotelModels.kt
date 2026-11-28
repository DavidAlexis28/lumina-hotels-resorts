package com.lumina.luminahotels.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
@Serializable
data class Hotel(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("city") val city: String,
    @SerialName("address") val address: String,
    @SerialName("star_rating") val starRating: Int = 5,
    @SerialName("review_score") val reviewScore: Double = 9.4,
    @SerialName("review_count") val reviewCount: Int = 840,
    @SerialName("review_label") val reviewLabel: String = "Excepcional",
    @SerialName("description") val description: String,
    @SerialName("amenities") val amenities: List<String> = emptyList(),
    @SerialName("price_per_night_starting") val pricePerNightStarting: Double,
    @SerialName("featured_tag") val featuredTag: String? = null,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    @SerialName("has_genius_discount") val hasGeniusDiscount: Boolean = true,
    @SerialName("hotel_rooms") val rooms: List<HotelRoom> = emptyList()
) {
    val coverImage: String get() = imageUrls.firstOrNull() ?: imageUrl
    val galleryImages: List<String> get() = (if (imageUrls.isNotEmpty()) imageUrls else listOf(imageUrl)).filter { it.isNotBlank() }.take(5)
}

/**
 * Tipo de habitación dentro de un hotel con diseño interior e imagen única.
 */
@Serializable
data class HotelRoom(
    @SerialName("room_id") val roomId: Int,
    @SerialName("hotel_id") val hotelId: String,
    @SerialName("name") val name: String,
    @SerialName("bed_configuration") val bedConfiguration: String = "1 Cama King",
    @SerialName("capacity") val capacity: Int = 2,
    @SerialName("size_m2") val sizeM2: Int = 42,
    @SerialName("price_per_night") val pricePerNight: Double,
    @SerialName("amenities") val amenities: List<String> = emptyList(),
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("free_cancellation") val freeCancellation: Boolean = true,
    @SerialName("breakfast_included") val breakfastIncluded: Boolean = true,
    @SerialName("backend_habitacion_id") val backendHabitacionId: Int = roomId,
    @SerialName("backend_tipo_id") val backendTipoId: Int = 1,
    @SerialName("available_rooms") val availableRooms: Int = 5,
    @SerialName("total_rooms") val totalRooms: Int = 5
)

/**
 * Atracción turística, tour o experiencia guiada local.
 */
@Serializable
data class TouristAttraction(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("city") val city: String,
    @SerialName("category") val category: String,
    @SerialName("rating") val rating: Double = 4.9,
    @SerialName("review_count") val reviewCount: Int = 310,
    @SerialName("duration") val duration: String,
    @SerialName("price_per_person") val pricePerPerson: Double,
    @SerialName("description") val description: String,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    @SerialName("highlights") val highlights: List<String> = emptyList(),
    @SerialName("inclusions") val inclusions: List<String> = emptyList(),
    @SerialName("cancellation_policy") val cancellationPolicy: String = "Cancelación gratuita hasta 24 horas antes",
    @SerialName("minibus_capacity") val minibusCapacity: Int = 14,
    @SerialName("available_spots") val availableSpots: Int = 14
) {
    val coverImage: String get() = imageUrls.firstOrNull() ?: imageUrl
    val galleryImages: List<String> get() = (if (imageUrls.isNotEmpty()) imageUrls else listOf(imageUrl)).filter { it.isNotBlank() }.take(5)
}

/**
 * Código de marcación telefónica internacional para el selector inteligente.
 */
@Serializable
data class CountryDialCode(
    val code: String,
    val dialCode: String,
    val name: String,
    val flagEmoji: String
)

/**
 * Historial de transacciones pasadas completadas del cliente.
 */
@Serializable
data class PastTransaction(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("type") val type: String, // "Estadía" o "Atracción"
    @SerialName("date") val date: String,
    @SerialName("booking_reference") val bookingReference: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("amount_paid") val amountPaid: Double,
    @SerialName("status") val status: String = "Completada"
)

/**
 * Perfil de cuenta del usuario cliente con datos de pasaporte y demográficos.
 */
@Serializable
data class UserProfile(
    @SerialName("nombres") val fullName: String,
    @SerialName("correo") val email: String,
    @SerialName("dial_code") val dialCode: String = "+503",
    @SerialName("telefono") val phoneNumber: String = "",
    @SerialName("is_genius_member") val isGeniusMember: Boolean = false,
    @SerialName("genero") val gender: String = "Prefiero no decirlo",
    @SerialName("fecha_nacimiento") val birthDate: String = "",
    @SerialName("pais") val country: String = "El Salvador",
    @SerialName("direccion") val address: String = "",
    @SerialName("numero_pasaporte") val passportNumber: String = "",
    @SerialName("pais_emisor_pasaporte") val passportCountry: String = "El Salvador",
    @SerialName("fecha_vencimiento_pasaporte") val passportExpiryDate: String = "",
    @SerialName("is_admin") val isAdmin: Boolean = false
)

/**
 * Reserva confirmada de estadía en hotel del cliente.
 */
@Serializable
data class CustomerHotelBooking(
    @SerialName("booking_reference") val bookingReference: String,
    @SerialName("hotel_id") val hotelId: String,
    @SerialName("hotel_name") val hotelName: String,
    @SerialName("hotel_city") val hotelCity: String,
    @SerialName("room_name") val roomName: String,
    @SerialName("guest_full_name") val guestFullName: String,
    @SerialName("guest_email") val guestEmail: String,
    @SerialName("guest_phone") val guestPhone: String,
    @SerialName("check_in_date") val checkInDate: String,
    @SerialName("check_out_date") val checkOutDate: String,
    @SerialName("nights") val nights: Int,
    @SerialName("guests_count") val guestsCount: Int,
    @SerialName("room_subtotal") val roomSubtotal: Double,
    @SerialName("taxes_and_fees") val taxesAndFees: Double,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("special_requests") val specialRequests: String? = null,
    @SerialName("status") val status: String = "CONFIRMADA",
    @SerialName("created_at") val createdAt: String = java.time.LocalDate.now().toString(),
    @SerialName("backend_reserva_id") val backendReservaId: Int? = null,
    @kotlinx.serialization.Transient val discountAmount: Double = 0.0,
    @kotlinx.serialization.Transient val originalSubtotal: Double? = null
)

/**
 * Reserva confirmada de entradas a atracciones o tours.
 */
@Serializable
data class CustomerAttractionBooking(
    @SerialName("ticket_reference") val ticketReference: String,
    @SerialName("attraction_id") val attractionId: String,
    @SerialName("attraction_title") val attractionTitle: String,
    @SerialName("city") val city: String,
    @SerialName("date") val date: String,
    @SerialName("tickets_count") val ticketsCount: Int,
    @SerialName("lead_guest_name") val leadGuestName: String,
    @SerialName("lead_guest_email") val leadGuestEmail: String,
    @SerialName("lead_guest_phone") val leadGuestPhone: String,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("status") val status: String = "CONFIRMADA",
    @SerialName("created_at") val createdAt: String = java.time.LocalDate.now().toString()
)

// =========================================================================
// DTOs Y CONTRATOS REST CON /backend (COMPATIBILIDAD CON SQL SERVER 2022 / SUPABASE)
// =========================================================================

@Serializable
data class HabitacionDto(
    @SerialName("habitacion_id") @SerializedName("habitacionId") val habitacionId: Int,
    @SerialName("numero") @SerializedName("numero") val numero: String,
    @SerialName("piso") @SerializedName("piso") val piso: Int,
    @SerialName("estado") @SerializedName("estado") val estado: String,
    @SerialName("tipo_habitacion_id") @SerializedName("tipoHabitacionId") val tipoHabitacionId: Int,
    @SerialName("nombre_tipo") @SerializedName("nombreTipo") val nombreTipo: String,
    @SerialName("capacidad") @SerializedName("capacidad") val capacidad: Int,
    @SerialName("precio_por_noche") @SerializedName("precioPorNoche") val precioPorNoche: Double
)

@Serializable
data class HabitacionRequest(
    @SerialName("tipo_habitacion_id") @SerializedName("tipoHabitacionId") val tipoHabitacionId: Int,
    @SerialName("numero") @SerializedName("numero") val numero: String,
    @SerialName("piso") @SerializedName("piso") val piso: Int,
    @SerialName("estado") @SerializedName("estado") val estado: String = "DISPONIBLE"
)

@Serializable
data class CrearReservaRequest(
    @SerialName("nombres") @SerializedName("nombres") val nombres: String,
    @SerialName("apellidos") @SerializedName("apellidos") val apellidos: String,
    @SerialName("documento_identidad") @SerializedName("documentoIdentidad") val documentoIdentidad: String,
    @SerialName("correo") @SerializedName("correo") val correo: String,
    @SerialName("telefono") @SerializedName("telefono") val telefono: String,
    @SerialName("habitacion_id") @SerializedName("habitacionId") val habitacionId: Int,
    @SerialName("fecha_entrada") @SerializedName("fechaEntrada") val fechaEntrada: String,
    @SerialName("fecha_salida") @SerializedName("fechaSalida") val fechaSalida: String,
    @SerialName("huespedes") @SerializedName("huespedes") val huespedes: Int,
    @SerialName("estado_reserva") @SerializedName("estado") val estado: String = "CONFIRMADA"
)

@Serializable
data class ActualizarReservaRequest(
    @SerialName("fecha_entrada") @SerializedName("fechaEntrada") val fechaEntrada: String,
    @SerialName("fecha_salida") @SerializedName("fechaSalida") val fechaSalida: String,
    @SerialName("huespedes") @SerializedName("huespedes") val huespedes: Int,
    @SerialName("estado_reserva") @SerializedName("estado") val estado: String
)

@Serializable
data class ReservaDetalleDto(
    @SerialName("reserva_id") @SerializedName("reservaId") val reservaId: Int,
    @SerialName("cliente_id") @SerializedName("clienteId") val clienteId: Int,
    @SerialName("nombre_completo_cliente") @SerializedName("nombreCompletoCliente") val nombreCompletoCliente: String,
    @SerialName("documento_identidad") @SerializedName("documentoIdentidad") val documentoIdentidad: String,
    @SerialName("correo_cliente") @SerializedName("correoCliente") val correoCliente: String,
    @SerialName("habitacion_id") @SerializedName("habitacionId") val habitacionId: Int,
    @SerialName("numero_habitacion") @SerializedName("numeroHabitacion") val numeroHabitacion: String,
    @SerialName("piso") @SerializedName("piso") val piso: Int,
    @SerialName("tipo_habitacion") @SerializedName("tipoHabitacion") val tipoHabitacion: String,
    @SerialName("precio_por_noche") @SerializedName("precioPorNoche") val precioPorNoche: Double,
    @SerialName("fecha_entrada") @SerializedName("fechaEntrada") val fechaEntrada: String,
    @SerialName("fecha_salida") @SerializedName("fechaSalida") val fechaSalida: String,
    @SerialName("cantidad_noches") @SerializedName("cantidadNoches") val cantidadNoches: Int,
    @SerialName("huespedes") @SerializedName("huespedes") val huespedes: Int,
    @SerialName("estado_reserva") @SerializedName("estadoReserva") val estadoReserva: String,
    @SerialName("total_estimado_cobro") @SerializedName("totalEstimadoCobro") val totalEstimadoCobro: Double
)

@Serializable
data class UsuarioSesion(
    val uid: String,
    val correo: String,
    val nombreMostrado: String
)

@Serializable
data class ClientePerfilDto(
    @SerialName("cliente_id") @SerializedName("clienteId") val clienteId: Int,
    @SerialName("nombres") @SerializedName("nombres") val nombres: String,
    @SerialName("apellidos") @SerializedName("apellidos") val apellidos: String,
    @SerialName("correo") @SerializedName("correo") val correo: String,
    @SerialName("telefono") @SerializedName("telefono") val telefono: String?,
    @SerialName("genero") @SerializedName("genero") val genero: String?,
    @SerialName("fecha_nacimiento") @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerialName("pais") @SerializedName("pais") val pais: String?,
    @SerialName("direccion") @SerializedName("direccion") val direccion: String?,
    @SerialName("numero_pasaporte") @SerializedName("numeroPasaporte") val numeroPasaporte: String?,
    @SerialName("pais_emisor_pasaporte") @SerializedName("paisEmisorPasaporte") val paisEmisorPasaporte: String?,
    @SerialName("fecha_vencimiento_pasaporte") @SerializedName("fechaVencimientoPasaporte") val fechaVencimientoPasaporte: String?
)

@Serializable
data class ClientePerfilRequest(
    @SerialName("full_name") @SerializedName("fullName") val fullName: String,
    @SerialName("email") @SerializedName("email") val email: String,
    @SerialName("dial_code") @SerializedName("dialCode") val dialCode: String,
    @SerialName("phone_number") @SerializedName("phoneNumber") val phoneNumber: String,
    @SerialName("gender") @SerializedName("gender") val gender: String,
    @SerialName("birth_date") @SerializedName("birthDate") val birthDate: String,
    @SerialName("country") @SerializedName("country") val country: String,
    @SerialName("address") @SerializedName("address") val address: String,
    @SerialName("passport_number") @SerializedName("passportNumber") val passportNumber: String,
    @SerialName("passport_country") @SerializedName("passportCountry") val passportCountry: String,
    @SerialName("passport_expiry_date") @SerializedName("passportExpiryDate") val passportExpiryDate: String
)

@Serializable
data class CrearHotelRequest(
    @SerialName("name") @SerializedName("name") val name: String,
    @SerialName("city") @SerializedName("city") val city: String,
    @SerialName("address") @SerializedName("address") val address: String,
    @SerialName("star_rating") @SerializedName("starRating") val starRating: Int,
    @SerialName("description") @SerializedName("description") val description: String,
    @SerialName("amenities") @SerializedName("amenities") val amenities: List<String>,
    @SerialName("image_url") @SerializedName("imageUrl") val imageUrl: String,
    @SerialName("room_name") @SerializedName("roomName") val roomName: String,
    @SerialName("price_per_night") @SerializedName("pricePerNight") val pricePerNight: Double,
    @SerialName("capacity") @SerializedName("capacity") val capacity: Int,
    @SerialName("available_rooms") @SerializedName("availableRooms") val availableRooms: Int
)

@Serializable
data class CrearAtraccionRequest(
    @SerialName("title") @SerializedName("title") val title: String,
    @SerialName("city") @SerializedName("city") val city: String,
    @SerialName("category") @SerializedName("category") val category: String,
    @SerialName("duration") @SerializedName("duration") val duration: String,
    @SerialName("price_per_person") @SerializedName("pricePerPerson") val pricePerPerson: Double,
    @SerialName("description") @SerializedName("description") val description: String,
    @SerialName("image_url") @SerializedName("imageUrl") val imageUrl: String,
    @SerialName("minibus_capacity") @SerializedName("minibusCapacity") val minibusCapacity: Int
)

@Serializable
data class AtraccionReservaRequest(
    @SerialName("attraction_id") @SerializedName("attractionId") val attractionId: String,
    @SerialName("tickets_count") @SerializedName("ticketsCount") val ticketsCount: Int,
    @SerialName("lead_guest_name") @SerializedName("leadGuestName") val leadGuestName: String,
    @SerialName("lead_guest_email") @SerializedName("leadGuestEmail") val leadGuestEmail: String,
    @SerialName("lead_guest_phone") @SerializedName("leadGuestPhone") val leadGuestPhone: String,
    @SerialName("total_price") @SerializedName("totalPrice") val totalPrice: Double,
    @SerialName("payment_method") @SerializedName("paymentMethod") val paymentMethod: String
)