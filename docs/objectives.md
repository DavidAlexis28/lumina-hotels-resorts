# Objetivos y Banco de Pruebas

## Objetivo General
Evaluar el rendimiento de las operaciones de consulta y almacenamiento de reservas en Java 21 utilizando SQL Server 2022, mediante un banco de pruebas controlado para la medición empírica de tiempos de respuesta ante diferentes volúmenes de datos.

## Objetivos Específicos
1. Implementar una aplicación modular en Java 21 que ejecute transacciones sobre una base de datos SQL Server 2022.
2. Medir los tiempos de respuesta y consumo de recursos durante la ejecución de consultas en escenarios con volúmenes de 100, 1.000, 10.000 y 100.000 registros.
3. Comparar el desempeño técnico obtenido en cada escenario para identificar cuellos de botella e impacto de la latencia en las operaciones de reserva.

## Evidencia Técnica (Banco de Pruebas Proyectado)

| Escenario de Prueba | Volumen de Registros | Operación Principal | Tiempo de Respuesta Promedio (ms) | Uso de CPU (%) | Uso de RAM (MB) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Escenario 1** | 100 | Búsqueda/Inserción | *Por medir* | *Por medir* | *Por medir* |
| **Escenario 2** | 1.000 | Búsqueda/Inserción | *Por medir* | *Por medir* | *Por medir* |
| **Escenario 3** | 10.000 | Búsqueda/Inserción | *Por medir* | *Por medir* | *Por medir* |
| **Escenario 4** | 100.000 | Búsqueda/Inserción | *Por medir* | *Por medir* | *Por medir* |