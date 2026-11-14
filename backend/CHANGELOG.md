Changelog

En este archivo se registran los cambios importantes realizados en el proyecto Lumina Hotels & Resorts.

[1.0.0] - 2026-11-14

Agregado

- Estructura inicial del backend en Java 21.
- Organización del código por capas: config, controllers, dto, models, repositories y services.
- Integración prevista con SQL Server mediante JDBC.
- Configuración de Firebase mediante Firebase Admin SDK.
- Documentación de decisiones arquitectónicas (ADR-0001 al ADR-0009).
- Documentación de configuración y despliegue del backend.

Seguridad

- Consideraciones para proteger las credenciales de Firebase.
- Recomendación de utilizar variables de entorno para información confidencial.
- Protección de archivos sensibles mediante ".gitignore".

Documentación

- Registro de decisiones arquitectónicas.
- Estrategia propuesta de validaciones y manejo de errores.
- Estrategia propuesta de pruebas del backend.
- Lineamientos propuestos para configuración y despliegue.

Pendiente de validación

- Verificar la compilación completa del proyecto.
- Ejecutar y documentar las pruebas.
- Confirmar la conexión con SQL Server y Firebase.
- Validar el despliegue del backend.