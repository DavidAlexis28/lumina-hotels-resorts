Metodología del Diseño Experimental

1. Objetivo

Evaluar la latencia de las operaciones OLTP en función del volumen de datos de un sistema de reservas hoteleras desarrollado con Java 21 y SQL Server 2022.

2. Factores experimentales

Factor| Tipo| Niveles
Volumen de datos| Independiente| 100, 1,000, 10,000, 50,000 y 100,000 registros
Operación OLTP| Independiente| SELECT, INSERT, UPDATE y DELETE
Latencia| Dependiente| Tiempo de ejecución en milisegundos (ms)

3. Repeticiones y calentamiento

Se evaluarán cinco niveles de volumen y cuatro operaciones OLTP, lo que genera 20 combinaciones experimentales.

Para cada combinación se realizarán cinco ejecuciones de calentamiento, que no se incluirán en los resultados, y treinta repeticiones medidas.

Total de mediciones: 20 combinaciones × 30 repeticiones = 600 mediciones.

4. Procedimiento

1. Preparar la base de datos y los datos de prueba.
2. Configurar los cinco niveles de volumen.
3. Ejecutar las cinco operaciones de calentamiento por combinación.
4. Realizar treinta repeticiones medidas por combinación.
5. Registrar el tiempo inicial y final de cada operación.
6. Convertir los tiempos a milisegundos.
7. Calcular la media, la mediana y el percentil 95 (p95).

5. Medición de latencia

Se utilizará "System.nanoTime()" para registrar el tiempo antes y después de cada operación.

Latencia en milisegundos = (tiempo final - tiempo inicial) / 1,000,000.0

También podrá utilizarse JMH (Java Microbenchmark Harness), documentando su configuración y diferenciando el calentamiento de las mediciones experimentales.

6. Control de variables

- Utilizar el mismo equipo y las mismas versiones de software.
- Mantener constante la configuración de SQL Server y la conexión.
- Usar consultas equivalentes y datos de prueba reproducibles.
- Restablecer el estado de los datos cuando sea necesario para INSERT, UPDATE y DELETE.
- Separar las ejecuciones de calentamiento de las mediciones.
- Evitar otros procesos que consuman recursos durante las pruebas.
- Registrar errores e incidencias.
- Mantener un procedimiento uniforme para todas las combinaciones.

7. Registro y análisis

Se guardarán el volumen, la operación, el número de repetición y la latencia en un archivo CSV. Los resultados se compararán para identificar cómo varía el tiempo de respuesta según el volumen y el tipo de operación.

8. Resultado esperado

El diseño permitirá comparar el rendimiento de las operaciones OLTP en los distintos volúmenes de datos y elaborar conclusiones basadas en las mediciones obtenidas.