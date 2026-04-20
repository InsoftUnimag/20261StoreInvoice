Implementation Plan: Registrar forma de pago del cliente

Date: 17-04-2026 Spec: especificaciones/Ingresar_forma_de_pago_cliente.md
Summary

Proceso interno del Sistema Financiero para registrar la forma de pago (CONTRA_ENTREGA o CARTERA_COMERCIAL) de un cliente por su ID Nacional. Este proceso NO es un endpoint REST sino una operación interna usada por el Asesor Comercial antes de generar pedidos.
Technical Context


Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
Storage: PostgreSQL con modelo puertos y adaptadores
Programming style: Programación reactiva, funcional, Optional, lambdas, Lombok para entidades, Records para DTOs, validaciones Bean Validation, excepciones de dominio
Arquitectura: Arquitectura limpia (domain, application, infrastructure) con principios SOLID
Testing: JUnit 5 + Mockito + TestContainers
Performance Goals: <500ms para guardado de forma de pago
Project Structure

especificaciones/Ingresar_forma_de_pago_cliente/
├── plan.md              # This file 
└── spec.md             # Feature specification

Source Code (repository root)

src/main/java/com/storeinvoice/store_invoice_api/
├── domain/
│   ├── model/
│   │   └── FormaPagoCliente.java               # Modelo - guarda forma_pago por id_cliente
│   ├── valueobject/
│   │   └── FormaPago.java                       # Enum Value Object
│   └── exception/
│       └── FormaPagoInvalidaException.java     # Excepción para forma de pago inválida
│
├── application/
│   ├── service/
│   │   └── cliente/
│   │       └── RegistrarFormaPagoClienteUseCase.java  # Caso de uso
│   ├── dto/
│   │   ├── command/
│   │   │   └── RegistrarFormaPagoCommand.java  # Comando entrada
│   │   └── response/
│   │       └── FormaPagoResponse.java          # Respuesta
│   └── port/
│       └── inbound/
│           └── FormaPagoInboundPort.java       # Puerto inbound
│
└── infrastructure/
    ├── port/
    │   └── outbound/
    │       └── FormaPagoClienteRepositoryPort.java  # Puerto outbound
    ├── adapter/
    │   └── outbound/
    │       └── persistence/
    │           └── FormaPagoClienteRepositoryAdapter.java  # Adapter
    └── persistence/
        ├── entity/
        │   └── FormaPagoClienteJpaEntity.java   # Entity JPA
        └── mapper/
            └── FormaPagoClienteMapper.java      # Mapper MapStruct

Structure Decision: API REST basada en Spring WebFlux. La forma de pago se guarda en tabla separada `formas_pago_cliente` asociada al id_cliente del sistema externo. Exposición mediante `FormaPagoController` en `/api/v1/clientes/forma-pago`.

Phase 1: Domain Layer - Modelo de Forma de Pago Cliente

Purpose: Agregar dominio para forma de pago

    [x] T001 Crear enum FormaPago en domain/valueobject/FormaPago.java con valores CONTRA_ENTREGA y CARTERA_COMERCIAL
    [x] T002 Crear excepción FormaPagoInvalidaException en domain/exception/FormaPagoInvalidaException.java
    [x] T003 Crear modelo FormaPagoCliente en domain/model/FormaPagoCliente.java

Checkpoint: Domain layer ready

Phase 2: Application Layer - Caso de Uso

Purpose: Implementar lógica de negocio para registrar forma de pago

    [x] T004 Crear RegistrarFormaPagoCommand en application/dto/command/RegistrarFormaPagoCommand.java
    [x] T005 Crear FormaPagoInboundPort en infrastructure/port/inbound/FormaPagoInboundPort.java
    [x] T006 Crear RegistrarFormaPagoClienteUseCase en application/service/cliente/RegistrarFormaPagoClienteUseCase.java
    [x] T007 Crear FormaPagoResponse en application/dto/response/FormaPagoResponse.java

Checkpoint: Application layer ready

Phase 3: Infrastructure Layer - Persistencia

Purpose: Guardar forma de pago en base de datos

    [x] T008 Crear migración Flyway V1__create_forma_pago_cliente_table.sql
    [x] T009 Crear FormaPagoClienteJpaEntity en infrastructure/persistence/entity/FormaPagoClienteJpaEntity.java
    [x] T010 Crear FormaPagoClienteMapper en infrastructure/persistence/mapper/FormaPagoClienteMapper.java
    [x] T011 Crear FormaPagoClienteRepositoryPort en infrastructure/port/outbound/FormaPagoClienteRepositoryPort.java
    [x] T012 Crear FormaPagoClienteRepositoryAdapter en infrastructure/adapter/outbound/persistence/FormaPagoClienteRepositoryAdapter.java

Checkpoint: Infrastructure layer ready

Phase 5: API REST Exposure & Integration Tests

Purpose: Expose endpoint and verify integration

    [x] T016 Crear FormaPagoController en infrastructure/adapter/inbound/rest/FormaPagoController.java
    [x] T017 Crear test de integración para el endpoint usando WebTestClient (test/java/infrastructure/adapter/inbound/rest/FormaPagoControllerIntegrationTest.java)

Checkpoint: API REST ready and tested

Dependencies & Execution Order

    Phase Dependencies
        Phase 1 (Domain): No dependencies - can start immediately
        Phase 2 (Application): Depends on Phase 1 - Domain model must exist
        Phase 3 (Infrastructure): Depends on Phase 2 - Use case must be defined
        Phase 4 (Tests): Depends on all implementation phases
        Phase 5 (API REST): Depends on Phase 2 and Phase 3 - Use case and repository ready

    User Story Dependencies
        This feature depends on User Story 1 (Consultar Cliente por ID Nacional) - existing implementation provides ClienteRepositoryPort

Prioridades de Implementación

    P1 (Alta): Extender domain layer para forma de pago
    P2 (Alta): Implementar caso de uso
    P3 (Media): Actualizar persistencia
    P4 (Baja): Tests unitarios
    P5 (Alta): API REST Exposure

Notes

Notes

    [Feature] This is an API REST endpoint, exposed at `/api/v1/clientes/forma-pago`
    The forma_pago is stored in separate table "formas_pago_cliente" associated to id_cliente (from external system)
    Valid payment methods: CONTRA_ENTREGA, CARTERA_COMERCIAL only
    Validation: Must verify cliente exists in external system before saving
    Uses ClienteWebClient to verify cliente exists, stores locally in formas_pago_cliente table
    Success Criteria (from spec)

    - SC-001: El sistema debe guardar la forma de pago en menos de 500ms.
    - SC-002: El cambio de forma de pago debe estar disponible inmediatamente para nuevos pedidos.

    Functional Requirements (from spec)

    - FR-001: El sistema DEBE permitir registrar la forma de pago de un cliente.
    - FR-002: El sistema DEBE validar que el cliente exista en la base de datos.
    - FR-003: El sistema DEBE guardar la forma de pago asociada al ID de BD del cliente.
    - FR-004: El sistema DEBE validar que la forma de pago sea CONTRA_ENTREGA o CARTERA_COMERCIAL.

    Acceptance Scenarios

    1. Registro exitoso de forma de pago - Given: cliente existe, When: forma_pago válida, Then: se guarda
    2. Cliente no encontrado - Given: id_nacional no existe, When: registrar forma_pago, Then: error "Cliente no encontrado"
    3. Forma de pago inválida - Given: forma_pago inválida, When: registrar, Then: error "Forma de pago inválida"