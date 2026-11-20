# Lumina Hotels & Resorts

**Full-Stack Hospitality, Attraction Booking & Real-Time Inventory Management Platform**

[![Status](https://img.shields.io/badge/Status-Production%20Ready%20%28Signed%20APK%29-brightgreen.svg)](#deployment--signed-apk-build)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/UI-Material%20Design%203-0B1F3A.svg)](https://m3.material.io/)
[![Database](https://img.shields.io/badge/SQL%20Server-2022%20%7C%20JDBC-CC2927.svg)](https://www.microsoft.com/sql-server)

> Formative research and full-stack software engineering project for **Programming 3 (Section 03)**, Faculty of Informatics and Applied Sciences.

---

## Executive Summary

**Lumina Hotels & Resorts** is an end-to-end B2C hospitality and tourism booking ecosystem combined with a real-time administrative inventory control suite. Built to handle concurrent Online Transaction Processing (OLTP) workloads, the platform pairs a **RESTful Java 21 + JDBC backend over Microsoft SQL Server 2022** with a **native Android application crafted in Kotlin and Jetpack Compose (Material Design 3)**.

From guest-first browsing and interactive calendar reservations to role-restricted Admin CRUD dashboards and live minibus capacity tracking, Lumina delivers a modern, resilient, and production-signed mobile experience.

---

## Team Members

| No. | Student ID | Full Name | GitHub Username |
| :-: | :-: | :--- | :--- |
| 1 | 27-4135-2022 | AGUILAR ASCENCIO DAVID ALEXIS | `@DavidAlexis28` |
| 2 | 25-2531-2022 | AMAYA HERNANDEZ ANGEL OTONIEL | `@Otoniel34` |
| 3 | 25-0574-2024 | LARA LOPEZ CESAR GONZALO | `@cesarlaralopezlaralopez-prog` |
| 4 | 17-0613-2024 | MENJIVAR CHAVEZ RICARDO FABIO | `@Fabio-Menjivar` |
| 5 | 25-4222-2024 | MIRANDA GUZMAN ERICK ARMANDO | `@erickmiranda2703-maker` |
| 6 | 25-4370-2023 | MORENO MEJIA HECTOR ANTONIO | `@hector-more` |
| 7 | 17-2696-2024 | REYES RODRIGUEZ SILVIA ARELY | `@SilviaReyes05` |

- **Instructor:** Eliseo Ernesto Hernández Díaz  
- **Institution:** Faculty of Informatics and Applied Sciences  
- **Academic Term:** 2026  

---

## Core Features & Capabilities

### 1. B2C Guest & Traveler Experience (Android App)
- **Guest-First Exploration:** Launches cleanly in unauthenticated guest mode so travelers can freely browse hotels, room tiers, and local attractions before deciding to sign in or create an account.
- **Dual-Catalog Booking Engine:**
  - **Alojamientos (Hotels & Rooms):** Real-time room availability, nightly pricing, guest capacity filtering, and responsive amenity chips.
  - **Atracciones (Guided Tours & Experiences):** Ticket booking integrated with live small-minibus seat tracking (`8–15` configurable seats, default `14`).
- **Interactive Material 3 Date Pickers:** Modal `DatePickerDialog` components forced into visual calendar grid mode (`DisplayMode.Picker`) for check-in/check-out dates, tour schedules, date of birth, and passport expiration.
- **Traveler Profile & Favorites:** Persistent wishlist/favorites, booking history (`Reservas`), and extended traveler profile management (contact details, nationality, and passport information).

### 2. Role-Based Security & Admin CRUD Dashboard
- **Restricted Admin Access:** The **Panel de Administración Lumina** is hidden from standard customer accounts and unlocked exclusively for authorized `@luminatravel.com` administrator credentials.
- **Full Real-Time CRUD (Create, Read, Update, Delete):**
  - **Hotel & Room Management:** Create new properties, edit room typologies, nightly rates, guest capacities, and available room counts, or delete listings with confirmation safeguards.
  - **Attraction & Minibus Fleet Management:** Add new tours, edit pricing, duration, descriptions, and small-minibus passenger capacities, or remove events with instant synchronization across the catalog and SQL database.

### 3. Resilient Full-Stack Architecture
- **MVVM + Reactive State:** Unidirectional data flow in Kotlin powered by `ViewModel` and `StateFlow` for zero-latency UI updates.
- **SQL Server Persistence + Offline Resilience:** REST API endpoints backed by parameterized JDBC (`PreparedStatement`) queries against SQL Server 2022, paired with seamless offline fallback support on mobile.
- **Custom Branding & Release Signing:** Custom adaptive launcher icon (`mipmap-anydpi-v26`) featuring the gold-and-navy Lumina emblem, compiled and signed as a production-ready `.apk` via `.jks` keystore.

---

## Technology Stack

| Layer | Technology / Specification | Role in Architecture |
| :--- | :--- | :--- |
| **Mobile UI & Architecture** | Kotlin, Jetpack Compose, Material Design 3, MVVM | Declarative, responsive B2C & Admin mobile interface |
| **Networking & Concurrency** | Retrofit / REST Client, Kotlin Coroutines, `StateFlow` | Asynchronous API communication and reactive UI state |
| **Backend Server** | Java 21 (LTS), Maven, embedded REST HTTP Server (`:8080`) | Business rules, role validation, and RESTful CRUD endpoints |
| **Data Access Layer** | JDBC (`mssql-jdbc`) with `PreparedStatement` | SQL-injection-safe transactional database operations |
| **Database Engine** | Microsoft SQL Server 2022 | Relational storage for users, hotels, rooms, attractions, and bookings |
| **Build & Packaging** | Gradle (Kotlin DSL), Android Asset Studio, `.jks` Signing | Multi-density adaptive icons and release APK generation |
| **DevOps & Docs** | Git, GitHub Actions CI, Architecture Decision Records (ADRs) | Automated builds, testing, and architectural governance |

---

## Repository Structure

```text
LuminaHotels_CRUD_Completo/
│
├── .github/
│   └── workflows/
│       └── ci.yml                      # Automated CI pipeline (GitHub Actions)
│
├── database/
│   ├── schema.sql                      # SQL Server relational tables & constraints
│   └── init.sql                        # Seed data for hotels, rooms & attractions
│
├── docker/                             # Containerized SQL Server environment setup
│
├── docs/                               # Formal academic & engineering documentation
│   ├── entrega1-perfil.md              # Research profile document
│   ├── adr/                            # Architecture Decision Records
│   └── entorno.md                      # Reproducible environment specifications
│
├── backend/                            # Java 21 Maven REST & JDBC Backend
│   ├── pom.xml                         # Maven dependencies (mssql-jdbc, JUnit)
│   └── src/                            # DAOs, Models, REST Controllers & Tests
│
├── mobile/                             # Native Android Application (Jetpack Compose)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/...                # UI Screens, Components, ViewModel, Repository, API
│   │   │   ├── res/                    # Adaptive Launcher Icons (mipmap-*), Themes & Strings
│   │   │   └── AndroidManifest.xml     # App permissions, network config & icon bindings
│   │   └── build.gradle.kts            # App module dependencies & signing config
│   └── build.gradle.kts                # Root Gradle configuration
│
├── README.md                           # Project overview, setup & architecture guide
├── LICENSE                             # MIT Open-Source License
└── .gitignore                          # Keystore (*.jks), IDE & build artifact exclusions
```
---
## Getting Started

### Prerequisites
- **JDK 21 (LTS)** & **Apache Maven 3.9+**
- **Microsoft SQL Server 2022** (local instance or Docker container)
- **Android Studio / IntelliJ IDEA** with Android SDK (API 26+)

### 1. Clone the Repository
```bash
git clone [https://github.com/erickmiranda2703-maker/investigacion-reservas-hotel.git](https://github.com/erickmiranda2703-maker/investigacion-reservas-hotel.git)
cd investigacion-reservas-hotel
```

### 2. Database Setup
1. Execute `database/schema.sql` in SQL Server Management Studio (SSMS) or Azure Data Studio to create the database tables.
2. Execute `database/init.sql` to populate initial hotels, room categories, and guided attractions.

### 3. Run the Java Backend (`Port 8080`)
```bash
cd backend
mvn clean compile exec:java
```
> The REST server starts on `http://localhost:8080` (accessible from the Android Emulator via `http://10.0.2.2:8080`).

### 4. Run the Android Mobile App
1. Open the `mobile/` project in **IntelliJ IDEA** or **Android Studio**.
2. Let Gradle sync all Jetpack Compose and Material 3 dependencies.
3. Select an Android Emulator (API 26+) and click **Run (`▶`)**.

---

## Demo & Administrative Access

| Role | Access Behavior | Credentials |
| :--- | :--- | :--- |
| **Guest Traveler** | Default state on app launch; browse all hotels & attractions freely | *No login required* |
| **Registered Customer** | Create an account or sign in with any standard email to book & save favorites | Any valid email & password |
| **System Administrator** | Unlocks **Panel Admin** inside *Mi cuenta* for full Hotel & Attraction CRUD | **Email:** `admin@luminatravel.com`<br>**Password:** `AdminLumina2026!` |

---

## Deployment & Signed APK Build

To generate a production-signed release APK:
1. Navigate to **Build > Generate Signed Bundle / APK...** in IntelliJ IDEA / Android Studio.
2. Select **APK** and point to your `.jks` keystore (`key0` alias).
3. Choose the **`release`** build variant and click **Create**.
4. Locate the signed deliverable at `mobile/app/build/outputs/apk/release/app-release.apk`.

---

## License

This project is developed for academic and research purposes at the **Faculty of Informatics and Applied Sciences** and is distributed under the terms of the [MIT License](LICENSE).