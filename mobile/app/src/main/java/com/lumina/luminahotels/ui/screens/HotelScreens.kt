@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.lumina.luminahotels.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lumina.luminahotels.data.mock.MockDataProvider
import com.lumina.luminahotels.domain.validation.FormValidator
import com.lumina.luminahotels.data.remote.PaymentGatewayManager
import com.lumina.luminahotels.model.*
import com.lumina.luminahotels.ui.components.*
import com.lumina.luminahotels.ui.theme.GoldAccent
import com.lumina.luminahotels.ui.theme.NavyDeep
import com.lumina.luminahotels.ui.theme.NavyLight
import com.lumina.luminahotels.viewmodel.HotelViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

// =========================================================================
// 1. PESTANA PRINCIPAL: "BUSCAR" CON SELECTOR SUPERIOR ALOJAMIENTOS / ATRACCIONES
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreSearchScreen(
    viewModel: HotelViewModel,
    onSelectHotel: (Hotel) -> Unit,
    onSelectAttraction: (TouristAttraction) -> Unit
) {
    val buscarTab by viewModel.buscarTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // SELECTOR SUPERIOR: ALOJAMIENTOS / ATRACCIONES DIRECTAMENTE ARRIBA DEL BUSCADOR
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = buscarTab == 0,
                    onClick = { viewModel.buscarTab.value = 0 },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Hotel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = { Text("Alojamientos", fontWeight = if (buscarTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = buscarTab == 1,
                    onClick = { viewModel.buscarTab.value = 1 },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = { Text("Atracciones", fontWeight = if (buscarTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (buscarTab == 0) {
                StaysSearchSection(viewModel = viewModel, onSelectHotel = onSelectHotel)
            } else {
                AttractionsSearchSection(viewModel = viewModel, onSelectAttraction = onSelectAttraction)
            }
        }
    }
}

// =========================================================================
// SECCIÓN ALOJAMIENTOS (CON BUSCADOR, DATE RANGE PICKER Y TARJETAS CON FOTOS)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaysSearchSection(
    viewModel: HotelViewModel,
    onSelectHotel: (Hotel) -> Unit
) {
    val query by viewModel.queryHotels.collectAsState()
    val categoria by viewModel.filtroCategoriaHotel.collectAsState()
    val hotels by viewModel.hotelsFiltrados.collectAsState()
    val isRefreshing by viewModel.isRefreshingHotels.collectAsState()
    val checkIn by viewModel.checkInDate.collectAsState()
    val checkOut by viewModel.checkOutDate.collectAsState()
    val nights by viewModel.stayNights.collectAsState()
    val guests by viewModel.guestsCount.collectAsState()
    val modoOffline by viewModel.modoOffline.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val favoriteIds by viewModel.favoriteHotelIds.collectAsState()

    var showDateRangePicker by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refrescarHoteles() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
        ) {
            item {
                OfflineStatusBanner(visible = modoOffline)
            }

            // TARJETA DE BÚSQUEDA Y SELECTOR DE FECHAS MODAL (ESTILO BOOKING)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Encuentra tu próxima estancia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Input de búsqueda de destino
                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.queryHotels.value = it },
                            placeholder = { Text("¿A dónde viajas? Ciudad, hotel o zona...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Selector Modal de Rango de Fechas
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDateRangePicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Column {
                                        Text("Fechas de Estadía", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$checkIn al $checkOut",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "$nights ${if (nights == 1) "noche" else "noches"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Selector de Huéspedes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Huéspedes:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalIconButton(
                                    onClick = { if (guests > 1) viewModel.guestsCount.value = guests - 1 },
                                    enabled = guests > 1
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Menos")
                                }
                                Text("$guests adultos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                FilledTonalIconButton(
                                    onClick = { if (guests < 10) viewModel.guestsCount.value = guests + 1 },
                                    enabled = guests < 10
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Más")
                                }
                            }
                        }
                    }
                }
            }

            // Chips de filtrado rápido
            item {
                FilterChipRow(
                    options = listOf("Todos", "5 Estrellas", "Frente a la Playa", "Con Spa"),
                    selectedOption = categoria,
                    onOptionSelected = { viewModel.filtroCategoriaHotel.value = it }
                )
            }

            // Lista de Hoteles
            if (hotels.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Hotel,
                        title = "No encontramos alojamientos",
                        description = "Prueba modificando tu búsqueda o los filtros aplicados.",
                        actionText = "Limpiar filtros",
                        onActionClick = {
                            viewModel.queryHotels.value = ""
                            viewModel.filtroCategoriaHotel.value = "Todos"
                        }
                    )
                }
            } else {
                items(hotels, key = { it.id }) { hotel ->
                    val isFav = favoriteIds.contains(hotel.id)
                    val discount = if (isLoggedIn && hotel.hasGeniusDiscount) 0.90 else 1.0
                    val nightlyRate = hotel.pricePerNightStarting * discount
                    val totalPrice = nightlyRate * nights

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectHotel(hotel) }
                    ) {
                        Column {
                            // FOTO DE ALTA DEFINICIÓN DEL HOTEL CON BOTÓN DE FAVORITO
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                LuminaNetworkImage(
                                    url = hotel.coverImage,
                                    contentDescription = hotel.name,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Botón Corazón de Favoritos
                                FavoriteHeartButton(
                                    isFavorite = isFav,
                                    onToggle = { viewModel.toggleFavoriteHotel(hotel.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                    )

                                if (isLoggedIn && hotel.hasGeniusDiscount) {
                                    GeniusDiscountBadge(
                                        text = "10% dto. Genius",
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(12.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        hotel.featuredTag?.let { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = GoldAccent.copy(alpha = 0.2f),
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            ) {
                                                Text(
                                                    text = tag.uppercase(),
                                                    color = GoldAccent,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = hotel.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            StarRatingRow(stars = hotel.starRating)
                                            Text(
                                                text = "• ${hotel.city}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Box(modifier = Modifier.wrapContentWidth()) {
                                        BookingRatingBadge(score = hotel.reviewScore, label = hotel.reviewLabel, reviewsCount = hotel.reviewCount)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = hotel.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                WrappingChipRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalSpacing = 8.dp,
                                    verticalSpacing = 8.dp
                                ) {
                                    hotel.amenities.take(4).forEach { AmenityChip(it) }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "$nights ${if (nights == 1) "noche" else "noches"}, $guests adultos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "US$${String.format("%.2f", totalPrice)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Incluye impuestos y cargos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = { onSelectHotel(hotel) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Ver Habitaciones", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDateRangePicker) {
        DateRangePickerModalDialog(
            initialCheckIn = checkIn,
            initialCheckOut = checkOut,
            onDismiss = { showDateRangePicker = false },
            onDatesSelected = { start, end, _ ->
                viewModel.actualizarRangoFechas(start, end)
                showDateRangePicker = false
            }
        )
    }
}

// =========================================================================
// SECCIÓN ATRACCIONES TURÍSTICAS
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionsSearchSection(
    viewModel: HotelViewModel,
    onSelectAttraction: (TouristAttraction) -> Unit
) {
    val query by viewModel.queryAttractions.collectAsState()
    val categoria by viewModel.filtroCategoriaAtraccion.collectAsState()
    val attractions by viewModel.attractionsFiltradas.collectAsState()
    val isRefreshing by viewModel.isRefreshingAttractions.collectAsState()
    val favoriteIds by viewModel.favoriteAttractionIds.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refrescarAtracciones() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
        ) {
            item {
                SearchBarField(
                    query = query,
                    onQueryChange = { viewModel.queryAttractions.value = it },
                    placeholder = "Buscar experiencias, tours o volcanes..."
                )
            }

            item {
                FilterChipRow(
                    options = listOf("Todas", "Naturaleza & Aventura", "Cultural & Tours", "Gastronomía", "Playas & Deportes"),
                    selectedOption = categoria,
                    onOptionSelected = { viewModel.filtroCategoriaAtraccion.value = it }
                )
            }

            if (attractions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Explore,
                        title = "No encontramos atracciones",
                        description = "Prueba con otra categoría o término de búsqueda.",
                        actionText = "Limpiar filtros",
                        onActionClick = {
                            viewModel.queryAttractions.value = ""
                            viewModel.filtroCategoriaAtraccion.value = "Todas"
                        }
                    )
                }
            } else {
                items(attractions, key = { it.id }) { attr ->
                    val isFav = favoriteIds.contains(attr.id)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAttraction(attr) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                            ) {
                                LuminaNetworkImage(
                                    url = attr.coverImage,
                                    contentDescription = attr.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                                FavoriteHeartButton(
                                    isFavorite = isFav,
                                    onToggle = { viewModel.toggleFavoriteAttraction(attr.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                )
                            }

                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = attr.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                        Text("${attr.rating} (${attr.reviewCount})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(attr.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(attr.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(attr.duration, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Text(
                                    text = attr.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Explore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${attr.availableSpots} / ${attr.minibusCapacity} cupos disponibles",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Desde", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "US$${String.format("%.2f", attr.pricePerPerson)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("por persona", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = { onSelectAttraction(attr) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Ver Boletos")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. PESTAÑA: "FAVORITOS" (HOTELES Y ATRACCIONES GUARDADOS)
// =========================================================================
@Composable
fun FavoritesScreen(
    viewModel: HotelViewModel,
    onSelectHotel: (Hotel) -> Unit,
    onSelectAttraction: (TouristAttraction) -> Unit,
    onExplore: () -> Unit
) {
    val favoriteHotelIds by viewModel.favoriteHotelIds.collectAsState()
    val favoriteAttractionIds by viewModel.favoriteAttractionIds.collectAsState()
    val hotelsState by viewModel.hotelsState.collectAsState()
    val attractionsState by viewModel.attractionsState.collectAsState()

    val allHotels = (hotelsState as? UiState.Success)?.data ?: emptyList()
    val allAttractions = (attractionsState as? UiState.Success)?.data ?: emptyList()

    val favHotels = allHotels.filter { favoriteHotelIds.contains(it.id) }
    val favAttractions = allAttractions.filter { favoriteAttractionIds.contains(it.id) }

    var tabFiltro by remember { mutableStateOf("Todos") }

    val isEmpty = when (tabFiltro) {
        "Hoteles" -> favHotels.isEmpty()
        "Atracciones" -> favAttractions.isEmpty()
        else -> favHotels.isEmpty() && favAttractions.isEmpty()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        item {
            FilterChipRow(
                options = listOf("Todos", "Hoteles", "Atracciones"),
                selectedOption = tabFiltro,
                onOptionSelected = { tabFiltro = it }
            )
        }

        if (isEmpty) {
            item {
                EmptyStateView(
                    icon = Icons.Default.FavoriteBorder,
                    title = "Aún no tienes favoritos",
                    description = "Toca el corazón en cualquier hotel o atracción para guardarlo aquí y planificar tu viaje.",
                    actionText = "Explorar Alojamientos",
                    onActionClick = onExplore
                )
            }
        } else {
            if (tabFiltro == "Todos" || tabFiltro == "Hoteles") {
                items(favHotels, key = { "fav-h-${it.id}" }) { hotel ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectHotel(hotel) }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LuminaNetworkImage(
                                url = hotel.imageUrl,
                                contentDescription = hotel.name,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(hotel.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                    IconButton(
                                        onClick = { viewModel.toggleFavoriteHotel(hotel.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text("• ${hotel.city}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Desde US$${String.format("%.2f", hotel.pricePerNightStarting)} / noche",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            if (tabFiltro == "Todos" || tabFiltro == "Atracciones") {
                items(favAttractions, key = { "fav-a-${it.id}" }) { attr ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAttraction(attr) }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LuminaNetworkImage(
                                url = attr.imageUrl,
                                contentDescription = attr.title,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(attr.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                    IconButton(
                                        onClick = { viewModel.toggleFavoriteAttraction(attr.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text(attr.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "US$${String.format("%.2f", attr.pricePerPerson)} por persona",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. PESTAÑA: "MI CUENTA" (LOGGED-IN) / "INICIAR SESIÓN" (LOGGED-OUT)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountOrLoginScreen(viewModel: HotelViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val pastTransactions by viewModel.pastTransactions.collectAsState()

    var showAuthModal by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }

    // Estados editables de datos personales
    var editFullName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var editEmail by remember(profile.email) { mutableStateOf(profile.email) }
    var editPhone by remember(profile.phoneNumber) { mutableStateOf(profile.phoneNumber) }
    var selectedCountry by remember(profile.dialCode) {
        mutableStateOf(MockDataProvider.countries.find { it.dialCode == profile.dialCode } ?: MockDataProvider.countries.first())
    }

    var editGender by remember(profile.gender) { mutableStateOf(profile.gender) }
    var editBirthDate by remember(profile.birthDate) { mutableStateOf(profile.birthDate) }
    var editCountry by remember(profile.country) { mutableStateOf(profile.country) }
    var editAddress by remember(profile.address) { mutableStateOf(profile.address) }
    var editPassportNumber by remember(profile.passportNumber) { mutableStateOf(profile.passportNumber) }
    var editPassportCountry by remember(profile.passportCountry) { mutableStateOf(profile.passportCountry) }
    var editPassportExpiryDate by remember(profile.passportExpiryDate) { mutableStateOf(profile.passportExpiryDate) }

    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showPassportExpiryPicker by remember { mutableStateOf(false) }
    var showCountrySelectorSheet by remember { mutableStateOf(false) }
    var showPassportCountrySheet by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        // =================================================================
        // VISTA LOGGED-OUT: HERO CARD CON 10% GENIUS & BOTÓN PROMINENTE
        // =================================================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        NavyDeep,
                                        NavyLight
                                    )
                                )
                            )
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldAccent
                        ) {
                            Text(
                                text = "BENEFICIO GENIUS NIVEL 1",
                                color = NavyDeep,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // TITULAR EXACTO SEGÚN REQUERIMIENTO
                        Text(
                            text = "Inicia sesión para gestionar tu viaje y ahorrar un 10% en estancias seleccionadas",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            lineHeight = MaterialTheme.typography.titleLarge.lineHeight
                        )

                        Text(
                            text = "Disfruta de tarifas exclusivas para miembros, sincronización en tiempo real de tus reservas y guardado de tarjetas para pagar en 1 clic.",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // BOTÓN PROMINENTE EXACTO
            item {
                Button(
                    onClick = { showAuthModal = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inicia sesión o hazte una cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Ventajas de viajar con Lumina Hotels:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                            Text("10% de descuento directo en hoteles participantes", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                            Text("Gestión de check-in y cancelaciones sin costo", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                            Text("Tus métodos de pago y facturación organizados", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    } else {
        // =================================================================
        // VISTA LOGGED-IN: PERFIL COMPLETO (DATOS, TARJETAS, TRANSACCIONES)
        // =================================================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Cabecera del Usuario
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = profile.fullName.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(profile.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(profile.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            GeniusDiscountBadge(text = "Genius Nivel 1 • 10% dto.")
                        }
                    }
                }
            }

            // PROMINENTE BOTÓN DE PANEL ADMIN (SOLO PARA ADMINISTRADORES)
            if (profile.isAdmin) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdminDashboard = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Panel Admin", tint = Color.White)
                                    }
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Panel Admin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                                            Text("ADMIN", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text("Inventario en tiempo real de Alojamientos y Atracciones", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Abrir Panel Admin", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 1. MODIFICAR MIS DATOS PERSONALES
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Modificar mis datos personales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        ValidatedTextField(
                            value = editFullName,
                            onValueChange = { editFullName = it },
                            label = "Nombre completo",
                            leadingIcon = Icons.Default.Person
                        )

                        ValidatedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = "Correo electrónico",
                            leadingIcon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        // Selector de Prefijo + Teléfono
                        Text("Número de teléfono de contacto:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CountryCodeButton(
                                selectedCountry = selectedCountry,
                                onClick = { showCountryPicker = true }
                            )

                            ValidatedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = "Teléfono",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 1. GÉNERO
                        val genderOptions = listOf("Masculino", "Femenino", "No binario", "Prefiero no decirlo")
                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = !genderExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = editGender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Género") },
                                leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                genderOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            editGender = opt
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. FECHA DE NACIMIENTO (+18 AÑOS)
                        Box(modifier = Modifier.fillMaxWidth().clickable { showBirthDatePicker = true }) {
                            OutlinedTextField(
                                value = editBirthDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Fecha de nacimiento (DD/MM/AAAA)") },
                                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // 3. PAÍS O REGIÓN
                        CountrySelectorButton(
                            selectedCountryName = editCountry,
                            flagEmoji = MockDataProvider.countries.find { it.name.equals(editCountry, ignoreCase = true) }?.flagEmoji ?: "🇸🇻",
                            onClick = { showCountrySelectorSheet = true },
                            label = "País o región"
                        )

                        // 4. DIRECCIÓN
                        ValidatedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = "Dirección de residencia",
                            leadingIcon = Icons.Default.LocationOn,
                            singleLine = false
                        )

                        // 5. DATOS DEL PASAPORTE (SUBSECCIÓN)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "Datos del pasaporte",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        ValidatedTextField(
                            value = editPassportNumber,
                            onValueChange = { editPassportNumber = it.uppercase().take(12) },
                            label = "Número de pasaporte (6–12 caracteres)",
                            leadingIcon = Icons.Default.Badge,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                        )

                        CountrySelectorButton(
                            selectedCountryName = editPassportCountry,
                            flagEmoji = MockDataProvider.countries.find { it.name.equals(editPassportCountry, ignoreCase = true) }?.flagEmoji ?: "🇸🇻",
                            onClick = { showPassportCountrySheet = true },
                            label = "País emisor del pasaporte"
                        )

                        Box(modifier = Modifier.fillMaxWidth().clickable { showPassportExpiryPicker = true }) {
                            OutlinedTextField(
                                value = editPassportExpiryDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Fecha de vencimiento del pasaporte (DD/MM/AAAA)") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.actualizarPerfil(
                                    fullName = editFullName,
                                    email = editEmail,
                                    dialCode = selectedCountry.dialCode,
                                    phoneNumber = editPhone,
                                    gender = editGender,
                                    birthDate = editBirthDate,
                                    country = editCountry,
                                    address = editAddress,
                                    passportNumber = editPassportNumber,
                                    passportCountry = editPassportCountry,
                                    passportExpiryDate = editPassportExpiryDate
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar cambios", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. MÉTODOS DE PAGO (PAGO SEGURO STRIPE)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Pagos seguros cifrados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Por su seguridad y cumplimiento PCI-DSS, la app no almacena datos de tarjetas de crédito. Los pagos en línea se procesan de forma cifrada mediante Stripe PaymentSheet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. TRANSACCIONES PASADAS
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Transacciones pasadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (pastTransactions.isEmpty()) {
                            Text("Aún no tienes transacciones registradas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            pastTransactions.forEach { tx ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tx.title,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                                            )
                                            Box(modifier = Modifier.wrapContentWidth()) {
                                                EstadoBadge(tx.status)
                                            }
                                        }
                                        Text("Fecha: ${tx.date} • Ref: ${tx.bookingReference}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(tx.paymentMethod, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                text = "US$${String.format("%.2f", tx.amountPaid)}",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. CERRAR SESIÓN
            item {
                OutlinedButton(
                    onClick = { viewModel.cerrarSesion() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal de Login / Registro
    if (showAuthModal) {
        DialogoLoginRegistro(
            onDismiss = { showAuthModal = false },
            onLogin = { email, pass, name ->
                viewModel.iniciarSesion(email, pass, name)
                showAuthModal = false
            }
        )
    }

    // Selector de Prefijo Telefónico
    if (showCountryPicker) {
        SearchableCountryCodeSheet(
            selectedCountry = selectedCountry,
            onCountrySelected = {
                selectedCountry = it
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }

    if (showBirthDatePicker) {
        val initDate = try {
            LocalDate.parse(editBirthDate, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) { LocalDate.now().minusYears(25) }
        BirthDatePickerModalDialog(
            initialDate = initDate,
            onDismiss = { showBirthDatePicker = false },
            onDateSelected = { date ->
                editBirthDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                showBirthDatePicker = false
            }
        )
    }

    if (showPassportExpiryPicker) {
        val initDate = try {
            LocalDate.parse(editPassportExpiryDate, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) { LocalDate.now().plusYears(5) }
        PassportExpiryDatePickerModalDialog(
            initialDate = initDate,
            onDismiss = { showPassportExpiryPicker = false },
            onDateSelected = { date ->
                editPassportExpiryDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                showPassportExpiryPicker = false
            }
        )
    }

    if (showCountrySelectorSheet) {
        val currentCountry = MockDataProvider.countries.find { it.name.equals(editCountry, ignoreCase = true) } ?: selectedCountry
        SearchableCountryCodeSheet(
            selectedCountry = currentCountry,
            onCountrySelected = {
                editCountry = it.name
                showCountrySelectorSheet = false
            },
            onDismiss = { showCountrySelectorSheet = false }
        )
    }

    if (showPassportCountrySheet) {
        val currentCountry = MockDataProvider.countries.find { it.name.equals(editPassportCountry, ignoreCase = true) } ?: selectedCountry
        SearchableCountryCodeSheet(
            selectedCountry = currentCountry,
            onCountrySelected = {
                editPassportCountry = it.name
                showPassportCountrySheet = false
            },
            onDismiss = { showPassportCountrySheet = false }
        )
    }

    if (showAdminDashboard) {
        if (profile.isAdmin) {
            AdminDashboardDialog(
                viewModel = viewModel,
                onDismiss = { showAdminDashboard = false }
            )
        } else {
            showAdminDashboard = false
        }
    }
}

// =========================================================================
// PANEL ADMIN: GESTIÓN DE INVENTARIO Y ALTAS DE ALOJAMIENTOS Y ATRACCIONES
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardDialog(
    viewModel: HotelViewModel,
    onDismiss: () -> Unit
) {
    var adminTab by remember { mutableIntStateOf(0) }
    var showAddHotelDialog by remember { mutableStateOf(false) }
    var showAddAttractionDialog by remember { mutableStateOf(false) }
    var hotelToEdit by remember { mutableStateOf<Hotel?>(null) }
    var hotelToDelete by remember { mutableStateOf<Hotel?>(null) }
    var attractionToEdit by remember { mutableStateOf<TouristAttraction?>(null) }
    var attractionToDelete by remember { mutableStateOf<TouristAttraction?>(null) }

    val hotelsState by viewModel.hotelsState.collectAsState()
    val attractionsState by viewModel.attractionsState.collectAsState()
    val hotels = (hotelsState as? UiState.Success)?.data ?: emptyList()
    val attractions = (attractionsState as? UiState.Success)?.data ?: emptyList()

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Panel de Administración Lumina", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF22C55E), modifier = Modifier.size(8.dp)) {}
                                    Text("Sincronizado con Base de Datos / SQL Server", style = MaterialTheme.typography.labelSmall, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabRow(
                        selectedTabIndex = adminTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = adminTab == 0,
                            onClick = { adminTab = 0 },
                            text = { Text("Alojamientos", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Hotel, contentDescription = null) }
                        )
                        Tab(
                            selected = adminTab == 1,
                            onClick = { adminTab = 1 },
                            text = { Text("Atracciones", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Explore, contentDescription = null) }
                        )
                    }

                    if (adminTab == 0) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text("Inventario de Alojamientos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                            val totalUnits = hotels.sumOf { h -> h.rooms.sumOf { it.availableRooms } }
                                            Text("$totalUnits unidades disponibles en ${hotels.size} alojamientos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Button(
                                            onClick = { showAddHotelDialog = true },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Agregar Alojamiento")
                                        }
                                    }
                                }
                            }

                            if (hotels.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        icon = Icons.Default.Hotel,
                                        title = "No hay alojamientos registrados",
                                        description = "Comienza agregando el primer alojamiento a la base de datos de Supabase.",
                                        actionText = "Agregar Alojamiento",
                                        onActionClick = { showAddHotelDialog = true }
                                    )
                                }
                            } else {
                                items(hotels, key = { it.id }) { hotel ->
                                    val hotelAvailable = hotel.rooms.sumOf { it.availableRooms }
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                    Text(hotel.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    Text("${hotel.city} • ${hotel.starRating} estrellas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "$hotelAvailable unidades disponibles",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                            hotel.rooms.forEach { room ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = room.name,
                                                                fontWeight = FontWeight.SemiBold,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                                                maxLines = 2,
                                                                softWrap = true
                                                            )
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = if (room.availableRooms > 0) Color(0xFFDCFCE7) else Color(0xFFFFEBEE),
                                                                modifier = Modifier.wrapContentWidth()
                                                            ) {
                                                                Text(
                                                                    text = "${room.availableRooms} disponibles",
                                                                    color = if (room.availableRooms > 0) Color(0xFF15803D) else Color(0xFFC62828),
                                                                    fontWeight = FontWeight.Bold,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                    maxLines = 1,
                                                                    softWrap = false
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "Capacidad: ${room.capacity} huéspedes • US$${String.format("%.2f", room.pricePerNight)} / noche • ${room.availableRooms} de ${room.totalRooms} unidades",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        val ratio = room.availableRooms.toFloat() / room.totalRooms.coerceAtLeast(1)
                                                        LinearProgressIndicator(
                                                            progress = { ratio.coerceIn(0f, 1f) },
                                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { hotelToEdit = hotel },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Editar")
                                                }
                                                OutlinedButton(
                                                    onClick = { hotelToDelete = hotel },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC92A3E))
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC92A3E), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Eliminar")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text("Catálogo de Atracciones", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                            val totalSpots = attractions.sumOf { it.availableSpots }
                                            Text("$totalSpots cupos disponibles en ${attractions.size} atracciones", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Button(
                                            onClick = { showAddAttractionDialog = true },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Agregar Atracción")
                                        }
                                    }
                                }
                            }

                            if (attractions.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        icon = Icons.Default.Explore,
                                        title = "No hay atracciones registradas",
                                        description = "Comienza agregando la primera atracción a la base de datos de Supabase.",
                                        actionText = "Agregar Atracción",
                                        onActionClick = { showAddAttractionDialog = true }
                                    )
                                }
                            } else {
                                items(attractions, key = { it.id }) { attr ->
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(attr.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(end = 8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "US$${String.format("%.2f", attr.pricePerPerson)}",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                            Text("${attr.city} • ${attr.category} • ${attr.duration}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                                        ) {
                                                            Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                            Text("Capacidad de la experiencia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 2, softWrap = true)
                                                        }
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (attr.availableSpots <= 3) Color(0xFFFFEBEE) else Color(0xFFDCFCE7)
                                                        ) {
                                                            Text(
                                                                text = "${attr.availableSpots} / ${attr.minibusCapacity} cupos disponibles",
                                                                fontWeight = FontWeight.Bold,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = if (attr.availableSpots <= 3) Color(0xFFC62828) else Color(0xFF15803D),
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                maxLines = 1,
                                                                softWrap = false
                                                            )
                                                        }
                                                    }
                                                    val occupancy = (attr.minibusCapacity - attr.availableSpots).toFloat() / attr.minibusCapacity.coerceAtLeast(1)
                                                    LinearProgressIndicator(
                                                        progress = { occupancy.coerceIn(0f, 1f) },
                                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                        color = if (attr.availableSpots <= 3) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    Text(
                                                        text = "Capacidad: ${attr.minibusCapacity} cupos (${attr.minibusCapacity - attr.availableSpots} reservados, ${attr.availableSpots} disponibles)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { attractionToEdit = attr },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Editar")
                                                }
                                                OutlinedButton(
                                                    onClick = { attractionToDelete = attr },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC92A3E))
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC92A3E), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Eliminar")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddHotelDialog) {
        DialogoAgregarAlojamiento(
            onDismiss = { showAddHotelDialog = false },
            onUploadImageBytes = { bytes -> viewModel.subirFotoCatalogo(bytes, "accommodations") },
            onSave = { name, city, stars, desc, amenities, img, roomName, price, cap, availableRooms ->
                viewModel.crearAlojamiento(
                    name = name,
                    city = city,
                    starRating = stars,
                    description = desc,
                    amenities = amenities,
                    imageUrl = img,
                    roomName = roomName,
                    pricePerNight = price,
                    capacity = cap,
                    initialAvailableRooms = availableRooms,
                    onSuccess = { showAddHotelDialog = false }
                )
            }
        )
    }

    if (showAddAttractionDialog) {
        DialogoAgregarAtraccion(
            onDismiss = { showAddAttractionDialog = false },
            onUploadImageBytes = { bytes -> viewModel.subirFotoCatalogo(bytes, "attractions") },
            onSave = { title, city, category, duration, price, desc, img, capacity ->
                viewModel.crearAtraccion(
                    title = title,
                    city = city,
                    category = category,
                    duration = duration,
                    pricePerPerson = price,
                    description = desc,
                    imageUrl = img,
                    minibusCapacity = capacity,
                    onSuccess = { showAddAttractionDialog = false }
                )
            }
        )
    }

    if (hotelToEdit != null) {
        val h = hotelToEdit!!
        DialogoEditarAlojamiento(
            hotel = h,
            onDismiss = { hotelToEdit = null },
            onUploadImageBytes = { bytes -> viewModel.subirFotoCatalogo(bytes, "accommodations") },
            onSave = { name, city, stars, desc, amenities, img, roomName, price, cap, availableRooms ->
                viewModel.actualizarAlojamiento(
                    hotelId = h.id,
                    name = name,
                    city = city,
                    starRating = stars,
                    description = desc,
                    amenities = amenities,
                    imageUrl = img,
                    roomName = roomName,
                    pricePerNight = price,
                    capacity = cap,
                    availableRooms = availableRooms,
                    onSuccess = { hotelToEdit = null }
                )
            }
        )
    }

    if (hotelToDelete != null) {
        val h = hotelToDelete!!
        DialogoConfirmarEliminarAlojamiento(
            hotel = h,
            onDismiss = { hotelToDelete = null },
            onConfirm = {
                viewModel.eliminarAlojamiento(h.id)
                hotelToDelete = null
            }
        )
    }

    if (attractionToEdit != null) {
        val a = attractionToEdit!!
        DialogoEditarAtraccion(
            attraction = a,
            onDismiss = { attractionToEdit = null },
            onUploadImageBytes = { bytes -> viewModel.subirFotoCatalogo(bytes, "attractions") },
            onSave = { title, city, category, duration, price, desc, img, capacity ->
                viewModel.actualizarAtraccionAdmin(
                    attrId = a.id,
                    title = title,
                    city = city,
                    category = category,
                    duration = duration,
                    pricePerPerson = price,
                    description = desc,
                    imageUrl = img,
                    minibusCapacity = capacity,
                    onSuccess = { attractionToEdit = null }
                )
            }
        )
    }

    if (attractionToDelete != null) {
        val a = attractionToDelete!!
        DialogoConfirmarEliminarAtraccion(
            attraction = a,
            onDismiss = { attractionToDelete = null },
            onConfirm = {
                viewModel.eliminarAtraccionAdmin(a.id)
                attractionToDelete = null
            }
        )
    }
}

// =========================================================================
// DIÁLOGOS DE EDICIÓN Y ELIMINACIÓN (CRUD PARITY)
// =========================================================================
@Composable
fun DialogoConfirmarEliminarAlojamiento(
    hotel: Hotel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar alojamiento?", fontWeight = FontWeight.Bold) },
        text = { Text("Esta acción eliminará '${hotel.name}' de la base de datos y del catálogo de hoteles.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC92A3E)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoConfirmarEliminarAtraccion(
    attraction: TouristAttraction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar atracción?", fontWeight = FontWeight.Bold) },
        text = { Text("Esta acción eliminará '${attraction.title}' de la base de datos y del catálogo de atracciones.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC92A3E)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoEditarAlojamiento(
    hotel: Hotel,
    onDismiss: () -> Unit,
    onUploadImageBytes: suspend (ByteArray) -> Result<String>,
    onSave: (
        name: String,
        city: String,
        starRating: Int,
        description: String,
        amenities: List<String>,
        imageUrl: String,
        roomName: String,
        pricePerNight: Double,
        capacity: Int,
        availableRooms: Int
    ) -> Unit
) {
    val room = hotel.rooms.firstOrNull()
    var name by remember { mutableStateOf(hotel.name) }
    var city by remember { mutableStateOf(hotel.city) }
    var starRating by remember { mutableIntStateOf(hotel.starRating) }
    var description by remember { mutableStateOf(hotel.description) }
    var roomName by remember { mutableStateOf(room?.name ?: "Habitación Suite") }
    var pricePerNight by remember { mutableStateOf(room?.pricePerNight?.toString() ?: hotel.pricePerNightStarting.toString()) }
    var capacity by remember { mutableIntStateOf(room?.capacity ?: 2) }
    var availableRooms by remember { mutableIntStateOf(room?.availableRooms ?: 5) }
    var imageUrl by remember { mutableStateOf(hotel.imageUrl) }

    val amenityOptions = listOf("Piscina Infinita", "Spa & Wellness", "Wi-Fi de Alta Velocidad", "Desayuno Buffet", "Gimnasio 24/7", "Valet Parking", "Restaurante Gourmet")
    var selectedAmenities by remember { mutableStateOf(hotel.amenities.toSet()) }

    val isFormValid = name.isNotBlank() && city.isNotBlank() && description.isNotBlank() &&
                      roomName.isNotBlank() && (pricePerNight.toDoubleOrNull() ?: 0.0) > 0.0 &&
                      availableRooms >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Editar Alojamiento", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ValidatedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nombre del hotel *",
                        leadingIcon = Icons.Default.Hotel
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ValidatedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = "Ciudad *",
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Default.LocationOn
                        )
                        ValidatedTextField(
                            value = starRating.toString(),
                            onValueChange = { starRating = it.toIntOrNull()?.coerceIn(1, 5) ?: 5 },
                            label = "Estrellas (1-5)",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.Star
                        )
                    }
                }
                item {
                    ValidatedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = "Tipo de habitación *",
                        leadingIcon = Icons.Default.Bed
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ValidatedTextField(
                            value = pricePerNight,
                            onValueChange = { pricePerNight = it },
                            label = "Precio/noche ($) *",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Huéspedes:", style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilledTonalIconButton(
                                    onClick = { if (capacity > 1) capacity-- },
                                    enabled = capacity > 1
                                ) { Icon(Icons.Default.Remove, contentDescription = null) }
                                Text("$capacity", fontWeight = FontWeight.Bold)
                                FilledTonalIconButton(
                                    onClick = { if (capacity < 8) capacity++ },
                                    enabled = capacity < 8
                                ) { Icon(Icons.Default.Add, contentDescription = null) }
                            }
                        }
                    }
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Cantidad disponible:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Inventario total de unidades",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { if (availableRooms > 0) availableRooms-- },
                                    enabled = availableRooms > 0
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Disminuir cantidad")
                                }
                                Text(
                                    text = "$availableRooms",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.widthIn(min = 28.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                FilledTonalIconButton(
                                    onClick = { if (availableRooms < 999) availableRooms++ },
                                    enabled = availableRooms < 999
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                                }
                            }
                        }
                    }
                }
                item {
                    Text("Amenidades principales:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    WrappingChipRow(horizontalSpacing = 6.dp, verticalSpacing = 6.dp) {
                        amenityOptions.forEach { am ->
                            val selected = selectedAmenities.contains(am)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedAmenities = if (selected) selectedAmenities - am else selectedAmenities + am
                                },
                                label = { Text(am, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
                item {
                    ValidatedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descripción *",
                        singleLine = false,
                        leadingIcon = Icons.Default.Description
                    )
                }
                item {
                    CatalogImagePicker(
                        imageUrl = imageUrl,
                        onImageUrlChange = { imageUrl = it },
                        onUploadImageBytes = onUploadImageBytes
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        city,
                        starRating,
                        description,
                        selectedAmenities.toList(),
                        imageUrl,
                        roomName,
                        pricePerNight.toDoubleOrNull() ?: 100.0,
                        capacity,
                        availableRooms
                    )
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoEditarAtraccion(
    attraction: TouristAttraction,
    onDismiss: () -> Unit,
    onUploadImageBytes: suspend (ByteArray) -> Result<String>,
    onSave: (
        title: String,
        city: String,
        category: String,
        duration: String,
        pricePerPerson: Double,
        description: String,
        imageUrl: String,
        minibusCapacity: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf(attraction.title) }
    var city by remember { mutableStateOf(attraction.city) }
    var category by remember { mutableStateOf(attraction.category) }
    var duration by remember { mutableStateOf(attraction.duration) }
    var pricePerPerson by remember { mutableStateOf(attraction.pricePerPerson.toString()) }
    var description by remember { mutableStateOf(attraction.description) }
    var minibusCapacity by remember { mutableIntStateOf(attraction.minibusCapacity) }
    var imageUrl by remember { mutableStateOf(attraction.imageUrl) }

    val categories = listOf("Naturaleza & Aventura", "Cultural & Tours", "Gastronomía", "Playas & Deportes")
    val isFormValid = title.isNotBlank() && city.isNotBlank() && description.isNotBlank() &&
                      (pricePerPerson.toDoubleOrNull() ?: 0.0) > 0.0 &&
                      minibusCapacity in 8..15

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Editar Atracción", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ValidatedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Título de la atracción turística *",
                        leadingIcon = Icons.Default.Explore
                    )
                }
                item {
                    ValidatedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "Ciudad o Ubicación *",
                        leadingIcon = Icons.Default.LocationOn
                    )
                }
                item {
                    Text("Categoría:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    WrappingChipRow(horizontalSpacing = 6.dp, verticalSpacing = 6.dp) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ValidatedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Duración (ej. 4 horas) *",
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Default.Schedule
                        )
                        ValidatedTextField(
                            value = pricePerPerson,
                            onValueChange = { pricePerPerson = it },
                            label = "Precio p/persona ($) *",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
                item {
                    ValidatedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descripción de la experiencia *",
                        singleLine = false,
                        leadingIcon = Icons.Default.Description
                    )
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Capacidad de la experiencia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        text = "$minibusCapacity cupos",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Capacidad máxima de asistentes configurable entre 8 y 15 cupos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = minibusCapacity.toFloat(),
                                onValueChange = { minibusCapacity = it.toInt() },
                                valueRange = 8f..15f,
                                steps = 7
                            )
                        }
                    }
                }
                item {
                    CatalogImagePicker(
                        imageUrl = imageUrl,
                        onImageUrlChange = { imageUrl = it },
                        onUploadImageBytes = onUploadImageBytes
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        city,
                        category,
                        duration,
                        pricePerPerson.toDoubleOrNull() ?: 50.0,
                        description,
                        imageUrl,
                        minibusCapacity
                    )
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// =========================================================================
// DIÁLOGO CREAR ALOJAMIENTO ("+ AGREGAR ALOJAMIENTO")
// =========================================================================
@Composable
fun DialogoAgregarAlojamiento(
    onDismiss: () -> Unit,
    onUploadImageBytes: suspend (ByteArray) -> Result<String>,
    onSave: (
        name: String,
        city: String,
        starRating: Int,
        description: String,
        amenities: List<String>,
        imageUrl: String,
        roomName: String,
        pricePerNight: Double,
        capacity: Int,
        initialAvailableRooms: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("San Salvador") }
    var starRating by remember { mutableIntStateOf(5) }
    var description by remember { mutableStateOf("") }
    var roomName by remember { mutableStateOf("Suite Deluxe con Balcón") }
    var pricePerNight by remember { mutableStateOf("125.00") }
    var capacity by remember { mutableIntStateOf(2) }
    var initialAvailableRooms by remember { mutableIntStateOf(5) }
    var imageUrl by remember { mutableStateOf("") }

    val amenityOptions = listOf("Piscina Infinita", "Spa & Wellness", "Wi-Fi de Alta Velocidad", "Desayuno Buffet", "Gimnasio 24/7", "Valet Parking", "Restaurante Gourmet")
    var selectedAmenities by remember { mutableStateOf(setOf("Wi-Fi de Alta Velocidad", "Desayuno Buffet", "Piscina Infinita")) }

    val isFormValid = name.isNotBlank() && city.isNotBlank() && description.isNotBlank() &&
                      roomName.isNotBlank() && (pricePerNight.toDoubleOrNull() ?: 0.0) > 0.0 &&
                      initialAvailableRooms >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("+ Agregar Alojamiento", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ValidatedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nombre del Hotel / Resort *",
                        leadingIcon = Icons.Default.Business
                    )
                }
                item {
                    ValidatedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "Ciudad o Destino *",
                        leadingIcon = Icons.Default.LocationOn
                    )
                }
                item {
                    Text("Categoría (Estrellas):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { starRating = star }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star estrellas",
                                    tint = if (star <= starRating) GoldAccent else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
                item {
                    ValidatedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descripción del alojamiento *",
                        singleLine = false,
                        leadingIcon = Icons.Default.Description
                    )
                }
                item {
                    Text("Amenidades principales:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    WrappingChipRow(
                        horizontalSpacing = 6.dp,
                        verticalSpacing = 6.dp
                    ) {
                        amenityOptions.forEach { am ->
                            val selected = selectedAmenities.contains(am)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedAmenities = if (selected) selectedAmenities - am else selectedAmenities + am
                                },
                                label = { Text(am, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Configuración de Unidad & Disponibilidad:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                item {
                    ValidatedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = "Nombre del tipo de habitación *",
                        leadingIcon = Icons.Default.MeetingRoom
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ValidatedTextField(
                            value = pricePerNight,
                            onValueChange = { pricePerNight = it },
                            label = "Precio por noche ($) *",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Capacidad huéspedes:", style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilledTonalIconButton(
                                    onClick = { if (capacity > 1) capacity-- },
                                    enabled = capacity > 1
                                ) { Icon(Icons.Default.Remove, contentDescription = null) }
                                Text("$capacity", fontWeight = FontWeight.Bold)
                                FilledTonalIconButton(
                                    onClick = { if (capacity < 8) capacity++ },
                                    enabled = capacity < 8
                                ) { Icon(Icons.Default.Add, contentDescription = null) }
                            }
                        }
                    }
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Cantidad disponible:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Inventario inicial en base de datos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { if (initialAvailableRooms > 0) initialAvailableRooms-- },
                                    enabled = initialAvailableRooms > 0
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Disminuir cantidad")
                                }
                                Text(
                                    text = "$initialAvailableRooms",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.widthIn(min = 28.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                FilledTonalIconButton(
                                    onClick = { if (initialAvailableRooms < 999) initialAvailableRooms++ },
                                    enabled = initialAvailableRooms < 999
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                                }
                            }
                        }
                    }
                }
                item {
                    CatalogImagePicker(
                        imageUrl = imageUrl,
                        onImageUrlChange = { imageUrl = it },
                        onUploadImageBytes = onUploadImageBytes
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = pricePerNight.toDoubleOrNull() ?: 100.0
                    onSave(
                        name, city, starRating, description, selectedAmenities.toList(),
                        imageUrl, roomName, price, capacity, initialAvailableRooms
                    )
                },
                enabled = isFormValid
            ) {
                Text("Guardar en Base de Datos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// =========================================================================
// DIÁLOGO CREAR ATRACCIÓN TURÍSTICA ("+ AGREGAR ATRACCIÓN")
// =========================================================================
@Composable
fun DialogoAgregarAtraccion(
    onDismiss: () -> Unit,
    onUploadImageBytes: suspend (ByteArray) -> Result<String>,
    onSave: (
        title: String,
        city: String,
        category: String,
        duration: String,
        pricePerPerson: Double,
        description: String,
        imageUrl: String,
        minibusCapacity: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Santa Ana") }
    var category by remember { mutableStateOf("Naturaleza & Aventura") }
    var duration by remember { mutableStateOf("4 horas") }
    var pricePerPerson by remember { mutableStateOf("50.00") }
    var description by remember { mutableStateOf("") }
    var minibusCapacity by remember { mutableIntStateOf(14) }
    var imageUrl by remember { mutableStateOf("") }

    val categories = listOf("Naturaleza & Aventura", "Cultural & Tours", "Gastronomía", "Playas & Deportes")
    val isFormValid = title.isNotBlank() && city.isNotBlank() && description.isNotBlank() &&
                      (pricePerPerson.toDoubleOrNull() ?: 0.0) > 0.0 &&
                      minibusCapacity in 8..15

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("+ Agregar Atracción", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ValidatedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Título de la atracción turística *",
                        leadingIcon = Icons.Default.Explore
                    )
                }
                item {
                    ValidatedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "Ciudad o Ubicación *",
                        leadingIcon = Icons.Default.LocationOn
                    )
                }
                item {
                    Text("Categoría:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    WrappingChipRow(
                        horizontalSpacing = 6.dp,
                        verticalSpacing = 6.dp
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ValidatedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Duración (ej. 4 horas) *",
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Default.Schedule
                        )
                        ValidatedTextField(
                            value = pricePerPerson,
                            onValueChange = { pricePerPerson = it },
                            label = "Precio p/persona ($) *",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
                item {
                    ValidatedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descripción de la experiencia *",
                        singleLine = false,
                        leadingIcon = Icons.Default.Description
                    )
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Capacidad de la experiencia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        text = "$minibusCapacity cupos",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Capacidad máxima de asistentes configurable entre 8 y 15 cupos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Capacidad de cupos:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilledTonalIconButton(
                                        onClick = { if (minibusCapacity > 8) minibusCapacity-- },
                                        enabled = minibusCapacity > 8
                                    ) { Icon(Icons.Default.Remove, contentDescription = null) }
                                    Text("$minibusCapacity", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    FilledTonalIconButton(
                                        onClick = { if (minibusCapacity < 15) minibusCapacity++ },
                                        enabled = minibusCapacity < 15
                                    ) { Icon(Icons.Default.Add, contentDescription = null) }
                                }
                            }
                        }
                    }
                }
                item {
                    CatalogImagePicker(
                        imageUrl = imageUrl,
                        onImageUrlChange = { imageUrl = it },
                        onUploadImageBytes = onUploadImageBytes
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = pricePerPerson.toDoubleOrNull() ?: 50.0
                    onSave(title, city, category, duration, price, description, imageUrl, minibusCapacity)
                },
                enabled = isFormValid
            ) {
                Text("Guardar en Base de Datos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// =========================================================================
// DIÁLOGO UNIFICADO DE INICIAR SESIÓN O CREAR CUENTA
// =========================================================================
@Composable
private fun DialogoLoginRegistro(
    onDismiss: () -> Unit,
    onLogin: (String, String, String) -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    val errorEmail = remember(email) {
        if (email.isEmpty()) null else FormValidator.validateEmail(email).errorMessageOrNull
    }
    val errorPass = remember(password) {
        if (password.isEmpty()) null else FormValidator.validatePassword(password).errorMessageOrNull
    }

    val isValid = errorEmail == null && errorPass == null && email.isNotBlank() && password.isNotBlank() &&
        (!isSignUp || fullName.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isSignUp) "Hazte una cuenta en Lumina" else "Inicia sesión en Lumina",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isSignUp) {
                    ValidatedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Nombre completo",
                        leadingIcon = Icons.Default.Person
                    )
                }

                ValidatedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    leadingIcon = Icons.Default.Email,
                    errorText = errorEmail,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )

                ValidatedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    errorText = errorPass,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                )

                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isSignUp) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLogin(email, password, if (isSignUp) fullName else "") },
                enabled = isValid
            ) {
                Text(if (isSignUp) "Crear cuenta" else "Iniciar sesión", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// =========================================================================
// 4. DETALLE DE HOTEL & SELECCIÓN DE HABITACIONES CON FOTOS ESPECÍFICAS
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    hotel: Hotel,
    viewModel: HotelViewModel,
    onBack: () -> Unit,
    onSelectRoom: (Hotel, HotelRoom) -> Unit
) {
    val checkIn by viewModel.checkInDate.collectAsState()
    val checkOut by viewModel.checkOutDate.collectAsState()
    val nights by viewModel.stayNights.collectAsState()
    val guests by viewModel.guestsCount.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val favoriteIds by viewModel.favoriteHotelIds.collectAsState()
    val isFav = favoriteIds.contains(hotel.id)

    var showDateRangePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hotel.name, maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    FavoriteHeartButton(
                        isFavorite = isFav,
                        onToggle = { viewModel.toggleFavoriteHotel(hotel.id) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
        ) {
            // Hero Photo / Carousel
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(210.dp)) {
                        ImageCarousel(
                            images = hotel.galleryImages.take(5),
                            contentDescription = hotel.name,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isLoggedIn && hotel.hasGeniusDiscount) {
                            GeniusDiscountBadge(
                                text = "10% dto. Genius aplicado",
                                modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                StarRatingRow(stars = hotel.starRating)
                            }
                            Box(modifier = Modifier.wrapContentWidth()) {
                                BookingRatingBadge(score = hotel.reviewScore, label = hotel.reviewLabel, reviewsCount = hotel.reviewCount)
                            }
                        }
                        Text(text = hotel.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(hotel.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = hotel.description, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Servicios más populares:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        WrappingChipRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalSpacing = 8.dp,
                            verticalSpacing = 8.dp
                        ) {
                            hotel.amenities.take(6).forEach { AmenityChip(it) }
                        }
                    }
                }
            }

            // Banner interactivo de fechas seleccionadas
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Tu estancia:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$checkIn al $checkOut ($nights ${if (nights == 1) "noche" else "noches"} • $guests adultos)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        CambiarAccionButton(
                            onClick = { showDateRangePicker = true },
                            text = "Cambiar",
                            icon = Icons.Default.EditCalendar
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Habitaciones disponibles con diseño exclusivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(hotel.rooms, key = { it.roomId }) { room ->
                val discount = if (isLoggedIn && hotel.hasGeniusDiscount) 0.90 else 1.0
                val roomNightly = room.pricePerNight * discount
                val roomSubtotal = roomNightly * nights

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // FOTO DE ALTA RESOLUCIÓN ESPECÍFICA DE LA HABITACIÓN
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            LuminaNetworkImage(
                                url = room.imageUrl,
                                contentDescription = room.name,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = RoundedCornerShape(topStart = 8.dp),
                                color = NavyDeep.copy(alpha = 0.8f),
                                modifier = Modifier.align(Alignment.BottomEnd)
                            ) {
                                Text(
                                    text = "${room.sizeM2} m² • Capacidad ${room.capacity}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = room.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.KingBed, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(room.bedConfiguration, style = MaterialTheme.typography.bodySmall)
                            }

                            if (room.freeCancellation) {
                                Text("✓ Cancelación GRATIS antes de 24h", color = Color(0xFF15803D), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            if (room.breakfastIncluded) {
                                Text("✓ Desayuno buffet incluido", color = Color(0xFF15803D), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "US$${String.format("%.2f", roomNightly)} / noche",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "US$${String.format("%.2f", roomSubtotal)} total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Button(
                                    onClick = { onSelectRoom(hotel, room) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Reservar Ahora")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDateRangePicker) {
        DateRangePickerModalDialog(
            initialCheckIn = checkIn,
            initialCheckOut = checkOut,
            onDismiss = { showDateRangePicker = false },
            onDatesSelected = { s, e, _ ->
                viewModel.actualizarRangoFechas(s, e)
                showDateRangePicker = false
            }
        )
    }
}

// =========================================================================
// 5. CHECKOUT CON SELECTOR DE PAÍSES Y AUTO-SLASH MM/AA
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelCheckoutScreen(
    hotel: Hotel,
    room: HotelRoom,
    viewModel: HotelViewModel,
    onBack: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    val checkIn by viewModel.checkInDate.collectAsState()
    val checkOut by viewModel.checkOutDate.collectAsState()
    val nights by viewModel.stayNights.collectAsState()
    val guests by viewModel.guestsCount.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var fullName by remember { mutableStateOf(if (isLoggedIn) profile.fullName else "") }
    var email by remember { mutableStateOf(if (isLoggedIn) profile.email else "") }
    var phone by remember { mutableStateOf(if (isLoggedIn) profile.phoneNumber else "") }
    var selectedCountry by remember {
        mutableStateOf(MockDataProvider.countries.find { it.dialCode == profile.dialCode } ?: MockDataProvider.countries.first())
    }
    var showCountryPicker by remember { mutableStateOf(false) }

    var specialRequests by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("PayPal / Tarjeta en Línea (PayPal Sandbox)") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Validaciones
    val errorName = remember(fullName) {
        if (fullName.isEmpty()) null else FormValidator.validateName(fullName, "Nombre completo").errorMessageOrNull
    }
    val errorEmail = remember(email) {
        if (email.isEmpty()) null else FormValidator.validateEmail(email).errorMessageOrNull
    }
    val errorPhone = remember(phone) {
        if (phone.isEmpty()) null else FormValidator.validatePhone(phone, isRequired = true).errorMessageOrNull
    }

    val isFormValid = errorName == null && errorEmail == null && errorPhone == null &&
        fullName.isNotBlank() && email.isNotBlank() && phone.isNotBlank()

    val rawSubtotal = room.pricePerNight * nights
    val hasGeniusDiscount = isLoggedIn && hotel.hasGeniusDiscount
    val discountAmount = if (hasGeniusDiscount) rawSubtotal * 0.10 else 0.0
    val discountedSubtotal = rawSubtotal - discountAmount
    val taxes = discountedSubtotal * 0.13
    val total = discountedSubtotal + taxes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Reserva", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
        ) {
            // Resumen del Alojamiento
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(hotel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(room.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Entrada", style = MaterialTheme.typography.labelSmall)
                                Text(checkIn.toString(), fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Salida", style = MaterialTheme.typography.labelSmall)
                                Text(checkOut.toString(), fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Duración", style = MaterialTheme.typography.labelSmall)
                                Text("$nights ${if (nights == 1) "noche" else "noches"} • $guests pers.", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Desglose de Precios con Descuento Real del 10% (Genius)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Resumen de Cobro", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (hasGeniusDiscount) {
                                GeniusDiscountBadge(text = "10% dto. Genius aplicado")
                            }
                        }

                        if (hasGeniusDiscount) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFECFDF5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "¡Beneficio Genius Nivel 1! Ahorras US$${String.format("%.2f", discountAmount)} por tener tu sesión iniciada.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else if (!isLoggedIn && hotel.hasGeniusDiscount) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Inicia sesión para activar el 10% de descuento Genius en este alojamiento.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Habitación ($nights ${if (nights == 1) "noche" else "noches"})")
                            Text("US$${String.format("%.2f", rawSubtotal)}")
                        }

                        if (hasGeniusDiscount) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Descuento Genius (10%)", color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                Text("-US$${String.format("%.2f", discountAmount)}", color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal neto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("US$${String.format("%.2f", discountedSubtotal)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Impuestos y cargos (13% IVA)")
                            Text("US$${String.format("%.2f", taxes)}")
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Precio Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "US$${String.format("%.2f", total)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Datos del Huésped
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Tus datos de contacto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        ValidatedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "Nombre completo del huésped",
                            leadingIcon = Icons.Default.Person,
                            errorText = errorName,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        ValidatedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico (confirmación)",
                            leadingIcon = Icons.Default.Email,
                            errorText = errorEmail,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )

                        // Selector de Prefijo Telefónico y Número
                        Text("Teléfono con código de país:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CountryCodeButton(
                                selectedCountry = selectedCountry,
                                onClick = { showCountryPicker = true }
                            )

                            ValidatedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = "Número de teléfono",
                                errorText = errorPhone,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        ValidatedTextField(
                            value = specialRequests,
                            onValueChange = { specialRequests = it },
                            label = "Peticiones especiales (opcional)",
                            leadingIcon = Icons.Default.ChatBubbleOutline,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }
            }

            // Método de Pago
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Método de pago", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        val paymentOptions = listOf("PayPal / Tarjeta en Línea (PayPal Sandbox)", "Pagar en el Alojamiento")

                        FilterChipRow(
                            options = paymentOptions,
                            selectedOption = paymentMethod,
                            onOptionSelected = { paymentMethod = it }
                        )

                        if (paymentMethod.equals("Pagar en el Alojamiento", ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "El pago se realiza directamente en recepción al momento del check-in en efectivo o con tarjeta física y sin cargos por adelantado.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botón de Confirmación
            item {
                Button(
                    onClick = {
                        val fullPhone = "${selectedCountry.dialCode} ${phone.trim()}"
                        if (paymentMethod.equals("Pagar en el Alojamiento", ignoreCase = true)) {
                            viewModel.confirmarReservaHotel(
                                hotel = hotel,
                                room = room,
                                guestFullName = fullName,
                                guestEmail = email,
                                guestPhone = fullPhone,
                                paymentMethod = "PAY_AT_PROPERTY",
                                specialRequests = specialRequests.ifBlank { null },
                                onSuccess = onBookingSuccess
                            )
                        } else {
                            coroutineScope.launch {
                                viewModel.createPayPalOrder(total).onSuccess { orderRes ->
                                    PaymentGatewayManager.openPayPalCheckout(context, orderRes.approveUrl)
                                    viewModel.confirmarReservaHotel(
                                        hotel = hotel,
                                        room = room,
                                        guestFullName = fullName,
                                        guestEmail = email,
                                        guestPhone = fullPhone,
                                        paymentMethod = "PAYPAL_EL_SALVADOR (${orderRes.orderId})",
                                        specialRequests = specialRequests.ifBlank { null },
                                        onSuccess = onBookingSuccess
                                    )
                                }.onFailure {
                                    viewModel.confirmarReservaHotel(
                                        hotel = hotel,
                                        room = room,
                                        guestFullName = fullName,
                                        guestEmail = email,
                                        guestPhone = fullPhone,
                                        paymentMethod = "PAYPAL_EL_SALVADOR (Sandbox Fallback)",
                                        specialRequests = specialRequests.ifBlank { null },
                                        onSuccess = onBookingSuccess
                                    )
                                }
                            }
                        }
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Completar Reserva con PayPal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCountryPicker) {
        SearchableCountryCodeSheet(
            selectedCountry = selectedCountry,
            onCountrySelected = {
                selectedCountry = it
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }
}

// =========================================================================
// 6. CONFIRMACIÓN CELEBRATORIA DE RESERVA
// =========================================================================
@Composable
fun BookingConfirmationScreen(
    booking: CustomerHotelBooking,
    onViewItinerary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFDCFCE7),
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("¡Reserva Confirmada!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            text = "Te hemos enviado la confirmación a ${booking.guestEmail}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Código: ${booking.bookingReference}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(booking.hotelName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(booking.roomName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Fechas: ${booking.checkInDate} al ${booking.checkOutDate} (${booking.nights} noches)")
                Text("Titular: ${booking.guestFullName}")
                Text(
                    text = "Total: US$${String.format("%.2f", booking.totalPrice)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onViewItinerary,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver en Mis Reservas", fontWeight = FontWeight.Bold)
        }
    }
}

// =========================================================================
// 7. DIÁLOGO DETALLE DE ATRACCIÓN Y COMPRA DE BOLETOS
// =========================================================================
@Composable
fun DialogoAtraccionDetalleYReserva(
    attraction: TouristAttraction,
    viewModel: HotelViewModel,
    onDismiss: () -> Unit
) {
    var ticketsCount by remember { mutableIntStateOf(2) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(2)) }
    var showDatePicker by remember { mutableStateOf(false) }

    val profile by viewModel.userProfile.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var leadName by remember { mutableStateOf(if (isLoggedIn) profile.fullName else "") }
    var leadEmail by remember { mutableStateOf(if (isLoggedIn) profile.email else "") }
    var leadPhone by remember { mutableStateOf(if (isLoggedIn) profile.phoneNumber else "") }

    val errorName = remember(leadName) {
        if (leadName.isEmpty()) null else FormValidator.validateName(leadName, "Nombre del titular").errorMessageOrNull
    }
    val errorEmail = remember(leadEmail) {
        if (leadEmail.isEmpty()) null else FormValidator.validateEmail(leadEmail).errorMessageOrNull
    }
    val errorPhone = remember(leadPhone) {
        if (leadPhone.isEmpty()) null else FormValidator.validatePhone(leadPhone, isRequired = true).errorMessageOrNull
    }

    val isFormValid = errorName == null && errorEmail == null && errorPhone == null &&
        leadName.isNotBlank() && leadEmail.isNotBlank() && leadPhone.isNotBlank()

    val total = ticketsCount * attraction.pricePerPerson

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(attraction.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    ImageCarousel(
                        images = attraction.galleryImages.take(5),
                        contentDescription = attraction.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                item {
                    Text(attraction.description, style = MaterialTheme.typography.bodySmall)
                }

                item {
                    Text("Incluye:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    attraction.highlights.forEach { h ->
                        Text("• $h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Información de capacidad y disponibilidad de la atracción
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text("Capacidad de la experiencia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                                Text(
                                    text = "${attraction.availableSpots} / ${attraction.minibusCapacity} cupos disponibles",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attraction.availableSpots <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                            val occupancy = (attraction.minibusCapacity - attraction.availableSpots).toFloat() / attraction.minibusCapacity.coerceAtLeast(1)
                            LinearProgressIndicator(
                                progress = { occupancy.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (attraction.availableSpots <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "Capacidad máxima de ${attraction.minibusCapacity} asistentes. Quedan ${attraction.availableSpots} cupos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Selector de Fecha
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Fecha de la experiencia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedDate.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            CambiarAccionButton(
                                onClick = { showDatePicker = true },
                                text = "Cambiar",
                                icon = Icons.Default.CalendarToday
                            )
                        }
                    }
                }

                // Contador de Entradas
                item {
                    val maxTickets = minOf(attraction.availableSpots, 15)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cantidad de entradas:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            if (maxTickets <= 0) {
                                Text("Sin cupos disponibles", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (ticketsCount > 1) ticketsCount-- },
                                enabled = ticketsCount > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Menos")
                            }
                            Text("$ticketsCount", fontWeight = FontWeight.Bold)
                            FilledTonalIconButton(
                                onClick = { if (ticketsCount < maxTickets) ticketsCount++ },
                                enabled = ticketsCount < maxTickets && maxTickets > 0
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Más")
                            }
                        }
                    }
                }

                // Datos de contacto
                item {
                    ValidatedTextField(
                        value = leadName,
                        onValueChange = { leadName = it },
                        label = "Nombre del titular",
                        errorText = errorName
                    )
                }
                item {
                    ValidatedTextField(
                        value = leadEmail,
                        onValueChange = { leadEmail = it },
                        label = "Correo para tus entradas",
                        errorText = errorEmail,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
                item {
                    ValidatedTextField(
                        value = leadPhone,
                        onValueChange = { leadPhone = it },
                        label = "Teléfono de contacto",
                        errorText = errorPhone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total ($ticketsCount boletos):", fontWeight = FontWeight.Bold)
                            Text("US$${String.format("%.2f", total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.confirmarReservaAtraccion(
                        attraction = attraction,
                        date = selectedDate,
                        ticketsCount = ticketsCount,
                        leadGuestName = leadName,
                        leadGuestEmail = leadEmail,
                        leadGuestPhone = leadPhone,
                        paymentMethod = "Tarjeta / En Línea",
                        onSuccess = onDismiss
                    )
                },
                enabled = isFormValid && attraction.availableSpots >= ticketsCount && ticketsCount > 0
            ) {
                Text("Confirmar Boletos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showDatePicker) {
        SingleDatePickerModalDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            }
        )
    }
}

// =========================================================================
// 8. PESTAÑA: "RESERVAS" (ITINERARIO UNIFICADO DE HOTELES Y ATRACCIONES)
// =========================================================================
@Composable
fun MyBookingsScreen(
    viewModel: HotelViewModel,
    onExploreStays: () -> Unit
) {
    val hotelBookings by viewModel.customerHotelBookings.collectAsState()
    val attractionBookings by viewModel.customerAttractionBookings.collectAsState()
    val filtro by viewModel.filtroTipoItinerario.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var bookingACancelar by remember { mutableStateOf<CustomerHotelBooking?>(null) }
    var ticketACancelar by remember { mutableStateOf<CustomerAttractionBooking?>(null) }

    val isEmpty = when (filtro) {
        "Hoteles" -> hotelBookings.isEmpty()
        "Atracciones" -> attractionBookings.isEmpty()
        else -> hotelBookings.isEmpty() && attractionBookings.isEmpty()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        if (!isLoggedIn || profile.email.isBlank()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Lock,
                    title = "Inicia sesión para ver tus reservas",
                    description = "Por tu privacidad y seguridad, tus reservas confirmadas e itinerarios solo son visibles cuando has iniciado sesión en tu cuenta.",
                    actionText = "Explorar Alojamientos",
                    onActionClick = onExploreStays
                )
            }
        } else {
            item {
                FilterChipRow(
                    options = listOf("Todas", "Hoteles", "Atracciones"),
                    selectedOption = filtro,
                    onOptionSelected = { viewModel.filtroTipoItinerario.value = it }
                )
            }

            if (isEmpty) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Luggage,
                        title = "No tienes reservas registradas",
                        description = "Tus reservas de alojamientos y entradas para atracciones aparecerán aquí ordenadas por fecha y estado.",
                        actionText = "Explorar Alojamientos",
                        onActionClick = onExploreStays
                    )
                }
            } else {
                // Sección Hoteles
                if (filtro == "Todas" || filtro == "Hoteles") {
                    items(hotelBookings, key = { it.bookingReference }) { b ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text("Estadía • Ref: ${b.bookingReference}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    EstadoBadge(b.status)
                                }

                                Text(b.hotelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(b.roomName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Fechas: ${b.checkInDate} al ${b.checkOutDate} (${b.nights} ${if (b.nights == 1) "noche" else "noches"} • ${b.guestsCount} pers.)", style = MaterialTheme.typography.bodySmall)
                                Text("Titular: ${b.guestFullName} • ${b.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "US$${String.format("%.2f", b.totalPrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (b.status == "CONFIRMADA") {
                                        Surface(
                                            onClick = { bookingACancelar = b },
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFF1F2),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                                            modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp),
                                                    tint = Color(0xFFE11D48)
                                                )
                                                Text(
                                                    text = "Cancelar Reserva",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE11D48),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sección Atracciones
                if (filtro == "Todas" || filtro == "Atracciones") {
                    items(attractionBookings, key = { it.ticketReference }) { a ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text("Actividad • Ref: ${a.ticketReference}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    EstadoBadge(a.status)
                                }

                                Text(a.attractionTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Fecha de actividad: ${a.date} • ${a.ticketsCount} ${if (a.ticketsCount == 1) "boleto" else "boletos"}", style = MaterialTheme.typography.bodySmall)
                                Text("Titular: ${a.leadGuestName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "US$${String.format("%.2f", a.totalPrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (a.status == "CONFIRMADA") {
                                        Surface(
                                            onClick = { ticketACancelar = a },
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFF1F2),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                                            modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp),
                                                    tint = Color(0xFFE11D48)
                                                )
                                                Text(
                                                    text = "Cancelar Entrada",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE11D48),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    bookingACancelar?.let { b ->
        ConfirmationDialog(
            title = "Cancelar Reserva de Hotel",
            message = "¿Deseas cancelar tu estancia en ${b.hotelName} (Ref: ${b.bookingReference})? No se aplicará ninguna penalidad.",
            confirmButtonText = "Sí, Cancelar",
            isDestructive = true,
            onConfirm = {
                viewModel.cancelarReservaHotel(b.bookingReference)
                bookingACancelar = null
            },
            onDismiss = { bookingACancelar = null }
        )
    }

    ticketACancelar?.let { t ->
        ConfirmationDialog(
            title = "Cancelar Actividad Turística",
            message = "¿Deseas cancelar tus entradas para ${t.attractionTitle} (Ref: ${t.ticketReference})?",
            confirmButtonText = "Sí, Cancelar",
            isDestructive = true,
            onConfirm = {
                viewModel.cancelarReservaAtraccion(t.ticketReference)
                ticketACancelar = null
            },
            onDismiss = { ticketACancelar = null }
        )
    }
}
