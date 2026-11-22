# 🏨 Lumina Hotels & Resorts

**Plataforma integral Full-Stack para gestión hotelera, reservas de atracciones y administración de inventario en tiempo real.**

Proyecto de investigación formativa e ingeniería de software Full-Stack desarrollado para la asignatura **Programación III (Sección 03)** de la Facultad de Informática y Ciencias Aplicadas de la Universidad Tecnológica de El Salvador (UTEC).

---

## 🆕 Novedades de esta actualización (v2.0 – Persistencia Multimotor e Integración con Cloud Firestore)

Esta versión incorpora mejoras importantes en la arquitectura, persistencia de datos, seguridad y gestión de inventario.

| Característica | Descripción |
|---|---|
| **Integración con Cloud Firestore** | Se incorporó persistencia NoSQL completamente asíncrona mediante Firebase Admin SDK 9.11.0 y `ApiFuture<T>`, convirtiendo excepciones de bajo nivel (`InterruptedException`, `ExecutionException`) en errores controlados de dominio mediante `RepositorioException`. |
| **Fábrica dinámica de bases de datos (`FabricaRepositorio`)** | Se desacoplaron los motores de persistencia mediante la variable de entorno `MOTOR_BD` (`sqlserver`, `mongo`, `firestore`), permitiendo cambiar entre bases de datos relacionales y NoSQL sin modificar la interfaz ni la lógica de negocio. |
| **Concurrencia y transacciones ACID** | Se implementaron transacciones atómicas de Firestore (`db.runTransaction(...)`) para importaciones masivas, prevención de identificadores duplicados y ajustes de inventario que garantizan que la disponibilidad nunca sea negativa. |
| **Índices compuestos y agregaciones en memoria** | Se configuraron índices compuestos de Firestore (`categoria + nombre`) y se implementaron agrupaciones mediante Java Streams (`Collectors.groupingBy`) y filtros de texto para complementar las limitaciones de consultas NoSQL. |
| **Seguridad IAM con privilegios mínimos** | Se reforzó el acceso a la nube utilizando cuentas de servicio dedicadas de Google Cloud con el rol `Cloud Datastore User`. Las credenciales externas se gestionan mediante `GOOGLE_APPLICATION_CREDENTIALS` y se excluyen del control de versiones. |

---

## 📋 Resumen Ejecutivo

**Lumina Hotels & Resorts** es un ecosistema integral B2C de hospitalidad y turismo que combina una plataforma de reservas con un sistema administrativo de control de inventario en tiempo real.

El proyecto está diseñado para gestionar operaciones transaccionales concurrentes (OLTP) y cargas de trabajo nativas de la nube.

La arquitectura integra:

- Un backend RESTful desarrollado en Java 21.
- Compatibilidad con Microsoft SQL Server 2022, MongoDB Atlas y Firebase Cloud Firestore.
- Una aplicación móvil nativa para Android desarrollada con Kotlin y Jetpack Compose.
- Una interfaz basada en Material Design 3.
- Un sistema de autenticación y autorización según roles.
- Gestión de reservas, alojamientos, habitaciones y atracciones turísticas.
- Control de disponibilidad y capacidad de transporte en tiempo real.

Desde la exploración de hoteles y reservas mediante calendarios interactivos hasta los paneles administrativos con operaciones CRUD y el seguimiento de la capacidad de minibuses, Lumina ofrece una experiencia móvil moderna, adaptable y preparada para su distribución mediante una versión firmada de Android.

---

## 👥 Integrantes del equipo

| N.º | Carnet | Nombre completo | Usuario de GitHub |
|---|---|---|---|
| 1 | 27-4135-2022 | AGUILAR ASCENCIO DAVID ALEXIS | [@DavidAlexis28](https://github.com/DavidAlexis28) |
| 2 | 25-2531-2022 | AMAYA HERNANDEZ ANGEL OTONIEL | [@Otoniel34](https://github.com/Otoniel34) |
| 3 | 25-0574-2024 | LARA LOPEZ CESAR GONZALO | [@cesarlaralopezlaralopez-prog](https://github.com/cesarlaralopezlaralopez-prog) |
| 4 | 17-0613-2024 | MENJIVAR CHAVEZ RICARDO FABIO | [@Fabio-Menjivar](https://github.com/Fabio-Menjivar) |
| 5 | 25-4222-2024 | MIRANDA GUZMAN ERICK ARMANDO | [@erickmiranda2703-maker](https://github.com/erickmiranda2703-maker) |
| 6 | 25-4370-2023 | MORENO MEJIA HECTOR ANTONIO | [@hector-more](https://github.com/hector-more) |
| 7 | 25-4370-2023 | REYES RODRIGUEZ SILVIA ARELY | [@SilviaReyes05](https://github.com/SilviaReyes05) |

**Docente:** Eliseo Ernesto Hernández Díaz

**Institución:** Facultad de Informática y Ciencias Aplicadas – Universidad Tecnológica de El Salvador (UTEC).

**Período académico:** 2026.

---

## 🚀 Características principales y funcionalidades

### 1. Experiencia para huéspedes y viajeros (Aplicación Android)

La aplicación ofrece una experiencia orientada al usuario, permitiendo explorar los servicios disponibles antes de registrarse.

**Exploración sin autenticación**

- Inicio de la aplicación en modo invitado.
- Exploración libre de hoteles, habitaciones y atracciones turísticas.
- Posibilidad de iniciar sesión o crear una cuenta cuando el usuario lo decida.

**Sistema de reservas con dos catálogos**

| Módulo | Funcionalidades |
|---|---|
| **Alojamientos** | Consulta de disponibilidad de habitaciones en tiempo real, precios por noche, filtrado por capacidad de huéspedes y visualización de servicios disponibles. |
| **Atracciones** | Reservación de excursiones y experiencias turísticas, con seguimiento de disponibilidad de asientos en minibuses. |

**Gestión de transporte turístico**

- Seguimiento de capacidad de minibuses en tiempo real.
- Capacidad configurable entre 8 y 15 pasajeros.
- Capacidad predeterminada de 14 pasajeros.

**Calendarios interactivos con Material Design 3**

Se utilizan componentes `DatePickerDialog` en modo de calendario visual (`DisplayMode.Picker`) para seleccionar:

- Fechas de entrada y salida de alojamientos.
- Fechas de excursiones.
- Fecha de nacimiento.
- Fecha de vencimiento del pasaporte.

**Perfil del viajero y favoritos**

- Gestión de información personal.
- Administración de datos de contacto y nacionalidad.
- Registro de información del pasaporte.
- Lista persistente de hoteles y atracciones favoritas.
- Historial de reservaciones.

### 2. Seguridad por roles y panel administrativo CRUD

El sistema incorpora control de acceso según el tipo de usuario.

**Acceso restringido para administradores**

- El Panel de Administración Lumina permanece oculto para los clientes convencionales.
- El acceso administrativo está reservado para credenciales autorizadas con el dominio `@luminatravel.com`.

**Operaciones CRUD en tiempo real**

CRUD corresponde a las operaciones Crear, Leer, Actualizar y Eliminar.

| Módulo administrativo | Funcionalidades |
|---|---|
| **Hoteles y habitaciones** | Crear establecimientos, modificar tipos de habitaciones, actualizar precios por noche, capacidades y cantidades disponibles, y eliminar alojamientos con confirmación. |
| **Atracciones y minibuses** | Registrar excursiones, modificar precios, duración, descripciones y capacidad de pasajeros, y eliminar actividades con sincronización inmediata en el catálogo y la base de datos. |

### 3. Persistencia multimotor e integridad de datos

La plataforma implementa el patrón de diseño **Polymorphic Repository Pattern**, permitiendo intercambiar motores de persistencia durante la ejecución mediante la variable `MOTOR_BD`.

Motores compatibles:

- Microsoft SQL Server.
- MongoDB Atlas.
- Firebase Cloud Firestore.

**Protección de integridad de datos en cuatro niveles**

| Nivel | Mecanismo de protección |
|---|---|
| **1. Validación de dominio** | Reglas estrictas en los métodos setter que impiden registrar precios negativos o cantidades de inventario inválidas. |
| **2. Sanitización contra inyección de rutas** | Validación de identificadores de documentos antes de construir rutas de Firestore. Se bloquean `/`, `.`, `..` y patrones reservados `__*__`. |
| **3. Transacciones atómicas y unicidad** | Prevención de claves primarias duplicadas durante escrituras concurrentes y reversión de importaciones masivas cuando algún registro presenta conflictos. |
| **4. Bloqueo pesimista de inventario** | Lectura y actualización de capacidades dentro de una misma transacción, evitando que las reservas concurrentes generen disponibilidad negativa. |

---

## 🛠️ Tecnologías utilizadas

La solución utiliza diferentes tecnologías distribuidas por capas arquitectónicas.

| Capa | Tecnología / Especificación | Función dentro de la arquitectura |
|---|---|---|
| Interfaz móvil y arquitectura | Kotlin, Jetpack Compose, Material Design 3, MVVM | Interfaz móvil declarativa y adaptable para clientes y administradores. |
| Comunicación y concurrencia | Retrofit / REST Client, Kotlin Coroutines, StateFlow | Comunicación asíncrona con el backend y actualización reactiva de la interfaz. |
| Servidor Backend | Java 21 (LTS), Maven, servidor HTTP REST integrado | Reglas de negocio, validación de roles y endpoints CRUD RESTful. |
| Base de datos NoSQL en la nube | Firebase Admin SDK 9.11.0, Cloud Firestore (Standard) | Almacenamiento documental, índices compuestos y transacciones ACID. |
| Bases de datos relacionales y documentales | Microsoft SQL Server 2022 (mssql-jdbc), MongoDB Atlas | Persistencia multimotor mediante `FabricaRepositorio`. |
| Seguridad en la nube (IAM) | Google Cloud IAM, Cloud Datastore User, claves JSON ADC | Autenticación del servidor con privilegios mínimos y credenciales externas al repositorio. |
| Construcción y empaquetado | Gradle (Kotlin DSL), Android Asset Studio, firma `.jks` | Generación de iconos adaptables y APK de distribución. |

---

## 📁 Estructura del repositorio

La organización del proyecto está distribuida en módulos independientes para facilitar el mantenimiento, desarrollo y escalabilidad.

```text
LuminaHotels_CRUD_Completo/
│
├── .github/
│   └── workflows/
│       └── ci.yml                      # Integración continua con GitHub Actions
│
├── database/
│   ├── schema.sql                      # Tablas y restricciones de SQL Server
│   └── init.sql                        # Datos iniciales de hoteles, habitaciones y atracciones
│
├── docker/                             # Configuración del entorno SQL Server con Docker
│
├── docs/                               # Documentación académica y técnica
│   ├── entrega1-perfil.md              # Documento del perfil de investigación
│   ├── adr/                            # Registros de decisiones arquitectónicas
│   └── entorno.md                      # Especificaciones del entorno reproducible
│
├── backend/                            # Backend REST Java 21 con Maven y múltiples bases de datos
│   ├── pom.xml                         # Dependencias del proyecto
│   └── src/
│       └── main/java/sv/edu/utec/
│           ├── app/                    # Servidor REST, ejecución CLI y pruebas
│           ├── datos/                  # Fábrica de repositorios y conexiones SQL, MongoDB y Firestore
│           └── modelo/                 # Entidades del dominio y DTO de agregación
│
├── mobile/                             # Aplicación Android nativa con Jetpack Compose
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/...                # Pantallas, componentes, ViewModel, Repository y API
│   │   │   ├── res/                    # Iconos, temas y cadenas de texto
│   │   │   └── AndroidManifest.xml     # Permisos, configuración de red e iconos
│   │   └── build.gradle.kts            # Dependencias y configuración de firma
│   └── build.gradle.kts                # Configuración principal de Gradle
│
├── README.md                           # Descripción, instalación y arquitectura
├── LICENSE                             # Licencia de código abierto MIT
└── .gitignore                          # Exclusiones de claves, credenciales y compilaciones
```

---

## ⚙️ Instalación y configuración (Getting Started)

### Requisitos previos

Antes de ejecutar el proyecto, es necesario contar con las siguientes herramientas y servicios:

| Requisito | Versión / Especificación |
|---|---|
| Java Development Kit (JDK) | 21 LTS |
| Apache Maven | 3.9 o superior |
| Firebase | Proyecto con Cloud Firestore, edición Standard y modo Production |
| Cuenta de servicio de Google Cloud | Archivo JSON con rol `Cloud Datastore User` |
| Microsoft SQL Server | 2022 (opcional, según el motor seleccionado) |
| MongoDB Atlas | Instancia configurada (opcional, según el motor seleccionado) |
| Android Studio o IntelliJ IDEA | Con Android SDK instalado |
| Android SDK | API 26 o superior |

### 1. Clonar el repositorio

Ejecutar los siguientes comandos en una terminal:

```bash
git clone https://github.com/DavidAlexis28/lumina-hotels-resorts.git

cd lumina-hotels-resorts
```

### 2. Configurar las variables de entorno

La clave JSON de la cuenta de servicio de Firebase debe almacenarse fuera de la carpeta del proyecto.

Ejemplo de ubicación:

```text
C:\Users\<usuario>\claves\distrisalva-app.json
```

En Linux o macOS:

```text
~/claves/distrisalva-app.json
```

Configurar las siguientes variables de entorno:

**Selección del motor de persistencia**

```bash
# Motores disponibles: firestore | mongo | sqlserver

export MOTOR_BD=firestore
```

**Credenciales de Google Cloud**

```bash
# Ruta de la clave JSON de la cuenta de servicio.
# Nunca subir este archivo a GitHub.

export GOOGLE_APPLICATION_CREDENTIALS="/path/to/claves/distrisalva-app.json"
```

> **Importante:** En Windows, si se utiliza PowerShell, las variables de entorno pueden configurarse mediante `$env:MOTOR_BD="firestore"` y `$env:GOOGLE_APPLICATION_CREDENTIALS="C:\ruta\claves\distrisalva-app.json"`.

### 3. Ejecutar el Backend Java (Puerto 8080)

Abrir una terminal dentro de la carpeta del backend:

```bash
cd backend
```

Ejecutar:

```bash
mvn clean compile exec:java
```

El servidor REST estará disponible en:

```text
http://localhost:8080
```

Para acceder desde un emulador Android, utilizar:

```text
http://10.0.2.2:8080
```

**Nota sobre Firestore:**

Durante la primera ejecución de consultas ordenadas por categoría, Firestore puede solicitar la creación de un índice compuesto.

En ese caso:

1. Revisar el enlace proporcionado en la consola.
2. Abrir el enlace de configuración del índice.
3. Habilitar el índice compuesto correspondiente.
4. Esperar a que su estado sea activo.

Índice requerido:

- `categoria` en orden ascendente.
- `nombre` en orden ascendente.

### 4. Ejecutar la aplicación móvil Android

1. Abrir la carpeta `mobile/` desde IntelliJ IDEA o Android Studio.
2. Esperar a que Gradle sincronice todas las dependencias de Jetpack Compose y Material Design 3.
3. Seleccionar un emulador Android con API 26 o superior.
4. Ejecutar la aplicación mediante el botón **Run (▶)**.
5. Comprobar la comunicación entre la aplicación móvil y el servidor REST.

---

## 🔐 Demostración y acceso administrativo

El sistema contempla tres modalidades de acceso:

| Rol | Comportamiento | Credenciales |
|---|---|---|
| **Invitado** | Acceso predeterminado al iniciar la aplicación. Permite explorar hoteles y atracciones sin autenticación. | No requiere inicio de sesión. |
| **Cliente registrado** | Puede crear una cuenta o iniciar sesión para realizar reservas y guardar favoritos. | Correo electrónico y contraseña válidos. |
| **Administrador del sistema** | Permite acceder al Panel de Administración desde la sección Mi cuenta para gestionar hoteles y atracciones mediante operaciones CRUD. | Cuenta administrativa autorizada. |

**Credenciales administrativas de demostración**

```text
Correo electrónico: admin@luminatravel.com
Contraseña: AdminLumina2026!
```

> **Advertencia de seguridad:** Estas credenciales son únicamente para demostración. No deben utilizarse en un entorno de producción ni mantenerse expuestas en un repositorio público. Se recomienda utilizar variables de entorno y credenciales seguras.

---

## 📦 Generación del APK firmado (Deployment)

Para generar una versión firmada de la aplicación Android destinada a distribución:

1. Abrir el proyecto desde IntelliJ IDEA o Android Studio.
2. Seleccionar la opción:

   `Build > Generate Signed Bundle / APK...`

3. Seleccionar **APK**.
4. Indicar la ubicación del archivo de almacenamiento de claves (`.jks`).
5. Seleccionar el alias correspondiente (`key0`).
6. Elegir la variante de compilación `release`.
7. Presionar **Create** para generar el APK.

**Ubicación del archivo generado:**

```text
mobile/app/build/outputs/apk/release/app-release.apk
```

Este archivo corresponde a la versión firmada de la aplicación y puede utilizarse para su instalación y distribución.

---

## 📄 Licencia

Este proyecto ha sido desarrollado con fines académicos y de investigación en la Facultad de Informática y Ciencias Aplicadas de la Universidad Tecnológica de El Salvador (UTEC).

Se distribuye bajo los términos de la **Licencia MIT**, que permite utilizar, modificar y distribuir el software de acuerdo con las condiciones establecidas en dicha licencia.

Consulta el archivo [LICENSE](LICENSE) para conocer los términos completos.

---

## 🎓 Proyecto académico

**Lumina Hotels & Resorts – Programación III**

Facultad de Informática y Ciencias Aplicadas

Universidad Tecnológica de El Salvador (UTEC)

**Año: 2026**

