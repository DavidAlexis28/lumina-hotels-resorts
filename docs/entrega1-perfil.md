# 6. Justificación

Esta investigación permitirá conocer cómo responde un sistema de reservas hoteleras desarrollado con Java 21, JDBC y SQL Server 2022 ante el incremento progresivo de información almacenada. En lugar de evaluar el software únicamente por su funcionalidad aparente, el estudio registrará métricas cuantitativas sobre los tiempos de respuesta de cada transacción, facilitando la detección de cuellos de botella en las consultas SQL, los índices y la comunicación mediante JDBC a partir de datos empíricos y no de estimaciones subjetivas.

---

### 6.1 Justificación técnica

Desde la perspectiva técnica, la investigación determinará las variaciones de latencia en las operaciones esenciales del sistema (registro, consulta, modificación y cancelación de reservas) frente a volúmenes escalonados de datos.

El flujo transaccional entre una aplicación Java y un motor relacional involucra la generación y reutilización de planes de ejecución dentro de SQL Server. La falta de índices adecuados y el crecimiento masivo de registros suelen traducirse en escaneos completos de tablas (*table scans*), bloqueos concurrentes (*locks*) y un incremento sustancial en el consumo de memoria e I/O de disco. Analizar estas operaciones mediante el uso parametrizado de `PreparedStatement` vía JDBC proporcionará datos verificables para optimizar las estructuras de consulta y mejorar el rendimiento global de la persistencia.

---

### 6.2 Justificación académica

En el ámbito metodológico y formativo, este trabajo integra conceptos de ingeniería de software, gestión de bases de datos y evaluación de rendimiento mediante un marco rigurosamente empírico y reproducible. Se supera la práctica empírica informal —donde la rapidez de un aplicativo suele determinarse por observación directa— y se establece un proceso basado en la recolección controlada de tiempos de respuesta en milisegundos.

El estudio adopta como base el estándar internacional **ISO/IEC 25010:2023** (*Systems and software engineering — SQuaRE — Product quality model*), centrándose específicamente en la característica de **eficiencia de desempeño** a través de dos subcaracterísticas principales:
* **Comportamiento temporal (*time behavior*):** Cuantificación de los tiempos de respuesta y procesamiento de cada operación bajo diferentes escenarios de carga de datos.
* **Capacidad (*capacity*):** Evaluación de los límites del sistema y la estabilidad operativa a medida que el volumen escala de 100 hasta 100.000 registros almacenados.

Esta aproximación promueve el rigor científico al articular la formulación de hipótesis técnicas con la recolección estructurada de datos y su análisis estadístico.

---

### 6.3 Justificación social y profesional

En el plano social y aplicado, los sistemas de reservas representan un componente crítico en la operatividad de la industria de la hospitalidad. Tiempos de respuesta lentos generan demoras en la atención, riesgos de sobreventa involuntaria (*overbooking*) por inconsistencias de inventario en tiempo real y fricción directa con los huéspedes. Generar directrices para mantener la velocidad transaccional contribuye directamente a la estabilidad del servicio y la satisfacción del usuario.

Desde la dimensión profesional, la investigación consolida habilidades técnicas fundamentales en el equipo de desarrollo:
* Construcción de arquitecturas backend eficientes empleando la plataforma **Java 21**.
* Optimización de acceso a datos relacionales con **JDBC** y **SQL Server 2022**, profundizando en la interacción con el optimizador de consultas del motor.
* Implementación de prácticas profesionales de trabajo en equipo mediante **Git y GitHub**, asegurando trazabilidad a través de ramas, issues y pull requests formales.
* Aplicación de principios formales de calidad de software y experimentación cuantitativa orientada a la ingeniería de software.