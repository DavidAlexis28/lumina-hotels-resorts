ADR 0002: Gestión de dependencias y compilación mediante Maven

* Estado: Propuesta pendiente de validación
* Fecha: 14 de noviembre de 2026

Contexto y problema

El proyecto Lumina Hotels and Resorts requiere una estructura organizada para administrar sus dependencias, configurar la compilación del backend y facilitar el mantenimiento del código fuente. También necesita integrar herramientas para la conexión con SQL Server y la ejecución de pruebas unitarias.

Opciones consideradas

1. Gestión manual de bibliotecas: requiere descargar y configurar individualmente las dependencias.
2. Apache Maven: permite administrar dependencias, definir configuraciones de compilación y organizar el proyecto mediante un archivo pom.xml.
3. Otros gestores de dependencias: existen alternativas como Gradle, que también permiten automatizar la construcción del proyecto.

Decisión

Se documenta el uso de Apache Maven como herramienta de gestión de dependencias y construcción del backend, utilizando Java 21.

El archivo pom.xml incorpora el controlador JDBC de Microsoft SQL Server 12.4.2.jre11 y JUnit Jupiter 5.10.0 para pruebas. También establece Maven Compiler Plugin 3.11.0.

Consecuencias

Positivas:

- Organización centralizada de dependencias.
- Configuración de compilación definida.
- Integración de herramientas para pruebas unitarias.
- Mayor facilidad para mantener el proyecto.

Consideraciones:

- Es necesario mantener actualizadas las dependencias.
- Se requiere configurar correctamente el entorno Maven y Java.
- Deben verificarse las pruebas unitarias para comprobar su funcionamiento.