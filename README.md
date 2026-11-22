Lumina Hotels & Resorts

Full-Stack Hospitality, Attraction Booking & Real-Time Inventory Management Platform

Formative research and full-stack software engineering project for Programming 3 (Section 03), Faculty of Informatics and Applied Sciences (UTEC).

What's New in This Update (v2.0 – Multi-Engine Persistence & Cloud Firestore)

Cloud Firestore Integration (firebase-admin 9.11.0): Added full asynchronous NoSQL persistence using Google Cloud Firestore (ApiFuture<T>), translating low-level SDK exceptions (InterruptedException, ExecutionException) into domain-level RepositorioException errors.

Dynamic Multi-Database Factory (FabricaRepositorio): Decoupled persistence engines via the MOTOR_BD environment variable (sqlserver, mongo, firestore), enabling hot-swapping between relational and cloud NoSQL backends without modifying UI or business logic.

Pessimistic Concurrency & ACID Transactions: Implemented atomic Firestore transactions (db.runTransaction(...)) for batch imports (all-or-nothing inserts), duplicate ID prevention, and stock/capacity adjustments that guarantee inventory never drops below zero.

Composite Indexing & In-Memory Aggregations: Configured Firestore composite indexes (categoria + nombre) and implemented Java Stream-based grouping (Collectors.groupingBy) and text filtering to bridge NoSQL query constraints.

Least-Privilege IAM Security: Hardened cloud access using dedicated Google Cloud Service Accounts (Cloud Datastore User role) via external GOOGLE_APPLICATION_CREDENTIALS keys excluded from version control.

Executive Summary

Lumina Hotels & Resorts is an end-to-end B2C hospitality and tourism booking ecosystem combined with a real-time administrative inventory control suite. Built to handle concurrent Online Transaction Processing (OLTP) and cloud-native workloads, the platform pairs a RESTful Java 21 backend supporting Microsoft SQL Server 2022, MongoDB Atlas, and Firebase Cloud Firestore with a native Android application crafted in Kotlin and Jetpack Compose (Material Design 3).

From guest-first browsing and interactive calendar reservations to role-restricted Admin CRUD dashboards and live minibus capacity tracking, Lumina delivers a modern, resilient, and production-signed mobile experience.

Team Members

No.

Student ID

Full Name

GitHub Username

1

27-4135-2022

AGUILAR ASCENCIO DAVID ALEXIS

@DavidAlexis28

2

25-2531-2022

AMAYA HERNANDEZ ANGEL OTONIEL

@Otoniel34

3

25-0574-2024

LARA LOPEZ CESAR GONZALO

@cesarlaralopezlaralopez-prog

4

17-0613-2024

MENJIVAR CHAVEZ RICARDO FABIO

@Fabio-Menjivar

5

25-4222-2024

MIRANDA GUZMAN ERICK ARMANDO

@erickmiranda2703-maker

6

25-4370-2023

MORENO MEJIA HECTOR ANTONIO

@hector-more

7

17-2696-2024

REYES RODRIGUEZ SILVIA ARELY

@SilviaReyes05

Instructor: Eliseo Ernesto Hernández Díaz

Institution: Faculty of Informatics and Applied Sciences (Universidad Tecnológica de El Salvador)

Academic Term: 2026

Core Features & Capabilities

1. B2C Guest & Traveler Experience (Android App)

Guest-First Exploration: Launches cleanly in unauthenticated guest mode so travelers can freely browse hotels, room tiers, and local attractions before deciding to sign in or create an account.

Dual-Catalog Booking Engine:

Alojamientos (Hotels & Rooms): Real-time room availability, nightly pricing, guest capacity filtering, and responsive amenity chips.

Atracciones (Guided Tours & Experiences): Ticket booking integrated with live small-minibus seat tracking (8–15 configurable seats, default 14).

Interactive Material 3 Date Pickers: Modal DatePickerDialog components forced into visual calendar grid mode (DisplayMode.Picker) for check-in/check-out dates, tour schedules, date of birth, and passport expiration.

Traveler Profile & Favorites: Persistent wishlist/favorites, booking history (Reservas), and extended traveler profile management (contact details, nationality, and passport information).

2. Role-Based Security & Admin CRUD Dashboard

Restricted Admin Access: The Panel de Administración Lumina is hidden from standard customer accounts and unlocked exclusively for authorized @luminatravel.com administrator credentials.

Full Real-Time CRUD (Create, Read, Update, Delete):

Hotel & Room Management: Create new properties, edit room typologies, nightly rates, guest capacities, and available room counts, or delete listings with confirmation safeguards.

Attraction & Minibus Fleet Management: Add new tours, edit pricing, duration, descriptions, and small-minibus passenger capacities, or remove events with instant synchronization across the catalog and database.

3. Multi-Engine Persistence & Data Integrity

Polymorphic Repository Pattern: Switch seamlessly between SQL Server, MongoDB Atlas, and Cloud Firestore at runtime via MOTOR_BD.

4-Layer Data Integrity Protection:

Domain Validation: Strict setter rules preventing negative prices or inventory counts.

Path-Injection Sanitization: Validates document IDs before constructing Firestore paths (blocking /, ., .., and reserved __*__ patterns).

Atomic Batch & Uniqueness Transactions: Prevents duplicate primary keys under concurrent writes and rolls back batch inserts if any record collides.

Pessimistic Stock Locking: Reads and updates capacity inside a single transaction so concurrent bookings never cause negative availability.

Technology Stack

Layer

Technology / Specification

Role in Architecture

Mobile UI & Architecture

Kotlin, Jetpack Compose, Material Design 3, MVVM

Declarative, responsive B2C & Admin mobile interface

Networking & Concurrency

Retrofit / REST Client, Kotlin Coroutines, StateFlow

Asynchronous API communication and reactive UI state

Backend Server

Java 21 (LTS), Maven, embedded REST HTTP Server (:8080)

Business rules, role validation, and RESTful CRUD endpoints

Cloud NoSQL Engine

Firebase Admin SDK (9.11.0), Cloud Firestore (Standard)

Serverless document storage, composite indexes & ACID transactions

Relational & Document DBs

Microsoft SQL Server 2022 (mssql-jdbc), MongoDB Atlas

Multi-engine persistence via FabricaRepositorio

Cloud Security (IAM)

Google Cloud IAM (Cloud Datastore User), ADC JSON Keys

Least-privilege server authentication outside the repository

Build & Packaging

Gradle (Kotlin DSL), Android Asset Studio, .jks Signing

Multi-density adaptive icons and release APK generation

Repository Structure

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
├── backend/                            # Java 21 Maven REST & Multi-DB Backend
│   ├── pom.xml                         # Dependencies (mssql-jdbc, mongodb, firebase-admin 9.11.0)
│   └── src/
│       └── main/java/sv/edu/utec/
│           ├── app/                    # REST Server, CLI Runner & Firestore/CRUD Verification Suites
│           ├── datos/                  # FabricaRepositorio, ConexionFirestore, Firestore/Mongo/SQL Repos
│           └── modelo/                 # Domain Entities & Aggregation DTOs
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
└── .gitignore                          # Keystore (*.jks), Service Account (*.json) & build exclusions


Getting Started

Prerequisites

JDK 21 (LTS) & Apache Maven 3.9+

Firebase Project with Cloud Firestore (Standard edition, Production Mode) & a Service Account JSON key (Cloud Datastore User role)

Microsoft SQL Server 2022 or MongoDB Atlas (optional, depending on active MOTOR_BD)

Android Studio / IntelliJ IDEA with Android SDK (API 26+)

1. Clone the Repository

git clone https://github.com/DavidAlexis28/lumina-hotels-resorts.git
cd lumina-hotels-resorts


2. Configure Environment Variables

Store your Firebase Service Account key outside the project folder (e.g., C:\Users\<user>\claves\distrisalva-app.json or ~/claves/distrisalva-app.json) and configure your environment:

# Select persistence engine: firestore | mongo | sqlserver
export MOTOR_BD=firestore

# Path to Google Cloud Service Account key (never commit this file)
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/claves/distrisalva-app.json"


3. Run the Java Backend (Port 8080)

cd backend
mvn clean compile exec:java


The REST server starts on http://localhost:8080 (accessible from the Android Emulator via http://10.0.2.2:8080).

Note: On first execution of category-sorted queries in Firestore, follow the console link to enable the composite index (categoria ASC, nombre ASC).

4. Run the Android Mobile App

Open the mobile/ project in IntelliJ IDEA or Android Studio.

Let Gradle sync all Jetpack Compose and Material 3 dependencies.

Select an Android Emulator (API 26+) and click Run (▶).

Demo & Administrative Access

Role

Access Behavior

Credentials

Guest Traveler

Default state on app launch; browse all hotels & attractions freely

No login required

Registered Customer

Create an account or sign in with any standard email to book & save favorites

Any valid email & password

System Administrator

Unlocks Panel Admin inside Mi cuenta for full Hotel & Attraction CRUD

Email: admin@luminatravel.com



Password: AdminLumina2026!

Deployment & Signed APK Build

To generate a production-signed release APK:

Navigate to Build > Generate Signed Bundle / APK... in IntelliJ IDEA / Android Studio.

Select APK and point to your .jks keystore (key0 alias).

Choose the release build variant and click Create.

Locate the signed deliverable at mobile/app/build/outputs/apk/release/app-release.apk.

License

This project is developed for academic and research purposes at the Faculty of Informatics and Applied Sciences and is distributed under the terms of the MIT License.
