Implementation Plan: Consultar Liquidaciones del Cliente

Date: 21-02-2026 Spec: especificaciones/consultar_liquidaciones_cliente.md
Summary

Implementar endpoint REST para que el cliente pueda consultar sus liquidaciones desde WebApp, con paginación, ordenamiento descendente y acceso a PDF.

Technical Context

Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Spring Security, Lombok 1.18.36, MapStruct 1.6.3, TestContainers
Storage: PostgreSQL con modelo puertos y adaptadores
Programming style: Programación reactiva, funcional, Optional, streams, lambdas. StringBuilder para concatenación. Excepciones de dominio. Global exception handler. Logging estructurado. Bean Validation. Spring Security. Lombok para entities/adapters. Records para DTOs response (inmutables, equals/hashCode/toString generados automáticamente). MapStruct para mappings entre entidades JPA y modelos de dominio. Principios SOLID.
Architecture: Arquitectura limpia (domain, application, infrastructure) con puertos y adaptadores
Testing: Test unitarios con Mockito, test de integración con TestContainers
Target Platform: Linux server, EC2
Project Type: Backend API REST (sistema financiero)
Performance Goals: <3000ms para consultas, soportar 100 solicitudes concurrentes

Project Structure

src/
├── main/
│   └── java/
│       └── com/
│           └── storeinvoice/
│               └── store_invoice_api/
│                   ├── domain/
│                   │   ├── model/
│                   │   │   ├── LiquidacionCliente.java          # Entidad dominio (Lombok @Entity)
│                   │   │   └── Cliente.java
│                   │   └── exception/
│                   │       ├── LiquidacionNotFoundException.java
│                   │       └── ClienteNotFoundException.java    # Excepciones de dominio
│                   │
│                   ├── application/
│                   │   ├── service/
│                   │   │   └── liquidacion/
│                   │   │       └── ConsultarLiquidacionesClienteUseCase.java
│                   │   └── dto/
│                   │       ├── query/
│                   │       │   └── ConsultarLiquidacionesQuery.java  # Query DTO (record)
│                   │       └── response/
│                   │           └── LiquidacionClienteResponse.java    # Response DTO (record)
│                   │
│                   └── infrastructure/
│                       ├── adapter/
│                       │   ├── inbound/
│                       │   │   └── rest/
│                       │   │       └── LiquidacionController.java
│                       │   └── outbound/
│                       │       └── persistence/
│                       │           └── LiquidacionRepositoryAdapter.java  # Adapter (Lombok @Repository)
│                       ├── persistence/
│                       │   ├── entity/
│                       │   │   └── LiquidacionClienteJpaEntity.java      # JPA Entity (Lombok)
│                       │   └── mapper/
│                       │       └── LiquidacionEntityMapper.java          # MapStruct mapper
│                       └── port/
│                           └── outbound/
│                               └── LiquidacionRepositoryPort.java     # Puerto outbound (interfaz)

Tests structure:
src/test/java/com/storeinvoice/store_invoice_api/
├── application/service/liquidacion/
│   └── ConsultarLiquidacionesClienteUseCaseTest.java
└── infrastructure/adapter/inbound/rest/
    └── LiquidacionControllerTest.java

Consideraciones de Programación:

- Utilizar streams y lambdas para transformaciones de datos
- Usar Optional para evitar NullPointerExceptions
- Emplear StringBuilder en lugar de concatenación con +
- Excepciones de dominio para errores del negocio (ClienteNotFoundException)
- Global Exception Handler para manejo consistente
- Logging estructurado con SLF4J
- Bean Validation en capa de entrada (@Valid, @NotNull, etc.)
- Spring Security para autenticación/autorización
- Lombok para entities y adapters (getters, setters, constructores, builders)
- Records para DTOs de response y query (inmutables, equals/hashCode/toString automáticos)
- MapStruct para mappers entre JPA entities y domain models (evita boilerplate manual)
- Tests unitarios con Mockito
- TestContainers para integración

Phase 1: Implementación (User Story - Consulta de Liquidaciones del Cliente)

Goal: Permitir al cliente consultar sus liquidaciones desde WebApp con paginación y acceso a PDF.

Independent Test: El endpoint debe retornar liquidaciones paginadas en menos de 3 segundos.

Tests

    [x] T001 Crear test de contrato para GET /api/v1/clientes/{id_cliente}/liquidaciones
    [x] T002 Crear test de integración para flujo completo de consulta

Implementation

    [x] T003 Crear entidad LiquidacionCliente en domain/model/LiquidacionCliente.java (Lombok @Entity, getters/setters)
    [x] T004 Crear excepción ClienteNotFoundException en domain/exception/
    [x] T005 Crear puerto LiquidacionRepositoryPort en infrastructure/port/outbound/ (interfaz)
    [x] T006 Crear LiquidacionRepositoryAdapter en infrastructure/adapter/outbound/persistence/ (Lombok @Repository)
    [x] T007 Crear LiquidacionClienteJpaEntity en infrastructure/persistence/entity/ (Lombok @Entity, @Table)
    [x] T008 Crear LiquidacionEntityMapper en infrastructure/persistence/mapper/ (MapStruct mapper)
    [x] T009 Crear caso de uso ConsultarLiquidacionesClienteUseCase en application/service/liquidacion/
    [x] T010 Crear ConsultarLiquidacionesQuery (record) en application/dto/query/
    [x] T011 Crear LiquidacionClienteResponse (record) en application/dto/response/
    [x] T012 Crear LiquidacionController en infrastructure/adapter/inbound/rest/
    [x] T013 Implementar GET /api/v1/clientes/{id_cliente}/liquidaciones con paginación (20 por defecto)
    [x] T014 Implementar ordenamiento cronológico descendente por fecha_liquidacion
    [x] T015 Implementar validación de id_cliente (Bean Validation)
    [x] T016 Agregar logging para operaciones de consulta

Dependencies & Execution Order

    Foundational (Phase 2 de plan.md): Debe estar completo antes de iniciar esta implementación
    Esta User Story depende de: Entity LiquidacionCliente existente (Phase 2)

Within This User Story

    Domain entities before services
    Services before endpoints
    Core implementation before tests

Success Criteria (from spec)

    SC-001: Listar 100% de las liquidaciones del cliente
    SC-002: Tiempo de respuesta < 3 segundos
    SC-003: Acceso a PDF en menos de 3 clics

Functional Requirements (from spec)

    FR-001: Identificar al cliente por ID de cliente
    FR-002: Ordenar cronológicamente descendente
    FR-003: Paginación 20 registros por defecto
    FR-004: Mostrar: id_liquidacion, id_pedido, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf
    FR-005: Acceso a PDF mediante uri_pdf