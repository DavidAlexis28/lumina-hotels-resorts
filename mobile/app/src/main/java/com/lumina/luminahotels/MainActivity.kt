package com.lumina.luminahotels

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.lumina.luminahotels.utils.SessionManager
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
                val context = LocalContext.current
                val sessionManager = remember { SessionManager(context) }
                var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                if (!isLoggedIn) {
                    AuthScreen(
                        onAuthSuccess = { email ->
                            sessionManager.saveSession(email)
                            isLoggedIn = true
                        }
                    )
                } else {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lumina Hotels & Resorts",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                TextButton(
                                    onClick = {
                                        sessionManager.logout()
                                        isLoggedIn = false
                                    }
                                ) {
                                    Text("Salir")
                                }
                            }

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

@Composable
fun AuthScreen(onAuthSuccess: (String) -> Unit) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Lumina Hotels & Resorts",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = if (isRegisterMode) "Crear cuenta de huésped" else "Iniciar sesión",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isRegisterMode) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val emailValido = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
                    when {
                        isRegisterMode && nombre.trim().isEmpty() ->
                            errorMessage = "Ingresa tu nombre completo"
                        !emailValido ->
                            errorMessage = "Ingresa un correo electrónico válido"
                        password.length < 6 ->
                            errorMessage = "La contraseña debe tener al menos 6 caracteres"
                        else -> {
                            errorMessage = ""
                            onAuthSuccess(email.trim())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "Registrarse e ingresar" else "Iniciar sesión")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión"
                    else "¿No tienes cuenta? Regístrate aquí"
                )
            }
        }
    }
}