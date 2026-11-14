Operacionalización de Variables

1. Objetivo

Definir las variables utilizadas para evaluar el rendimiento de las operaciones OLTP en un sistema de reservas hoteleras.

2. Tabla de operacionalización

Tipo| Variable| Definición operacional| Indicador| Unidad| Método de medición
Independiente| Volumen de datos| Cantidad de registros disponibles en la base de datos durante la prueba.| Número de registros| Registros| Preparación de conjuntos de datos de prueba
Independiente| Operación OLTP| Tipo de operación transaccional ejecutada.| SELECT, INSERT, UPDATE o DELETE| Tipo de operación| Consultas SQL controladas
Dependiente| Latencia| Tiempo transcurrido desde el inicio hasta la finalización de una operación.| Tiempo de ejecución| Milisegundos (ms)| "System.nanoTime()" o JMH

3. Niveles del volumen de datos

Se utilizarán los siguientes niveles:

- 100 registros.
- 1,000 registros.
- 10,000 registros.
- 50,000 registros.
- 100,000 registros.

4. Operaciones OLTP

- SELECT: consultar registros.
- INSERT: insertar registros.
- UPDATE: actualizar registros.
- DELETE: eliminar registros de prueba.

5. Medición de la latencia

Se registrará el tiempo inicial y final de cada operación utilizando "System.nanoTime()".

La fórmula será:

"Latencia (ms) = (tiempo final - tiempo inicial) / 1,000,000.0"

Se realizarán cinco ejecuciones de calentamiento y treinta repeticiones medidas por cada combinación de volumen y operación.

Se calcularán la latencia media y la mediana. También se podrá calcular el percentil 95 (p95).

6. Control de medición

Se mantendrán constantes las condiciones de hardware, software, configuración de la base de datos y consultas. Las ejecuciones de calentamiento no formarán parte de las treinta mediciones registradas.

La preparación de los datos se realizará antes de medir las operaciones, y se documentarán las condiciones de cada prueba.