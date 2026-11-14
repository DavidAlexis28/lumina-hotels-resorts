# Entorno Experimental y Especificaciones del Banco de Pruebas

## 1. Justificación del Entorno Experimental
El objetivo de documentar detalladamente el entorno experimental es garantizar el principio de **reproducibilidad** exigido en las investigaciones de ingeniería de software (Wohlin et al., 2012; ISO/IEC 25010:2023). Al aislar y controlar las variables de hardware, sistema operativo, parámetros de la Máquina Virtual de Java (JVM) y el motor de base de datos relacional, se asegura que las variaciones observadas en la latencia de las operaciones de reserva respondan al volumen de datos (100, 1.000, 10.000 y 100.000 registros) y no a ruido informático o interferencias externas.

---

## 2. Especificaciones de Hardware del Banco de Pruebas
Las pruebas experimentales para la medición de tiempos de respuesta se ejecutan sobre un equipo anfitrión estandarizado con el siguiente perfil técnico:

| Componente | Especificación Técnica | Propósito / Control |
| :--- | :--- | :--- |
| **Procesador (CPU)** | Intel Core i5 / AMD Ryzen 5 (4 núcleos / 8 hilos @ 3.00 GHz) | Frecuencia fija para minimizar escalado dinámico |
| **Memoria RAM** | 8.00 GB DDR4 @ 2666 MHz | Capacidad suficiente para alojar la JVM y SQL Server |
| **Almacenamiento** | SSD NVMe / M.2 de 256 GB | Minimizar cuellos de botella por I/O de disco |
| **Arquitectura** | 64 bits (x86_64) | Compatibilidad completa con Java 21 LTS y SQL Server |

---

## 3. Entorno de Software y Componentes de Ejecución
Para evitar discrepancies entre entornos de desarrollo, todas las versiones y conectores están fijados explícitamente:

* **Sistema Operativo Anfitrión:** Windows 11 Home / Pro (64 bits, versión 22H2 / 23H2).
* **Entorno de Ejecución Java:** OpenJDK 21 LTS (Build `21.0.2+13`, Eclipse Temurin).
* **Motor de Base de Datos:** Microsoft SQL Server 2022 Developer Edition (v16.0).
* **Conector de Red / Persistencia:** Microsoft JDBC Driver for SQL Server (`mssql-jdbc` v12.6.1.jre11).
* **Motor de Contenedores:** Docker Desktop (Engine v26.x) utilizando backend WSL2 (Windows Subsystem for Linux 2).

---

## 4. Parámetros de Configuración y Control de Interferencias
Para mitigar la varianza en las mediciones (*amenaza a la validez interna*), se aplican las siguientes reglas de control:

1. **Memoria de la JVM:** Asignación fija del montículo (*heap*) para evitar pausas impredecibles por Recolección de Basura (*Garbage Collection*):
    * `-Xms1024m` (Memoria inicial)
    * `-Xmx2048m` (Memoria máxima)
2. **Buffer Pool de SQL Server:** Límite máximo de memoria asignado al contenedor en 2048 MB para simular entornos de recursos acotados.
3. **Calentamiento Previo (*Warmup*):** Descarte de las primeras 5 a 10 ejecuciones por cada bloque de prueba para estabilizar la compilación JIT (*Just-In-Time*) de Java y la carga de páginas en caché de SQL Server.
4. **Cargas de Fondo:** Durante las rondas de pruebas (30 repeticiones por escenario), se desactivan procesos no esenciales, actualizaciones automáticas y software antivirus en el sistema anfitrión.

---

## 5. Guía de Despliegue y Reproducción del Entorno (Docker)

Para garantizar que cualquier miembro del equipo o evaluador pueda replicar exactamente el mismo entorno de base de datos sin realizar instalaciones complejas, se incluye un archivo de orquestación `docker-compose.yml` en la raíz del proyecto.

### Requisitos Previos
* Tener instalado **Docker Desktop** (con soporte para contenedores Linux / WSL2 en Windows).
* Tener **Git** y **Java 21 LTS** instalados.

### Instrucciones para Desplegar la Base de Datos

1. **Iniciar el Contenedor de SQL Server 2022:**
   Desde la terminal en la raíz del proyecto, ejecutar:
   ```bash
   docker-compose up -d