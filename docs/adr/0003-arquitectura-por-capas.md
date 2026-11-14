ADR-0003: Arquitectura organizacional por capas

Proyecto: Lumina Hotels & Resorts
Estado: Aceptado (sujeto a validación del equipo)
Fecha: 14 de noviembre de 2026

1. Contexto

El backend de Lumina Hotels & Resorts necesita una estructura organizada que permita separar las responsabilidades del sistema, facilitar el mantenimiento del código y mejorar su comprensión. Por esta razón, se establece una arquitectura organizada por capas.

2. Decisión

Se establece una arquitectura por capas, donde cada paquete cumple una función específica dentro del backend:

- config: contiene las configuraciones del sistema, como FirebaseConfig.java.
- controllers: recibe y gestiona las solicitudes HTTP.
- dto: permite transportar datos mediante objetos como ReservaRequestDTO.java.
- models: representa las entidades principales del sistema, como Habitacion.java y Reserva.java.
- repositories: administra las operaciones de acceso, consulta y almacenamiento de datos.
- services: contiene la lógica del negocio y las operaciones relacionadas con las reservas.

3. Justificación

Esta organización permite mantener el código ordenado, separar las responsabilidades y facilitar el trabajo colaborativo entre los integrantes del equipo. También permite realizar modificaciones en una capa sin afectar innecesariamente las demás.

4. Consecuencias

Positivas:

- Código más organizado y fácil de mantener.
- Mejor separación de responsabilidades.
- Facilita las pruebas y futuras mejoras.
- Permite trabajar de manera más ordenada en equipo.

Desventajas:

- Requiere crear y mantener varias clases y paquetes.
- Puede aumentar la cantidad de archivos del proyecto.

5. Alternativas consideradas

- Organizar todo el código en un solo paquete.
- Utilizar una arquitectura sin separación entre controladores, servicios y repositorios.

Se eligió la arquitectura por capas porque ofrece una mejor organización y facilita el crecimiento del sistema.

6. Referencia de implementación

Paquete principal: "com.lumina.backend"

Estructura de paquetes:

- config
- controllers
- dto
- models
- repositories
- services