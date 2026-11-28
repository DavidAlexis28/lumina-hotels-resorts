package com.lumina.luminahotels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.luminahotels.data.mock.MockDataProvider
import com.lumina.luminahotels.data.remote.PaymentGatewayManager
import com.lumina.luminahotels.data.repository.HotelRepository
import com.lumina.luminahotels.data.repository.SupabaseHotelRepository
import com.lumina.luminahotels.domain.validation.FormValidator
import com.lumina.luminahotels.domain.validation.ValidationResult
import com.lumina.luminahotels.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ViewModel central para la experiencia de cliente de Lumina Hotels (Estilo Booking.com).
 * Maneja navegación reactiva, selección de fechas con DateRangePicker, catálogo de hoteles,
 * atracciones turísticas, checkout con validación en tiempo real, favoritos, perfil de usuario,
 * métodos de pago guardados e historial de transacciones.
 */
class HotelViewModel(
    private val repository: HotelRepository = SupabaseHotelRepository()
) : ViewModel() {

    // Eventos UI (Snackbars, alertas)
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    // Estado Offline del repositorio
    val modoOffline: StateFlow<Boolean> = repository.modoOfflineActivo

    // =========================================================================
    // NAVEGACIÓN SEARCH: CATEGORÍA SUPERIOR (ALOJAMIENTOS / ATRACCIONES)
    // =========================================================================
    val buscarTab = MutableStateFlow(0) // 0: Alojamientos, 1: Atracciones

    // =========================================================================
    // AUTENTICACIÓN Y SESIÓN ("MI CUENTA" / "INICIAR SESIÓN")
    // =========================================================================
    val isLoggedIn = MutableStateFlow(false)

    val userProfile = MutableStateFlow(UserProfile(fullName = "", email = "", phoneNumber = "", isGeniusMember = false, isAdmin = false))

    val pastTransactions = MutableStateFlow<List<PastTransaction>>(emptyList())

    // =========================================================================
    // FAVORITOS (HOTELES Y ATRACCIONES)
    // =========================================================================
    val favoriteHotelIds = MutableStateFlow(emptySet<String>())
    val favoriteAttractionIds = MutableStateFlow(emptySet<String>())

    // =========================================================================
    // SELECCIÓN GLOBAL DE FECHAS & HUÉSPEDES (Material 3 DateRangePicker)
    // =========================================================================
    val checkInDate = MutableStateFlow<LocalDate>(LocalDate.now().plusDays(1))
    val checkOutDate = MutableStateFlow<LocalDate>(LocalDate.now().plusDays(3))
    val guestsCount = MutableStateFlow<Int>(2)

    val stayNights: StateFlow<Int> = combine(checkInDate, checkOutDate) { start, end ->
        val diff = ChronoUnit.DAYS.between(start, end).toInt()
        if (diff > 0) diff else 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    fun actualizarRangoFechas(nuevaEntrada: LocalDate, nuevaSalida: LocalDate) {
        if (nuevaSalida.isAfter(nuevaEntrada)) {
            checkInDate.value = nuevaEntrada
            checkOutDate.value = nuevaSalida
            val noches = ChronoUnit.DAYS.between(nuevaEntrada, nuevaSalida).toInt()
            emitEvent(UiEvent.ShowSnackbar("Fechas actualizadas: $noches ${if (noches == 1) "noche" else "noches"}"))
        }
    }

    // =========================================================================
    // 1. PESTAÑA ALOJAMIENTOS / HOTELES (STAYS)
    // =========================================================================
    private val _hotelsState = MutableStateFlow<UiState<List<Hotel>>>(UiState.Loading)
    val hotelsState: StateFlow<UiState<List<Hotel>>> = _hotelsState.asStateFlow()

    private val _isRefreshingHotels = MutableStateFlow(false)
    val isRefreshingHotels: StateFlow<Boolean> = _isRefreshingHotels.asStateFlow()

    val queryHotels = MutableStateFlow("")
    val filtroCategoriaHotel = MutableStateFlow("Todos")

    // Filtro reactivo en tiempo real
    val hotelsFiltrados: StateFlow<List<Hotel>> = combine(
        _hotelsState,
        queryHotels,
        filtroCategoriaHotel
    ) { state, query, categoria ->
        val list = (state as? UiState.Success)?.data ?: emptyList()
        val q = query.trim().lowercase()

        list.filter { hotel ->
            val matchQuery = q.isEmpty() ||
                hotel.name.lowercase().contains(q) ||
                hotel.city.lowercase().contains(q) ||
                hotel.address.lowercase().contains(q)

            val matchCategory = when (categoria) {
                "5 Estrellas" -> hotel.starRating == 5
                "Frente a la Playa" -> hotel.featuredTag == "Frente a la Playa" || hotel.amenities.any { it.contains("Playa", ignoreCase = true) }
                "Con Spa" -> hotel.amenities.any { it.contains("Spa", ignoreCase = true) }
                else -> true
            }

            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navegación interna: Hotel seleccionado para ver detalle
    val selectedHotelForDetails = MutableStateFlow<Hotel?>(null)

    // Navegación interna: Habitación y Hotel seleccionados para Checkout
    val selectedRoomForCheckout = MutableStateFlow<Pair<Hotel, HotelRoom>?>(null)

    // Reserva recientemente confirmada para mostrar pantalla de éxito
    val recentHotelBookingConfirmed = MutableStateFlow<CustomerHotelBooking?>(null)

    // =========================================================================
    // 2. PESTAÑA ATRACCIONES TURÍSTICAS (ATTRACTIONS)
    // =========================================================================
    private val _attractionsState = MutableStateFlow<UiState<List<TouristAttraction>>>(UiState.Loading)
    val attractionsState: StateFlow<UiState<List<TouristAttraction>>> = _attractionsState.asStateFlow()

    private val _isRefreshingAttractions = MutableStateFlow(false)
    val isRefreshingAttractions: StateFlow<Boolean> = _isRefreshingAttractions.asStateFlow()

    val queryAttractions = MutableStateFlow("")
    val filtroCategoriaAtraccion = MutableStateFlow("Todas")

    val attractionsFiltradas: StateFlow<List<TouristAttraction>> = combine(
        _attractionsState,
        queryAttractions,
        filtroCategoriaAtraccion
    ) { state, query, categoria ->
        val list = (state as? UiState.Success)?.data ?: emptyList()
        val q = query.trim().lowercase()

        list.filter { attr ->
            val matchQuery = q.isEmpty() ||
                attr.title.lowercase().contains(q) ||
                attr.city.lowercase().contains(q) ||
                attr.description.lowercase().contains(q)

            val matchCategory = categoria == "Todas" || attr.category.equals(categoria, ignoreCase = true)
            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedAttractionForDetails = MutableStateFlow<TouristAttraction?>(null)
    val recentAttractionBookingConfirmed = MutableStateFlow<CustomerAttractionBooking?>(null)

    // =========================================================================
    // 3. PESTAÑA MIS RESERVAS (MY BOOKINGS - ITINERARIO UNIFICADO)
    // =========================================================================
    private val _customerHotelBookings = MutableStateFlow<List<CustomerHotelBooking>>(emptyList())
    val customerHotelBookings: StateFlow<List<CustomerHotelBooking>> = _customerHotelBookings.asStateFlow()

    private val _customerAttractionBookings = MutableStateFlow<List<CustomerAttractionBooking>>(emptyList())
    val customerAttractionBookings: StateFlow<List<CustomerAttractionBooking>> = _customerAttractionBookings.asStateFlow()

    val filtroTipoItinerario = MutableStateFlow("Todas")

    init {
        viewModelScope.launch {
            repository.sincronizarConSupabase()
        }
        cargarHoteles()
        cargarAtracciones()
        cargarReservasHuesped()
        iniciarEscuchaRealtime()
    }

    private fun iniciarEscuchaRealtime() {
        viewModelScope.launch {
            try {
                repository.observeCatalogChanges().collect {
                    cargarHoteles()
                    cargarAtracciones()
                }
            } catch (e: Exception) {
                android.util.Log.e("HotelViewModel", "Error al escuchar cambios en tiempo real", e)
            }
        }
    }

    suspend fun subirFotoCatalogo(bytes: ByteArray, folder: String): Result<String> {
        val fileName = "$folder/${java.util.UUID.randomUUID()}.jpg"
        return repository.uploadCatalogImage(bytes, fileName)
    }

    // =========================================================================
    // MÉTODOS DE AUTENTICACIÓN Y PERFIL
    // =========================================================================
    fun iniciarSesion(email: String, pass: String, fullName: String = "") {
        val trimmedEmail = email.trim()
        val isAdminUser = trimmedEmail.equals("admin@luminatravel.com", ignoreCase = true) || trimmedEmail.lowercase().endsWith("@luminatravel.com")

        if (isAdminUser && pass != "AdminLumina2026!") {
            emitEvent(UiEvent.ShowSnackbar("Contraseña de Administrador incorrecta. Use AdminLumina2026!", isError = true))
            return
        }

        val emailVal = FormValidator.validateEmail(trimmedEmail)
        val passVal = FormValidator.validatePassword(pass)

        if (!emailVal.isValid || !passVal.isValid) {
            val err = (emailVal as? ValidationResult.Invalid)?.errorMessage
                ?: (passVal as? ValidationResult.Invalid)?.errorMessage
                ?: "Verifique sus credenciales."
            emitEvent(UiEvent.ShowSnackbar(err, isError = true))
            return
        }

        val name = if (fullName.isNotBlank()) {
            FormValidator.sanitize(fullName)
        } else if (isAdminUser) {
            "Administrador Lumina"
        } else {
            trimmedEmail.substringBefore("@")
                .replace(".", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }

        val cleanEmail = FormValidator.sanitize(trimmedEmail)
        userProfile.value = UserProfile(
            fullName = name,
            email = cleanEmail,
            phoneNumber = "",
            isGeniusMember = true,
            gender = "Prefiero no decirlo",
            birthDate = "",
            country = "El Salvador",
            address = "",
            passportNumber = "",
            passportCountry = "El Salvador",
            passportExpiryDate = "",
            isAdmin = isAdminUser
        )
        isLoggedIn.value = true
        pastTransactions.value = MockDataProvider.defaultPastTransactions

        viewModelScope.launch {
            repository.getPerfil(cleanEmail).onSuccess { remoteProfile ->
                if (remoteProfile != null) {
                    userProfile.value = remoteProfile.copy(isAdmin = isAdminUser)
                }
            }
            cargarReservasHuesped()
        }

        if (isAdminUser) {
            emitEvent(UiEvent.ShowSnackbar("¡Bienvenido Admin, $name! Panel Admin desbloqueado."))
        } else {
            emitEvent(UiEvent.ShowSnackbar("¡Bienvenido, $name! 10% de descuento Genius activado."))
        }
    }

    fun cerrarSesion() {
        isLoggedIn.value = false
        userProfile.value = UserProfile(
            fullName = "",
            email = "",
            phoneNumber = "",
            isGeniusMember = false,
            gender = "Prefiero no decirlo",
            birthDate = "",
            country = "El Salvador",
            address = "",
            passportNumber = "",
            passportCountry = "El Salvador",
            passportExpiryDate = "",
            isAdmin = false
        )
        pastTransactions.value = emptyList()
        favoriteHotelIds.value = emptySet()
        favoriteAttractionIds.value = emptySet()
        _customerHotelBookings.value = emptyList()
        _customerAttractionBookings.value = emptyList()
        emitEvent(UiEvent.ShowSnackbar("Sesión cerrada. Puedes seguir explorando libremente."))
    }

    fun actualizarPerfil(
        fullName: String,
        email: String,
        dialCode: String,
        phoneNumber: String,
        gender: String = userProfile.value.gender,
        birthDate: String = userProfile.value.birthDate,
        country: String = userProfile.value.country,
        address: String = userProfile.value.address,
        passportNumber: String = userProfile.value.passportNumber,
        passportCountry: String = userProfile.value.passportCountry,
        passportExpiryDate: String = userProfile.value.passportExpiryDate
    ) {
        val nameVal = FormValidator.validateName(fullName, "Nombre completo")
        val emailVal = FormValidator.validateEmail(email)
        val phoneVal = if (phoneNumber.isNotBlank()) FormValidator.validatePhone(phoneNumber, isRequired = true) else ValidationResult.Valid
        val addressVal = if (address.isNotBlank()) FormValidator.validateAddress(address) else ValidationResult.Valid
        val passportVal = if (passportNumber.isNotBlank()) FormValidator.validatePassportNumber(passportNumber) else ValidationResult.Valid
        val birthVal = if (birthDate.isNotBlank()) FormValidator.validateBirthDate(birthDate) else ValidationResult.Valid
        val expiryVal = if (passportExpiryDate.isNotBlank()) FormValidator.validatePassportExpiry(passportExpiryDate) else ValidationResult.Valid

        if (!nameVal.isValid || !emailVal.isValid || !phoneVal.isValid ||
            !addressVal.isValid || !passportVal.isValid || !birthVal.isValid || !expiryVal.isValid) {
            val err = (nameVal as? ValidationResult.Invalid)?.errorMessage
                ?: (emailVal as? ValidationResult.Invalid)?.errorMessage
                ?: (phoneVal as? ValidationResult.Invalid)?.errorMessage
                ?: (addressVal as? ValidationResult.Invalid)?.errorMessage
                ?: (passportVal as? ValidationResult.Invalid)?.errorMessage
                ?: (birthVal as? ValidationResult.Invalid)?.errorMessage
                ?: (expiryVal as? ValidationResult.Invalid)?.errorMessage
                ?: "Revise los campos del perfil y pasaporte."
            emitEvent(UiEvent.ShowSnackbar(err, isError = true))
            return
        }

        val updated = userProfile.value.copy(
            fullName = FormValidator.sanitize(fullName),
            email = FormValidator.sanitize(email),
            dialCode = dialCode,
            phoneNumber = FormValidator.sanitize(phoneNumber),
            gender = gender,
            birthDate = birthDate.trim(),
            country = country,
            address = FormValidator.sanitize(address),
            passportNumber = FormValidator.sanitize(passportNumber).uppercase(),
            passportCountry = passportCountry,
            passportExpiryDate = passportExpiryDate.trim()
        )
        userProfile.value = updated

        viewModelScope.launch {
            repository.actualizarPerfil(updated)
            emitEvent(UiEvent.ShowSnackbar("Datos personales y de pasaporte guardados en base de datos."))
        }
    }

    // =========================================================================
    // MÉTODOS PANEL ADMIN (CREACIÓN DE ALOJAMIENTOS Y ATRACCIONES)
    // =========================================================================
    fun crearAlojamiento(
        name: String,
        city: String,
        starRating: Int,
        description: String,
        amenities: List<String>,
        imageUrl: String,
        roomName: String,
        pricePerNight: Double,
        capacity: Int,
        initialAvailableRooms: Int,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || city.isBlank() || description.isBlank() || roomName.isBlank()) {
            emitEvent(UiEvent.ShowSnackbar("Todos los campos marcados son obligatorios.", isError = true))
            return
        }
        if (pricePerNight <= 0) {
            emitEvent(UiEvent.ShowSnackbar("El precio por noche debe ser mayor a 0.", isError = true))
            return
        }
        if (initialAvailableRooms < 0) {
            emitEvent(UiEvent.ShowSnackbar("La cantidad disponible no puede ser negativa.", isError = true))
            return
        }

        val newHotelId = "hotel-${System.currentTimeMillis() % 10000}"
        val newRoomId = 100 + (System.currentTimeMillis() % 899).toInt()

        val room = HotelRoom(
            roomId = newRoomId,
            hotelId = newHotelId,
            name = FormValidator.sanitize(roomName),
            bedConfiguration = if (capacity <= 2) "1 cama King" else "2 camas Queen",
            capacity = capacity,
            sizeM2 = 42,
            pricePerNight = pricePerNight,
            amenities = amenities,
            imageUrl = imageUrl.trim(),
            freeCancellation = true,
            breakfastIncluded = true,
            backendHabitacionId = newRoomId,
            availableRooms = initialAvailableRooms,
            totalRooms = initialAvailableRooms
        )

        val hotel = Hotel(
            id = newHotelId,
            name = FormValidator.sanitize(name),
            city = FormValidator.sanitize(city),
            address = "${FormValidator.sanitize(city)}, El Salvador",
            starRating = starRating.coerceIn(1, 5),
            reviewScore = 9.5,
            reviewCount = 1,
            reviewLabel = "Excepcional",
            description = FormValidator.sanitize(description),
            amenities = if (amenities.isEmpty()) listOf("Wi-Fi", "Piscina", "Desayuno") else amenities,
            pricePerNightStarting = pricePerNight,
            featuredTag = "Nuevo",
            imageUrl = imageUrl.trim(),
            hasGeniusDiscount = true,
            rooms = listOf(room)
        )

        viewModelScope.launch {
            repository.crearHotel(hotel, room)
                .onSuccess {
                    cargarHoteles()
                    emitEvent(UiEvent.ShowSnackbar("¡Alojamiento '$name' guardado exitosamente en Supabase!"))
                    onSuccess()
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al guardar el alojamiento en Supabase", isError = true))
                }
        }
    }

    fun crearAtraccion(
        title: String,
        city: String,
        category: String,
        duration: String,
        pricePerPerson: Double,
        description: String,
        imageUrl: String,
        minibusCapacity: Int,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || city.isBlank() || description.isBlank()) {
            emitEvent(UiEvent.ShowSnackbar("Complete los campos obligatorios de la atracción.", isError = true))
            return
        }
        if (pricePerPerson <= 0) {
            emitEvent(UiEvent.ShowSnackbar("El precio por persona debe ser mayor a 0.", isError = true))
            return
        }
        val capVal = FormValidator.validateMinibusCapacity(minibusCapacity)
        if (!capVal.isValid) {
            emitEvent(UiEvent.ShowSnackbar((capVal as ValidationResult.Invalid).errorMessage, isError = true))
            return
        }

        val newAttrId = "attr-${System.currentTimeMillis() % 10000}"
        val attraction = TouristAttraction(
            id = newAttrId,
            title = FormValidator.sanitize(title),
            city = FormValidator.sanitize(city),
            category = category,
            rating = 4.9,
            reviewCount = 1,
            duration = duration.ifBlank { "3 horas" },
            pricePerPerson = pricePerPerson,
            description = FormValidator.sanitize(description),
            imageUrl = imageUrl.trim(),
            highlights = listOf("Guía profesional", "Experiencia guiada", "Equipamiento"),
            inclusions = listOf("Cupos reservados ($minibusCapacity cupos)", "Entradas incluidas"),
            minibusCapacity = minibusCapacity,
            availableSpots = minibusCapacity
        )

        viewModelScope.launch {
            repository.crearAtraccion(attraction)
                .onSuccess {
                    cargarAtracciones()
                    emitEvent(UiEvent.ShowSnackbar("¡Atracción '$title' guardada exitosamente en Supabase!"))
                    onSuccess()
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al guardar la atracción en Supabase", isError = true))
                }
        }
    }

    fun actualizarAlojamiento(
        hotelId: String,
        name: String,
        city: String,
        starRating: Int,
        description: String,
        amenities: List<String>,
        imageUrl: String,
        roomName: String,
        pricePerNight: Double,
        capacity: Int,
        availableRooms: Int,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || city.isBlank() || description.isBlank() || roomName.isBlank()) {
            emitEvent(UiEvent.ShowSnackbar("Todos los campos marcados son obligatorios.", isError = true))
            return
        }
        val room = HotelRoom(
            roomId = 101,
            hotelId = hotelId,
            name = FormValidator.sanitize(roomName),
            bedConfiguration = if (capacity <= 2) "1 cama King" else "2 camas Queen",
            capacity = capacity,
            sizeM2 = 45,
            pricePerNight = pricePerNight,
            amenities = amenities,
            imageUrl = imageUrl.trim(),
            freeCancellation = true,
            breakfastIncluded = true,
            backendHabitacionId = 101,
            availableRooms = availableRooms,
            totalRooms = availableRooms
        )
        val hotel = Hotel(
            id = hotelId,
            name = FormValidator.sanitize(name),
            city = FormValidator.sanitize(city),
            address = "${FormValidator.sanitize(city)}, El Salvador",
            starRating = starRating.coerceIn(1, 5),
            reviewScore = 9.5,
            reviewCount = 100,
            reviewLabel = "Excepcional",
            description = FormValidator.sanitize(description),
            amenities = if (amenities.isEmpty()) listOf("Wi-Fi", "Piscina", "Desayuno") else amenities,
            pricePerNightStarting = pricePerNight,
            featuredTag = "Destacado",
            imageUrl = imageUrl.trim(),
            hasGeniusDiscount = true,
            rooms = listOf(room)
        )

        viewModelScope.launch {
            repository.updateHotel(hotelId, hotel, room)
                .onSuccess {
                    cargarHoteles()
                    emitEvent(UiEvent.ShowSnackbar("Alojamiento actualizado correctamente en Supabase."))
                    onSuccess()
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al actualizar alojamiento en Supabase", isError = true))
                }
        }
    }

    fun eliminarAlojamiento(hotelId: String) {
        viewModelScope.launch {
            repository.deleteHotel(hotelId)
                .onSuccess {
                    cargarHoteles()
                    emitEvent(UiEvent.ShowSnackbar("Alojamiento eliminado de Supabase."))
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al eliminar alojamiento en Supabase", isError = true))
                }
        }
    }

    fun actualizarAtraccionAdmin(
        attrId: String,
        title: String,
        city: String,
        category: String,
        duration: String,
        pricePerPerson: Double,
        description: String,
        imageUrl: String,
        minibusCapacity: Int,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || city.isBlank() || description.isBlank()) {
            emitEvent(UiEvent.ShowSnackbar("Todos los campos obligatorios deben completarse.", isError = true))
            return
        }
        val attr = TouristAttraction(
            id = attrId,
            title = FormValidator.sanitize(title),
            city = FormValidator.sanitize(city),
            category = category,
            rating = 4.9,
            reviewCount = 80,
            duration = duration,
            pricePerPerson = pricePerPerson,
            description = FormValidator.sanitize(description),
            imageUrl = imageUrl.trim(),
            highlights = listOf("Guía bilingüe certificado", "Experiencia guiada", "Seguro de viaje"),
            inclusions = listOf("Cupos reservados ($minibusCapacity cupos)", "Entradas incluidas"),
            minibusCapacity = minibusCapacity,
            availableSpots = minibusCapacity
        )

        viewModelScope.launch {
            repository.updateAttraction(attrId, attr)
                .onSuccess {
                    cargarAtracciones()
                    emitEvent(UiEvent.ShowSnackbar("Atracción actualizada correctamente en Supabase."))
                    onSuccess()
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al actualizar atracción en Supabase", isError = true))
                }
        }
    }

    fun eliminarAtraccionAdmin(attrId: String) {
        viewModelScope.launch {
            repository.deleteAttraction(attrId)
                .onSuccess {
                    cargarAtracciones()
                    emitEvent(UiEvent.ShowSnackbar("Atracción eliminada de Supabase."))
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al eliminar atracción en Supabase", isError = true))
                }
        }
    }

    // =========================================================================
    // MÉTODOS DE FAVORITOS
    // =========================================================================
    fun toggleFavoriteHotel(hotelId: String) {
        val current = favoriteHotelIds.value
        if (current.contains(hotelId)) {
            favoriteHotelIds.value = current - hotelId
            emitEvent(UiEvent.ShowSnackbar("Hotel eliminado de Favoritos"))
        } else {
            favoriteHotelIds.value = current + hotelId
            emitEvent(UiEvent.ShowSnackbar("Hotel guardado en Favoritos ❤️"))
        }
    }

    fun toggleFavoriteAttraction(attractionId: String) {
        val current = favoriteAttractionIds.value
        if (current.contains(attractionId)) {
            favoriteAttractionIds.value = current - attractionId
            emitEvent(UiEvent.ShowSnackbar("Atracción eliminada de Favoritos"))
        } else {
            favoriteAttractionIds.value = current + attractionId
            emitEvent(UiEvent.ShowSnackbar("Atracción guardada en Favoritos ❤️"))
        }
    }

    // =========================================================================
    // CARGAS Y PULL-TO-REFRESH
    // =========================================================================
    fun cargarHoteles() {
        viewModelScope.launch {
            _hotelsState.value = UiState.Loading
            repository.getHotels()
                .onSuccess { list ->
                    _hotelsState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
                .onFailure { err ->
                    _hotelsState.value = UiState.Error(err.message ?: "Error al cargar hoteles")
                }
        }
    }

    fun refrescarHoteles() {
        viewModelScope.launch {
            _isRefreshingHotels.value = true
            repository.getHotels()
                .onSuccess { list ->
                    _hotelsState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al sincronizar hoteles", isError = true))
                }
            _isRefreshingHotels.value = false
        }
    }

    fun cargarAtracciones() {
        viewModelScope.launch {
            _attractionsState.value = UiState.Loading
            repository.getAttractions()
                .onSuccess { list ->
                    _attractionsState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
                .onFailure { err ->
                    _attractionsState.value = UiState.Error(err.message ?: "Error al cargar atracciones")
                }
        }
    }

    fun refrescarAtracciones() {
        viewModelScope.launch {
            _isRefreshingAttractions.value = true
            repository.getAttractions()
                .onSuccess { list ->
                    _attractionsState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al sincronizar atracciones", isError = true))
                }
            _isRefreshingAttractions.value = false
        }
    }

    private fun sortHotelBookings(list: List<CustomerHotelBooking>): List<CustomerHotelBooking> {
        val (confirmadas, canceladas) = list.partition { it.status.equals("CONFIRMADA", ignoreCase = true) }
        val sortedConfirmadas = confirmadas.sortedWith(
            compareByDescending<CustomerHotelBooking> { it.createdAt }
                .thenByDescending { it.checkInDate }
                .thenByDescending { it.bookingReference }
        )
        val sortedCanceladas = canceladas.sortedWith(
            compareByDescending<CustomerHotelBooking> { it.createdAt }
                .thenByDescending { it.checkInDate }
                .thenByDescending { it.bookingReference }
        )
        return sortedConfirmadas + sortedCanceladas
    }

    private fun sortAttractionBookings(list: List<CustomerAttractionBooking>): List<CustomerAttractionBooking> {
        val (confirmadas, canceladas) = list.partition { it.status.equals("CONFIRMADA", ignoreCase = true) }
        val sortedConfirmadas = confirmadas.sortedWith(
            compareByDescending<CustomerAttractionBooking> { it.createdAt }
                .thenByDescending { it.date }
                .thenByDescending { it.ticketReference }
        )
        val sortedCanceladas = canceladas.sortedWith(
            compareByDescending<CustomerAttractionBooking> { it.createdAt }
                .thenByDescending { it.date }
                .thenByDescending { it.ticketReference }
        )
        return sortedConfirmadas + sortedCanceladas
    }

    fun cargarReservasHuesped() {
        val currentEmail = userProfile.value.email.trim()
        if (!isLoggedIn.value || currentEmail.isBlank()) {
            _customerHotelBookings.value = emptyList()
            _customerAttractionBookings.value = emptyList()
            return
        }
        viewModelScope.launch {
            repository.getCustomerHotelBookings(currentEmail).onSuccess { list ->
                val strictlyMine = list.filter { it.guestEmail.trim().equals(currentEmail, ignoreCase = true) }
                _customerHotelBookings.value = sortHotelBookings(strictlyMine)
            }
            repository.getCustomerAttractionBookings(currentEmail).onSuccess { list ->
                val strictlyMine = list.filter { it.leadGuestEmail.trim().equals(currentEmail, ignoreCase = true) }
                _customerAttractionBookings.value = sortAttractionBookings(strictlyMine)
            }
        }
    }

    // =========================================================================
    // FLUJO DE CHECKOUT Y RESERVAS
    // =========================================================================
    fun iniciarCheckoutHabitacion(hotel: Hotel, room: HotelRoom) {
        selectedRoomForCheckout.value = hotel to room
    }

    suspend fun createPayPalOrder(amountInUsd: Double): Result<PaymentGatewayManager.PayPalOrderResponse> {
        return PaymentGatewayManager.createPayPalOrder(amountInUsd)
    }

    fun confirmarReservaHotel(
        hotel: Hotel,
        room: HotelRoom,
        guestFullName: String,
        guestEmail: String,
        guestPhone: String,
        paymentMethod: String,
        specialRequests: String?,
        onSuccess: () -> Unit
    ) {
        val nameVal = FormValidator.validateName(guestFullName, "Nombre del huésped")
        val emailVal = FormValidator.validateEmail(guestEmail)
        val phoneVal = FormValidator.validatePhone(guestPhone, isRequired = true)

        if (!nameVal.isValid || !emailVal.isValid || !phoneVal.isValid) {
            val err = (nameVal as? ValidationResult.Invalid)?.errorMessage
                ?: (emailVal as? ValidationResult.Invalid)?.errorMessage
                ?: (phoneVal as? ValidationResult.Invalid)?.errorMessage
                ?: "Por favor revise los datos del huésped."
            emitEvent(UiEvent.ShowSnackbar(err, isError = true))
            return
        }

        val nights = stayNights.value
        val hasGeniusDiscount = isLoggedIn.value && hotel.hasGeniusDiscount
        val rawSubtotal = nights * room.pricePerNight
        val discountAmount = if (hasGeniusDiscount) rawSubtotal * 0.10 else 0.0
        val subtotal = rawSubtotal - discountAmount
        val taxes = subtotal * 0.13 // 13% IVA
        val total = subtotal + taxes

        val bookingReference = "LUM-${(10000..99999).random()}"
        val sanitizedEmail = FormValidator.sanitize(guestEmail).trim().lowercase()
        val sanitizedName = FormValidator.sanitize(guestFullName)
        val sanitizedPhone = FormValidator.sanitize(guestPhone)

        val newBooking = CustomerHotelBooking(
            bookingReference = bookingReference,
            hotelId = hotel.id,
            hotelName = hotel.name,
            hotelCity = hotel.city,
            roomName = room.name,
            guestFullName = sanitizedName,
            guestEmail = sanitizedEmail,
            guestPhone = sanitizedPhone,
            checkInDate = checkInDate.value.toString(),
            checkOutDate = checkOutDate.value.toString(),
            nights = nights,
            guestsCount = guestsCount.value,
            roomSubtotal = subtotal,
            taxesAndFees = taxes,
            totalPrice = total,
            paymentMethod = paymentMethod,
            specialRequests = specialRequests?.ifBlank { null },
            discountAmount = discountAmount,
            originalSubtotal = rawSubtotal
        )

        viewModelScope.launch {
            repository.bookHotel(newBooking).onSuccess { confirmed ->
                recentHotelBookingConfirmed.value = confirmed

                if (!isLoggedIn.value && sanitizedEmail.isNotBlank()) {
                    iniciarSesion(sanitizedEmail, sanitizedName)
                } else {
                    _customerHotelBookings.value = sortHotelBookings(listOf(confirmed) + _customerHotelBookings.value.filter { it.bookingReference != confirmed.bookingReference })
                    cargarReservasHuesped()
                }

                // Registrar en transacciones pasadas
                val newTx = PastTransaction(
                    id = "tx-${System.currentTimeMillis()}",
                    title = "${hotel.name} ($nights noches)",
                    type = "Estadía",
                    date = LocalDate.now().toString(),
                    bookingReference = confirmed.bookingReference,
                    paymentMethod = paymentMethod,
                    amountPaid = total,
                    status = "Completada"
                )
                pastTransactions.value = listOf(newTx) + pastTransactions.value

                emitEvent(UiEvent.ShowSnackbar("¡Reserva confirmada con éxito! Referencia: ${confirmed.bookingReference}"))
                onSuccess()
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: err.message ?: "Error al procesar la reserva"
                emitEvent(UiEvent.ShowSnackbar(errorMsg, isError = true))
            }
        }
    }

    fun confirmarReservaAtraccion(
        attraction: TouristAttraction,
        date: LocalDate,
        ticketsCount: Int,
        leadGuestName: String,
        leadGuestEmail: String,
        leadGuestPhone: String,
        paymentMethod: String,
        onSuccess: () -> Unit
    ) {
        val nameVal = FormValidator.validateName(leadGuestName, "Nombre del titular")
        val emailVal = FormValidator.validateEmail(leadGuestEmail)
        val phoneVal = FormValidator.validatePhone(leadGuestPhone, isRequired = true)

        if (!nameVal.isValid || !emailVal.isValid || !phoneVal.isValid) {
            val err = (nameVal as? ValidationResult.Invalid)?.errorMessage
                ?: (emailVal as? ValidationResult.Invalid)?.errorMessage
                ?: (phoneVal as? ValidationResult.Invalid)?.errorMessage
                ?: "Revise los datos del titular de las entradas."
            emitEvent(UiEvent.ShowSnackbar(err, isError = true))
            return
        }

        val total = ticketsCount * attraction.pricePerPerson
        val ticketRef = "ATR-${(10000..99999).random()}"
        val sanitizedEmail = FormValidator.sanitize(leadGuestEmail).trim().lowercase()
        val sanitizedName = FormValidator.sanitize(leadGuestName)
        val sanitizedPhone = FormValidator.sanitize(leadGuestPhone)

        val booking = CustomerAttractionBooking(
            ticketReference = ticketRef,
            attractionId = attraction.id,
            attractionTitle = attraction.title,
            city = attraction.city,
            date = date.toString(),
            ticketsCount = ticketsCount,
            leadGuestName = sanitizedName,
            leadGuestEmail = sanitizedEmail,
            leadGuestPhone = sanitizedPhone,
            totalPrice = total,
            paymentMethod = paymentMethod
        )

        viewModelScope.launch {
            repository.bookAttraction(booking).onSuccess { confirmed ->
                recentAttractionBookingConfirmed.value = confirmed

                if (!isLoggedIn.value && sanitizedEmail.isNotBlank()) {
                    iniciarSesion(sanitizedEmail, sanitizedName)
                } else {
                    _customerAttractionBookings.value = sortAttractionBookings(listOf(confirmed) + _customerAttractionBookings.value.filter { it.ticketReference != confirmed.ticketReference })
                    cargarReservasHuesped()
                }

                // Registrar en transacciones pasadas
                val newTx = PastTransaction(
                    id = "tx-${System.currentTimeMillis()}",
                    title = "${attraction.title} ($ticketsCount tickets)",
                    type = "Atracción",
                    date = LocalDate.now().toString(),
                    bookingReference = confirmed.ticketReference,
                    paymentMethod = paymentMethod,
                    amountPaid = total,
                    status = "Completada"
                )
                pastTransactions.value = listOf(newTx) + pastTransactions.value

                emitEvent(UiEvent.ShowSnackbar("¡Entrada confirmada! Ref: ${confirmed.ticketReference}"))
                onSuccess()
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: err.message ?: "Error al reservar actividad"
                emitEvent(UiEvent.ShowSnackbar(errorMsg, isError = true))
            }
        }
    }

    fun cancelarReservaHotel(reference: String) {
        viewModelScope.launch {
            repository.cancelHotelBooking(reference).onSuccess {
                cargarReservasHuesped()
                emitEvent(UiEvent.ShowSnackbar("La reserva $reference ha sido cancelada."))
            }.onFailure { err ->
                emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al cancelar reserva", isError = true))
            }
        }
    }

    fun cancelarReservaAtraccion(reference: String) {
        viewModelScope.launch {
            repository.cancelAttractionBooking(reference).onSuccess {
                cargarReservasHuesped()
                emitEvent(UiEvent.ShowSnackbar("La entrada $reference ha sido cancelada."))
            }.onFailure { err ->
                emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al cancelar entrada", isError = true))
            }
        }
    }

    private fun emitEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }
}
