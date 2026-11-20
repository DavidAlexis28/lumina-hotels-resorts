# ADR 0001: Arquitectura en Capas, API REST y Patrón DAO con SQL Server 2022

## Estado
Aceptado

## Contexto
El sistema **Lumina Hotels & Resorts** requiere una aplicación móvil Android en Kotlin con Jetpack Compose que gestione disponibilidad, reservas y cancelaciones evitando traslapes de fechas y sin exponer credenciales de base de datos en el cliente móvil.

## Decisión
1. Adoptar el patrón **MVVM** en el cliente Android (`mobile/`) consumiendo contratos **HTTP/JSON** mediante Retrofit.
2. Implementar un servicio **Backend en Java 21 LTS** (`backend/`) estructurado mediante el patrón **Data Access Object (DAO)** y consultas parametrizadas JDBC (`mssql-jdbc 12.6.1.jre11`).
3. Centralizar la integridad referencial, la vista consolidada `vw_ReservasDetalle` y la lógica transaccional anti-traslape en **Microsoft SQL Server 2022** (`database/schema.sql`).

## Consecuencias
- Desacoplamiento total entre interfaz móvil, lógica de negocio y motor relacional.
- Trazabilidad y reproducibilidad experimental OLTP ($N=600$) y cumplimiento del estándar **ISO/IEC 25010**.
