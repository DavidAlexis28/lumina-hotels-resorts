package com.lumina.luminahotels.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lumina.luminahotels.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface LuminaApiService {

    // READ Disponibles (dbo.sp_HabitacionesDisponibles)
    @GET("api/habitaciones/disponibles")
    suspend fun getHabitacionesDisponibles(
        @Query("fechaEntrada") fechaEntrada: String,
        @Query("fechaSalida") fechaSalida: String,
        @Query("huespedes") huespedes: Int
    ): Response<List<HabitacionDto>>

    // CRUD Habitaciones
    @GET("api/habitaciones")
    suspend fun getTodasHabitaciones(): Response<List<HabitacionDto>>

    @POST("api/habitaciones")
    suspend fun crearHabitacion(@Body request: HabitacionRequest): Response<Map<String, Any>>

    @PUT("api/habitaciones/{id}")
    suspend fun actualizarHabitacion(
        @Path("id") id: Int,
        @Body request: HabitacionRequest
    ): Response<Map<String, Any>>

    @DELETE("api/habitaciones/{id}")
    suspend fun eliminarHabitacion(@Path("id") id: Int): Response<Map<String, Any>>

    // CRUD Reservas
    @GET("api/reservas")
    suspend fun getReservasDetalle(): Response<List<ReservaDetalleDto>>

    @POST("api/reservas")
    suspend fun crearReserva(@Body request: CrearReservaRequest): Response<ReservaDetalleDto>

    @PUT("api/reservas/{id}")
    suspend fun actualizarReserva(
        @Path("id") id: Int,
        @Body request: ActualizarReservaRequest
    ): Response<ReservaDetalleDto>

    @PUT("api/reservas/{id}/cancelar")
    suspend fun cancelarReserva(@Path("id") id: Int): Response<ReservaDetalleDto>

    @DELETE("api/reservas/{id}")
    suspend fun eliminarReserva(@Path("id") id: Int): Response<Map<String, Any>>

    // Perfil y Pasaporte
    @GET("api/cliente/perfil")
    suspend fun getPerfil(@Query("correo") correo: String): Response<ClientePerfilDto>

    @POST("api/cliente/perfil")
    suspend fun actualizarPerfil(@Body request: ClientePerfilRequest): Response<Map<String, Any>>

    // Hoteles y Habitaciones
    @GET("api/hoteles")
    suspend fun getHoteles(): Response<List<Hotel>>

    @POST("api/hoteles")
    suspend fun crearHotel(@Body request: CrearHotelRequest): Response<Hotel>

    @PUT("api/hoteles/{id}")
    suspend fun actualizarHotel(@Path("id") id: String, @Body request: CrearHotelRequest): Response<Hotel>

    @DELETE("api/hoteles/{id}")
    suspend fun eliminarHotel(@Path("id") id: String): Response<Map<String, Any>>

    // Atracciones Turísticas y Microbús
    @GET("api/atracciones")
    suspend fun getAtracciones(): Response<List<TouristAttraction>>

    @POST("api/atracciones")
    suspend fun crearAtraccion(@Body request: CrearAtraccionRequest): Response<TouristAttraction>

    @PUT("api/atracciones/{id}")
    suspend fun actualizarAtraccion(@Path("id") id: String, @Body request: CrearAtraccionRequest): Response<TouristAttraction>

    @DELETE("api/atracciones/{id}")
    suspend fun eliminarAtraccion(@Path("id") id: String): Response<Map<String, Any>>

    @POST("api/reservas/atraccion")
    suspend fun reservarAtraccion(@Body request: AtraccionReservaRequest): Response<Map<String, Any>>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private val gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: LuminaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LuminaApiService::class.java)
    }

    /**
     * Parsea respuestas de error del backend LuminaRestServer (ej. {"error": "mensaje"}).
     */
    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val json = gson.fromJson(errorBody, JsonObject::class.java)
                if (json.has("error")) {
                    json.get("error").asString
                } else {
                    errorBody
                }
            } else {
                "Error HTTP ${response.code()}: ${response.message()}"
            }
        } catch (_: Exception) {
            "Error HTTP ${response.code()}: ${response.message()}"
        }
    }
}
