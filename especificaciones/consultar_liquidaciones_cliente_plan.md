Implementation Plan: Consultar Liquidaciones del Cliente

Date: 21-02-2026 Spec: especificaciones/consultar_liquidaciones_cliente.md
Summary

Implementar un endpoint REST para que el Sistema Financiero (o WebApp) pueda consultar las liquidaciones de un cliente específico, con paginación, ordenamiento y acceso a PDFs.
Technical Context


Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.x, Spring Data JPA, Spring Security
Storage: PostgreSQL con modelo puertos y adaptadores
Programming style: Usar programación reactiva, funcional, usar Optional, streams, lambdas, usar desarrollo con el menor memory leak (usar StringBuilder en vez de "2 " + "asda") de acuerdo al lenguaje. Usar excepciones particulares del dominio para manejar excepciones de dominio y usar global exceptions handler para manejar excepciones globales, usar logging para loguear errores y excepciones, usar validación de datos para validar datos en entrada, usar Spring Security para autenticar y autorizar usuarios. Priorizar la codificación en prosa y la codificación en sintaxis, permitiendo entender el código más fácilmente.
Arquitectura: Arquitectura limpia (domain, use cases, infrastructure) con principios SOLID, sin acoplamiento entre capas
Testing: Test unitarios con Mockito, test de integración con TestContainers
Target Platform: Linux server, EC2
Project Type: Backend API REST (sistema financiero)
Performance Goals: <3000ms para consultas, soportar 100 solicitudes concurrentes
Constraints: <200ms p95 para endpoints críticos, alta disponibilidad

Project Structure

Documentation (this feature)

specs/[feature]/
├── plan.md              # This file 
└── spec.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)

Source Code (repository root)
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
│   │           │   │   ├── LiquidacionCliente.java
│   │           │   │   └── Cliente.java
│   │           │   └── exception/
│   │           │       ├── ClienteNotFoundException.java
│   │           │       └── LiquidacionNotFoundException.java
│   │           │
│   │           ├── application/                         # CAPA DE APLICACIÓN - Depende solo de domain
│   │           │   ├── service/                         # CASOS DE USO (Application Services)
│   │           │   │   └── cliente/
│   │           │   │       └── ConsultarLiquidacionesClienteUseCase.java
│   │           │   │   └── liquidacion/
│   │           │   │       └── ConsultarLiquidacionesUseCase.java
│   │           │   ├── dto/                             # DTOs - Transferencia de datos
│   │           │   │   ├── query/                       # Consultas (lectura con filtros)
│   │           │   │   │   └── ConsultarLiquidacionesQuery.java
│   │           │   │   └── response/                    # Respuestas (lectura)
│   │           │   │       └── LiquidacionClienteResponse.java
│   │           │   └── exception/                       # Excepciones de aplicación
│   │           │       ├── ApplicationException.java
│   │           │       └── UseCaseExecutionException.java
│   │           │
│   │           └── infrastructure/                      # INFRAESTRUCTURA - Adaptadores concretos
│   │               ├── port/                            # PUERTOS (Interfaces) - Clasificación hexagonal
│   │               │   ├── inbound/                     # Inbound Ports (Driving/Primary)
│   │               │   │   ├── ClienteInboundPort.java  # Interface para operaciones de cliente
│   │               │   │   ├── PedidoInboundPort.java   # Interface para operaciones de pedido
│   │               │   │   └── LiquidacionInboundPort.java # Interface para operaciones de liquidación
│   │               │   └── outbound/                    # Outbound Ports (Driven/Secondary)
│   │               │       ├── PedidoRepositoryPort.java
│   │               │       ├── ClienteRepositoryPort.java
│   │               │       ├── LiquidacionRepositoryPort.java
│   │               │       ├── InventarioServicePort.java
│   │               │       └── TransporteServicePort.java
│   │               ├── adapter/
│   │               │   ├── inbound/                     # ADAPTADORES ENTRANTES (Driving Adapters)
│   │               │   │   ├── rest/                    # Controllers REST
│   │               │   │   │   └── LiquidacionController.java
│   │               │   │   └── messaging/               # Consumers de eventos
│   │               │   │       ├── PedidoEventConsumer.java
│   │               │   │       └── EstadoFinalEventConsumer.java
│   │               │   └── outbound/                    # ADAPTADORES SALIENTES (Driven Adapters)
│   │               │       └── persistence/             # Implementaciones de repositorios
│   │               │           └── LiquidacionRepositoryAdapter.java
│   │               │           └── external/                # Implementaciones de servicios externos
│   │               │               ├── InventarioServiceAdapter.java
│   │               │               └── TransporteServiceAdapter.java
│   │               ├── persistence/                     # CONFIGURACIÓN DE PERSISTENCIA
│   │               │   ├── entity/                      # Entidades JPA (mapeo a BD)
│   │               │   │   └── LiquidacionClienteJpaEntity.java
│   │               │   └── mapper/
│   │               │       └── LiquidacionClienteEntityMapper.java
│   │               ├── messaging/                       # CONFIGURACIÓN DE MENSAJERÍA
│   │               │   ├── EventConfig.java
│   │               │   └── DeadLetterQueueConfig.java
│   │               ├── security/                        # CONFIGURACIÓN DE SEGURIDAD
│   │               │   ├── SecurityConfig.java
│   │               │   └── JwtAuthenticationFilter.java
│   │               └── pdf/                             # GENERACIÓN DE PDF
│   │                   ├── PdfGeneratorAdapter.java
│   │                   └── PdfTemplateConfig.java
│   │               ├── config/                              # CONFIGURACIÓN GENERAL
│   │               │   ├── ApplicationProperties.java
│   │               │   └── SwaggerConfig.java
│   │           │
│   │           └── resources/
│   │               ├── application.yml
│   │               ├── application-dev.yml
│   │               ├── application-prod.yml
│   │               └── db/migration/
│   │
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── storeinvoice/
│       │           ├── domain/
│       │           │   ├── model/
│       │           │   │   └── PedidoTest.java
│       │           │   ├── valueobject/
│       │           │   │   ├── IdNacionalTest.java
│       │           │   │   └── MontoTest.java
│       │           │   ├── event/
│       │           │   │   └── # Tests de eventos de dominio
│       │           │   └── exception/
│       │           │       └── # Tests de excepciones de dominio
│       │           ├── application/
│       │           │   ├── port/
│       │           │   │   └── # Tests de contratos de puertos (interface tests)
│       │           │   ├── service/
│       │           │   │   ├── cliente/
│       │           │   │   │   ├── ConsultarClientePorIdNacionalUseCaseTest.java
│       │           │   │   │   └── ConsultarFormaPagoClienteUseCaseTest.java
│       │           │   │   ├── pedido/
│       │           │   │   │   ├── RegistrarPedidoUseCaseTest.java
│       │           │   │   │   └── ConsultarPedidoUseCaseTest.java
│       │           │   │   └── liquidacion/
│       │           │   │       ├── GenerarLiquidacionesUseCaseTest.java
│       │           │   │       └── ConsultarLiquidacionesUseCaseTest.java
│       │           │   └── dto/
│       │           │       └── # Tests de validación de DTOs
│       │           └── infrastructure/
│       │               ├── adapter/
│       │               │   ├── inbound/
│       │               │   │   ├── rest/
│       │               │   │   │   ├── ClienteControllerTest.java
│       │               │   │   │   ├── PedidoControllerTest.java
│       │               │   │   │   └── LiquidacionControllerTest.java
│       │               │   │   └── messaging/
│       │               │   │       ├── PedidoEventConsumerTest.java
│       │               │   │       └── EstadoFinalEventConsumerTest.java
│       │               │   └── outbound/
│       │               │       ├── persistence/
│       │               │       │   ├── PedidoRepositoryAdapterTest.java
│       │               │       │   ├── ClienteRepositoryAdapterTest.java
│       │               │       │   └── LiquidacionRepositoryAdapterTest.java
│       │               │       └── external/
│       │               │           ├── InventarioServiceAdapterTest.java
│       │               │           └── TransporteServiceAdapterTest.java
│       │               └── integration/
│       │                   ├── PedidoIntegrationTest.java
│       │                   ├── ClienteIntegrationTest.java
│       │                   └── LiquidacionIntegrationTest.java
│       │               └── resources/
│       │                   ├── test-application.yml
│       │                   └── test-containers-config.yml
│       │
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── db/migration/
│
└── test/
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

Phase 1: Setup (Shared Infrastructure)

Purpose: Project initialization and basic structure

    [ ] P001 Crear proyecto Maven/Gradle con Java 21 y Spring Boot 3.x
    [ ] P002 Configurar dependencias principales (Spring Data JPA, Spring Security, TestContainers)
    [ ] P003 Crear configuración base de Docker y Docker Compose para PostgreSQL
    [ ] P004 Configurar CI/CD básico (GitHub Actions o GitLab CI)
    [ ] P005 Crear documentación inicial del proyecto (README, arquitectura, guía de desarrollo)

Phase 2: Foundational (Blocking Prerequisites)

Purpose: Core infrastructure that MUST be complete before ANY user story can be implemented

⚠️ CRITICAL: No user story work can begin until this phase is complete

    [ ] P006 Crear entidades base del dominio (Cliente, LiquidacionCliente)
    [ ] T007 Configurar base de datos PostgreSQL con Flyway para migraciones
    [ ] T008 Implementar capa de persistencia con JPA/Hibernate (puertos y adaptadores)
    [ ] T009 Configurar manejo de excepciones global y respuestas de error estandarizadas
    [ ] T010 Configurar logging estructurado (SLF4J + Logback)
    [ ] T011 Configurar validación de datos con Bean Validation (JSR-380)
    [ ] T012 Configurar manejo de excepciones global y respuestas de error estandarizadas
    [ ] T013 Configurar seguridad básica (Spring Security)
    [ ] T014 Crear DTOs base para transferencia de datos entre capas
    [ ] T015 Configurar Swagger/OpenAPI para documentación de APIs
    [ ] T016 Implementar configuración de entornos (dev, test, prod)
    [ ] T017 Crear pruebas unitarias base para capa de dominio

Checkpoint: Foundation ready - user story implementation can now begin in parallel

Checkpoint: Foundation ready - user story implementation can now begin in parallel

Phase 3: User Story 1 - Consulta de Liquidaciones (P1)

Goal: Permitir al cliente consultar sus liquidaciones desde una interfaz web con paginación y acceso a PDFs

Independent Test: El endpoint debe retornar las liquidaciones paginadas en menos de 3 segundos

Tests for User Story 1

    [ ] T018 Crear test de contrato para GET /api/v1/liquidaciones/cliente/{id_cliente}
    [ ] T019 Crear test de integración para flujo completo de consulta de liquidaciones

Implementation for User Story 1

    [ ] T020 Crear entidad LiquidacionCliente en domain/model/
    [ ] T021 Crear excepción LiquidacionNotFoundException en domain/exception/
    [ ] T022 Crear puerto LiquidacionRepositoryPort en infrastructure/port/outbound/
    [ ] T023 Crear LiquidacionRepositoryAdapter en infrastructure/adapter/outbound/persistence/
    [ ] T024 Crear LiquidacionEntityMapper en infrastructure/persistence/mapper/
    [ ] T025 Crear caso de uso ConsultarLiquidacionesClienteUseCase en application/service/
    [ ] T026 Crear ConsultarLiquidacionesQuery en application/dto/query/
    [ ] T027 Crear LiquidacionClienteResponse en application/dto/response/
    [ ] T028 Crear LiquidacionController en infrastructure/adapter/inbound/rest/
    [ ] T029 Implementar GET /api/v1/liquidaciones/cliente/{id_cliente} con paginación
    [ ] T030 Agregar logging para operaciones de consulta

Checkpoint: At this point, User Story 1 should be fully functional and testable independently

Phase 4: Polish & Cross-Cutting Concerns

Purpose: Improvements that affect multiple user stories

    [ ] T031 Validar criterios de éxito (SC-001, SC-002, SC-003)
    [ ] T032 Pruebas de carga (100 solicitudes simultáneas)
    [ ] T033 Documentación de API con ejemplos
    [ ] T034 Refactorización si es nécessaire

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

Within Each User Story

    Models before services
    Services before endpoints
    Core implementation before integration
    Story complete before moving to next priority
    Tests after implementation

Prioridades de Implementación

    P1 (Alta): Consulta de Liquidaciones - Funcionalidad central

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
    TestContainers: Pruebas de integración con contenedores Docker reales
    CI/CD: Integración continua para validación automática de cambios

Consideraciones Específicas del Dominio Financiero

    Alta disponibilidad: El sistema debe estar disponible 99.9% del tiempo
    Consistencia transaccional: Todas las operaciones financieras deben ser atómicas
    Auditoría completa: Logging detallado de todas las operaciones críticas
    Validación estricta: Validación de datos en múltiples capas (entrada, dominio, persistencia)
    Seguridad: Spring Security para autenticación y autorización
    Performance: Tiempos de respuesta críticos para endpoints de consulta (<3000ms)

Especificaciones de Negocio Implementadas

    Paginación: 20 registros por página por defecto
    Ordenamiento: Cronológico descendente
    Acceso a PDF: A través de uri_pdf