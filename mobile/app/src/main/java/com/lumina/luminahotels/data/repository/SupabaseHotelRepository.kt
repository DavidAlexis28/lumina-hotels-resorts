package com.lumina.luminahotels.data.repository

import android.util.Log
import com.lumina.luminahotels.data.remote.SupabaseClient
import com.lumina.luminahotels.model.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SupabaseHotelRepository : HotelRepository {

    private val _modoOfflineActivo = MutableStateFlow(false)
    override val modoOfflineActivo: StateFlow<Boolean> = _modoOfflineActivo.asStateFlow()

    private val db get() = SupabaseClient.client

    override suspend fun uploadCatalogImage(bytes: ByteArray, path: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bucket = db.storage.from("catalog-images")
            bucket.upload(path, bytes) {
                upsert = true
            }
            bucket.publicUrl(path)
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error subiendo imagen $path a Supabase Storage", e)
        }
    }

    override suspend fun deleteCatalogImage(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.storage.from("catalog-images").delete(listOf(path))
            Unit
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error eliminando imagen $path de Supabase Storage", e)
        }
    }

    override suspend fun createPaymentIntent(amount: Long, currency: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val client = okhttp3.OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val jsonBody = """{"amount": $amount, "currency": "$currency"}"""
            val requestBody = okhttp3.RequestBody.create(mediaType, jsonBody)
            val request = okhttp3.Request.Builder()
                .url("https://ugcyptfjwsrdcjtymasr.supabase.co/functions/v1/create-payment-intent")
                .header("Authorization", "Bearer sb_publishable_EZCAqI7-Z04wQbLpHrgZ6w_LnvMdUvm")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: throw Exception("Respuesta vacía del servidor de pagos")
            if (!response.isSuccessful) throw Exception("Error al crear intención de pago: $bodyString")
            val json = com.google.gson.Gson().fromJson(bodyString, com.google.gson.JsonObject::class.java)
            json.get("client_secret")?.asString ?: json.get("clientSecret")?.asString ?: throw Exception("client_secret no encontrado en la respuesta")
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error creando payment intent en Supabase Edge Function", e)
        }
    }

    override fun observeCatalogChanges(): Flow<Unit> = channelFlow {
        val channel = db.realtime.channel("catalog-realtime")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public")
        val subJob = launch(Dispatchers.IO) {
            try {
                channel.subscribe(blockUntilSubscribed = true)
                changeFlow.collect {
                    trySend(Unit)
                }
            } catch (e: Exception) {
                Log.e("SupabaseRepo", "Error en realtime channel", e)
            }
        }
        awaitClose {
            subJob.cancel()
            launch(Dispatchers.IO) {
                try {
                    channel.unsubscribe()
                } catch (_: Exception) {}
            }
        }
    }

    override suspend fun sincronizarConSupabase(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("hoteles").select(Columns.raw("id"))
            _modoOfflineActivo.value = false
            Unit
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error al sincronizar con Supabase", e)
            _modoOfflineActivo.value = true
        }
    }

    // =========================================================================
    // HOTELES & HABITACIONES (B2C + ADMIN)
    // =========================================================================

    override suspend fun getHotels(): Result<List<Hotel>> = withContext(Dispatchers.IO) {
        runCatching {
            val hoteles = db.from("hoteles")
                .select(Columns.raw("*, hotel_rooms(*)"))
                .decodeList<Hotel>()
            _modoOfflineActivo.value = false
            hoteles
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error cargando hoteles", e)
            _modoOfflineActivo.value = true
        }
    }

    override suspend fun getHotelById(hotelId: String): Result<Hotel?> = withContext(Dispatchers.IO) {
        runCatching {
            val list = db.from("hoteles")
                .select(Columns.raw("*, hotel_rooms(*)")) {
                    filter { eq("id", hotelId) }
                }
                .decodeList<Hotel>()
            _modoOfflineActivo.value = false
            list.firstOrNull()
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error cargando hotel $hotelId", e)
            _modoOfflineActivo.value = true
        }
    }

    override suspend fun crearHotel(hotel: Hotel, room: HotelRoom): Result<Hotel> = withContext(Dispatchers.IO) {
        runCatching {
            val hotelPayload = buildJsonObject {
                put("id", hotel.id)
                put("name", hotel.name)
                put("city", hotel.city)
                put("address", hotel.address)
                put("star_rating", hotel.starRating)
                put("review_score", hotel.reviewScore)
                put("review_count", hotel.reviewCount)
                put("review_label", hotel.reviewLabel)
                put("description", hotel.description)
                put("price_per_night_starting", hotel.pricePerNightStarting)
                hotel.featuredTag?.let { put("featured_tag", it) }
                put("image_url", hotel.coverImage)
                putJsonArray("image_urls") {
                    hotel.galleryImages.forEach { add(JsonPrimitive(it)) }
                }
                put("has_genius_discount", hotel.hasGeniusDiscount)
                putJsonArray("amenities") {
                    hotel.amenities.forEach { add(JsonPrimitive(it)) }
                }
            }
            db.from("hoteles").insert(hotelPayload)

            val roomPayload = buildJsonObject {
                put("room_id", room.roomId)
                put("hotel_id", hotel.id)
                put("name", room.name)
                put("bed_configuration", room.bedConfiguration)
                put("capacity", room.capacity)
                put("size_m2", room.sizeM2)
                put("price_per_night", room.pricePerNight)
                put("image_url", room.imageUrl)
                put("free_cancellation", room.freeCancellation)
                put("breakfast_included", room.breakfastIncluded)
                put("available_rooms", room.availableRooms)
                put("total_rooms", room.totalRooms)
                putJsonArray("amenities") {
                    room.amenities.forEach { add(JsonPrimitive(it)) }
                }
            }
            db.from("hotel_rooms").insert(roomPayload)

            _modoOfflineActivo.value = false
            hotel.copy(rooms = listOf(room.copy(hotelId = hotel.id)))
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error creando hotel en Supabase", e)
            _modoOfflineActivo.value = true
        }
    }

    override suspend fun updateHotel(hotelId: String, hotel: Hotel, room: HotelRoom): Result<Hotel> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("hoteles").update({
                set("name", hotel.name)
                set("city", hotel.city)
                set("address", hotel.address)
                set("star_rating", hotel.starRating)
                set("description", hotel.description)
                set("amenities", hotel.amenities)
                set("price_per_night_starting", hotel.pricePerNightStarting)
                set("image_url", hotel.coverImage)
                set("image_urls", hotel.galleryImages)
            }) {
                filter { eq("id", hotelId) }
            }

            db.from("hotel_rooms").update({
                set("name", room.name)
                set("capacity", room.capacity)
                set("price_per_night", room.pricePerNight)
                set("available_rooms", room.availableRooms)
                set("total_rooms", room.totalRooms)
                set("image_url", room.imageUrl)
            }) {
                filter { eq("hotel_id", hotelId) }
            }

            _modoOfflineActivo.value = false
            hotel.copy(id = hotelId, rooms = listOf(room))
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error actualizando hotel $hotelId", e)
            _modoOfflineActivo.value = true
        }
    }

    override suspend fun deleteHotel(hotelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("hotel_rooms").delete {
                filter { eq("hotel_id", hotelId) }
            }
            db.from("hoteles").delete {
                filter { eq("id", hotelId) }
            }
            _modoOfflineActivo.value = false
            Unit
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error eliminando hotel $hotelId", e)
            _modoOfflineActivo.value = true
        }
    }

    // =========================================================================
    // ATRACCIONES TURÍSTICAS
    // =========================================================================

    override suspend fun getAttractions(): Result<List<TouristAttraction>> = withContext(Dispatchers.IO) {
        runCatching {
            val list = db.from("atracciones")
                .select()
                .decodeList<TouristAttraction>()
            _modoOfflineActivo.value = false
            list
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error cargando atracciones", e)
            _modoOfflineActivo.value = true
        }
    }

    override suspend fun crearAtraccion(attraction: TouristAttraction): Result<TouristAttraction> = withContext(Dispatchers.IO) {
        runCatching {
            val attrPayload = buildJsonObject {
                put("id", attraction.id)
                put("title", attraction.title)
                put("city", attraction.city)
                put("category", attraction.category)
                put("rating", attraction.rating)
                put("review_count", attraction.reviewCount)
                put("duration", attraction.duration)
                put("price_per_person", attraction.pricePerPerson)
                put("description", attraction.description)
                put("image_url", attraction.coverImage)
                putJsonArray("image_urls") {
                    attraction.galleryImages.forEach { add(JsonPrimitive(it)) }
                }
                put("cancellation_policy", attraction.cancellationPolicy)
                put("minibus_capacity", attraction.minibusCapacity)
                put("available_spots", attraction.availableSpots)
                putJsonArray("highlights") {
                    attraction.highlights.forEach { add(JsonPrimitive(it)) }
                }
                putJsonArray("inclusions") {
                    attraction.inclusions.forEach { add(JsonPrimitive(it)) }
                }
            }
            db.from("atracciones").insert(attrPayload)
            _modoOfflineActivo.value = false
            attraction
        }.onFailure { e ->
            Log.e("SupabaseRepo", "Error creando atracción", e)
            _modoOfflineActivo.value = true
        }
    }

    override suspend fun updateAttraction(attrId: String, attraction: TouristAttraction): Result<TouristAttraction> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("atracciones").update({
                set("title", attraction.title)
                set("city", attraction.city)
                set("category", attraction.category)
                set("duration", attraction.duration)
                set("price_per_person", attraction.pricePerPerson)
                set("description", attraction.description)
                set("image_url", attraction.coverImage)
                set("image_urls", attraction.galleryImages)
                set("minibus_capacity", attraction.minibusCapacity)
                set("available_spots", attraction.availableSpots)
            }) {
                filter { eq("id", attrId) }
            }
            _modoOfflineActivo.value = false
            attraction.copy(id = attrId)
        }
    }

    override suspend fun deleteAttraction(attrId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("atracciones").delete {
                filter { eq("id", attrId) }
            }
            _modoOfflineActivo.value = false
            Unit
        }
    }

    // =========================================================================
    // RESERVAS B2C (HOTELES Y ATRACCIONES DEL CLIENTE)
    // =========================================================================

    override suspend fun getCustomerHotelBookings(userEmail: String?): Result<List<CustomerHotelBooking>> = withContext(Dispatchers.IO) {
        runCatching {
            val email = userEmail?.trim()?.lowercase()
            if (email.isNullOrBlank()) {
                return@runCatching emptyList()
            }
            db.from("customer_hotel_bookings")
                .select {
                    filter {
                        ilike("guest_email", email)
                    }
                }
                .decodeList<CustomerHotelBooking>()
        }
    }

    override suspend fun getCustomerAttractionBookings(userEmail: String?): Result<List<CustomerAttractionBooking>> = withContext(Dispatchers.IO) {
        runCatching {
            val email = userEmail?.trim()?.lowercase()
            if (email.isNullOrBlank()) {
                return@runCatching emptyList()
            }
            db.from("customer_attraction_bookings")
                .select {
                    filter {
                        ilike("lead_guest_email", email)
                    }
                }
                .decodeList<CustomerAttractionBooking>()
        }
    }

    override suspend fun bookHotel(booking: CustomerHotelBooking): Result<CustomerHotelBooking> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject {
                put("booking_reference", booking.bookingReference)
                put("hotel_id", booking.hotelId)
                put("hotel_name", booking.hotelName)
                put("hotel_city", booking.hotelCity)
                put("room_name", booking.roomName)
                put("guest_full_name", booking.guestFullName)
                put("guest_email", booking.guestEmail)
                put("guest_phone", booking.guestPhone)
                put("check_in_date", booking.checkInDate)
                put("check_out_date", booking.checkOutDate)
                put("nights", booking.nights)
                put("guests_count", booking.guestsCount)
                put("room_subtotal", booking.roomSubtotal)
                put("taxes_and_fees", booking.taxesAndFees)
                put("total_price", booking.totalPrice)
                put("payment_method", booking.paymentMethod)
                if (booking.specialRequests != null) {
                    put("special_requests", booking.specialRequests)
                }
                put("status", booking.status)
                put("created_at", booking.createdAt)
            }
            db.from("customer_hotel_bookings").insert(payload)
            booking
        }
    }

    override suspend fun bookAttraction(booking: CustomerAttractionBooking): Result<CustomerAttractionBooking> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("customer_attraction_bookings").insert(booking)
            booking
        }
    }

    override suspend fun cancelHotelBooking(bookingReference: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("customer_hotel_bookings").update({
                set("status", "CANCELADA")
            }) {
                filter { eq("booking_reference", bookingReference) }
            }
            Unit
        }
    }

    override suspend fun cancelAttractionBooking(ticketReference: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("customer_attraction_bookings").update({
                set("status", "CANCELADA")
            }) {
                filter { eq("ticket_reference", ticketReference) }
            }
            Unit
        }
    }

    // =========================================================================
    // PERFIL DE CLIENTE
    // =========================================================================

    override suspend fun getPerfil(correo: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("clientes")
                .select { filter { eq("correo", correo) } }
                .decodeList<UserProfile>()
                .firstOrNull()
        }
    }

    override suspend fun actualizarPerfil(profile: UserProfile): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("clientes").upsert(profile) {
                onConflict = "correo"
            }
            profile
        }
    }

    // =========================================================================
    // INVENTARIO FÍSICO & RPC (sp_habitaciones_disponibles)
    // =========================================================================

    override suspend fun getHabitacionesDisponibles(
        fechaEntrada: String,
        fechaSalida: String,
        huespedes: Int
    ): Result<List<HabitacionDto>> = withContext(Dispatchers.IO) {
        runCatching {
            db.postgrest.rpc(
                function = "sp_habitaciones_disponibles",
                parameters = buildJsonObject {
                    put("p_fecha_entrada", fechaEntrada)
                    put("p_fecha_salida", fechaSalida)
                    put("p_huespedes", huespedes)
                }
            ).decodeList<HabitacionDto>()
        }
    }

    override suspend fun getTodasHabitaciones(): Result<List<HabitacionDto>> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("vw_habitaciones_detalle")
                .select()
                .decodeList<HabitacionDto>()
        }
    }

    override suspend fun crearHabitacion(
        request: HabitacionRequest,
        nombreTipo: String,
        capacidad: Int,
        precioPorNoche: Double
    ): Result<HabitacionDto> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("habitaciones").insert(request)
            getTodasHabitaciones().getOrThrow().first { it.numero == request.numero }
        }
    }

    override suspend fun actualizarHabitacion(
        id: Int,
        request: HabitacionRequest,
        nombreTipo: String,
        capacidad: Int,
        precioPorNoche: Double
    ): Result<HabitacionDto> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("habitaciones").update({
                set("tipo_habitacion_id", request.tipoHabitacionId)
                set("numero", request.numero)
                set("piso", request.piso)
                set("estado", request.estado)
            }) {
                filter { eq("habitacion_id", id) }
            }
            HabitacionDto(
                habitacionId = id,
                numero = request.numero,
                piso = request.piso,
                estado = request.estado,
                tipoHabitacionId = request.tipoHabitacionId,
                nombreTipo = nombreTipo,
                capacidad = capacidad,
                precioPorNoche = precioPorNoche
            )
        }
    }

    override suspend fun eliminarHabitacion(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("habitaciones").delete {
                filter { eq("habitacion_id", id) }
            }
            Unit
        }
    }

    override suspend fun getReservas(): Result<List<ReservaDetalleDto>> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("reservas")
                .select()
                .decodeList<ReservaDetalleDto>()
        }
    }

    override suspend fun crearReserva(
        request: CrearReservaRequest,
        habitacion: HabitacionDto
    ): Result<ReservaDetalleDto> = withContext(Dispatchers.IO) {
        runCatching {
            val clienteInsertado = db.from("clientes").upsert(
                buildJsonObject {
                    put("nombres", request.nombres)
                    put("apellidos", request.apellidos)
                    put("correo", request.correo)
                    put("telefono", request.telefono)
                }
            ) {
                onConflict = "correo"
                select()
            }.decodeSingle<ClientePerfilDto>()

            val reservaPayload = buildJsonObject {
                put("cliente_id", clienteInsertado.clienteId)
                put("habitacion_id", request.habitacionId)
                put("fecha_entrada", request.fechaEntrada)
                put("fecha_salida", request.fechaSalida)
                put("huespedes", request.huespedes)
                put("estado_reserva", request.estado)
                put("total_estimado_cobro", habitacion.precioPorNoche)
            }
            db.from("reservas").insert(reservaPayload)

            ReservaDetalleDto(
                reservaId = 0,
                clienteId = clienteInsertado.clienteId,
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
                cantidadNoches = 1,
                huespedes = request.huespedes,
                estadoReserva = request.estado,
                totalEstimadoCobro = habitacion.precioPorNoche
            )
        }
    }

    override suspend fun actualizarReserva(
        id: Int,
        request: ActualizarReservaRequest,
        habitacion: HabitacionDto?
    ): Result<ReservaDetalleDto> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("reservas").update({
                set("fecha_entrada", request.fechaEntrada)
                set("fecha_salida", request.fechaSalida)
                set("huespedes", request.huespedes)
                set("estado_reserva", request.estado)
            }) {
                filter { eq("reserva_id", id) }
            }
            getReservas().getOrThrow().first { it.reservaId == id }
        }
    }

    override suspend fun cancelarReserva(id: Int): Result<ReservaDetalleDto> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("reservas").update({
                set("estado_reserva", "CANCELADA")
            }) {
                filter { eq("reserva_id", id) }
            }
            getReservas().getOrThrow().first { it.reservaId == id }
        }
    }

    override suspend fun eliminarReserva(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.from("reservas").delete {
                filter { eq("reserva_id", id) }
            }
            Unit
        }
    }
}