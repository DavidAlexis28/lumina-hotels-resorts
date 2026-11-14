\### 6.2 Justificación académica



Desde una perspectiva académica y metodológica, esta investigación permite articular conceptos fundamentales de ingeniería de software, arquitectura de bases de datos y evaluación de rendimiento mediante un enfoque rigurosamente empírico y reproducible\[cite: 1, 2]. En el desarrollo de software formativo es habitual determinar la eficiencia de un aplicativo de manera subjetiva u observacional, asumiendo que una operación es rápida o lenta sin sustento cuantitativo\[cite: 1]. Este trabajo rompe dicho paradigma al sustituir apreciaciones cualitativas por mediciones controladas de tiempo de respuesta (latencia en milisegundos) obtenidas bajo cargas progresivas de datos\[cite: 1, 2].



Asimismo, el proyecto fundamenta su marco de evaluación en el estándar internacional \*\*ISO/IEC 25010:2023\*\* (\*Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE) — Product quality model\*), adoptando específicamente la característica de \*\*eficiencia de desempeño\*\* (\*performance efficiency\*)\[cite: 1, 2]. A través de esta norma, se analizan sistemáticamente dos subcaracterísticas críticas\[cite: 1]:

\* \*\*Comportamiento temporal (\*time behavior\*):\*\* Evaluando cómo varían los tiempos de respuesta y procesamiento en las operaciones de consulta, inserción, actualización y cancelación de reservas a medida que el volumen de la base de datos se incrementa\[cite: 1].

\* \*\*Capacidad (\*capacity\*):\*\* Determinando la viabilidad de las transacciones y la estabilidad del backend al procesar volúmenes escalonados de información (100, 1.000, 10.000 y 100.000 registros)\[cite: 1].



La investigación fomenta el diseño de experimentos reproducibles en ingeniería de software, articulando el ciclo completo de formulación de hipótesis, recolección de métricas numéricas y análisis estadístico\[cite: 1, 2].



\---



\### 6.3 Justificación social y profesional



En el ámbito social y de aplicación práctica, los sistemas de gestión hotelera constituyen el núcleo operativo para la atención de huéspedes, administración de inventarios y disponibilidad de habitaciones\[cite: 1]. Un sistema con tiempos de respuesta degradados no solo genera pérdidas económicas directas y fricción operativa en el mostrador, sino que propicia incidentes como sobreventas (\*overbooking\*), consultas desactualizadas y tiempos de espera prolongados que afectan la satisfacción del usuario final\[cite: 1]. Producir evidencia sobre el comportamiento del sistema ante volúmenes masivos de datos provee criterios sólidos para la optimización de procesos y la toma de decisiones tecnológicas en el sector servicios\[cite: 1].



En el plano profesional y formativo, el desarrollo y análisis del proyecto capacita a los integrantes en competencias de alta demanda dentro de la industria de software contemporánea\[cite: 1]:

\* Implementación de arquitecturas de backend robustas empleando características modernas de \*\*Java 21\*\*\[cite: 1].

\* Optimización del acceso y persistencia relacional con \*\*JDBC\*\* y \*\*SQL Server 2022\*\*, explorando el comportamiento de sentencias preparadas (\*PreparedStatements\*) y la mitigación de cuellos de botella derivados del plan de ejecución del motor de base de datos\[cite: 1].

\* Dominio de prácticas profesionales de control de versiones distribuidas (\*\*Git\*\* y \*\*GitHub\*\*), estructuración de flujos basados en ramas, trazabilidad mediante \*issues\* e integración formal de entregables técnicos\[cite: 1, 2].

\* Competencias en pruebas de rendimiento, análisis cuantitativo y aseguramiento de la calidad de software bajo estándares internacionales\[cite: 1, 2].



\---



\### 11. Referencias



Chacon, S., \& Straub, B. (2014). \*Pro Git\* (2.ª ed.). Apress. https://git-scm.com/book/es\[cite: 1, 2]



International Organization for Standardization. (2023). \*Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE) — Product quality model\* (ISO/IEC Standard No. 25010:2023). https://www.iso.org/standard/78176.html\[cite: 1]



Microsoft. (2026). \*Query processing architecture guide\*. Microsoft Learn. https://learn.microsoft.com/en-us/sql/relational-databases/query-processing-architecture-guide\[cite: 1]



Oracle. (2026). \*PreparedStatement (Java SE 21 \& JDK 21)\*. Oracle Java Documentation. https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html\[cite: 1]



Wohlin, C., Runeson, P., Höst, M., Ohlsson, M. C., Regnell, B., \& Wesslén, A. (2012). \*Experimentation in software engineering\*. Springer. https://doi.org/10.1007/978-3-642-29044-2\[cite: 1, 2]

