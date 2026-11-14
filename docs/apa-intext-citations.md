# Normalización de Citas Técnicas en Texto (Normas APA 7.ª Edición)

## Justificación Académica con Citas Normalizadas
Desde una perspectiva metodológica en ingeniería de software, la evaluación del comportamiento de sistemas transaccionales requiere transitar de valoraciones empíricas intuitivas hacia mediciones cuantitativas controladas[cite: 1]. La calidad de software no puede fundamentarse únicamente en la percepción subjetiva de fluidez; exige parámetros verificables de latencia y procesamiento[cite: 1].

Para establecer un marco de evaluación formal, esta investigación adopta el modelo de calidad de producto de la norma ISO/IEC 25010:2023, centrándose en la característica de eficiencia de desempeño. Dentro de este estándar, se analizan dos subcaracterísticas esenciales: el comportamiento temporal, que mide los tiempos de respuesta y tasas de procesamiento del sistema frente a consultas concurrentes, y la capacidad, orientada a determinar los límites operativos del software conforme aumenta el volumen de transacciones almacenadas[cite: 1]. Asimismo, el diseño de la experimentación sigue las directrices metodológicas para ingeniería de software establecidas por Wohlin et al. (2012), asegurando la reproducibilidad de las pruebas mediante la definición explícita de variables, hipótesis y réplicas[cite: 1, 2].

---

## Justificación Técnica con Citas Normalizadas
La interacción entre una capa de aplicación construida en Java y un motor relacional involucra transformaciones críticas en el plan de cómputo[cite: 1]. De acuerdo con la arquitectura de procesamiento de consultas documentada por Microsoft (2026), SQL Server compila, optimiza y almacena planes de ejecución que pueden degradarse severamente ante variaciones masivas en las estadísticas de datos[cite: 1]. Cuando las tablas de reservas carecen de estrategias adecuadas de indexación, el optimizador recurre a escaneos completos (*table scans*) y escalamientos de bloqueos (*lock escalation*), incrementando el consumo de memoria RAM y las operaciones de entrada/salida en disco[cite: 1].

En el nivel de la aplicación, el acceso a los datos se implementa mediante la interfaz JDBC de Java 21, utilizando de forma estandarizada objetos `PreparedStatement`[cite: 1]. Según Oracle (2026), las sentencias precompiladas permiten parametrizar consultas y reutilizar planes de ejecución en el servidor de base de datos, mitigando la sobrecarga computacional asociada a la recompilación continua de instrucciones SQL y reduciendo los tiempos de respuesta del cliente[cite: 1]. Adicionalmente, el control de cambios y la trazabilidad de las mediciones se fundamentan en las prácticas de gestión distribuida descritas por Chacon y Straub (2014)[cite: 1, 2].

---

## Tabla de Mapeo de Citas APA 7.ª Edición

| Fuente Técnica | Tipo de Cita en Texto (Parentética) | Tipo de Cita en Texto (Narrativa) | Sección del Documento |
| :--- | :--- | :--- | :--- |
| ISO/IEC 25010:2023 | (ISO/IEC, 2023)[cite: 1, 2] | Según la norma ISO/IEC 25010:2023...[cite: 1] | 4.1 Delimitación teórica / 6.2 Justificación académica[cite: 1] |
| Microsoft (Guía de Arquitectura SQL Server) | (Microsoft, 2026)[cite: 1] | De acuerdo con Microsoft (2026)...[cite: 1] | 2. Planteamiento del problema / 6.1 Justificación técnica[cite: 1] |
| Oracle (Documentación JDBC PreparedStatement) | (Oracle, 2026)[cite: 1] | Como especifica Oracle (2026)...[cite: 1] | 2. Planteamiento del problema / 6.1 Justificación técnica[cite: 1] |
| Wohlin et al. (Experimentación en Ingeniería) | (Wohlin et al., 2012)[cite: 1, 2] | Wohlin et al. (2012) proponen que...[cite: 1, 2] | 6.2 Justificación académica / 9. Evidencia técnica[cite: 1] |
| Chacon & Straub (Gestión de Versiones con Git) | (Chacon & Straub, 2014)[cite: 1, 2] | Siguiendo a Chacon y Straub (2014)...[cite: 1, 2] | 6.3 Justificación social y profesional / 10. Repositorio[cite: 1] |