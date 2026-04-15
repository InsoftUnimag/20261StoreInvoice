Implementation Plan: [FEATURE]

Date: [DATE] Spec: [link]
Summary

[Extract from feature spec: primary requirement + technical approach from research]
Technical Context


Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Spring Cloud Stream (para eventos), Lombok 1.18.36, MapStruct 1.6.3, TestContainers
Storage: PostgreSQL con modelo puertos y adaptadores (arquitectura limpia) para no acoplarse a una BD específica
Programming style: Usar programación reactiva, funcional, usar Optional, streams, lambdas, usar desarrollo con el menor memory leak (usar StringBuilder en vez de "2 " + "asda") de acuerdo al lenguaje. Usar excepciones particulares del dominio para manejar excepciones de dominio y usar global exceptions handler para manejar excepciones globales, usar logging para loguear errores y excepciones, usar validación de datos para validar datos en entrada, usar Spring Security para autenticar y autorizar usuarios. Priorizar la codificación en prosa y la codificación en sintaxis, permitiendo entender el código más fácilmente. Lombok para evitar boilerplate (getters, setters, constructors, builders en entities y adapters). Records para DTOs de respuesta y de entrada de datos (inmutables,equals/hashCode/toString). Validaciones de acuerdo a los test comentados en la especificación.
Arquitectura: Arquitectura limpia (domain, use cases, infrastructure) con principios SOLID, sin acoplamiento entre capas
Testing: Test unitarios con Mockito, test de integración con TestContainers
Target Platform: Linux server, EC2
Project Type: Backend API (sistema financiero que se comunica mediante eventos y APIs REST)
Performance Goals: <500ms para consultas, <2s para generación de liquidaciones, soportar 100 solicitudes concurrentes
Constraints: <200ms p95 para endpoints críticos, <100MB memoria heap, alta disponibilidad para procesos financieros
Scale/Scope: 10k transacciones diarias, 1M registros en BD, 50 endpoints API
Project Structure
Documentation (this feature)

specs/[feature]/
├── plan.md              # This file 
└── spec.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)

Source Code (repository root)
<!-- ACTION REQUIRED: Replace the placeholder tree below with the concrete layout for this feature. Delete unused options and expand the chosen structure with real paths (e.g., apps/admin, packages/something). The delivered plan must not include Option labels. -->

# Estructura de Proyecto: Backend API (Sistema Financiero) - Arquitectura Hexagonal
#
# PRINCIPIOS DE ARQUITECTURA HEXAGONAL (PUERTOS Y ADAPTADORES):
# - Domain (hexágono central): NO depende de nada externo. Contiene entidades, value objects, eventos de dominio
# - Application: Depende SOLO de domain. Contiene casos de uso, puertos (interfaces), DTOs
# - Infrastructure: Depende de application y domain. Contiene adaptadores concretos (REST, messaging, persistence)
#
# FLUJO DE DEPENDENCIAS: infrastructure → application → domain
#
# CLASIFICACIÓN DE PUERTOS:
# - Inbound Ports (Driving/Primary): Interfaces que definen cómo se usa la aplicación (implementadas por adaptadores entrantes)
# - Outbound Ports (Driven/Secondary): Interfaces que definen qué necesita la aplicación del exterior (implementadas por adaptadores salientes)
#
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── storeinvoice/
│   │           ├── StoreInvoiceApplication.java
│   │           │
│   │           ├── domain/                              # HEXÁGONO CENTRAL - Sin dependencias externas
│   │           │   ├── model/                           # Entidades de dominio (POJOs ricos con lógica)
│   │           │   │   ├── Pedido.java
│   │           │   │   ├── Cliente.java
│   │           │   │   ├── LiquidacionCliente.java
│   │           │   │   ├── LiquidacionTransportista.java
│   │           │   │   └── FormaPago.java
│   │           │   ├── valueobject/                     # Value Objects (objetos inmutables por valor)
│   │           │   │   ├── IdNacional.java
│   │           │   │   ├── Direccion.java
│   │           │   │   ├── Monto.java
│   │           │   │   └── TasaEfectividad.java
│   │           │   ├── event/                           # Eventos de dominio
│   │           │   │   ├── PedidoRegistradoEvent.java
│   │           │   │   ├── LiquidacionGeneradaEvent.java
│   │           │   │   └── EstadoFinalEntregaEvent.java
│   │           │   └── exception/                       # Excepciones de dominio
│   │           │       ├── PedidoNotFoundException.java
│   │           │       ├── ClienteNotFoundException.java
│   │           │       ├── LiquidacionException.java
│   │           │       └── DomainException.java
│   │           │
│   │           ├── application/                         # CAPA DE APLICACIÓN - Depende solo de domain
│   │           │   ├── service/                         # CASOS DE USO (Application Services)
│   │           │   │   ├── cliente/
│   │           │   │   │   ├── ConsultarClientePorIdNacionalUseCase.java
│   │           │   │   │   └── ConsultarFormaPagoClienteUseCase.java
│   │           │   │   ├── pedido/
│   │           │   │   │   ├── RegistrarPedidoUseCase.java
│   │           │   │   │   └── ConsultarPedidoUseCase.java
│   │           │   │   └── liquidacion/
│   │           │   │       ├── GenerarLiquidacionesUseCase.java
│   │           │   │       └── ConsultarLiquidacionesUseCase.java
│   │           │   ├── dto/                             # DTOs - Transferencia de datos
│   │           │   │   ├── command/                     # Comandos (escritura)
│   │           │   │   │   ├── RegistrarPedidoCommand.java
│   │           │   │   │   └── ProcesarEstadoFinalCommand.java
│   │           │   │   ├── query/                       # Consultas (lectura con filtros)
│   │           │   │   │   ├── ConsultarLiquidacionesQuery.java
│   │           │   │   │   └── ConsultarClienteQuery.java
│   │           │   │   └── response/                    # Respuestas (lectura)
│   │           │   │       ├── ClienteResponse.java
│   │           │   │       ├── FormaPagoResponse.java
│   │           │   │       ├── LiquidacionClienteResponse.java
│   │           │   │       └── LiquidacionTransportistaResponse.java
│   │           │   └── exception/                       # Excepciones de aplicación
│   │           │       ├── ApplicationException.java
│   │           │       └── UseCaseExecutionException.java
│   │           │
│   │           ├── infrastructure/                      # INFRAESTRUCTURA - Adaptadores concretos
│   │           │   ├── adapter/
│   │           │   │   ├── inbound/                     # ADAPTADORES ENTRANTES (Driving Adapters)
│   │           │   │   │   ├── rest/                    # Controllers REST
│   │           │   │   │   │   ├── ClienteController.java
│   │           │   │   │   │   ├── PedidoController.java
│   │           │   │   │   │   └── LiquidacionController.java
│   │           │   │   │   └── messaging/               # Consumers de eventos
│   │           │   │   │       ├── PedidoEventConsumer.java
│   │           │   │   │       └── EstadoFinalEventConsumer.java
│   │           │   │   └── outbound/                    # ADAPTADORES SALIENTES (Driven Adapters)
│   │           │   │       ├── persistence/             # Implementaciones de repositorios
│   │           │   │       │   ├── PedidoRepositoryAdapter.java
│   │           │   │       │   ├── ClienteRepositoryAdapter.java
│   │           │   │       │   └── LiquidacionRepositoryAdapter.java
│   │           │   │       └── external/                # Implementaciones de servicios externos
│   │           │   │           ├── InventarioServiceAdapter.java
│   │           │   │           └── TransporteServiceAdapter.java
│   │           │   ├── persistence/                     # CONFIGURACIÓN DE PERSISTENCIA
│   │           │   │   ├── entity/                      # Entidades JPA (mapeo a BD)
│   │           │   │   │   ├── PedidoJpaEntity.java
│   │           │   │   │   ├── ClienteJpaEntity.java
│   │           │   │   │   ├── LiquidacionClienteJpaEntity.java
│   │           │   │   │   └── LiquidacionTransportistaJpaEntity.java
│   │           │   │   ├── mapper/                      # Mappers entre Entities y Models
│   │           │   │   │   ├── PedidoEntityMapper.java
│   │           │   │   │   ├── ClienteEntityMapper.java
│   │           │   │   │   └── LiquidacionEntityMapper.java
│   │           │   │   └── DatabaseConfig.java
│   │           │   ├── messaging/                       # CONFIGURACIÓN DE MENSAJERÍA
│   │           │   │   ├── EventConfig.java
│   │           │   │   └── DeadLetterQueueConfig.java
│   │           │   ├── security/                        # CONFIGURACIÓN DE SEGURIDAD
│   │           │   │   ├── SecurityConfig.java
│   │           │   │   └── JwtAuthenticationFilter.java
│   │           │   └── pdf/                             # GENERACIÓN DE PDF
│   │           │       ├── PdfGeneratorAdapter.java
│   │           │       └── PdfTemplateConfig.java
│   │           │   ├── port/                            # PUERTOS (Interfaces) - Clasificación hexagonal
│   │           │   │   ├── inbound/                     # Inbound Ports (Driving/Primary)
│   │           │   │   │   ├── ClienteInboundPort.java  # Interface para operaciones de cliente
│   │           │   │   │   ├── PedidoInboundPort.java   # Interface para operaciones de pedido
│   │           │   │   │   └── LiquidacionInboundPort.java # Interface para operaciones de liquidación
│   │           │   │   └── outbound/                    # Outbound Ports (Driven/Secondary)
│   │           │   │       ├── PedidoRepositoryPort.java
│   │           │   │       ├── ClienteRepositoryPort.java
│   │           │   │       ├── LiquidacionRepositoryPort.java
│   │           │   │       ├── InventarioServicePort.java
│   │           │   │       └── TransporteServicePort.java
│   │           │
│   │           └── config/                              # CONFIGURACIÓN GENERAL
│   │               ├── ApplicationProperties.java
│   │               └── SwaggerConfig.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│
└── test/
    ├── java/
    │   └── com/
    │       └── storeinvoice/
    │           ├── domain/
    │           │   ├── model/
    │           │   │   └── PedidoTest.java
    │           │   ├── valueobject/
    │           │   │   ├── IdNacionalTest.java
    │           │   │   └── MontoTest.java
    │           │   ├── event/
    │           │   │   └── # Tests de eventos de dominio
    │           │   └── exception/
    │           │       └── # Tests de excepciones de dominio
    │           ├── application/
    │           │   ├── port/
    │           │   │   └── # Tests de contratos de puertos (interface tests)
    │           │   ├── service/
    │           │   │   ├── cliente/
    │           │   │   │   ├── ConsultarClientePorIdNacionalUseCaseTest.java
    │           │   │   │   └── ConsultarFormaPagoClienteUseCaseTest.java
    │           │   │   ├── pedido/
    │           │   │   │   ├── RegistrarPedidoUseCaseTest.java
    │           │   │   │   └── ConsultarPedidoUseCaseTest.java
    │           │   │   └── liquidacion/
    │           │   │       ├── GenerarLiquidacionesUseCaseTest.java
    │           │   │       └── ConsultarLiquidacionesUseCaseTest.java
    │           │   └── dto/
    │           │       └── # Tests de validación de DTOs
    │           └── infrastructure/
    │               ├── adapter/
    │               │   ├── inbound/
    │               │   │   ├── rest/
    │               │   │   │   ├── ClienteControllerTest.java
    │               │   │   │   ├── PedidoControllerTest.java
    │               │   │   │   └── LiquidacionControllerTest.java
    │               │   │   └── messaging/
    │               │   │       ├── PedidoEventConsumerTest.java
    │               │   │       └── EstadoFinalEventConsumerTest.java
    │               │   └── outbound/
    │               │       ├── persistence/
    │               │       │   ├── PedidoRepositoryAdapterTest.java
    │               │       │   ├── ClienteRepositoryAdapterTest.java
    │               │       │   └── LiquidacionRepositoryAdapterTest.java
    │               │       └── external/
    │               │           ├── InventarioServiceAdapterTest.java
    │               │           └── TransporteServiceAdapterTest.java
    │               └── integration/
    │                   ├── PedidoIntegrationTest.java
    │                   ├── ClienteIntegrationTest.java
    │                   └── LiquidacionIntegrationTest.java
    └── resources/
        ├── test-application.yml
        └── test-containers-config.yml

Structure Decision: Backend API con arquitectura limpia (Clean Architecture) basada en capas de dominio, aplicación e infraestructura. El proyecto sigue el patrón puertos y adaptadores para mantener el dominio desacoplado de la infraestructura. Se utiliza Spring Boot 3.x con Java 21 para implementar un sistema financiero que se comunica mediante eventos y APIs REST.

Consideraciones de Programación Reactiva y Funcional:

- Utilizar programación reactiva con Spring WebFlux para endpoints críticos que requieran alta concurrencia
- Implementar streams y lambdas para operaciones de colecciones y transformaciones de datos
- Usar Optional para evitar NullPointerExceptions y manejar valores opcionales de forma segura
- Emplear StringBuilder en lugar de concatenación de strings con el operador + para evitar memory leaks
- Implementar excepciones particulares del dominio (Domain Exceptions) para manejo de errores específicos del negocio
- Utilizar Global Exception Handler para manejar excepciones globales de forma consistente
- Implementar logging estructurado con SLF4J para trazabilidad y monitoreo
- Aplicar validación de datos en capa de entrada con Bean Validation (JSR-380)
- Configurar Spring Security para autenticación y autorización robusta
- Aplicar principios SOLID para mantener código limpio y mantenible
- Implementar tests unitarios con Mockito para simular dependencias externas
- Utilizar TestContainers para pruebas de integración con bases de datos reales en contenedores Docker
- Implementar tests de contrato con Spring Cloud Contract para validar contratos entre servicios
- Priorizar la codificación en prosa y la codificación en sintaxis, permitiendo entender el código más fácilmente
<!-- ============================================================================ IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only. You MUST replace these with actual tasks based on: - User stories from spec.md - Feature requirements from this file - Entities required for the use case - Endpoints required DO NOT keep these sample tasks. ============================================================================ -->
Phase 1: Setup (Shared Infrastructure)

Purpose: Project initialization and basic structure

    [ ] T001 Crear proyecto Maven/Gradle con Java 21 y Spring Boot 3.x
    [ ] T002 Configurar dependencias principales (Spring Data JPA, Spring Security, Spring Cloud Stream, TestContainers)
    [ ] T003 Crear configuración base de Docker y Docker Compose para PostgreSQL y Redis
    [ ] T004 Configurar CI/CD básico (GitHub Actions o GitLab CI)
    [ ] T005 Crear documentación inicial del proyecto (README, arquitectura, guía de desarrollo)

Phase 2: Foundational (Blocking Prerequisites)

Purpose: Core infrastructure that MUST be complete before ANY user story can be implemented

⚠️ CRITICAL: No user story work can begin until this phase is complete

    [ ] T006 Crear entidades base del dominio (Pedido, Cliente, LiquidacionCliente, LiquidacionTransportista, FormaPago)
    [ ] T007 Configurar base de datos PostgreSQL con Flyway para migraciones
    [ ] T008 Implementar capa de persistencia con JPA/Hibernate (puertos y adaptadores)
    [ ] T009 Configurar manejo de eventos con Spring Cloud Stream (RabbitMQ) // 
    [ ] T010 Implementar configuración de logging estructurado (SLF4J + Logback)
    [ ] T011 Configurar manejo de excepciones global y respuestas de error estandarizadas
    [ ] T012 Implementar configuración de seguridad básica (Spring Security)
    [ ] T013 Configurar validación de datos con Bean Validation (JSR-380)
    [ ] T014 Crear DTOs base para transferencia de datos entre capas
    [ ] T015 Configurar Swagger/OpenAPI para documentación de APIs
    [ ] T016 Implementar configuración de entornos (dev, test, prod)
    [ ] T017 Crear pruebas unitarias base para capa de dominio

Checkpoint: Foundation ready - user story implementation can now begin in parallel

Checkpoint: Foundation ready - user story implementation can now begin in parallel
Phase 3: User Story 1 - Consulta de Cliente por ID Nacional (Priority: P1)

Goal: Permitir al Sistema Financiero consultar un cliente utilizando su ID Nacional (número de documento) para obtener su ID de base de datos y poder guardar la forma de pago asociada al cliente correcto en la base de datos.

Independent Test: El endpoint debe retornar el ID de base de datos del cliente en menos de 500ms cuando se proporciona un ID Nacional válido.

Tests for User Story 1

    [ ] T018 Crear test de contrato para GET /api/v1/clientes/{id_nacional} en tests/contract/test_consultar_cliente_id_nacional.java
    [ ] T019 Crear test de integración para flujo completo de consulta de cliente en tests/integration/test_consulta_cliente_completa.java

Implementation for User Story 1

    [ ] T020 Crear entidad Cliente en src/main/java/com/storeinvoice/domain/model/Cliente.java
    [ ] T021 Crear puerto ClienteRepositoryPort en src/main/java/com/storeinvoice/application/port/outbound/ClienteRepositoryPort.java
    [ ] T022 Implementar ClienteRepositoryAdapter en src/main/java/com/storeinvoice/infrastructure/adapter/outbound/persistence/ClienteRepositoryAdapter.java
    [ ] T023 Crear caso de uso ConsultarClientePorIdNacionalUseCase en src/main/java/com/storeinvoice/application/service/cliente/
    [ ] T024 Crear ClienteController en src/main/java/com/storeinvoice/infrastructure/adapter/inbound/rest/ClienteController.java
    [ ] T025 Implementar endpoint GET /api/v1/clientes/{id_nacional} con validación y manejo de errores
    [ ] T026 Agregar logging para operaciones de consulta de cliente
    [ ] T027 Crear ClienteResponse en src/main/java/com/storeinvoice/application/dto/response/

Checkpoint: At this point, User Story 1 should be fully functional and testable independently
Phase 4: User Story 2 - Consulta de Forma de Pago del Cliente (Priority: P2)

Goal: Permitir al Sistema Financiero consultar la forma de pago de un cliente por su ID para usar en funciones internas como la generación de liquidaciones.

Independent Test: El endpoint debe retornar la forma de pago del cliente en menos de 500ms cuando se proporciona un ID de pedido válido.

Tests for User Story 2

    [ ] T028 Crear test de contrato para GET /api/v1/pedidos/{id_pedido}/forma-pago en tests/contract/test_consultar_forma_pago.java
    [ ] T029 Crear test de integración para flujo completo de consulta de forma de pago en tests/integration/test_consulta_forma_pago_completa.java

Implementation for User Story 2

    [ ] T030 Crear lógica de dominio para FormaPago en src/main/java/com/storeinvoice/domain/model/FormaPago.java
    [ ] T031 Implementar lógica para buscar forma de pago por ID de cliente
    [ ] T032 Crear caso de uso ConsultarFormaPagoClienteUseCase en src/main/java/com/storeinvoice/application/service/cliente/
    [ ] T033 Crear endpoint GET /api/v1/pedidos/{id_pedido}/forma-pago en ClienteController
    [ ] T034 Implementar validación de formas de pago válidas (CONTRA_ENTREGA, CARTERA_COMERCIAL)
    [ ] T035 Agregar logging para operaciones de consulta de forma de pago
    [ ] T036 Crear FormaPagoResponse en src/main/java/com/storeinvoice/application/dto/response/

Checkpoint: At this point, User Stories 1 AND 2 should both work independently
Phase 5: User Story 3 - Recepción de Datos del Pedido desde Módulo de Inventario (Priority: P3)

Goal: Permitir al Sistema Financiero recibir los datos del pedido del Módulo de Inventario cuando se crea un nuevo pedido, para usarlos en la liquidación final.

Independent Test: El sistema debe recibir y almacenar exitosamente los datos del pedido (id_pedido, id_cliente, total_pedido, direccion) en menos de 2 segundos.

Tests for User Story 3

    [ ] T037 Crear test de contrato para evento de recepción de pedido en tests/contract/test_recepcion_pedido_event.java
    [ ] T038 Crear test de integración para flujo completo de recepción y almacenamiento de pedido en tests/integration/test_recepcion_pedido_completa.java

Implementation for User Story 3

    [ ] T039 Crear entidad Pedido en src/main/java/com/storeinvoice/domain/model/Pedido.java
    [ ] T040 Crear puerto PedidoRepositoryPort en src/main/java/com/storeinvoice/application/port/outbound/PedidoRepositoryPort.java
    [ ] T041 Implementar PedidoRepositoryAdapter en src/main/java/com/storeinvoice/infrastructure/adapter/outbound/persistence/PedidoRepositoryAdapter.java
    [ ] T042 Crear caso de uso RegistrarPedidoUseCase en src/main/java/com/storeinvoice/application/service/pedido/
    [ ] T043 Crear PedidoEventConsumer en src/main/java/com/storeinvoice/infrastructure/adapter/inbound/messaging/PedidoEventConsumer.java
    [ ] T044 Implementar lógica para consultar forma de pago del cliente al recibir el pedido
    [ ] T045 Crear RegistrarPedidoCommand en src/main/java/com/storeinvoice/application/dto/command/
    [ ] T046 Implementar validación de datos del pedido recibido
    [ ] T047 Agregar logging para operaciones de recepción de pedido

Checkpoint: All user stories should now be independently functional

Phase 6: User Story 4 - Recepción de Estado Final de Entrega del Módulo de Transporte (Priority: P4)

Goal: Permitir al Sistema Financiero recibir el estado final de entrega del Módulo de Transporte para generar las liquidaciones del cliente y transportista.

Independent Test: El sistema debe recibir el evento, validar los datos y procesar las liquidaciones en menos de 5 segundos.

Tests for User Story 4

    [ ] T048 Crear test de contrato para evento de estado final en tests/contract/test_estado_final_event.java
    [ ] T049 Crear test de integración para flujo completo de recepción y procesamiento de liquidaciones en tests/integration/test_procesamiento_liquidaciones_completa.java

Implementation for User Story 4

    [ ] T050 Crear EstadoFinalEventConsumer en src/main/java/com/storeinvoice/infrastructure/adapter/inbound/messaging/EstadoFinalEventConsumer.java
    [ ] T051 Crear lógica de generación de liquidaciones en src/main/java/com/storeinvoice/application/service/liquidacion/GenerarLiquidacionesUseCase.java
    [ ] T052 Implementar lógica para generar liquidación del cliente según fórmula: (precio_pedido + tarifa_envío) × (tasa_efectividad / 100)
    [ ] T053 Implementar lógica para generar liquidación del transportista según matriz de porcentajes
    [ ] T054 Crear PdfGeneratorAdapter en src/main/java/com/storeinvoice/infrastructure/adapter/outbound/pdf/PdfGeneratorAdapter.java
    [ ] T055 Implementar validación de datos del evento (id_pedido, estado_final, tasa_efectividad, id_transportista)
    [ ] T056 Crear entidades LiquidacionCliente y LiquidacionTransportista en domain/model/
    [ ] T057 Implementar puerto LiquidacionRepositoryPort en application/port/outbound/ y adapter en infrastructure/adapter/outbound/persistence/
    [ ] T058 Agregar logging para operaciones de generación de liquidaciones

Phase 7: User Story 5 - Consulta de Liquidaciones (Priority: P5)

Goal: Permitir a clientes, transportistas y contadores consultar las liquidaciones generadas.

Independent Test: Los endpoints deben retornar las liquidaciones paginadas en menos de 3 segundos.

Tests for User Story 5

    [ ] T059 Crear test de contrato para endpoints de consulta de liquidaciones en tests/contract/test_consulta_liquidaciones.java
    [ ] T060 Crear test de integración para flujo completo de consulta de liquidaciones en tests/integration/test_consulta_liquidaciones_completa.java

Implementation for User Story 5

    [ ] T061 Crear LiquidacionController en src/main/java/com/storeinvoice/infrastructure/adapter/inbound/rest/LiquidacionController.java
    [ ] T062 Implementar endpoint GET /api/v1/pedidos/{id_pedido}/liquidaciones para clientes
    [ ] T063 Implementar endpoint GET /api/v1/pedidos/{id_pedido}/liquidaciones para transportistas
    [ ] T064 Implementar endpoint GET /api/v1/liquidaciones con filtros para contadores
    [ ] T065 Implementar paginación (20 registros por página por defecto)
    [ ] T066 Implementar filtros por tipo, cliente, transportista y rango de fechas
    [ ] T067 Crear LiquidacionClienteResponse y LiquidacionTransportistaResponse en application/dto/response/
    [ ] T068 Agregar logging para operaciones de consulta de liquidaciones

Phase N: Polish & Cross-Cutting Concerns

Purpose: Improvements that affect multiple user stories

    [ ] T069 Actualizaciones de documentación en docs/
    [ ] T070 Limpieza y refactorización de código
    [ ] T071 Optimización de performance en todas las historias de usuario
    [ ] T072 Pruebas unitarias adicionales en tests/unit/
    [ ] T073 Refuerzo de seguridad y validación de datos
    [ ] T074 Configuración de monitoreo y métricas (Prometheus/Grafana)
    [ ] T075 Pruebas de carga y estrés
    [ ] T076 Documentación de API completa con ejemplos
    [ ] T077 Guía de despliegue y operación

Dependencies & Execution Order
Phase Dependencies

    Setup (Phase 1): No dependencies - can start immediately
    Foundational (Phase 2): Depends on Setup completion - BLOCKS all user stories
    User Stories (Phase 3+): All depend on Foundational phase completion
        User stories can then proceed in parallel (if staffed)
        Or sequentially in priority order (P1 → P2 → P3)
    Polish (Final Phase): Depends on all desired user stories being complete

User Story Dependencies

    User Story 1 (P1): Can start after Foundational (Phase 2) - No dependencies on other stories
    User Story 2 (P2): Can start after Foundational (Phase 2) - Depende de US1 para consultar clientes
    User Story 3 (P3): Can start after Foundational (Phase 2) - No dependencies directas, pero usa entidades de US1
    User Story 4 (P4): Can start after Foundational (Phase 2) - Depende de US3 para datos del pedido
    User Story 5 (P5): Can start after Foundational (Phase 2) - Depende de US4 para datos de liquidaciones

Within Each User Story

    Models before services
    Services before endpoints
    Core implementation before integration
    Story complete before moving to next priority
    Tests after implementation

Prioridades de Implementación

    P1 (Alta): Consulta de Cliente por ID Nacional - Base para todas las operaciones
    P2 (Alta): Consulta de Forma de Pago - Necesaria para liquidaciones
    P3 (Media): Recepción de Datos del Pedido - Base para procesamiento de liquidaciones
    P4 (Alta): Recepción de Estado Final y Generación de Liquidaciones - Funcionalidad central
    P5 (Media): Consulta de Liquidaciones - Para usuarios finales y contadores

Notes

    [Story] label maps task to specific user story for traceability
    Each user story should be independently completable and testable
    Verify tests pass
    Commit after each task or logical group
    Stop at any checkpoint to validate story independently
    Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

Decisiones Técnicas Clave

    Java 21 LTS: Versión estable con mejor performance y features modernas
    Spring Boot 3.x: Framework principal para desarrollo rápido y robusto
    PostgreSQL: Base de datos relacional con soporte para transacciones complejas
    Arquitectura Limpia: Patrón puertos y adaptadores para mantener dominio desacoplado
    Event-Driven: Comunicación asíncrona con otros módulos mediante eventos
    TestContainers: Pruebas de integración con contenedores Docker reales
    CI/CD: Integración continua para validación automática de cambios

Consideraciones Específicas del Dominio Financiero

    Alta disponibilidad: El sistema debe estar disponible 99.9% del tiempo
    Consistencia transaccional: Todas las operaciones financieras deben ser atómicas
    Auditoría completa: Logging detallado de todas las operaciones críticas
    Validación estricta: Validación de datos en múltiples capas (entrada, dominio, persistencia)
    Seguridad: Spring Security para autenticación y autorización
    Performance: Tiempos de respuesta críticos para endpoints de consulta (<500ms)

Especificaciones de Negocio Implementadas

    Matriz de liquidación transportista: Entregado Completo (100%), Rechazo Parcial (80%), Devolución (0%), Faltante de Inventario (-100%)
    Formas de pago: CONTRA_ENTREGA, CARTERA_COMERCIAL
    Fórmula de liquidación cliente: (precio_pedido + tarifa_envío) × (tasa_efectividad / 100)
    Comunicación con módulos externos: Eventos asíncronos para evitar acoplamiento