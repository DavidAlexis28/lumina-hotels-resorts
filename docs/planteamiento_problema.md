**# Investigación: Optimización del rendimiento en la gestión de reservas hoteleras mediante indexación masiva en bases de datos SQL Server**



**## 1. Planteamiento del Problema**



**### 1.1 Contexto General**

**En la industria hotelera actual, la digitalización de los sistemas de procesamiento de transacciones en tiempo real (OLTP) ha transformado la gestión de reservaciones en un activo crítico para la continuidad del negocio. La capacidad de procesar concurrencia alta y registrar transacciones continuas determina la eficiencia operativa de las cadenas hoteleras globales.**



**### 1.2 Problema Técnico**

**Sin embargo, a medida que los volúmenes de datos escalan a millones de registros, los motores relacionales como Microsoft SQL Server experimentan severas degradaciones en los tiempos de respuesta. Las búsquedas sobre tablas de reservas no optimizadas generan escaneos completos (table scans), bloqueos de filas (lock escalation) y un consumo excesivo de memoria RAM e I/O de disco, impactando directamente la experiencia del cliente final.**



**### 1.3 Referencias**

**\* Microsoft. (2023). \*SQL Server transaction processing architecture guide\*. Microsoft Learn. https://learn.microsoft.com/**

**\* Silberschatz, A., Korth, H. F., \& Sudarshan, S. (2020). \*Database system concepts\* (7th ed.). McGraw-Hill.**

