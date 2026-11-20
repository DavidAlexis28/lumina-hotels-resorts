# Lumina Hotels & Resorts — Guía de Instalación, Arquitectura y Calidad ISO/IEC 25010

## 1. Arquitectura Integrada
El sistema sigue el flujo estricto documentado en el marco teórico:
`Android (Kotlin + Jetpack Compose + MVVM + Retrofit) -> API REST (HTTP/JSON) -> Backend Java 21 (Patrón DAO + JDBC) -> Microsoft SQL Server 2022`

## 2. Instrucciones de Despliegue e Instalación

### Paso 1: Base de Datos SQL Server 2022 (Docker)
```bash
export LUMINA_DB_PASSWORD="TuPasswordSegura2026!"
cd docker
docker compose up -d
```
Ejecutar `database/schema.sql` y `database/init.sql` sobre la instancia en `localhost:1433` para crear `lumina_hotel_and_resorts`, las tablas (`Cliente`, `TipoHabitacion`, `Habitacion`, `Reserva`), la vista `vw_ReservasDetalle` y los procedimientos `sp_HabitacionesDisponibles`, `sp_RegistrarReserva` y `sp_CancelarReserva`.

### Paso 2: Backend Java 21 + Maven
```bash
cd backend
mvn clean test
mvn exec:java -Dexec.mainClass="com.lumina.hotels.controller.LuminaRestServer"
```

### Paso 3: Banco Experimental OLTP (600 Mediciones)
```bash
cd experiments
mvn clean compile exec:java -Dexec.mainClass="com.lumina.experiments.OltpBenchmarkRunner"
python3 procesar_estadisticas.py
```

### Paso 4: Aplicación Móvil Android (`mobile/`)
Abrir la carpeta `mobile/` en Android Studio, sincronizar Gradle y ejecutar sobre emulador API 28+ (`10.0.2.2:8080` apunta al backend local).

---

## 3. Evaluación de Calidad bajo el Estándar ISO/IEC 25010

| Característica ISO/IEC 25010 | Aplicación en Lumina Hotels & Resorts | Módulos Relacionados | Criterio de Evaluación y Prueba |
| :--- | :--- | :--- | :--- |
| **1. Adecuación funcional** | Permite consultar disponibilidad, registrar reservas sin traslape, actualizar y cancelar reservas viendo el historial consolidado. | `sp_HabitacionesDisponibles`, `ReservaDAO`, `HotelViewModel`, `HotelScreens.kt` | Verificación de creación, validación anti-traslape (`50015`) y cambio a estado `CANCELADA`. |
| **2. Eficiencia de desempeño** | Índices compuestos en `Reserva` y `Habitacion`, y medición empírica OLTP ($N=600$) con `System.nanoTime()`. | `schema.sql`, `OltpBenchmarkRunner.java` | Latencia media, mediana y $p95$ exportadas en CSV para volúmenes de 100 a 100,000 registros. |
| **3. Compatibilidad** | Interoperabilidad mediante contratos JSON estándar sobre HTTP REST entre cliente Android y servidor Java 21. | `LuminaRestServer.java`, `LuminaApiService.kt` | Serialización/deserialización exitosa de DTOs entre Retrofit y Gson. |
| **4. Usabilidad** | Interfaz Material Design 3 con navegación inferior clara, selección directa desde disponibilidad y mensajes descriptivos ante entradas inválidas. | `HotelScreens.kt`, `Theme.kt` | Flujo completo de reserva ejecutable por un usuario nuevo sin asistencia. |
| **5. Fiabilidad** | Manejo de pérdida de conexión HTTP sin cierre inesperado mediante modo respaldo local y transacciones SQL `XACT_ABORT`. | `HotelViewModel.kt`, `sp_RegistrarReserva` | Desconexión del servidor backend sin provocar `Crash` en Android. |
| **6. Seguridad** | Pantalla de inicio de sesión obligatoria, variables de entorno para credenciales SQL y consultas 100 % parametrizadas (`PreparedStatement`/`CallableStatement`). | `LoginScreen`, `ConexionDB.java`, `ReservaDAO.java` | Cero credenciales en código fuente y prevención de inyección SQL. |
| **7. Mantenibilidad** | Separación limpia de responsabilidades: MVVM en Android y patrón DAO + Controlador REST en Java 21. | `mobile/`, `backend/` | Compilación modular con Gradle/Maven y pruebas unitarias automatizadas en JUnit 5. |
| **8. Portabilidad** | Soporte Android desde API 28 (`minSdk = 28`) hasta API 37 y despliegue contenerizado de SQL Server 2022 mediante Docker. | `mobile/app/build.gradle.kts`, `docker/docker-compose.yml` | Ejecución reproducible en distintos dispositivos Android y entornos WSL2/Linux. |
