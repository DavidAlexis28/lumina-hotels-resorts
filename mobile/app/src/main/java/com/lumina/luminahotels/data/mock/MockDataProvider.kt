package com.lumina.luminahotels.data.mock

import com.lumina.luminahotels.model.*
import java.time.LocalDate

/**
 * Fuente de datos enriquecida y realista para la experiencia B2C (Estilo Booking.com).
 * Provee hoteles de lujo, habitaciones con imágenes arquitectónicas únicas,
 * experiencias turísticas curadas, selector de países, tarjetas y transacciones.
 */
object MockDataProvider {

    val countries: List<CountryDialCode> = listOf(
        CountryDialCode("SV", "+503", "El Salvador", "🇸🇻"),
        CountryDialCode("US", "+1", "Estados Unidos", "🇺🇸"),
        CountryDialCode("MX", "+52", "México", "🇲🇽"),
        CountryDialCode("ES", "+34", "España", "🇪🇸"),
        CountryDialCode("GT", "+502", "Guatemala", "🇬🇹"),
        CountryDialCode("HN", "+504", "Honduras", "🇭🇳"),
        CountryDialCode("CR", "+506", "Costa Rica", "🇨🇷"),
        CountryDialCode("PA", "+507", "Panamá", "🇵🇦"),
        CountryDialCode("CO", "+57", "Colombia", "🇨🇴"),
        CountryDialCode("AR", "+54", "Argentina", "🇦🇷"),
        CountryDialCode("CL", "+56", "Chile", "🇨🇱"),
        CountryDialCode("PE", "+51", "Perú", "🇵🇪"),
        CountryDialCode("EC", "+593", "Ecuador", "🇪🇨"),
        CountryDialCode("DO", "+1809", "República Dominicana", "🇩🇴"),
        CountryDialCode("CA", "+1", "Canadá", "🇨🇦"),
        CountryDialCode("GB", "+44", "Reino Unido", "🇬🇧"),
        CountryDialCode("DE", "+49", "Alemania", "🇩🇪"),
        CountryDialCode("FR", "+33", "Francia", "🇫🇷"),
        CountryDialCode("IT", "+39", "Italia", "🇮🇹"),
        CountryDialCode("BR", "+55", "Brasil", "🇧🇷")
    )

    val defaultHotels: List<Hotel> = listOf(
        Hotel(
            id = "hotel-1",
            name = "Lumina Grand Palace Resort & Spa",
            city = "San Salvador",
            address = "Av. Las Magnolias 102, Colonia San Benito",
            starRating = 5,
            reviewScore = 9.4,
            reviewCount = 1240,
            reviewLabel = "Excepcional",
            description = "Un oasis de lujo en el corazón de la zona más exclusiva. Cuenta con spa de clase mundial, piscina infinita climatizada, jardines tropicales y tres restaurantes de alta cocina internacional.",
            amenities = listOf("Piscina Infinita", "Spa & Wellness", "Wi-Fi de Alta Velocidad", "Desayuno Buffet", "Gimnasio 24/7", "Valet Parking"),
            pricePerNightStarting = 115.0,
            featuredTag = "Más Vendido",
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
            hasGeniusDiscount = true,
            rooms = listOf(
                HotelRoom(
                    roomId = 1,
                    hotelId = "hotel-1",
                    name = "Habitación Deluxe Queen Superior",
                    bedConfiguration = "1 cama Queen extragrande",
                    capacity = 2,
                    sizeM2 = 38,
                    pricePerNight = 115.0,
                    amenities = listOf("Vista a jardines", "Smart TV 55\"", "Cafetera Nespresso", "Bata de baño de lujo"),
                    imageUrl = "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 1,
                    backendTipoId = 1
                ),
                HotelRoom(
                    roomId = 3,
                    hotelId = "hotel-1",
                    name = "Suite Junior Ejecutiva con Balcón",
                    bedConfiguration = "1 cama King + 1 sofá cama",
                    capacity = 3,
                    sizeM2 = 54,
                    pricePerNight = 175.0,
                    amenities = listOf("Balcón privado", "Área de estar", "Tina de hidromasaje", "Acceso al Executive Lounge"),
                    imageUrl = "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 3,
                    backendTipoId = 2
                ),
                HotelRoom(
                    roomId = 5,
                    hotelId = "hotel-1",
                    name = "Villa Presidencial Lumina",
                    bedConfiguration = "2 camas King en dormitorios independientes",
                    capacity = 4,
                    sizeM2 = 98,
                    pricePerNight = 310.0,
                    amenities = listOf("Piscina privada tipo plunge", "Mayordomo dedicado", "Cava de vinos", "Terraza panorámica"),
                    imageUrl = "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 5,
                    backendTipoId = 3
                )
            )
        ),
        Hotel(
            id = "hotel-2",
            name = "Lumina Royal Ocean Beach Resort",
            city = "Costa del Sol",
            address = "Km 72 Bulevar Costa del Sol, La Paz",
            starRating = 5,
            reviewScore = 9.6,
            reviewCount = 980,
            reviewLabel = "Excepcional",
            description = "Resort de lujo frente al mar pacífico con acceso exclusivo a playa de arena dorada, club de playa privado, paseos en catamarán y gastronomía marina contemporánea.",
            amenities = listOf("Acceso a Playa Privada", "3 Piscinas frente al mar", "Bar en la piscina", "Deportes acuáticos", "Restaurante de Mariscos", "Spa Oceanview"),
            pricePerNightStarting = 185.0,
            featuredTag = "Frente a la Playa",
            imageUrl = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80",
            hasGeniusDiscount = true,
            rooms = listOf(
                HotelRoom(
                    roomId = 201,
                    hotelId = "hotel-2",
                    name = "Oceanfront Deluxe King",
                    bedConfiguration = "1 cama King extragrande con dosel",
                    capacity = 2,
                    sizeM2 = 46,
                    pricePerNight = 185.0,
                    amenities = listOf("Vista directa al océano", "Terraza con hamaca", "Minibar gourmet", "Ducha tipo lluvia"),
                    imageUrl = "https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 1,
                    backendTipoId = 2
                ),
                HotelRoom(
                    roomId = 202,
                    hotelId = "hotel-2",
                    name = "Master Suite Familiar con Piscina",
                    bedConfiguration = "2 camas Queen dobles + terraza",
                    capacity = 4,
                    sizeM2 = 72,
                    pricePerNight = 260.0,
                    amenities = listOf("Salida directa a la arena", "Piscina plunge privada", "Comedor al aire libre"),
                    imageUrl = "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 3,
                    backendTipoId = 2
                )
            )
        ),
        Hotel(
            id = "hotel-3",
            name = "Lumina Boutique & Coffee Estate",
            city = "Ruta de las Flores",
            address = "Finca San Cayetano, Apaneca, Ahuachapán",
            starRating = 4,
            reviewScore = 9.2,
            reviewCount = 650,
            reviewLabel = "Fantástico",
            description = "Exclusivo hotel boutique inmerso en una centenaria finca de café de altura. Disfrute del clima fresco de montaña, senderos entre bruma, chimenea en la habitación y catas de café de especialidad.",
            amenities = listOf("Chimenea en Cabañas", "Tour de Café incluido", "Restaurante Farm-to-Table", "Senderismo guiado", "Mirador panorámico"),
            pricePerNightStarting = 120.0,
            featuredTag = "Naturaleza & Café",
            imageUrl = "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80",
            hasGeniusDiscount = false,
            rooms = listOf(
                HotelRoom(
                    roomId = 301,
                    hotelId = "hotel-3",
                    name = "Cabaña Rústica Deluxe entre Cafetales",
                    bedConfiguration = "1 cama King con plumón térmico",
                    capacity = 2,
                    sizeM2 = 42,
                    pricePerNight = 120.0,
                    amenities = listOf("Chimenea de leña", "Cafetera prensa francesa", "Vistas a la montaña"),
                    imageUrl = "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 2,
                    backendTipoId = 1
                ),
                HotelRoom(
                    roomId = 302,
                    hotelId = "hotel-3",
                    name = "Suite Panorámica Alta Vista",
                    bedConfiguration = "1 cama King + 1 cama individual",
                    capacity = 3,
                    sizeM2 = 58,
                    pricePerNight = 165.0,
                    amenities = listOf("Ventanales de piso a techo", "Deck de madera privado", "Tina vintage"),
                    imageUrl = "https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 4,
                    backendTipoId = 2
                )
            )
        ),
        Hotel(
            id = "hotel-4",
            name = "Lumina Lakeside Retreat & Wellness",
            city = "Lago de Coatepeque",
            address = "Ribera Poniente, Lago de Coatepeque, Santa Ana",
            starRating = 5,
            reviewScore = 9.5,
            reviewCount = 790,
            reviewLabel = "Excepcional",
            description = "Retiro de bienestar y calma ubicado en el majestuoso lago cratérico de Coatepeque. Muelle privado, kayaks, sesiones de yoga al amanecer y alta gastronomía fusión.",
            amenities = listOf("Muelle Privado", "Kayaks y Paddleboards", "Piscina infinita al lago", "Yoga Shala", "Sauna y Temazcal"),
            pricePerNightStarting = 195.0,
            featuredTag = "Retiro Exclusivo",
            imageUrl = "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80",
            hasGeniusDiscount = true,
            rooms = listOf(
                HotelRoom(
                    roomId = 401,
                    hotelId = "hotel-4",
                    name = "Lakefront Sunset Suite",
                    bedConfiguration = "1 cama King con vista 180° al lago",
                    capacity = 2,
                    sizeM2 = 50,
                    pricePerNight = 195.0,
                    amenities = listOf("Muelle privado directo", "Bañera de hidromasaje exterior", "Desayuno en la terraza"),
                    imageUrl = "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=1000&q=80",
                    freeCancellation = true,
                    breakfastIncluded = true,
                    backendHabitacionId = 6,
                    backendTipoId = 3
                )
            )
        )
    )

    val defaultAttractions: List<TouristAttraction> = listOf(
        TouristAttraction(
            id = "attr-1",
            title = "Ascenso Guiado al Cráter del Volcán de Santa Ana & Mirador Coatepeque",
            city = "Santa Ana",
            category = "Naturaleza & Aventura",
            rating = 4.9,
            reviewCount = 420,
            duration = "6 horas",
            pricePerPerson = 55.0,
            description = "Caminata de ensueño hacia la cima del volcán Ilamatepec para admirar su laguna esmeralda turquesa brillante en el cráter, seguida de almuerzo con vista panorámica al Lago de Coatepeque.",
            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1000&q=80",
            highlights = listOf("Guía bilingüe de montaña certificado", "Transporte con A/C ida y vuelta", "Entradas al Parque Nacional", "Equipo de hidratación y bastones"),
            inclusions = listOf("Transporte privado", "Tarifas de ingreso", "Box lunch energético", "Seguro contra accidentes")
        ),
        TouristAttraction(
            id = "attr-2",
            title = "Ruta de las Flores: Cascadas de Don Juan & Cata de Café de Especialidad",
            city = "Ahuachapán",
            category = "Cultural & Tours",
            rating = 4.8,
            reviewCount = 310,
            duration = "7 horas",
            pricePerPerson = 48.0,
            description = "Explora los pintorescos pueblos coloniales de Juayúa y Ataco, maravíllate con las cascadas naturales de agua cristalina y participa en una cata guiada de café galardonado Cup of Excellence.",
            imageUrl = "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=1000&q=80",
            highlights = listOf("Cata guiada con barista profesional", "Visita a talleres de telar de palanca", "Paseo por cascadas escondidas"),
            inclusions = listOf("Transporte turístico", "Degustación de café", "Guía local conocedor", "Almuerzo regional")
        ),
        TouristAttraction(
            id = "attr-3",
            title = "Paseo Exclusivo en Catamarán al Atardecer por el Golfo",
            city = "La Unión",
            category = "Playas & Deportes",
            rating = 4.9,
            reviewCount = 190,
            duration = "3.5 horas",
            pricePerPerson = 75.0,
            description = "Navegación en yate/catamarán entre las islas volcánicas del Golfo de Fonseca con barra libre de cócteles tropicales, tabla de tapas y parada para nadar en aguas tranquilas al caer la tarde.",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1000&q=80",
            highlights = listOf("Vistas épicas del atardecer pacífico", "Barra libre y canapés de mariscos", "Equipo de snorkel y toallas"),
            inclusions = listOf("Paseo en embarcación de lujo", "Bebidas premium", "Tripulación profesional")
        ),
        TouristAttraction(
            id = "attr-4",
            title = "City Tour Histórico Nocturno, Teatro Nacional & Cena de Alta Cocina",
            city = "San Salvador",
            category = "Gastronomía",
            rating = 4.7,
            reviewCount = 280,
            duration = "4 horas",
            pricePerPerson = 42.0,
            description = "Descubre la vibrante vida nocturna del Centro Histórico renovado: Palacio Nacional iluminado, Iglesia El Rosario y culmina con un menú degustación de cocina salvadoreña contemporánea.",
            imageUrl = "https://images.unsplash.com/photo-1519671482749-fd09be7ccebf?auto=format&fit=crop&w=1000&q=80",
            highlights = listOf("Ingreso exclusivo guiado a salones históricos", "Cena gourmet de 4 tiempos", "Recorrido peatonal seguro y ameno"),
            inclusions = listOf("Guía historiador", "Cena completa", "Entradas a monumentos")
        ),
        TouristAttraction(
            id = "attr-5",
            title = "Circuito de Canopy Extremo & Aguas Termales de Santa Teresa",
            city = "Ahuachapán",
            category = "Naturaleza & Aventura",
            rating = 4.8,
            reviewCount = 250,
            duration = "5 horas",
            pricePerPerson = 50.0,
            description = "Vuela sobre las copas de los árboles en tirolesas panorámicas y relájate posteriormente en las piscinas naturales de aguas termales ricas en minerales curativos.",
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1000&q=80",
            highlights = listOf("Tirolesa de 1.5 km entre cañones", "Baño en lodo termal exfoliante", "Piscinas de distintas temperaturas"),
            inclusions = listOf("Equipo de seguridad certificado", "Acceso total a termales", "Lockers y vestidores")
        ),
        TouristAttraction(
            id = "attr-6",
            title = "Clase de Surf Privada & Safari de Olas en El Tunco / El Zonte",
            city = "La Libertad",
            category = "Playas & Deportes",
            rating = 4.9,
            reviewCount = 340,
            duration = "3 horas",
            pricePerPerson = 60.0,
            description = "Aprende a surfear o perfecciona tu técnica con instructores profesionales ISA en las mundialmente reconocidas olas de Surf City, con análisis fotográfico de tu sesión incluido.",
            imageUrl = "https://images.unsplash.com/photo-1502680390469-be75c86b636f?auto=format&fit=crop&w=1000&q=80",
            highlights = listOf("Instructor certificado 1 a 1", "Tabla de surf adecuada a tu nivel", "Fotografías digitales de recuerdo"),
            inclusions = listOf("Tabla de surf y rashguard", "Fotos de alta resolución", "Hidratación de coco fresca")
        )
    )

    val sampleBookings: List<CustomerHotelBooking> = listOf(
        CustomerHotelBooking(
            bookingReference = "LUM-98421",
            hotelId = "hotel-1",
            hotelName = "Lumina Grand Palace Resort & Spa",
            hotelCity = "San Salvador",
            roomName = "Habitación Deluxe Queen Superior",
            guestFullName = "Carlos Alberto Mendoza",
            guestEmail = "carlos.mendoza@ejemplo.com",
            guestPhone = "+503 7123-4567",
            checkInDate = LocalDate.now().plusDays(2).toString(),
            checkOutDate = LocalDate.now().plusDays(5).toString(),
            nights = 3,
            guestsCount = 2,
            roomSubtotal = 345.0,
            taxesAndFees = 44.85,
            totalPrice = 389.85,
            paymentMethod = "Tarjeta •••• 4242",
            specialRequests = "Habitación en piso alto con vista despejada",
            status = "CONFIRMADA",
            backendReservaId = 1
        )
    )

    val sampleAttractionBookings: List<CustomerAttractionBooking> = listOf(
        CustomerAttractionBooking(
            ticketReference = "ATR-41092",
            attractionId = "attr-1",
            attractionTitle = "Ascenso Guiado al Cráter del Volcán de Santa Ana",
            city = "Santa Ana",
            date = LocalDate.now().plusDays(3).toString(),
            ticketsCount = 2,
            leadGuestName = "Carlos Alberto Mendoza",
            leadGuestEmail = "carlos.mendoza@ejemplo.com",
            leadGuestPhone = "+503 7123-4567",
            totalPrice = 110.0,
            paymentMethod = "Tarjeta •••• 4242",
            status = "CONFIRMADA"
        )
    )

    val defaultSavedCards: List<SavedPaymentCard> = listOf(
        SavedPaymentCard(
            id = "card-1",
            cardNumberMasked = "•••• 4242",
            cardHolder = "CARLOS A MENDOZA",
            expiry = "11/28",
            cardBrand = "Visa",
            isDefault = true
        ),
        SavedPaymentCard(
            id = "card-2",
            cardNumberMasked = "•••• 8819",
            cardHolder = "CARLOS A MENDOZA",
            expiry = "04/29",
            cardBrand = "Mastercard",
            isDefault = false
        )
    )

    val defaultPastTransactions: List<PastTransaction> = listOf(
        PastTransaction(
            id = "tx-1",
            title = "Lumina Grand Palace Resort (3 noches)",
            type = "Estadía",
            date = "15/10/2026",
            bookingReference = "LUM-98421",
            paymentMethod = "Visa •••• 4242",
            amountPaid = 389.85,
            status = "Completada"
        ),
        PastTransaction(
            id = "tx-2",
            title = "Ascenso Guiado Volcán de Santa Ana (2 tickets)",
            type = "Atracción",
            date = "17/10/2026",
            bookingReference = "ATR-41092",
            paymentMethod = "Visa •••• 4242",
            amountPaid = 110.00,
            status = "Completada"
        ),
        PastTransaction(
            id = "tx-3",
            title = "Lumina Royal Ocean Beach Resort (2 noches)",
            type = "Estadía",
            date = "04/08/2026",
            bookingReference = "LUM-76120",
            paymentMethod = "Mastercard •••• 8819",
            amountPaid = 418.10,
            status = "Completada"
        )
    )

    val defaultProfile = UserProfile(
        fullName = "Carlos Alberto Mendoza",
        email = "carlos.mendoza@ejemplo.com",
        dialCode = "+503",
        phoneNumber = "7123-4567",
        isGeniusMember = true
    )
}
