Implementation Plan: [FEATURE]

Date: [DATE] Spec: [link]
Summary

[Extract from feature spec: primary requirement + technical approach from research]
Technical Context

Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.x, Spring Data JPA, Spring Security, Spring Cloud Stream (para eventos)
Storage: PostgreSQL con modelo puertos y adaptadores (arquitectura limpia) para no acoplarse a una BD específica
Programming style: Usar programación reactiva, funcional, usar Optional, streams, lambdas, usar desarrollo con el menor memory leak (usar StringBuilder en vez de "2 " + "asda") de acuerdo al lenguaje. Usar excepciones particulares del dominio para manejar excepciones de dominio y usar global exceptions handler para manejar excepciones globales, usar logging para loguear errores y excepciones, usar validación de datos para validar datos en entrada, usar Spring Security para autenticar y autorizar usuarios. Priorizar la codificación en prosa y la codificación en sintaxis, permitiendo entender el código más fácilmente.
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

src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── storeinvoice/
│   │           ├── StoreInvoiceApplication.java
│   │           │
│   │           ├── domain/                                       🟢 DOMINIO PURO (sin dependencias externas)
│   │           │   ├── model/
│   │           │   │   ├── Pedido.java
│   │           │   │   ├── Cliente.java
│   │           │   │   ├── LiquidacionCliente.java
│   │           │   │   ├── LiquidacionTransportista.java
│   │           │   │   ├── FormaPago.java
│   │           │   │   ├── Transportista.java
│   │           │   │   └── TasaEfectividad.java (Value Object)
│   │           │   │
│   │           │   ├── event/                                    Domain Events (hechos del negocio)
│   │           │   │   ├── DomainEvent.java (interfaz base)
│   │           │   │   ├── PedidoRecibidoEvent.java
│   │           │   │   ├── LiquidacionClienteGeneradaEvent.java
│   │           │   │   ├── LiquidacionTransportistaGeneradaEvent.java
│   │           │   │   ├── EstadoFinalRecibidoEvent.java
│   │           │   │   └── DomainEventPublisher.java (port)
│   │           │   │
│   │           │   ├── exception/                                Excepciones de negocio
│   │           │   │   ├── ClienteNoEncontradoException.java
│   │           │   │   ├── FormaPagoNoRegistradaException.java
│   │           │   │   ├── ErrorGenerarPDFException.java
│   │           │   │   ├── TasaEfectividadInvalidaException.java
│   │           │   │   ├── PedidoNoEncontradoException.java
│   │           │   │   └── DomainException.java (base)
│   │           │   │
│   │           │   └── port/                                     Contratos con el exterior
│   │           │       ├── out/                                  Puertos de SALIDA (dependencias del dominio)
│   │           │       │   ├── PedidoRepository.java
│   │           │       │   ├── ClienteRepository.java
│   │           │       │   ├── LiquidacionRepository.java
│   │           │       │   ├── PdfGenerator.java
│   │           │       │   ├── DomainEventPublisher.java
│   │           │       │   └── StorageClient.java (para subir PDFs)
│   │           │       │
│   │           │       └── in/                                   Puertos de ENTRADA (APIs que expone el dominio)
│   │           │           ├── ConsultarClientePort.java
│   │           │           ├── ConsultarFormaPagoPort.java
│   │           │           ├── ConsultarTotalPedidoPort.java
│   │           │           ├── GenerarLiquidacionClientePort.java
│   │           │           ├── GenerarLiquidacionTransportistaPort.java
│   │           │           └── ConsultarLiquidacionesPort.java
│   │           │
│   │           ├── application/                                  🟠 CAPA DE APLICACIÓN (Orquestación de casos de uso)
│   │           │   ├── service/                                  Application Services / Use Cases
│   │           │   │   ├── ConsultarClienteUseCase.java
│   │           │   │   ├── ConsultarFormaPagoUseCase.java
│   │           │   │   ├── ConsultarTotalPedidoUseCase.java
│   │           │   │   ├── GenerarLiquidacionClienteUseCase.java
│   │           │   │   ├── GenerarLiquidacionTransportistaUseCase.java
│   │           │   │   ├── ConsultarLiquidacionesUseCase.java
│   │           │   │   ├── ProcesarEventoPedidoUseCase.java
│   │           │   │   ├── ProcesarEventoEstadoFinalUseCase.java
│   │           │   │   └── IngresoFormaPagoUseCase.java
│   │           │   │
│   │           │   ├── dto/
│   │           │   │   ├── input/                                DTOs de entrada
│   │           │   │   │   ├── GenerarLiquidacionClienteRequest.java
│   │           │   │   │   ├── GenerarLiquidacionTransportistaRequest.java
│   │           │   │   │   ├── ConsultarClienteRequest.java
│   │           │   │   │   ├── FiltrosLiquidacionRequest.java
│   │           │   │   │   ├── EventoPedidoRequest.java
│   │           │   │   │   └── EventoEstadoFinalRequest.java
│   │           │   │   │
│   │           │   │   └── output/                               DTOs de salida
│   │           │   │       ├── ClienteResponse.java
│   │           │   │       ├── FormaPagoResponse.java
│   │           │   │       ├── TotalPedidoResponse.java
│   │           │   │       ├── LiquidacionClienteResponse.java
│   │           │   │       ├── LiquidacionTransportistaResponse.java
│   │           │   │       ├── LiquidacionesClienteResponse.java
│   │           │   │       ├── LiquidacionesTransportistaResponse.java
│   │           │   │       └── LiquidacionesGeneralesResponse.java
│   │           │   │
│   │           │   └── mapper/                                   Mapeo entre capas
│   │           │       ├── ClienteMapper.java
│   │           │       ├── LiquidacionMapper.java
│   │           │       ├── FormaPagoMapper.java
│   │           │       └── EventoMapper.java
│   │           │
│   │           ├── adapter/                                      🔴 INFRAESTRUCTURA (Implementaciones)
│   │           │   │
│   │           │   ├── in/                                       ADAPTADORES DE ENTRADA
│   │           │   │   ├── http/
│   │           │   │   │   ├── ClienteController.java
│   │           │   │   │   ├── LiquidacionController.java
│   │           │   │   │   ├── FormaPagoController.java
│   │           │   │   │   ├── GlobalExceptionHandler.java
│   │           │   │   │   ├── RestExceptionTranslator.java
│   │           │   │   │   └── HttpConfig.java
│   │           │   │   │
│   │           │   │   └── event/
│   │           │   │       ├── PedidoEventConsumer.java         (Consumer de eventos desde Inventario)
│   │           │   │       ├── EstadoFinalEventConsumer.java    (Consumer de eventos desde Transporte)
│   │           │   │       ├── EventConsumerConfig.java
│   │           │   │       └── KafkaConsumerConfig.java
│   │           │   │
│   │           │   └── out/                                      ADAPTADORES DE SALIDA
│   │           │       ├── persistence/
│   │           │       │   ├── entity/
│   │           │       │   │   ├── PedidoEntity.java
│   │           │       │   │   ├── ClienteEntity.java
│   │           │       │   │   ├── LiquidacionClienteEntity.java
│   │           │       │   │   ├── LiquidacionTransportistaEntity.java
│   │           │       │   │   └── FormaPagoEntity.java
│   │           │       │   │
│   │           │       │   ├── repository/
│   │           │       │   │   ├── PedidoRepositoryAdapter.java
│   │           │       │   │   ├── ClienteRepositoryAdapter.java
│   │           │       │   │   ├── LiquidacionRepositoryAdapter.java
│   │           │       │   │   └── RepositoryExceptionTranslator.java
│   │           │       │   │
│   │           │       │   ├── jpa/
│   │           │       │   │   ├── PedidoJpaRepository.java
│   │           │       │   │   ├── ClienteJpaRepository.java
│   │           │       │   │   ├── LiquidacionJpaRepository.java
│   │           │       │   │   └── FormaPagoJpaRepository.java
│   │           │       │   │
│   │           │       │   └── config/
│   │           │       │       ├── PersistenceConfig.java
│   │           │       │       └── JpaAuditingConfig.java
│   │           │       │
│   │           │       ├── messaging/
│   │           │       │   ├── publisher/
│   │           │       │   │   ├── LiquidacionEventPublisher.java
│   │           │       │   │   └── KafkaEventPublisher.java
│   │           │       │   │
│   │           │       │   └── config/
│   │           │       │       ├── KafkaProducerConfig.java
│   │           │       │       └── MessagingConfig.java
│   │           │       │
│   │           │       ├── generator/
│   │           │       │   ├── impl/
│   │           │       │   │   ├── PdfGeneratorImpl.java
│   │           │       │   │   └── StorageClientImpl.java
│   │           │       │   │
│   │           │       │   └── config/
│   │           │       │       ├── PdfGeneratorConfig.java
│   │           │       │       └── StorageConfig.java
│   │           │       │
│   │           │       └── external/                             Clientes HTTP para otros módulos
│   │           │           ├── InventarioClient.java
│   │           │           ├── TransporteClient.java
│   │           │           └── config/
│   │           │               └── ExternalServicesConfig.java
│   │           │
│   │           └── config/                                       📋 CONFIGURACIÓN GLOBAL
│   │               ├── security/
│   │               │   └── SecurityConfig.java
│   │               ├── properties/
│   │               │   └── ApplicationProperties.java
│   │               ├── bean/
│   │               │   └── BeanConfig.java
│   │               └── logger/
│   │                   └── LoggingConfig.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__init_schema.sql
│           └── V2__add_indexes.sql
│
└── test/
    ├── java/
    │   └── com/
    │       └── storeinvoice/
    │           ├── domain/                        Pruebas del dominio (sin dependencias externas)
    │           │   ├── model/
    │           │   │   ├── PedidoTest.java
    │           │   │   ├── ClienteTest.java
    │           │   │   └── TasaEfectividadTest.java
    │           │   │
    │           │   └── service/                    IMPORTANTE: No hay service tests porque el dominio
    │           │       └── (vacío)                 no tiene servicios de aplicación
    │           │
    │           ├── application/
    │           │   └── service/                    Pruebas de use cases
    │           │       ├── ConsultarClienteUseCaseTest.java
    │           │       ├── GenerarLiquidacionClienteUseCaseTest.java
    │           │       ├── GenerarLiquidacionTransportistaUseCaseTest.java
    │           │       ├── ConsultarLiquidacionesUseCaseTest.java
    │           │       └── ProcesarEventoEstadoFinalUseCaseTest.java
    │           │
    │           ├── adapter/
    │           │   ├── in/
    │           │   │   ├── http/
    │           │   │   │   ├── ClienteControllerTest.java
    │           │   │   │   ├── LiquidacionControllerTest.java
    │           │   │   │   └── GlobalExceptionHandlerTest.java
    │           │   │   │
    │           │   │   └── event/
    │           │   │       ├── PedidoEventConsumerTest.java
    │           │   │       └── EstadoFinalEventConsumerTest.java
    │           │   │
    │           │   └── out/
    │           │       ├── persistence/
    │           │       │   ├── PedidoRepositoryAdapterTest.java
    │           │       │   ├── ClienteRepositoryAdapterTest.java
    │           │       │   └── LiquidacionRepositoryAdapterTest.java
    │           │       │
    │           │       └── generator/
    │           │           ├── PdfGeneratorImplTest.java
    │           │           └── StorageClientImplTest.java
    │           │
    │           └── integration/                   Pruebas de integración (con TestContainers)
    │               ├── ConsultarClienteIntegrationTest.java
    │               ├── GenerarLiquidacionIntegrationTest.java
    │               ├── RecepcionPedidoIntegrationTest.java
    │               ├── EstadoFinalIntegrationTest.java
    │               └── ConsultarLiquidacionesIntegrationTest.java
    │
    └── resources/
        ├── test-application.yml
        ├── test-containers-config.yml
        ├── fixtures/
        │   ├── cliente-fixture.json
        │   ├── pedido-fixture.json
        │   └── liquidacion-fixture.json
        └── contracts/
            ├── consultar-cliente.yaml
            ├── consultar-forma-pago.yaml
            ├── consultar-liquidaciones.yaml
            └── recibir-evento-pedido.yaml

Structure Decision: Arquitectura Hexagonal (Puertos y Adaptadores) con Clean Architecture. El dominio es completamente independiente de frameworks (Java puro). La capa de aplicación contiene los Use Cases (orquestación de reglas de negocio). Los adaptadores implementan puertos y encapsulan toda la infraestructura (HTTP, bases de datos, eventos, almacenamiento, etc.). Esta estructura garantiza:
✅ Testabilidad: Dominio sin dependencias externas
✅ Mantenibilidad: Reglas de negocio centralizadas
✅ Escalabilidad: Fácil agregar nuevos adaptadores (REST, SOAP, gRPC, etc.)
✅ Flexibilidad de infraestructura: Cambiar BD, message broker, storage sin tocar dominio

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
    [ ] T003 Configurar herramientas de linting y formateo (Checkstyle, Spotless, SonarQube)
    [ ] T004 Crear configuración base de Docker y Docker Compose para PostgreSQL y Redis
    [ ] T005 Configurar CI/CD básico (GitHub Actions o GitLab CI)
    [ ] T006 Crear documentación inicial del proyecto (README, arquitectura, guía de desarrollo)

Phase 2: Foundational (Blocking Prerequisites)

Purpose: Core infrastructure that MUST be complete before ANY user story can be implemented

⚠️ CRITICAL: No user story work can begin until this phase is complete

    [ ] T007 Crear entidades base del dominio (Pedido, Cliente, LiquidacionCliente, LiquidacionTransportista, FormaPago)
    [ ] T008 Configurar base de datos PostgreSQL con Flyway para migraciones
    [ ] T009 Implementar capa de persistencia con JPA/Hibernate (puertos y adaptadores)
    [ ] T010 Configurar manejo de eventos con Spring Cloud Stream (Kafka/RabbitMQ)
    [ ] T011 Implementar configuración de logging estructurado (SLF4J + Logback)
    [ ] T012 Configurar manejo de excepciones global y respuestas de error estandarizadas
    [ ] T013 Implementar configuración de seguridad básica (Spring Security)
    [ ] T014 Configurar validación de datos con Bean Validation (JSR-380)
    [ ] T015 Crear DTOs base para transferencia de datos entre capas
    [ ] T016 Configurar Swagger/OpenAPI para documentación de APIs
    [ ] T017 Implementar configuración de entornos (dev, test, prod)
    [ ] T018 Crear pruebas unitarias base para capa de dominio

Checkpoint: Foundation ready - user story implementation can now begin in parallel

Checkpoint: Foundation ready - user story implementation can now begin in parallel
Phase 3: User Story 1 - Consulta de Cliente por ID Nacional (Priority: P1)

Goal: Permitir al Sistema Financiero consultar un cliente utilizando su ID Nacional (número de documento) para obtener su ID de base de datos y poder guardar la forma de pago asociada al cliente correcto en la base de datos.

Independent Test: El endpoint debe retornar el ID de base de datos del cliente en menos de 500ms cuando se proporciona un ID Nacional válido.

Tests for User Story 1

    [ ] T019 Crear test de contrato para GET /api/v1/clientes/{id_nacional} en tests/contract/test_consultar_cliente_id_nacional.java
    [ ] T020 Crear test de integración para flujo completo de consulta de cliente en tests/integration/test_consulta_cliente_completa.java

Implementation for User Story 1

    [ ] T021 Crear entidad Cliente en src/main/java/com/storeinvoice/domain/model/Cliente.java
    [ ] T022 Crear puerto ClienteRepository en src/main/java/com/storeinvoice/domain/port/out/ClienteRepository.java
    [ ] T023 Implementar ClienteRepositoryAdapter en src/main/java/com/storeinvoice/adapter/out/persistence/repository/ClienteRepositoryAdapter.java
    [ ] T024 Crear ClienteMapper en src/main/java/com/storeinvoice/application/mapper/ClienteMapper.java
    [ ] T025 Crear ConsultarClienteUseCase en src/main/java/com/storeinvoice/application/service/ConsultarClienteUseCase.java
    [ ] T026 Crear ClienteController en src/main/java/com/storeinvoice/adapter/in/http/ClienteController.java
    [ ] T027 Implementar endpoint GET /api/v1/clientes/{id_nacional} con validación y manejo de errores
    [ ] T028 Crear ClienteResponse en src/main/java/com/storeinvoice/application/dto/output/ClienteResponse.java

Checkpoint: At this point, User Story 1 should be fully functional and testable independently
Phase 4: User Story 2 - Consulta de Forma de Pago del Cliente (Priority: P2)

Goal: Permitir al Sistema Financiero consultar la forma de pago de un cliente por su ID para usar en funciones internas como la generación de liquidaciones.

Independent Test: El endpoint debe retornar la forma de pago del cliente en menos de 500ms cuando se proporciona un ID de pedido válido.

Tests for User Story 2

    [ ] T029 Crear test de contrato para GET /api/v1/pedidos/{id_pedido}/forma-pago en tests/contract/test_consultar_forma_pago.java
    [ ] T030 Crear test de integración para flujo completo de consulta de forma de pago en tests/integration/test_consulta_forma_pago_completa.java

Implementation for User Story 2

    [ ] T031 Crear ConsultarFormaPagoUseCase en src/main/java/com/storeinvoice/application/service/ConsultarFormaPagoUseCase.java
    [ ] T032 Crear FormaPagoResponse en src/main/java/com/storeinvoice/application/dto/output/FormaPagoResponse.java
    [ ] T033 Crear endpoint GET /api/v1/pedidos/{id_pedido}/forma-pago en FormaPagoController
    [ ] T034 Crear endpoint GET /api/v1/clientes/{id_cliente}/tiene-forma-pago en FormaPagoController
    [ ] T035 Implementar validación de formas de pago válidas (CONTRA_ENTREGA, CARTERA_COMERCIAL)
    [ ] T036 Agregar logging para operaciones de consulta de forma de pago
    [ ] T037 Crear FormaPagoController en src/main/java/com/storeinvoice/adapter/in/http/FormaPagoController.java

Checkpoint: At this point, User Stories 1 AND 2 should both work independently
Phase 5: User Story 3 - Recepción de Datos del Pedido desde Módulo de Inventario (Priority: P3)

Goal: Permitir al Sistema Financiero recibir los datos del pedido del Módulo de Inventario cuando se crea un nuevo pedido, para usarlos en la liquidación final.

Independent Test: El sistema debe recibir y almacenar exitosamente los datos del pedido (id_pedido, id_cliente, total_pedido, direccion) en menos de 2 segundos.

Tests for User Story 3

    [ ] T038 Crear test de contrato para evento de recepción de pedido en tests/contract/test_recepcion_pedido_event.java
    [ ] T039 Crear test de integración para flujo completo de recepción y almacenamiento de pedido en tests/integration/test_recepcion_pedido_completa.java

Implementation for User Story 3

    [ ] T040 Crear entidad Pedido en src/main/java/com/storeinvoice/domain/model/Pedido.java
    [ ] T041 Crear puerto PedidoRepository en src/main/java/com/storeinvoice/domain/port/out/PedidoRepository.java
    [ ] T042 Implementar PedidoRepositoryAdapter en src/main/java/com/storeinvoice/adapter/out/persistence/repository/PedidoRepositoryAdapter.java
    [ ] T043 Crear ProcesarEventoPedidoUseCase en src/main/java/com/storeinvoice/application/service/ProcesarEventoPedidoUseCase.java
    [ ] T044 Crear PedidoEventConsumer en src/main/java/com/storeinvoice/adapter/in/event/PedidoEventConsumer.java
    [ ] T045 Implementar lógica para consultar forma de pago del cliente al recibir el pedido
    [ ] T046 Crear EventoPedidoRequest en src/main/java/com/storeinvoice/application/dto/input/EventoPedidoRequest.java
    [ ] T047 Implementar validación de datos del pedido recibido
    [ ] T048 Agregar logging para operaciones de recepción de pedido

Checkpoint: All user stories should now be independently functional

Phase 6: User Story 4 - Recepción de Estado Final de Entrega del Módulo de Transporte (Priority: P4)

Goal: Permitir al Sistema Financiero recibir el estado final de entrega del Módulo de Transporte para generar las liquidaciones del cliente y transportista.

Independent Test: El sistema debe recibir el evento, validar los datos y procesar las liquidaciones en menos de 5 segundos.

Tests for User Story 4

    [ ] T049 Crear test de contrato para evento de estado final en tests/contract/test_estado_final_event.java
    [ ] T050 Crear test de integración para flujo completo de recepción y procesamiento de liquidaciones en tests/integration/test_procesamiento_liquidaciones_completa.java

Implementation for User Story 4

    [ ] T051 Crear EstadoFinalEventConsumer en src/main/java/com/storeinvoice/adapter/in/event/EstadoFinalEventConsumer.java
    [ ] T052 Crear GenerarLiquidacionClienteUseCase en src/main/java/com/storeinvoice/application/service/GenerarLiquidacionClienteUseCase.java
    [ ] T053 Implementar lógica para generar liquidación del cliente según fórmula: (precio_pedido + tarifa_envío) × (tasa_efectividad / 100)
    [ ] T054 Crear GenerarLiquidacionTransportistaUseCase en src/main/java/com/storeinvoice/application/service/GenerarLiquidacionTransportistaUseCase.java
    [ ] T055 Crear PdfGeneratorImpl en src/main/java/com/storeinvoice/adapter/out/generator/impl/PdfGeneratorImpl.java
    [ ] T056 Implementar validación de datos del evento (id_pedido, estado_final, tasa_efectividad, id_transportista)
    [ ] T057 Crear entidades LiquidacionCliente y LiquidacionTransportista en domain/model/
    [ ] T058 Crear puerto LiquidacionRepository en src/main/java/com/storeinvoice/domain/port/out/LiquidacionRepository.java
    [ ] T059 Agregar logging para operaciones de generación de liquidaciones

Phase 7: User Story 5 - Consulta de Liquidaciones (Priority: P5)

Goal: Permitir a clientes, transportistas y contadores consultar las liquidaciones generadas.

Independent Test: Los endpoints deben retornar las liquidaciones paginadas en menos de 3 segundos.

Tests for User Story 5

    [ ] T060 Crear test de contrato para endpoints de consulta de liquidaciones en tests/contract/test_consulta_liquidaciones.java
    [ ] T061 Crear test de integración para flujo completo de consulta de liquidaciones en tests/integration/test_consulta_liquidaciones_completa.java

Implementation for User Story 5

    [ ] T062 Crear LiquidacionController en src/main/java/com/storeinvoice/adapter/in/http/LiquidacionController.java
    [ ] T063 Crear ConsultarLiquidacionesClienteUseCase en application/service/
    [ ] T064 Crear ConsultarLiquidacionesTransportistaUseCase en application/service/
    [ ] T065 Crear ConsultarLiquidacionesGeneralesUseCase en application/service/
    [ ] T066 Implementar paginación (20 registros por página por defecto)
    [ ] T067 Implementar filtros por tipo, cliente, transportista y rango de fechas
    [ ] T068 Crear LiquidacionClienteResponse y LiquidacionTransportistaResponse en application/dto/output/
    [ ] T069 Agregar logging para operaciones de consulta de liquidaciones

Phase N: Polish & Cross-Cutting Concerns

Purpose: Improvements that affect multiple user stories

    [ ] T070 Actualizaciones de documentación en docs/
    [ ] T071 Limpieza y refactorización de código
    [ ] T072 Optimización de performance en todas las historias de usuario
    [ ] T073 Pruebas unitarias adicionales en tests/unit/
    [ ] T074 Refuerzo de seguridad y validación de datos
    [ ] T075 Configuración de monitoreo y métricas (Prometheus/Grafana)
    [ ] T076 Pruebas de carga y estrés
    [ ] T077 Documentación de API completa con ejemplos
    [ ] T078 Guía de despliegue y operación

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

Within Each User Story (Hexagonal)

    Domain models first (no framework dependencies)
    Define ports (interfaces for external dependencies)
    Create use cases (application services that orchestrate domain logic)
    Implement adapters (controllers, repositories, event consumers)
    Tests after implementation (unit tests for domain, integration tests for adapters)

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
