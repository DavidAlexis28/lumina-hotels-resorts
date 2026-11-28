package com.lumina.luminahotels.data.mock

import com.lumina.luminahotels.model.CountryDialCode
import com.lumina.luminahotels.model.PastTransaction

/**
 * Catálogo de configuración estática para selectores de países.
 * Toda la información de alojamientos, atracciones y reservas se obtiene
 * exclusivamente desde Supabase como única fuente de verdad.
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

    val defaultPastTransactions: List<PastTransaction> = emptyList()
}
