package com.lumina.luminahotels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumina.luminahotels.model.TouristAttraction
import com.lumina.luminahotels.model.UiEvent
import com.lumina.luminahotels.ui.screens.*
import com.lumina.luminahotels.ui.theme.GoldAccent
import com.lumina.luminahotels.ui.theme.LuminahotelsTheme
import com.lumina.luminahotels.viewmodel.HotelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuminahotelsTheme {
                LuminaCustomerAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuminaCustomerAppRoot(hotelViewModel: HotelViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado de navegación: 0: Buscar, 1: Favoritos, 2: Reservas, 3: Mi cuenta / Iniciar sesión
    var tabActual by remember { mutableIntStateOf(0) }
    val isLoggedIn by hotelViewModel.isLoggedIn.collectAsState()

    val selectedHotelForDetails by hotelViewModel.selectedHotelForDetails.collectAsState()
    val selectedRoomForCheckout by hotelViewModel.selectedRoomForCheckout.collectAsState()
    val recentBookingConfirmed by hotelViewModel.recentHotelBookingConfirmed.collectAsState()
    var selectedAttractionForBooking by remember { mutableStateOf<TouristAttraction?>(null) }

    // Manejo del botón atrás del dispositivo
    BackHandler(enabled = recentBookingConfirmed != null) {
        hotelViewModel.recentHotelBookingConfirmed.value = null
        tabActual = 2
    }
    BackHandler(enabled = selectedRoomForCheckout != null && recentBookingConfirmed == null) {
        hotelViewModel.selectedRoomForCheckout.value = null
    }
    BackHandler(enabled = selectedHotelForDetails != null && selectedRoomForCheckout == null && recentBookingConfirmed == null) {
        hotelViewModel.selectedHotelForDetails.value = null
    }
    BackHandler(enabled = tabActual != 0 && selectedHotelForDetails == null && selectedRoomForCheckout == null && recentBookingConfirmed == null) {
        tabActual = 0
    }

    // Colector central de notificaciones y alertas
    LaunchedEffect(Unit) {
        hotelViewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Flujo 1: Pantalla Celebratoria de Reserva Confirmada
    if (recentBookingConfirmed != null) {
        BookingConfirmationScreen(
            booking = recentBookingConfirmed!!,
            onViewItinerary = {
                hotelViewModel.recentHotelBookingConfirmed.value = null
                tabActual = 2
            }
        )
        return
    }

    // Flujo 2: Pantalla de Checkout de Habitación
    if (selectedRoomForCheckout != null) {
        val (hotel, room) = selectedRoomForCheckout!!
        HotelCheckoutScreen(
            hotel = hotel,
            room = room,
            viewModel = hotelViewModel,
            onBack = { hotelViewModel.selectedRoomForCheckout.value = null },
            onBookingSuccess = {
                hotelViewModel.selectedRoomForCheckout.value = null
                hotelViewModel.selectedHotelForDetails.value = null
            }
        )
        return
    }

    // Flujo 3: Pantalla de Detalle de Hotel y Habitaciones
    if (selectedHotelForDetails != null) {
        HotelDetailScreen(
            hotel = selectedHotelForDetails!!,
            viewModel = hotelViewModel,
            onBack = { hotelViewModel.selectedHotelForDetails.value = null },
            onSelectRoom = { hotel, room ->
                hotelViewModel.iniciarCheckoutHabitacion(hotel, room)
            }
        )
        return
    }

    // Flujo Principal: 4 Pestañas B2C Estilo Booking.com
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LUMINA HOTELS",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (tabActual) {
                                0 -> "Hoteles y Experiencias de Lujo"
                                1 -> "Tus Alojamientos y Tours Favoritos"
                                2 -> "Itinerario de Viajes Confirmados"
                                else -> if (isLoggedIn) "Mi Perfil & Métodos de Pago" else "Acceso de Miembros & Ahorro 10%"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Tab 1: Buscar
                    NavigationBarItem(
                        selected = tabActual == 0,
                        onClick = { tabActual = 0 },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        label = { Text("Buscar", fontWeight = if (tabActual == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors
                    )

                    // Tab 2: Favoritos
                    NavigationBarItem(
                        selected = tabActual == 1,
                        onClick = { tabActual = 1 },
                        icon = {
                            Icon(
                                imageVector = if (tabActual == 1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favoritos"
                            )
                        },
                        label = { Text("Favoritos", fontWeight = if (tabActual == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors
                    )

                    // Tab 3: Reservas
                    NavigationBarItem(
                        selected = tabActual == 2,
                        onClick = { tabActual = 2 },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Reservas") },
                        label = { Text("Reservas", fontWeight = if (tabActual == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors
                    )

                    // Tab 4 Dinámica: "Mi cuenta" (logged-in) vs. "Iniciar sesión" (logged-out)
                    NavigationBarItem(
                        selected = tabActual == 3,
                        onClick = { tabActual = 3 },
                        icon = {
                            Icon(
                                imageVector = if (isLoggedIn) Icons.Default.AccountCircle else Icons.Default.Person,
                                contentDescription = if (isLoggedIn) "Mi cuenta" else "Iniciar sesión"
                            )
                        },
                        label = {
                            Text(
                                text = if (isLoggedIn) "Mi cuenta" else "Iniciar sesión",
                                fontWeight = if (tabActual == 3) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = navItemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (tabActual) {
                0 -> ExploreSearchScreen(
                    viewModel = hotelViewModel,
                    onSelectHotel = { hotelViewModel.selectedHotelForDetails.value = it },
                    onSelectAttraction = { selectedAttractionForBooking = it }
                )
                1 -> FavoritesScreen(
                    viewModel = hotelViewModel,
                    onSelectHotel = { hotelViewModel.selectedHotelForDetails.value = it },
                    onSelectAttraction = { selectedAttractionForBooking = it },
                    onExplore = { tabActual = 0 }
                )
                2 -> MyBookingsScreen(
                    viewModel = hotelViewModel,
                    onExploreStays = { tabActual = 0 }
                )
                3 -> AccountOrLoginScreen(viewModel = hotelViewModel)
            }
        }
    }

    // Diálogo de Reserva de Atracción
    selectedAttractionForBooking?.let { attraction ->
        DialogoAtraccionDetalleYReserva(
            attraction = attraction,
            viewModel = hotelViewModel,
            onDismiss = { selectedAttractionForBooking = null }
        )
    }
}
