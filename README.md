# Lumina Hotels and Resorts

[![Entrega 1](https://img.shields.io/badge/Entrega-v0.1--perfil-blue.svg)](#project-status)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-Build-blue.svg)](https://maven.apache.org/)
[![Database](https://img.shields.io/badge/SQL%20Server-2022-red.svg)](https://www.microsoft.com/sql-server)
[![Android](https://img.shields.io/badge/Android-Gradle-blueviolet.svg)](https://developer.android.com/)

> Formative research and software development project for **Programming 3 (Section 03)**, Faculty of Informatics and Applied Sciences.

---

## Team Members

| No. | Student ID | Full Name | GitHub Username |
| :-: | :-: | :--- | :--- |
| 1 | 27-4135-2022 | AGUILAR ASCENCIO DAVID ALEXIS | `@DavidAlexis28` |
| 2 | 25-2531-2022 | AMAYA HERNANDEZ ANGEL OTONIEL | `?` |
| 3 | 25-0574-2024 | LARA LOPEZ CESAR GONZALO | `@cesarlaralopezlaralopez-prog` |
| 4 | 17-0613-2024 | MENJIVAR CHAVEZ RICARDO FABIO | `@Fabio-Menjivar` |
| 5 | 25-4222-2024 | MIRANDA GUZMAN ERICK ARMANDO | `@erickmiranda2703-maker` |
| 6 | 25-4370-2023 | MORENO MEJIA HECTOR ANTONIO | `@hector-more` |
| 7 | 17-2696-2024 | REYES RODRIGUEZ SILVIA ARELY | `@SilviaReyes05` |

- **Instructor:** Eliseo Ernesto Hernández Díaz  
- **Institution:** Faculty of Informatics and Applied Sciences  
- **Date:** October 10, 2026  

---

## Project Description
**Lumina Hotels and Resorts** is an integrated platform designed for the automated management of hotel operations. The system centralizes room availability tracking, guest registration, real-time booking transactions, and administrative controls.

To guarantee optimal performance under online transaction processing (OLTP) workloads, the architecture consists of a **modular Java 21 backend with Maven and JDBC over SQL Server 2022**, coupled with a **native mobile application built in Android Studio**, incorporating software engineering best practices and strict quality and performance controls.

---

## Key System Features
* **Room and Typology Management:** Real-time availability queries, filtering by features, and rate management.
* **Transactional Booking Engine:** Secure creation, modification, lookup, and cancellation of reservations using JDBC prepared statements (`PreparedStatement`).
* **Scalable Monorepo Architecture:** A clear modular split separating server-side logic (`backend/`) from the mobile client (`mobile/`).
* **Monitoring and Quality:** Continuous integration via GitHub Actions, automated testing, and Architecture Decision Records (ADRs).

---

## Technology Stack

| Component | Technology / Specification | Purpose |
| :--- | :--- | :--- |
| **Backend / Logic** | Java 21 (LTS) / Maven | Business logic and server dependency management |
| **Persistence / Connection** | JDBC (`mssql-jdbc`) | High-performance standardized database communication |
| **Database Engine** | Microsoft SQL Server 2022 | Relational storage for reservations, users, and inventory |
| **Mobile Client** | Android Studio (Gradle / Kotlin) | User interface for guests and reservation management on mobile devices |
| **Development Environment** | IntelliJ IDEA / Android Studio | Local compilation, deployment, and testing |
| **Version Control** | Git / GitHub (CI/CD) | Collaborative code management and automated pipelines |

---

## Repository Structure

```text
investigacion-reservas-hotel/
│
├── .github/
│   └── workflows/
│       └── ci.yml                      # Continuous Integration workflow (GitHub Actions)
│
├── docs/                               # Formal documentation and academic deliverables
│   ├── entrega1-perfil.md              # Research profile document
│   ├── adr/                            # Architecture Decision Records
│   └── entorno.md                      # Reproducible environment specifications
│
├── backend/                            # Java Maven Project (IntelliJ IDEA)
│   ├── pom.xml                         # Maven configuration and JDBC dependencies
│   └── src/                            # Backend source code and unit tests
│
├── mobile/                             # Mobile Application (Android Studio / Gradle)
│   ├── app/                            # Main application module
│   └── build.gradle                    # Mobile build configuration
│
├── README.md                           # General cover page and project metadata
├── LICENSE                             # Open-source license
└── .gitignore                          # System and IDE file exclusions
