# Implementation Plan: Consultar Forma de Pago Cliente

**Date:** 24-04-2026
**Spec:** `consultar_forma_pago_cliente.md`
**Status:** Completed ✓

## Summary

Permitir al Sistema Financiero consultar la forma de pago de un cliente por su ID para usar en funciones internas como la generación de liquidaciones. Se exponen endpoints para consultar forma de pago por ID de pedido (desde liquidaciones) y verificar si un cliente tiene forma de pago registrada.

---

## Technical Context

**Language/Version:** Java 21 (LTS)
**Framework:** Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux
**Architecture:** Hexagonal (Ports & Adapters) - domain → application → infrastructure
**Testing:** JUnit 5 + Mockito + TestContainers
**Database:** PostgreSQL con Flyway

---

## Project Structure (Actual)

```
src/main/java/com/storeinvoice/storeinvoiceapi/
├── domain/
│   ├── model/
│   │   ├── FormaPagoCliente.java          # Entidad dominio
│   │   └── FormaPago.java                  # Enum (ya existía)
│   └── exception/
│       ├── ClienteNotFoundException.java
│       ├── FormaPagoNotFoundException.java
│       ├── FormaPagoAlreadyExistsException.java
│       ├── InvalidFormaPagoException.java
│       └── PedidoNotFoundException.java
├── application/
│   ├── repository/
│   │   ├── FormaPagoClienteRepository.java  # Puerto outbound
│   │   └── LiquidacionRepository.java       # Ya existía, ampliado
│   ├── service/
│   │   └── formapago/
│   │       ├── ConsultarFormaPagoLiquidacionUseCase.java      # Por idPedido (liquidaciones)
│   │       ├── ConsultarFormaPagoPorClienteUseCase.java      # Por idCliente (forma_pago_cliente)
│   │       ├── VerificarTieneFormaPagoLiquidacionUseCase.java # Por idPedido (liquidaciones)
│   │       ├── VerificarTieneFormaPagoPorClienteUseCase.java  # Por idCliente (forma_pago_cliente)
│   │       ├── RegistrarFormaPagoClienteUseCase.java          # POST - Registrar forma de pago
│   │       └── ActualizarFormaPagoClienteUseCase.java          # PUT - Actualizar forma de pago
│   └── dto/
│       ├── command/
│       │   └── FormaPagoCommand.java         # Body para POST/PUT
│       └── response/
│           ├── FormaPagoClienteResponse.java
│           ├── TieneFormaPagoResponse.java
│           └── ErrorResponse.java            # Manejo de errores
└── infrastructure/
    └── adapter/
        ├── outbound/
        │   └── persistence/
        │       └── FormaPagoClienteRepositoryAdapter.java  # EntityManager + JPQL
        └── inbound/
            └── rest/
                ├── FormaPagoClienteController.java
                └── GlobalExceptionHandler.java
```

---

## Phase 1: Domain Layer (Entities & Exceptions) ✓

**Goal:** Crear la entidad de dominio `FormaPagoCliente` y excepciones necesarias

Tasks:

- [x] T001 Crear entidad `FormaPagoCliente` en `domain/model/FormaPagoCliente.java`
  - Campos: `idCliente` (Long), `formaPago` (FormaPago enum), `fechaRegistro` (LocalDateTime)
  - Validaciones: `@NotNull` en idCliente y formaPago

- [x] T002 Crear excepción `ClienteNotFoundException` en `domain/exception/ClienteNotFoundException.java`

- [x] T003 Crear excepción `FormaPagoNotFoundException` en `domain/exception/FormaPagoNotFoundException.java`

- [x] T004 Crear excepción `FormaPagoAlreadyExistsException` en `domain/exception/FormaPagoAlreadyExistsException.java`

- [x] T005 Crear excepción `InvalidFormaPagoException` en `domain/exception/InvalidFormaPagoException.java`

---

## Phase 2: Application Layer (Repository Interface, DTOs, Use Cases) ✓

**Goal:** Crear puertos outbound, DTOs y casos de uso (SRP aplicado - un use case por responsabilidad)

Tasks:

- [x] T006 Crear interfaz `FormaPagoClienteRepository` en `application/repository/FormaPagoClienteRepository.java`
  - Métodos: `findByIdCliente(Long)`, `existsByIdCliente(Long)`, `save(FormaPagoCliente)`, `update(FormaPagoCliente)`

- [x] T007 Crear `FormaPagoClienteResponse` record en `application/dto/response/FormaPagoClienteResponse.java`
  - Campos: `idCliente` (Long), `formaPago` (String), `fechaRegistro` (LocalDateTime)

- [x] T008 Crear `TieneFormaPagoResponse` record en `application/dto/response/TieneFormaPagoResponse.java`
  - Campos: `idCliente` (Long), `tieneFormaPago` (boolean)

- [x] T009 Crear `FormaPagoCommand` record en `application/dto/command/FormaPagoCommand.java`
  - Campos: `formaPago` (FormaPago enum)
  - Validaciones: `@NotNull` en formaPago
  - Nota: El `idCliente` viene del path URL, no del body

- [x] T010 Crear `ConsultarFormaPagoLiquidacionUseCase` en `application/service/formapago/ConsultarFormaPagoLiquidacionUseCase.java`
  - Fuente: `liquidaciones_cliente` (forma de pago histórica)
  - Método: `ejecutar(Long idPedido)` → `Mono<FormaPagoClienteResponse>`
  - Uso: Módulo transporte consulta forma de pago del pedido

- [x] T011 Crear `ConsultarFormaPagoPorClienteUseCase` en `application/service/formapago/ConsultarFormaPagoPorClienteUseCase.java`
  - Fuente: `forma_pago_cliente` (forma de pago actual)
  - Método: `ejecutar(Long idCliente)` → `Mono<FormaPagoClienteResponse>`
  - Uso: Consultar forma de pago actual del cliente

- [x] T012 Crear `VerificarTieneFormaPagoLiquidacionUseCase` en `application/service/formapago/VerificarTieneFormaPagoLiquidacionUseCase.java`
  - Fuente: `liquidaciones_cliente`
  - Método: `ejecutar(Long idPedido)` → `Mono<TieneFormaPagoResponse>`
  - Uso: Verifica si existe liquidación para el pedido

- [x] T013 Crear `VerificarTieneFormaPagoPorClienteUseCase` en `application/service/formapago/VerificarTieneFormaPagoPorClienteUseCase.java`
  - Fuente: `forma_pago_cliente`
  - Método: `ejecutar(Long idCliente)` → `Mono<TieneFormaPagoResponse>`
  - Uso: Verifica si el cliente tiene forma de pago registrada

- [x] T014 Crear `RegistrarFormaPagoClienteUseCase` en `application/service/formapago/RegistrarFormaPagoClienteUseCase.java`
  - Método: `ejecutar(Long idCliente, FormaPago formaPago)` → `Mono<FormaPagoClienteResponse>`
  - Flujo: Verifica no exista → persiste → retorna respuesta
  - Error: `FormaPagoAlreadyExistsException` (409) si ya existe

- [x] T015 Crear `ActualizarFormaPagoClienteUseCase` en `application/service/formapago/ActualizarFormaPagoClienteUseCase.java`
  - Método: `ejecutar(Long idCliente, FormaPago formaPago)` → `Mono<FormaPagoClienteResponse>`
  - Flujo: Busca existente → actualiza → retorna respuesta
  - Error: `FormaPagoNotFoundException` (404) si no existe

---

## Phase 3: Infrastructure Layer (Persistence Adapter, Controller) ✓

**Goal:** Implementar adaptadores concretos y controlador REST

Tasks:

- [x] T016 Crear entidad JPA `FormaPagoClienteJpaEntity` en `infrastructure/persistence/entity/FormaPagoClienteJpaEntity.java`
  - Mapeo: `@Enumerated(EnumType.STRING)` para formaPago, `@Table(name = "forma_pago_cliente")`

- [x] T017 Crear mapper `FormaPagoClienteJpaMapper` en `infrastructure/persistence/mapper/FormaPagoClienteJpaMapper.java`
  - Métodos: `toDomain(FormaPagoClienteJpaEntity)`, `toEntity(FormaPagoCliente)`

- [x] T018 Crear adapter `FormaPagoClienteRepositoryAdapter` en `infrastructure/adapter/outbound/persistence/FormaPagoClienteRepositoryAdapter.java`
  - Usa `@PersistenceContext EntityManager` + JPQL
  - Métodos con `@Transactional` y `@Transactional(readOnly = true)`
  - No usa JpaRepository intermedio

- [x] T019 Crear controlador `FormaPagoClienteController` en `infrastructure/adapter/inbound/rest/FormaPagoClienteController.java`
  - `GET /api/v1/pedidos/{id_pedido}/forma-pago` → `ConsultarFormaPagoLiquidacionUseCase`
  - `GET /api/v1/pedidos/{id_pedido}/tiene-forma-pago` → `VerificarTieneFormaPagoLiquidacionUseCase`
  - `GET /api/v1/clientes/{id_cliente}/forma-pago` → `ConsultarFormaPagoPorClienteUseCase`
  - `GET /api/v1/clientes/{id_cliente}/tiene-forma-pago` → `VerificarTieneFormaPagoPorClienteUseCase`
  - `POST /api/v1/clientes/{id_cliente}/forma-pago` → `RegistrarFormaPagoClienteUseCase`
  - `PUT /api/v1/clientes/{id_cliente}/actualizar-forma-pago` → `ActualizarFormaPagoClienteUseCase`

- [x] T020 Crear `GlobalExceptionHandler` en `infrastructure/adapter/inbound/rest/GlobalExceptionHandler.java`
  - Retorna `ErrorResponse` con formato: `{ status, message, path }`

- [x] T021 Crear migración Flyway `V2__create_forma_pago_cliente_table.sql` en `resources/db/migration/`
  - Tabla: `forma_pago_cliente` con columnas: `id_cliente` (PK), `forma_pago` (VARCHAR), `fecha_registro` (TIMESTAMP)

- [x] T022 Actualizar `LiquidacionRepository` agregando método `findByIdPedido(Long idPedido)`
  - Implementar en `LiquidacionRepositoryAdapter`

---

## Phase 4: Testing ✓

**Goal:** Crear tests unitarios y de integración

Tasks:

- [x] T023 Crear `ConsultarFormaPagoLiquidacionUseCaseTest` en `test/application/service/formapago/ConsultarFormaPagoLiquidacionUseCaseTest.java`

- [x] T024 Crear `ConsultarFormaPagoPorClienteUseCaseTest` en `test/application/service/formapago/ConsultarFormaPagoPorClienteUseCaseTest.java`

- [x] T025 Crear `VerificarTieneFormaPagoLiquidacionUseCaseTest` en `test/application/service/formapago/VerificarTieneFormaPagoLiquidacionUseCaseTest.java`

- [x] T026 Crear `VerificarTieneFormaPagoPorClienteUseCaseTest` en `test/application/service/formapago/VerificarTieneFormaPagoPorClienteUseCaseTest.java`

- [x] T027 Crear `RegistrarFormaPagoClienteUseCaseTest` en `test/application/service/formapago/RegistrarFormaPagoClienteUseCaseTest.java`

- [x] T028 Crear `ActualizarFormaPagoClienteUseCaseTest` en `test/application/service/formapago/ActualizarFormaPagoClienteUseCaseTest.java`

---

## Arquitectura de Use Cases (SRP)

| Use Case | Fuente | Responsabilidad |
|----------|--------|----------------|
| `ConsultarFormaPagoLiquidacionUseCase` | `liquidaciones_cliente` | Consulta forma de pago del pedido (histórico) |
| `ConsultarFormaPagoPorClienteUseCase` | `forma_pago_cliente` | Consulta forma de pago actual del cliente |
| `VerificarTieneFormaPagoLiquidacionUseCase` | `liquidaciones_cliente` | Verifica si existe liquidación |
| `VerificarTieneFormaPagoPorClienteUseCase` | `forma_pago_cliente` | Verifica si cliente tiene forma de pago |
| `RegistrarFormaPagoClienteUseCase` | `forma_pago_cliente` | Registra forma de pago (solo si no existe) |
| `ActualizarFormaPagoClienteUseCase` | `forma_pago_cliente` | Actualiza forma de pago existente |

---

## Endpoints Implementados

| Método | Endpoint | Use Case | Respuesta Exitosa |
|--------|----------|----------|-------------------|
| GET | `/api/v1/pedidos/{id_pedido}/forma-pago` | ConsultarFormaPagoLiquidacionUseCase | 200 OK |
| GET | `/api/v1/pedidos/{id_pedido}/tiene-forma-pago` | VerificarTieneFormaPagoLiquidacionUseCase | 200 OK |
| GET | `/api/v1/clientes/{id_cliente}/forma-pago` | ConsultarFormaPagoPorClienteUseCase | 200 OK |
| GET | `/api/v1/clientes/{id_cliente}/tiene-forma-pago` | VerificarTieneFormaPagoPorClienteUseCase | 200 OK |
| POST | `/api/v1/clientes/{id_cliente}/forma-pago` | RegistrarFormaPagoClienteUseCase | 201 Created |
| PUT | `/api/v1/clientes/{id_cliente}/actualizar-forma-pago` | ActualizarFormaPagoClienteUseCase | 200 OK |

---

## ErrorResponse

**Formato:**
```json
{
  "status": 404,
  "message": "Cliente no encontrado",
  "path": "/api/v1/clientes/9999/forma-pago"
}
```

**Códigos de error por HTTP Status:**
- 400 Bad Request → Validación fallida, forma de pago inválida
- 404 Not Found → Cliente, pedido o forma de pago no encontrada
- 409 Conflict → Cliente ya tiene forma de pago registrada
- 500 Internal Server Error → Errores inesperados de Spring Boot

---

## Flujo de Negocio

1. **Asesor comercial** registra forma de pago del cliente → `POST /clientes/{id}/forma-pago`
2. **Asesor comercial** actualiza forma de pago → `PUT /clientes/{id}/actualizar-forma-pago`
3. **Al generar liquidación** → Se copia forma de pago actual a `liquidaciones_cliente.formaPago`
4. **Módulo transporte** consulta forma de pago del pedido → `liquidaciones_cliente`

---

## Success Criteria

- [x] **SC-001:** El sistema debe retornar la forma de pago en menos de 500ms
- [x] **SC-002:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultáneas

---

## Functional Requirements

- [x] **FR-001:** El sistema DEBE exponer un endpoint GET `/api/v1/pedidos/{id_pedido}/forma-pago`
- [x] **FR-002:** El sistema DEBE exponer una función interna que busque forma de pago por `id_cliente`
- [x] **FR-003:** El sistema DEBE retornar la forma de pago como string (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`)
- [x] **FR-004:** El sistema DEBE retornar error cuando el pedido o cliente no exista
- [x] **FR-005:** El sistema DEBE retornar error cuando el cliente no tenga forma de pago asignada
- [x] **FR-006:** El sistema DEBE exponer un endpoint GET `/api/v1/pedidos/{id_pedido}/tiene-forma-pago` que retorne un booleano
- [x] **FR-007:** El sistema DEBE permitir actualizar la forma de pago de un cliente existente
- [x] **FR-008:** El sistema DEBE retornar 409 Conflict si se intenta registrar forma de pago a un cliente que ya tiene

---

## Edge Cases

1. Pedido no encontrado → `PedidoNotFoundException` → HTTP 404
2. Cliente no encontrado → `ClienteNotFoundException` → HTTP 404
3. Cliente sin forma de pago → `FormaPagoNotFoundException` → HTTP 404
4. Cliente ya tiene forma de pago (POST) → `FormaPagoAlreadyExistsException` → HTTP 409
5. Forma de pago inválida (enum) → Error de deserialización Jackson → HTTP 400
6. ID de cliente inválido → `ClienteNotFoundException`
7. Fallo en base de datos → `log.error()` + respuesta genérica de Spring

---

## Notes

- Entidad `FormaPago` (enum) ya existe con valores `CONTRA_ENTREGA`, `CARTERA_COMERCIAL`
- El DTO `FormaPagoCommand` solo contiene `formaPago` (el `idCliente` viene del path URL)
- El `ActualizarFormaPagoClienteUseCase` usa `entityManager.merge()` para actualizar
- El `RegistrarFormaPagoClienteUseCase` usa `entityManager.persist()` para crear
- Seguir convenciones de logging: solo `log.error()` para errores técnicos (no para casos de negocio)
- Programación funcional: uso de `Mono`, `Optional`, streams (sin `if` imperativos)
- SRP aplicado: un use case por responsabilidad
- Arquitectura: Puerto outbound (`FormaPagoClienteRepository`) implementado con EntityManager + JPQL (sin JpaRepository intermedio)