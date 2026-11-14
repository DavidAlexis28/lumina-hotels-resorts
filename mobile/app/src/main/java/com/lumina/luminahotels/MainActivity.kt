
package com.lumina.luminahotels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class Habitacion(
    val numero: String,
    val tipo: String,
    val piso: Int,
    val capacidad: Int,
    val precio: Double
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                var habitaciones by remember {
                    mutableStateOf<List<Habitacion>>(emptyList())
                }

                var cargando by remember { mutableStateOf(true) }
                var mensaje by remember {
                    mutableStateOf("Conectando con el servidor...")
                }

                fun cargarHabitaciones() {
                    lifecycleScope.launch {
                        cargando = true
                        mensaje = "Consultando habitaciones..."

                        try {
                            val resultado = withContext(Dispatchers.IO) {
                                obtenerHabitaciones()
                            }

                            habitaciones = resultado

                            mensaje = if (resultado.isEmpty()) {
                                "No hay habitaciones disponibles."
                            } else {
                                "Habitaciones disponibles: ${resultado.size}"
                            }

                        } catch (e: Exception) {
                            mensaje = "Error de conexión: ${e.message}"
                        }

                        cargando = false
                    }
                }

                LaunchedEffect(Unit) {
                    cargarHabitaciones()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                    ) {

                        Text(
                            text = "Lumina Hotels & Resorts",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { cargarHabitaciones() },
                            enabled = !cargando
                        ) {
                            Text("Actualizar habitaciones")
                        }

                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(habitaciones) { habitacion ->

                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Habitación ${habitacion.numero}",
                                            style = MaterialTheme.typography.titleLarge
                                        )

                                        Text("Tipo: ${habitacion.tipo}")
                                        Text("Piso: ${habitacion.piso}")
                                        Text("Capacidad: ${habitacion.capacidad} huéspedes")
                                        Text("Precio por noche: $${habitacion.precio}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun obtenerHabitaciones(): List<Habitacion> {

        val direccion =
            "http://10.0.2.2:8080/api/habitaciones/disponibles" +
                    "?fechaEntrada=2026-12-01" +
                    "&fechaSalida=2026-12-03" +
                    "&huespedes=2"

        val conexion = URL(direccion).openConnection() as HttpURLConnection

        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10000
            conexion.readTimeout = 10000

            if (conexion.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Código HTTP: ${conexion.responseCode}")
            }

            val respuesta = conexion.inputStream.bufferedReader().use {
                it.readText()
            }

            val json = JSONArray(respuesta)
            val lista = mutableListOf<Habitacion>()

            for (i in 0 until json.length()) {
                val objeto = json.getJSONObject(i)

                lista.add(
                    Habitacion(
                        numero = objeto.getString("numero"),
                        tipo = objeto.getString("nombreTipo"),
                        piso = objeto.getInt("piso"),
                        capacidad = objeto.getInt("capacidad"),
                        precio = objeto.getDouble("precioPorNoche")
                    )
                )
            }

            return lista

        } finally {
            conexion.disconnect()
        }
    }
}
