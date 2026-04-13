Implementation Plan: Consultar Cliente

Date: 12-04-2026 Spec: especificaciones/consultar_cliente.md
Summary

El Sistema Financiero consume endpoints del Módulo de Gestión de Clientes para obtener datos de clientes. Se implementa como cliente HTTP que consume los endpoints proporcionados por el Módulo de Clientes.
Technical Context


Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.x, Spring WebClient (reactive), Spring Security, Lombok 1.18.36
Storage: No requiere BD propia - consume datos del Módulo de Clientes
Programming style: Usar programación reactiva, funcional, usar Optional, streams, lambdas, usar desarrollo con el menor memory leak de acuerdo al lenguaje. Usar excepciones particulares del dominio para manejar excepciones de dominio y usar global exceptions handler para manejar excepciones globales, usar logging para loguear errores y excepciones, usar validación de datos para validar datos en entrada, usar Spring Security para autenticar y autorizar usuarios. Priorizar la codificación en prosa y la codificación en sintaxis, permitiendo entender el código más fácilmente. Lombok para evitar boilerplate. Records para DTOs de respuesta y de entrada de datos (inmutables, equals/hashCode/toString). Validaciones de acuerdo a los test comentados en la especificación.
Arquitectura: Arquitectura limpia (domain, use cases, infrastructure) con principios SOLID, sin acoplamiento entre capas
Testing: Test unitarios con Mockito, test de integración con TestContainers
Target Platform: Linux server, EC2
Project Type: Backend API consumidor de servicios externos
Performance Goals: <500ms para consultas, soportar 100 solicitudes concurrentes
Constraints: <200ms p95 para consultas críticas, manejo de errores de conexión
Scale/Scope: 10k transacciones diarias
Project Structure

Documentation (this feature)

specs/[feature]/
├── plan.md              # This file 
└── spec.md             # Phase 2 output

Source Code (repository root)

src/
├── main/
│   java/
│   └── com/
│       └── storeinvoice/
│           ├── StoreInvoiceApplication.java
│           │
│           ├── domain/                              # HEXÁGONO CENTRAL
│           │   ├── model/                           # Modelos de dominio
│           │   │   └── Cliente.java
│           │   └── exception/                       # Excepciones de dominio
│           │       ├── ClienteNotFoundException.java
│           │       └── DomainException.java
│           │
│           ├── application/                         # CAPA DE APLICACIÓN
│           │   ├── service/                       # CASOS DE USO
│           │   │   └── cliente/
│           │   │       ├── ConsultarClientePorIdNacionalUseCase.java
│           │   │       └── ConsultarClientePorIdUseCase.java
│           │   ├── dto/                            # DTOs
│           │   │   └── response/                    # Respuestas
│           │   │       └── ClienteResponse.java
│           │   └── exception/                      # Excepciones de aplicación
│           │       └── ApplicationException.java
│           │
│           ├── infrastructure/                     # INFRAESTRUCTURA
│           │   └── adapter/
│           │       └── outbound/                    # ADAPTADORES SALIENTES
│           │           └── external/                 # Consumo de servicios externos
│           │               ��── ClienteServiceAdapter.java  # Consume Módulo de Clientes
│           │
│           └── port/                                # PUERTOS
│               ├── inbound/                         # Inbound Ports (Driving)
│               │   └── ClienteInboundPort.java
│               └── outbound/                        # Outbound Ports (Driven)
│                   └── ClienteExternalPort.java     # Interface para consumir servicio externo
│
└── test/
    java/
    └── com/
        └── storeinvoice/
            ├── application/
            │   └── service/
            │       └── cliente/
            │           ├── ConsultarClientePorIdNacionalUseCaseTest.java
            │           └── ConsultarClientePorIdUseCaseTest.java
            └── infrastructure/
                └── adapter/
                    └── outbound/
                        └── external/
                            └── ClienteServiceAdapterTest.java

Structure Decision: El Sistema Financiero consume endpoints del Módulo de Clientes. No expone propios endpoints REST, sino que se integra como cliente HTTP.

Phase 1: Setup & Configuration

    [ ] T001 Configure WebClient for external service consumption
    [ ] T002 Configure application properties for Módulo de Clientes base URL
    [ ] T003 Add error handling for connection failures

Checkpoint: Setup ready

Phase 2: User Story 1 - Consultar Cliente por ID Nacional (Priority: P1)

Goal: El Sistema Financiero consume el endpoint GET /api/v1/clientes/{id_nacional} del Módulo de Clientes para obtener el ID de base de datos del cliente usando su ID Nacional. Retornar en menos de 500ms.

Tests for User Story 1

    [ ] T004 Create unit test for ConsultarClientePorIdNacionalUseCase
    [ ] T005 Create test for client not found scenario
    [ ] T006 Create test for empty id_nacional validation
    [ ] T007 Create test for connection error handling

Implementation for User Story 1

    [ ] T008 Create ClienteExternalPort interface in port/outbound/
    [ ] T009 Implement ClienteServiceAdapter in infrastructure/adapter/outbound/external/ to call Módulo de Clientes
    [ ] T010 Create ConsultarClientePorIdNacionalUseCase in application/service/cliente/
    [ ] T011 Create ClienteResponse record in application/dto/response/
    [ ] T012 Handle success response mapping
    [ ] T013 Handle error: "Cliente no encontrado con el ID Nacional proporcionado"
    [ ] T014 Handle error: "El ID Nacional es requerido"
    [ ] T015 Handle error: "Error al consultar el Módulo de Clientes. Intente más tarde"
    [ ] T016 Add logging for all operations

Checkpoint: User Story 1 functional

Phase 3: User Story 2 - Consultar Cliente por ID de BD (Priority: P1)

Goal: El Sistema Financiero consume el endpoint GET /api/v1/clientes/{id_cliente} del Módulo de Clientes para obtener los datos completos del cliente usando su ID de base de datos. Retornar en menos de 500ms.

Tests for User Story 2

    [ ] T017 Create unit test for ConsultarClientePorIdUseCase
    [ ] T018 Create test for client not found scenario
    [ ] T019 Create test for invalid id validation

Implementation for User Story 2

    [ ] T020 Add method findById in ClienteExternalPort
    [ ] T021 Add method call in ClienteServiceAdapter
    [ ] T022 Create ConsultarClientePorIdUseCase in application/service/cliente/
    [ ] T023 Handle error: "Cliente no encontrado con el ID proporcionado"
    [ ] T024 Handle error: "ID de cliente inválido"
    [ ] T025 Add logging for operations

Checkpoint: User Stories 1 AND 2 functional

Phase 4: Integration & Load Tests

    [ ] T026 Integration test with mocked Módulo de Clientes response
    [ ] T027 Verify <500ms response time
    [ ] T028 Verify handling of 100 concurrent requests

Success Criteria

    [SC-001]: El sistema debe retornar el ID de BD del cliente en menos de 500ms después de recibir la solicitud.
    [SC-002]: El 100% de las consultas con ID Nacional válido de clientes existentes deben retornar el ID de BD correcto.
    [SC-003]: El sistema debe retornar un mensaje de error apropiado cuando el cliente no existe.
    [SC-004]: La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultáneas.

Notes

    - El Sistema Financiero es CONSUMIDOR, no PROVEEDOR de endpoints
    - Usa WebClient reactivo para consumo de servicios externos
    - Maneja errores de conexión apropiadamente
    - No expone REST endpoints propios
    - La respuesta del Módulo de Clientes debe mapearse a ClienteResponse