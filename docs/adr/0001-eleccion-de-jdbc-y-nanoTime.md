# ADR 0001: Elección de JDBC nativo y System.nanoTime() para el motor de reservas y medición de latencia

* **Estado:** Aceptada
* **Fecha:** 18 de Setiembre de 2026
* **Contexto y Problema:**
  Para el desarrollo del sistema de reservas de **Lumina Hotels and Resorts**, el equipo requiere implementar una capa de persistencia ligera y eficiente que interactúe directamente con Microsoft SQL Server 2022. Asimismo, como parte de la investigación formativa, es necesario medir con alta precisión temporal (en nanosegundos) el tiempo de respuesta de las transacciones críticas bajo diferentes volúmenes de datos. Se evaluó el uso de ORMs pesados (como Hibernate/JPA) frente a mecanismos de acceso a datos nativos.

## Opciones Consideradas

1. **JPA / Hibernate (ORM):**
    * *Ventajas:* Mapeo automático de objetos y abstracción de consultas SQL.
    * *Desventajas:* Introduce sobrecarga de memoria (*overhead*), cachés internas y capas de abstracción intermedias que pueden alterar o sesgar las mediciones reales de latencia del motor de base de datos.
2. **JDBC Nativo (`PreparedStatement`):**
    * *Ventajas:* Acceso directo, control total y explícito de las sentencias SQL, menor consumo de recursos y máxima fidelidad en el rendimiento de las operaciones transaccionales (OLTP).
    * *Desventajas:* Mayor cantidad de código repetitivo (*boilerplate*), el cual se mitiga estructurando adecuadamente las clases de conexión.
3. **Medición temporal con `System.currentTimeMillis()` vs `System.nanoTime()`:**
    * *currentTimeMillis():* Sujeta a cambios en el reloj del sistema operativo, con menor precisión.
    * *nanoTime():* Proporciona un valor de alta resolución ideal para medir intervalos de tiempo transcurridos con precisión de nanosegundos de forma independiente al reloj del sistema.

## Decisión
Se decide adoptar **JDBC nativo** (`PreparedStatement`) como tecnología de conectividad para el backend en Java 21 debido a su predictibilidad, rendimiento y control transaccional sobre SQL Server 2022. Adicionalmente, se selecciona **`System.nanoTime()`** como el mecanismo estándar en Java para la captura experimental de las métricas de latencia de las operaciones de reserva.

## Consecuencias
* **Positivas:** Mediciones experimentales precisas y puras, libres de interferencias de frameworks de mapeo; control absoluto sobre las consultas SQL ejecutadas; y cumplimiento estricto con los requerimientos de rendimiento del proyecto.
* **Negativas:** La gestión de conexiones y el mapeo de resultados a objetos de dominio deben implementarse de forma manual en el código del servidor.