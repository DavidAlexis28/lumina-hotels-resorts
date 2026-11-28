package com.lumina.luminahotels.data.repository

import com.lumina.luminahotels.data.remote.LuminaApiService
import com.lumina.luminahotels.data.remote.RetrofitClient
import com.lumina.luminahotels.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Implementación en memoria / Retrofit para HotelRepository.
 * Base de datos como única fuente de verdad (estrictamente vacía por defecto).
 */
class HotelRepositoryImpl(
    private val api: LuminaApiService = RetrofitClient.api
) : HotelRepository {

    private val _modoOfflineActivo = MutableStateFlow(false)
    override val modoOfflineActivo: StateFlow<Boolean> = _modoOfflineActivo.asStateFlow()

    // Datos B2C en memoria (estrictamente vacíos, Supabase como única fuente de verdad)
    private val hotelsList = mutableListOf<Hotel>()
    private val attractionsList = mutableListOf<TouristAttraction>()
    private val customerHotelBookings = mutableListOf<CustomerHotelBooking>()
    private val customerAttractionBookings = mutableListOf<CustomerAttractionBooking>()

    // Catálogo base de habitaciones para sincronización técnica
    private val localHabitaciones = mutableListOf<HabitacionDto>()
    private val localReservas = mutableListOf<ReservaDetalleDto>()

    override suspend fun uploadCatalogImage(bytes: ByteArray, path: String): Result<String> = Result.success("")
    override suspend fun deleteCatalogImage(path: String): Result<Unit> = Result.success(Unit)
    override suspend fun createPaymentIntent(amount: Long, currency: String): Result<String> = Result.success("pi_mock_${System.currentTimeMillis()}_secret_mock")
    override fun observeCatalogChanges(): Flow<Unit> = emptyFlow()

    override suspend fun sincronizarConSupabase(): Result<Unit> = Result.success(Unit)

    override suspend fun getPerfil(correo: String): Result<UserProfile?> = Result.success(null)

    // =========================================================================
    // MÉTODOS B2C (CUSTOMER-FACING)
    // =========================================================================


    override suspend fun getHotels(): Result<List<Hotel>> {
        try {
            val response = api.getHoteles()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                _modoOfflineActivo.value = false
                val remoteList = response.body()!!
                remoteList.forEach { remote ->
                    val idx = hotelsList.indexOfFirst { it.id == remote.id }
                    if (idx != -1) {
                        hotelsList[idx] = remote
                    } else {
                        hotelsList.add(remote)
                    }
                }
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(hotelsList.toList())
    }

    override suspend fun getHotelById(hotelId: String): Result<Hotel?> {
        val hotel = hotelsList.find { it.id == hotelId }
        return Result.success(hotel)
    }

    override suspend fun getAttractions(): Result<List<TouristAttraction>> {
        try {
            val response = api.getAtracciones()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                _modoOfflineActivo.value = false
                val remoteList = response.body()!!
                remoteList.forEach { remote ->
                    val idx = attractionsList.indexOfFirst { it.id == remote.id }
                    if (idx != -1) {
                        attractionsList[idx] = remote
                    } else {
                        attractionsList.add(remote)
                    }
                }
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(attractionsList.toList())
    }

    override suspend fun getCustomerHotelBookings(userEmail: String?): Result<List<CustomerHotelBooking>> {
        val filtered = if (userEmail.isNullOrBlank()) {
            emptyList()
        } else {
            customerHotelBookings.filter { it.guestEmail.trim().equals(userEmail.trim(), ignoreCase = true) }
        }
        return Result.success(filtered)
    }

    override suspend fun getCustomerAttractionBookings(userEmail: String?): Result<List<CustomerAttractionBooking>> {
        val filtered = if (userEmail.isNullOrBlank()) {
            emptyList()
        } else {
            customerAttractionBookings.filter { it.leadGuestEmail.trim().equals(userEmail.trim(), ignoreCase = true) }
        }
        return Result.success(filtered)
    }

    override suspend fun bookHotel(booking: CustomerHotelBooking): Result<CustomerHotelBooking> {
        var finalBooking = booking

        // Decrementar cuartos disponibles localmente
        val hotelIndex = hotelsList.indexOfFirst { it.id == booking.hotelId }
        if (hotelIndex != -1) {
            val hotel = hotelsList[hotelIndex]
            val updatedRooms = hotel.rooms.map { room ->
                if (room.name == booking.roomName && room.availableRooms > 0) {
                    room.copy(availableRooms = room.availableRooms - 1)
                } else room
            }
            hotelsList[hotelIndex] = hotel.copy(rooms = updatedRooms)
        }

        // Intentar registrar en backend REST si está activo
        try {
            val names = booking.guestFullName.trim().split(" ")
            val nombres = names.firstOrNull() ?: booking.guestFullName
            val apellidos = if (names.size > 1) names.drop(1).joinToString(" ") else "Cliente"

            val hotel = hotelsList.find { it.id == booking.hotelId }
            val room = hotel?.rooms?.find { it.name == booking.roomName }
            val habitacionId = room?.backendHabitacionId ?: 1

            val request = CrearReservaRequest(
                nombres = nombres,
                apellidos = apellidos,
                documentoIdentidad = "DOC-${(100000..999999).random()}",
                correo = booking.guestEmail,
                telefono = booking.guestPhone,
                habitacionId = habitacionId,
                fechaEntrada = booking.checkInDate,
                fechaSalida = booking.checkOutDate,
                huespedes = booking.guestsCount,
                estado = "CONFIRMADA"
            )

            val response = api.crearReserva(request)
            if (response.isSuccessful && response.body() != null) {
                _modoOfflineActivo.value = false
                val backendRes = response.body()!!
                finalBooking = booking.copy(backendReservaId = backendRes.reservaId)
            } else {
                _modoOfflineActivo.value = true
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }

        customerHotelBookings.add(0, finalBooking)
        return Result.success(finalBooking)
    }

    override suspend fun bookAttraction(booking: CustomerAttractionBooking): Result<CustomerAttractionBooking> {
        // Decrementar lugares disponibles de microbús localmente
        val attrIndex = attractionsList.indexOfFirst { it.id == booking.attractionId }
        if (attrIndex != -1) {
            val attr = attractionsList[attrIndex]
            val nuevosSpots = (attr.availableSpots - booking.ticketsCount).coerceAtLeast(0)
            attractionsList[attrIndex] = attr.copy(availableSpots = nuevosSpots)
        }

        // Sincronizar con backend si está disponible
        try {
            val req = AtraccionReservaRequest(
                attractionId = booking.attractionId,
                ticketsCount = booking.ticketsCount,
                leadGuestName = booking.leadGuestName,
                leadGuestEmail = booking.leadGuestEmail,
                leadGuestPhone = booking.leadGuestPhone,
                totalPrice = booking.totalPrice,
                paymentMethod = booking.paymentMethod
            )
            val res = api.reservarAtraccion(req)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }

        customerAttractionBookings.add(0, booking)
        return Result.success(booking)
    }

    override suspend fun cancelHotelBooking(bookingReference: String): Result<Unit> {
        val index = customerHotelBookings.indexOfFirst { it.bookingReference == bookingReference }
        if (index != -1) {
            val current = customerHotelBookings[index]
            customerHotelBookings[index] = current.copy(status = "CANCELADA")

            // Restaurar cuarto disponible
            val hotelIndex = hotelsList.indexOfFirst { it.id == current.hotelId }
            if (hotelIndex != -1) {
                val hotel = hotelsList[hotelIndex]
                val updatedRooms = hotel.rooms.map { room ->
                    if (room.name == current.roomName && room.availableRooms < room.totalRooms) {
                        room.copy(availableRooms = room.availableRooms + 1)
                    } else room
                }
                hotelsList[hotelIndex] = hotel.copy(rooms = updatedRooms)
            }

            // Sincronizar cancelación con backend si tenía ID
            current.backendReservaId?.let { resId ->
                try {
                    api.cancelarReserva(resId)
                } catch (_: Exception) {}
            }
            return Result.success(Unit)
        }
        return Result.failure(Exception("Reserva no encontrada"))
    }

    override suspend fun cancelAttractionBooking(ticketReference: String): Result<Unit> {
        val index = customerAttractionBookings.indexOfFirst { it.ticketReference == ticketReference }
        if (index != -1) {
            val current = customerAttractionBookings[index]
            customerAttractionBookings[index] = current.copy(status = "CANCELADA")

            // Restaurar spots de microbús
            val attrIndex = attractionsList.indexOfFirst { it.id == current.attractionId }
            if (attrIndex != -1) {
                val attr = attractionsList[attrIndex]
                val nuevosSpots = (attr.availableSpots + current.ticketsCount).coerceAtMost(attr.minibusCapacity)
                attractionsList[attrIndex] = attr.copy(availableSpots = nuevosSpots)
            }
            return Result.success(Unit)
        }
        return Result.failure(Exception("Entrada no encontrada"))
    }

    override suspend fun actualizarPerfil(profile: UserProfile): Result<UserProfile> {
        try {
            val req = ClientePerfilRequest(
                fullName = profile.fullName,
                email = profile.email,
                dialCode = profile.dialCode,
                phoneNumber = profile.phoneNumber,
                gender = profile.gender,
                birthDate = profile.birthDate,
                country = profile.country,
                address = profile.address,
                passportNumber = profile.passportNumber,
                passportCountry = profile.passportCountry,
                passportExpiryDate = profile.passportExpiryDate
            )
            val res = api.actualizarPerfil(req)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(profile)
    }

    override suspend fun crearHotel(hotel: Hotel, room: HotelRoom): Result<Hotel> {
        val fullHotel = if (hotel.rooms.isEmpty()) hotel.copy(rooms = listOf(room)) else hotel
        hotelsList.add(0, fullHotel)

        try {
            val req = CrearHotelRequest(
                name = hotel.name,
                city = hotel.city,
                address = hotel.address,
                starRating = hotel.starRating,
                description = hotel.description,
                amenities = hotel.amenities,
                imageUrl = hotel.imageUrl,
                roomName = room.name,
                pricePerNight = room.pricePerNight,
                capacity = room.capacity,
                availableRooms = room.availableRooms
            )
            val res = api.crearHotel(req)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(fullHotel)
    }

    override suspend fun updateHotel(hotelId: String, hotel: Hotel, room: HotelRoom): Result<Hotel> {
        val idx = hotelsList.indexOfFirst { it.id == hotelId }
        val fullHotel = if (hotel.rooms.isEmpty()) hotel.copy(rooms = listOf(room)) else hotel
        if (idx != -1) {
            hotelsList[idx] = fullHotel
        } else {
            hotelsList.add(0, fullHotel)
        }

        try {
            val req = CrearHotelRequest(
                name = hotel.name,
                city = hotel.city,
                address = hotel.address,
                starRating = hotel.starRating,
                description = hotel.description,
                amenities = hotel.amenities,
                imageUrl = hotel.imageUrl,
                roomName = room.name,
                pricePerNight = room.pricePerNight,
                capacity = room.capacity,
                availableRooms = room.availableRooms
            )
            val res = api.actualizarHotel(hotelId, req)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(fullHotel)
    }

    override suspend fun deleteHotel(hotelId: String): Result<Unit> {
        hotelsList.removeIf { it.id == hotelId }
        try {
            val res = api.eliminarHotel(hotelId)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(Unit)
    }

    override suspend fun crearAtraccion(attraction: TouristAttraction): Result<TouristAttraction> {
        attractionsList.add(0, attraction)

        try {
            val req = CrearAtraccionRequest(
                title = attraction.title,
                city = attraction.city,
                category = attraction.category,
                duration = attraction.duration,
                pricePerPerson = attraction.pricePerPerson,
                description = attraction.description,
                imageUrl = attraction.imageUrl,
                minibusCapacity = attraction.minibusCapacity
            )
            val res = api.crearAtraccion(req)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(attraction)
    }

    override suspend fun updateAttraction(attrId: String, attraction: TouristAttraction): Result<TouristAttraction> {
        val idx = attractionsList.indexOfFirst { it.id == attrId }
        if (idx != -1) {
            attractionsList[idx] = attraction
        } else {
            attractionsList.add(0, attraction)
        }

        try {
            val req = CrearAtraccionRequest(
                title = attraction.title,
                city = attraction.city,
                category = attraction.category,
                duration = attraction.duration,
                pricePerPerson = attraction.pricePerPerson,
                description = attraction.description,
                imageUrl = attraction.imageUrl,
                minibusCapacity = attraction.minibusCapacity
            )
            val res = api.actualizarAtraccion(attrId, req)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(attraction)
    }

    override suspend fun deleteAttraction(attrId: String): Result<Unit> {
        attractionsList.removeIf { it.id == attrId }
        try {
            val res = api.eliminarAtraccion(attrId)
            if (res.isSuccessful) {
                _modoOfflineActivo.value = false
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
        }
        return Result.success(Unit)
    }

    // =========================================================================
    // MÉTODOS TÉCNICOS / BACKEND
    // =========================================================================

    override suspend fun getHabitacionesDisponibles(
        fechaEntrada: String,
        fechaSalida: String,
        huespedes: Int
    ): Result<List<HabitacionDto>> {
        return try {
            val response = api.getHabitacionesDisponibles(fechaEntrada, fechaSalida, huespedes)
            if (response.isSuccessful && response.body() != null) {
                _modoOfflineActivo.value = false
                Result.success(response.body()!!)
            } else {
                val errorMsg = RetrofitClient.parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
            val entrada = try { LocalDate.parse(fechaEntrada) } catch (_: Exception) { null }
            val salida = try { LocalDate.parse(fechaSalida) } catch (_: Exception) { null }

            if (entrada == null || salida == null) {
                return Result.failure(Exception("Formato de fechas inválido"))
            }

            val disponibles = localHabitaciones.filter { h ->
                h.estado == "DISPONIBLE" &&
                h.capacidad >= huespedes &&
                localReservas.none { r ->
                    r.habitacionId == h.habitacionId &&
                    (r.estadoReserva == "PENDIENTE" || r.estadoReserva == "CONFIRMADA") &&
                    LocalDate.parse(r.fechaEntrada).isBefore(salida) &&
                    LocalDate.parse(r.fechaSalida).isAfter(entrada)
                }
            }.sortedBy { it.precioPorNoche }

            Result.success(disponibles)
        }
    }

    override suspend fun getTodasHabitaciones(): Result<List<HabitacionDto>> {
        return try {
            val response = api.getTodasHabitaciones()
            if (response.isSuccessful && response.body() != null) {
                _modoOfflineActivo.value = false
                val remoteList = response.body()!!
                localHabitaciones.clear()
                localHabitaciones.addAll(remoteList)
                Result.success(remoteList)
            } else {
                val errorMsg = RetrofitClient.parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (_: Exception) {
            _modoOfflineActivo.value = true
            Result.success(localHabitaciones.toList())
        }
    }

    override suspend fun crearHabitacion(
        request: HabitacionRequest,
        nombreTipo: String,
        capacidad: Int,
        precioPorNoche: Double
    ): Result<HabitacionDto> {
        val nuevoId = (localHabitaciones.maxOfOrNull { it.habitacionId } ?: 0) + 1
        val localDto = HabitacionDto(
            habitacionId = nuevoId,
            numero = request.numero,
            piso = request.piso,
            estado = request.estado,
            tipoHabitacionId = request.tipoHabitacionId,
            nombreTipo = nombreTipo,
            capacidad = capacidad,
            precioPorNoche = precioPorNoche
        )
        localHabitaciones.add(localDto)
        return Result.success(localDto)
    }

    override suspend fun actualizarHabitacion(
        id: Int,
        request: HabitacionRequest,
        nombreTipo: String,
        capacidad: Int,
        precioPorNoche: Double
    ): Result<HabitacionDto> {
        val index = localHabitaciones.indexOfFirst { it.habitacionId == id }
        val actualizada = HabitacionDto(
            habitacionId = id,
            numero = request.numero,
            piso = request.piso,
            estado = request.estado,
            tipoHabitacionId = request.tipoHabitacionId,
            nombreTipo = nombreTipo,
            capacidad = capacidad,
            precioPorNoche = precioPorNoche
        )
        if (index != -1) localHabitaciones[index] = actualizada
        return Result.success(actualizada)
    }

    override suspend fun eliminarHabitacion(id: Int): Result<Unit> {
        localHabitaciones.removeAll { it.habitacionId == id }
        return Result.success(Unit)
    }

    override suspend fun getReservas(): Result<List<ReservaDetalleDto>> {
        return Result.success(localReservas.toList())
    }

    override suspend fun crearReserva(
        request: CrearReservaRequest,
        habitacion: HabitacionDto
    ): Result<ReservaDetalleDto> {
        val entrada = LocalDate.parse(request.fechaEntrada)
        val salida = LocalDate.parse(request.fechaSalida)
        val noches = ChronoUnit.DAYS.between(entrada, salida).toInt()
        val nuevoId = (localReservas.maxOfOrNull { it.reservaId } ?: 0) + 1
        val localDto = ReservaDetalleDto(
            reservaId = nuevoId,
            clienteId = nuevoId,
            nombreCompletoCliente = "${request.nombres} ${request.apellidos}",
            documentoIdentidad = request.documentoIdentidad,
            correoCliente = request.correo,
            habitacionId = habitacion.habitacionId,
            numeroHabitacion = habitacion.numero,
            piso = habitacion.piso,
            tipoHabitacion = habitacion.nombreTipo,
            precioPorNoche = habitacion.precioPorNoche,
            fechaEntrada = request.fechaEntrada,
            fechaSalida = request.fechaSalida,
            cantidadNoches = noches,
            huespedes = request.huespedes,
            estadoReserva = request.estado,
            totalEstimadoCobro = noches * habitacion.precioPorNoche
        )
        localReservas.add(0, localDto)
        return Result.success(localDto)
    }

    override suspend fun actualizarReserva(
        id: Int,
        request: ActualizarReservaRequest,
        habitacion: HabitacionDto?
    ): Result<ReservaDetalleDto> {
        val idx = localReservas.indexOfFirst { it.reservaId == id }
        if (idx != -1) {
            val anterior = localReservas[idx]
            val entrada = LocalDate.parse(request.fechaEntrada)
            val salida = LocalDate.parse(request.fechaSalida)
            val noches = ChronoUnit.DAYS.between(entrada, salida).toInt()
            val localActualizada = anterior.copy(
                fechaEntrada = request.fechaEntrada,
                fechaSalida = request.fechaSalida,
                cantidadNoches = noches,
                huespedes = request.huespedes,
                estadoReserva = request.estado,
                totalEstimadoCobro = noches * anterior.precioPorNoche
            )
            localReservas[idx] = localActualizada
            return Result.success(localActualizada)
        }
        return Result.failure(Exception("Reserva no encontrada."))
    }

    override suspend fun cancelarReserva(id: Int): Result<ReservaDetalleDto> {
        val idx = localReservas.indexOfFirst { it.reservaId == id }
        if (idx != -1) {
            val cancelada = localReservas[idx].copy(estadoReserva = "CANCELADA")
            localReservas[idx] = cancelada
            return Result.success(cancelada)
        }
        return Result.failure(Exception("Reserva no encontrada."))
    }

    override suspend fun eliminarReserva(id: Int): Result<Unit> {
        localReservas.removeAll { it.reservaId == id }
        return Result.success(Unit)
    }
}
