package com.lumina.luminahotels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.luminahotels.data.mock.MockDataProvider
import com.lumina.luminahotels.data.repository.HotelRepository
import com.lumina.luminahotels.data.repository.HotelRepositoryImpl
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
    private val repository: HotelRepository = HotelRepositoryImpl()
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

    val savedCards = MutableStateFlow<List<SavedPaymentCard>>(emptyList())

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
        cargarHoteles()
        cargarAtracciones()
        cargarReservasHuesped()
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

        userProfile.value = UserProfile(
            fullName = name,
            email = FormValidator.sanitize(trimmedEmail),
            isGeniusMember = true,
            isAdmin = isAdminUser
        )
        isLoggedIn.value = true
        savedCards.value = MockDataProvider.defaultSavedCards
        pastTransactions.value = MockDataProvider.defaultPastTransactions
        if (isAdminUser) {
            emitEvent(UiEvent.ShowSnackbar("¡Bienvenido Admin, $name! Panel Admin desbloqueado."))
        } else {
            emitEvent(UiEvent.ShowSnackbar("¡Bienvenido, $name! 10% de descuento Genius activado."))
        }
    }

    fun cerrarSesion() {
        isLoggedIn.value = false
        userProfile.value = UserProfile(fullName = "", email = "", phoneNumber = "", isGeniusMember = false, isAdmin = false)
        savedCards.value = emptyList()
        pastTransactions.value = emptyList()
        favoriteHotelIds.value = emptySet()
        favoriteAttractionIds.value = emptySet()
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
        val phoneVal = FormValidator.validatePhone(phoneNumber, isRequired = true)
        val addressVal = FormValidator.validateAddress(address)
        val passportVal = FormValidator.validatePassportNumber(passportNumber)
        val birthVal = FormValidator.validateBirthDate(birthDate)
        val expiryVal = FormValidator.validatePassportExpiry(passportExpiryDate)

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
            birthDate = birthDate,
            country = country,
            address = FormValidator.sanitize(address),
            passportNumber = FormValidator.sanitize(passportNumber).uppercase(),
            passportCountry = passportCountry,
            passportExpiryDate = passportExpiryDate
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
        if (initialAvailableRooms <= 0) {
            emitEvent(UiEvent.ShowSnackbar("La cantidad de cuartos disponibles debe ser mayor a 0.", isError = true))
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
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1000&q=80" },
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
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80" },
            hasGeniusDiscount = true,
            rooms = listOf(room)
        )

        viewModelScope.launch {
            repository.crearHotel(hotel, room)
            cargarHoteles()
            emitEvent(UiEvent.ShowSnackbar("¡Alojamiento '$name' agregado y guardado en base de datos!"))
            onSuccess()
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
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1000&q=80" },
            highlights = listOf("Guía profesional", "Transporte: Microbús pequeño", "Equipamiento"),
            inclusions = listOf("Transporte en Microbús pequeño ($minibusCapacity spots)", "Entradas incluidas"),
            minibusCapacity = minibusCapacity,
            availableSpots = minibusCapacity
        )

        viewModelScope.launch {
            repository.crearAtraccion(attraction)
            cargarAtracciones()
            emitEvent(UiEvent.ShowSnackbar("¡Atracción '$title' agregada con $minibusCapacity spots de microbús!"))
            onSuccess()
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
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1000&q=80" },
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
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80" },
            hasGeniusDiscount = true,
            rooms = listOf(room)
        )

        viewModelScope.launch {
            repository.updateHotel(hotelId, hotel, room)
                .onSuccess {
                    cargarHoteles()
                    emitEvent(UiEvent.ShowSnackbar("Alojamiento actualizado correctamente."))
                    onSuccess()
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al actualizar alojamiento", isError = true))
                }
        }
    }

    fun eliminarAlojamiento(hotelId: String) {
        viewModelScope.launch {
            repository.deleteHotel(hotelId)
                .onSuccess {
                    cargarHoteles()
                    emitEvent(UiEvent.ShowSnackbar("Alojamiento eliminado del catálogo."))
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al eliminar alojamiento", isError = true))
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
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1000&q=80" },
            highlights = listOf("Guía bilingüe certificado", "Transporte: Microbús pequeño", "Seguro de viaje"),
            inclusions = listOf("Transporte: Microbús pequeño ($minibusCapacity asientos)", "Entradas incluidas"),
            minibusCapacity = minibusCapacity,
            availableSpots = minibusCapacity
        )

        viewModelScope.launch {
            repository.updateAttraction(attrId, attr)
                .onSuccess {
                    cargarAtracciones()
                    emitEvent(UiEvent.ShowSnackbar("Atracción actualizada correctamente."))
                    onSuccess()
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al actualizar atracción", isError = true))
                }
        }
    }

    fun eliminarAtraccionAdmin(attrId: String) {
        viewModelScope.launch {
            repository.deleteAttraction(attrId)
                .onSuccess {
                    cargarAtracciones()
                    emitEvent(UiEvent.ShowSnackbar("Atracción eliminada del catálogo."))
                }
                .onFailure { err ->
                    emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al eliminar atracción", isError = true))
                }
        }
    }

    fun agregarTarjeta(cardNumber: String, cardHolder: String, expiry: String) {
        val cardVal = FormValidator.validateCardNumber(cardNumber)
        val expiryVal = FormValidator.validateCardExpiry(expiry)

        if (!cardVal.isValid || !expiryVal.isValid || cardHolder.isBlank()) {
            val err = (cardVal as? ValidationResult.Invalid)?.errorMessage
                ?: (expiryVal as? ValidationResult.Invalid)?.errorMessage
                ?: "Ingrese el titular de la tarjeta."
            emitEvent(UiEvent.ShowSnackbar(err, isError = true))
            return
        }

        val cleanDigits = cardNumber.replace("[\\s-]".toRegex(), "")
        val last4 = if (cleanDigits.length >= 4) cleanDigits.takeLast(4) else "4242"
        val brand = if (cleanDigits.startsWith("4")) "Visa" else if (cleanDigits.startsWith("5")) "Mastercard" else "Amex"

        val newCard = SavedPaymentCard(
            id = "card-${System.currentTimeMillis()}",
            cardNumberMasked = "•••• $last4",
            cardHolder = cardHolder.uppercase().trim(),
            expiry = expiry.trim(),
            cardBrand = brand,
            isDefault = savedCards.value.isEmpty()
        )

        savedCards.value = savedCards.value + newCard
        emitEvent(UiEvent.ShowSnackbar("Tarjeta guardada: $brand •••• $last4"))
    }

    fun eliminarTarjeta(cardId: String) {
        savedCards.value = savedCards.value.filterNot { it.id == cardId }
        emitEvent(UiEvent.ShowSnackbar("Tarjeta eliminada de tus métodos de pago."))
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

    fun cargarReservasHuesped() {
        viewModelScope.launch {
            repository.getCustomerHotelBookings().onSuccess {
                _customerHotelBookings.value = it
            }
            repository.getCustomerAttractionBookings().onSuccess {
                _customerAttractionBookings.value = it
            }
        }
    }

    // =========================================================================
    // FLUJO DE CHECKOUT Y RESERVAS
    // =========================================================================
    fun iniciarCheckoutHabitacion(hotel: Hotel, room: HotelRoom) {
        selectedRoomForCheckout.value = hotel to room
    }

    fun confirmarReservaHotel(
        hotel: Hotel,
        room: HotelRoom,
        guestFullName: String,
        guestEmail: String,
        guestPhone: String,
        paymentMethod: String,
        cardNumber: String,
        cardExpiry: String,
        cardCvv: String,
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

        if (paymentMethod.contains("Tarjeta", ignoreCase = true) && !paymentMethod.contains("••••")) {
            val cardVal = FormValidator.validateCardNumber(cardNumber)
            val expiryVal = FormValidator.validateCardExpiry(cardExpiry)
            val cvvVal = FormValidator.validateCardCvv(cardCvv)

            if (!cardVal.isValid || !expiryVal.isValid || !cvvVal.isValid) {
                val err = (cardVal as? ValidationResult.Invalid)?.errorMessage
                    ?: (expiryVal as? ValidationResult.Invalid)?.errorMessage
                    ?: (cvvVal as? ValidationResult.Invalid)?.errorMessage
                    ?: "Revise los datos de la tarjeta de pago."
                emitEvent(UiEvent.ShowSnackbar(err, isError = true))
                return
            }
        }

        val nights = stayNights.value
        val discountMultiplier = if (isLoggedIn.value && hotel.hasGeniusDiscount) 0.90 else 1.0
        val subtotal = (nights * room.pricePerNight) * discountMultiplier
        val taxes = subtotal * 0.13 // 13% IVA
        val total = subtotal + taxes

        val bookingReference = "LUM-${(10000..99999).random()}"

        val newBooking = CustomerHotelBooking(
            bookingReference = bookingReference,
            hotelId = hotel.id,
            hotelName = hotel.name,
            hotelCity = hotel.city,
            roomName = room.name,
            guestFullName = FormValidator.sanitize(guestFullName),
            guestEmail = FormValidator.sanitize(guestEmail),
            guestPhone = FormValidator.sanitize(guestPhone),
            checkInDate = checkInDate.value.toString(),
            checkOutDate = checkOutDate.value.toString(),
            nights = nights,
            guestsCount = guestsCount.value,
            roomSubtotal = subtotal,
            taxesAndFees = taxes,
            totalPrice = total,
            paymentMethod = paymentMethod,
            specialRequests = specialRequests?.ifBlank { null }
        )

        viewModelScope.launch {
            repository.bookHotel(newBooking).onSuccess { confirmed ->
                recentHotelBookingConfirmed.value = confirmed
                cargarReservasHuesped()

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
                emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al procesar la reserva", isError = true))
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

        val booking = CustomerAttractionBooking(
            ticketReference = ticketRef,
            attractionId = attraction.id,
            attractionTitle = attraction.title,
            city = attraction.city,
            date = date.toString(),
            ticketsCount = ticketsCount,
            leadGuestName = FormValidator.sanitize(leadGuestName),
            leadGuestEmail = FormValidator.sanitize(leadGuestEmail),
            leadGuestPhone = FormValidator.sanitize(leadGuestPhone),
            totalPrice = total,
            paymentMethod = paymentMethod
        )

        viewModelScope.launch {
            repository.bookAttraction(booking).onSuccess { confirmed ->
                recentAttractionBookingConfirmed.value = confirmed
                cargarReservasHuesped()

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

                emitEvent(UiEvent.ShowSnackbar("¡Entradas confirmadas! Referencia: ${confirmed.ticketReference}"))
                onSuccess()
            }.onFailure { err ->
                emitEvent(UiEvent.ShowSnackbar(err.message ?: "Error al reservar entradas", isError = true))
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
