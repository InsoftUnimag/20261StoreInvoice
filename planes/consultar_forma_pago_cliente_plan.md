# Implementation Plan: Consultar Forma de Pago Cliente

**Date:** 24-04-2026
**Spec:** `consultar_forma_pago_cliente.md`
**Status:** Completed âœ“

## Summary

Permitir al Sistema Financiero consultar la forma de pago de un cliente por su ID para usar en funciones internas como la generaciÃ³n de liquidaciones. Se exponen endpoints para consultar forma de pago por ID de pedido (desde liquidaciones) y verificar si un cliente tiene forma de pago registrada.

---

## Technical Context

**Language/Version:** Java 21 (LTS)
**Framework:** Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux
**Architecture:** Hexagonal (Ports & Adapters) - domain â†’ application â†’ infrastructure
**Testing:** JUnit 5 + Mockito + TestContainers
**Database:** PostgreSQL con Flyway

---

## Project Structure (Actual)

```
src/main/java/com/storeinvoice/storeinvoiceapi/
â”œâ”€â”€ domain/
â”‚   â”œâ”€â”€ model/
â”‚   â”‚   â”œâ”€â”€ FormaPagoCliente.java          # Entidad dominio
â”‚   â”‚   â””â”€â”€ FormaPago.java                  # Enum (ya existÃ­a)
â”‚   â””â”€â”€ exception/
â”‚       â”œâ”€â”€ ClienteNotFoundException.java
â”‚       â”œâ”€â”€ FormaPagoNotFoundException.java
â”‚       â”œâ”€â”€ FormaPagoAlreadyExistsException.java
â”‚       â”œâ”€â”€ InvalidFormaPagoException.java
â”‚       â””â”€â”€ PedidoNotFoundException.java
â”œâ”€â”€ application/
â”‚   â”œâ”€â”€ repository/
â”‚   â”‚   â”œâ”€â”€ FormaPagoClienteRepository.java  # Puerto outbound
â”‚   â”‚   â””â”€â”€ LiquidacionRepository.java       # Ya existÃ­a, ampliado
â”‚   â”œâ”€â”€ service/
â”‚   â”‚   â””â”€â”€ formapago/
â”‚   â”‚       â”œâ”€â”€ ConsultarFormaPagoLiquidacionUseCase.java      # Por idPedido (liquidaciones)
â”‚   â”‚       â”œâ”€â”€ ConsultarFormaPagoPorClienteUseCase.java      # Por idCliente (forma_pago_cliente)
â”‚   â”‚       â”œâ”€â”€ VerificarTieneFormaPagoLiquidacionUseCase.java # Por idPedido (liquidaciones)
â”‚   â”‚       â”œâ”€â”€ VerificarTieneFormaPagoPorClienteUseCase.java  # Por idCliente (forma_pago_cliente)
â”‚   â”‚       â”œâ”€â”€ RegistrarFormaPagoClienteUseCase.java          # POST - Registrar forma de pago
â”‚   â”‚       â””â”€â”€ ActualizarFormaPagoClienteUseCase.java          # PUT - Actualizar forma de pago
â”‚   â””â”€â”€ dto/
â”‚       â”œâ”€â”€ command/
â”‚       â”‚   â””â”€â”€ FormaPagoCommand.java         # Body para POST/PUT
â”‚       â””â”€â”€ response/
â”‚           â”œâ”€â”€ FormaPagoClienteResponse.java
â”‚           â”œâ”€â”€ TieneFormaPagoResponse.java
â”‚           â””â”€â”€ ErrorResponse.java            # Manejo de errores
â””â”€â”€ infrastructure/
    â””â”€â”€ adapter/
        â”œâ”€â”€ outbound/
        â”‚   â””â”€â”€ persistence/
        â”‚       â””â”€â”€ FormaPagoClienteRepositoryAdapter.java  # EntityManager + JPQL
        â””â”€â”€ inbound/
            â””â”€â”€ rest/
                â”œâ”€â”€ FormaPagoClienteController.java
                â””â”€â”€ GlobalExceptionHandler.java
```

---

## Phase 1: Domain Layer (Entities & Exceptions) âœ“

**Goal:** Crear la entidad de dominio `FormaPagoCliente` y excepciones necesarias

Tasks:

- [x] T001 Crear entidad `FormaPagoCliente` en `domain/model/FormaPagoCliente.java`
  - Campos: `idCliente` (Long), `formaPago` (FormaPago enum), `fechaRegistro` (LocalDateTime)
  - Validaciones: `@NotNull` en idCliente y formaPago

- [x] T002 Crear excepciÃ³n `ClienteNotFoundException` en `domain/exception/ClienteNotFoundException.java`

- [x] T003 Crear excepciÃ³n `FormaPagoNotFoundException` en `domain/exception/FormaPagoNotFoundException.java`

- [x] T004 Crear excepciÃ³n `FormaPagoAlreadyExistsException` en `domain/exception/FormaPagoAlreadyExistsException.java`

- [x] T005 Crear excepciÃ³n `InvalidFormaPagoException` en `domain/exception/InvalidFormaPagoException.java`

---

## Phase 2: Application Layer (Repository Interface, DTOs, Use Cases) âœ“

**Goal:** Crear puertos outbound, DTOs y casos de uso (SRP aplicado - un use case por responsabilidad)

Tasks:

- [x] T006 Crear interfaz `FormaPagoClienteRepository` en `application/repository/FormaPagoClienteRepository.java`
  - MÃ©todos: `findByIdCliente(Long)`, `existsByIdCliente(Long)`, `save(FormaPagoCliente)`, `update(FormaPagoCliente)`

- [x] T007 Crear `FormaPagoClienteResponse` record en `application/dto/response/FormaPagoClienteResponse.java`
  - Campos: `idCliente` (Long), `formaPago` (String), `fechaRegistro` (LocalDateTime)

- [x] T008 Crear `TieneFormaPagoResponse` record en `application/dto/response/TieneFormaPagoResponse.java`
  - Campos: `idCliente` (Long), `tieneFormaPago` (boolean)

- [x] T009 Crear `FormaPagoCommand` record en `application/dto/command/FormaPagoCommand.java`
  - Campos: `formaPago` (FormaPago enum)
  - Validaciones: `@NotNull` en formaPago
  - Nota: El `idCliente` viene del path URL, no del body

- [x] T010 Crear `ConsultarFormaPagoLiquidacionUseCase` en `application/service/formapago/ConsultarFormaPagoLiquidacionUseCase.java`
  - Fuente: `liquidaciones_cliente` (forma de pago histÃ³rica)
  - MÃ©todo: `ejecutar(Long idPedido)` â†’ `Mono<FormaPagoClienteResponse>`
  - Uso: MÃ³dulo transporte consulta forma de pago del pedido

- [x] T011 Crear `ConsultarFormaPagoPorClienteUseCase` en `application/service/formapago/ConsultarFormaPagoPorClienteUseCase.java`
  - Fuente: `forma_pago_cliente` (forma de pago actual)
  - MÃ©todo: `ejecutar(Long idCliente)` â†’ `Mono<FormaPagoClienteResponse>`
  - Uso: Consultar forma de pago actual del cliente

- [x] T012 Crear `VerificarTieneFormaPagoLiquidacionUseCase` en `application/service/formapago/VerificarTieneFormaPagoLiquidacionUseCase.java`
  - Fuente: `liquidaciones_cliente`
  - MÃ©todo: `ejecutar(Long idPedido)` â†’ `Mono<TieneFormaPagoResponse>`
  - Uso: Verifica si existe liquidaciÃ³n para el pedido

- [x] T013 Crear `VerificarTieneFormaPagoPorClienteUseCase` en `application/service/formapago/VerificarTieneFormaPagoPorClienteUseCase.java`
  - Fuente: `forma_pago_cliente`
  - MÃ©todo: `ejecutar(Long idCliente)` â†’ `Mono<TieneFormaPagoResponse>`
  - Uso: Verifica si el cliente tiene forma de pago registrada

- [x] T014 Crear `RegistrarFormaPagoClienteUseCase` en `application/service/formapago/RegistrarFormaPagoClienteUseCase.java`
  - MÃ©todo: `ejecutar(Long idCliente, FormaPago formaPago)` â†’ `Mono<FormaPagoClienteResponse>`
  - Flujo: Verifica no exista â†’ persiste â†’ retorna respuesta
  - Error: `FormaPagoAlreadyExistsException` (409) si ya existe

- [x] T015 Crear `ActualizarFormaPagoClienteUseCase` en `application/service/formapago/ActualizarFormaPagoClienteUseCase.java`
  - MÃ©todo: `ejecutar(Long idCliente, FormaPago formaPago)` â†’ `Mono<FormaPagoClienteResponse>`
  - Flujo: Busca existente â†’ actualiza â†’ retorna respuesta
  - Error: `FormaPagoNotFoundException` (404) si no existe

---

## Phase 3: Infrastructure Layer (Persistence Adapter, Controller) âœ“

**Goal:** Implementar adaptadores concretos y controlador REST

Tasks:

- [x] T016 Crear entidad JPA `FormaPagoClienteJpaEntity` en `infrastructure/persistence/entity/FormaPagoClienteJpaEntity.java`
  - Mapeo: `@Enumerated(EnumType.STRING)` para formaPago, `@Table(name = "forma_pago_cliente")`

- [x] T017 Crear mapper `FormaPagoClienteJpaMapper` en `infrastructure/persistence/mapper/FormaPagoClienteJpaMapper.java`
  - MÃ©todos: `toDomain(FormaPagoClienteJpaEntity)`, `toEntity(FormaPagoCliente)`

- [x] T018 Crear adapter `FormaPagoClienteRepositoryAdapter` en `infrastructure/adapter/outbound/persistence/FormaPagoClienteRepositoryAdapter.java`
  - Usa `@PersistenceContext EntityManager` + JPQL
  - MÃ©todos con `@Transactional` y `@Transactional(readOnly = true)`
  - No usa JpaRepository intermedio

- [x] T019 Crear controlador `FormaPagoClienteController` en `infrastructure/adapter/inbound/rest/FormaPagoClienteController.java`
  - `GET /api/v1/pedidos/{id_pedido}/forma-pago` â†’ `ConsultarFormaPagoLiquidacionUseCase`
  - `GET /api/v1/pedidos/{id_pedido}/tiene-forma-pago` â†’ `VerificarTieneFormaPagoLiquidacionUseCase`
  - `GET /api/v1/clientes/{id_cliente}/forma-pago` â†’ `ConsultarFormaPagoPorClienteUseCase`
  - `GET /api/v1/clientes/{id_cliente}/tiene-forma-pago` â†’ `VerificarTieneFormaPagoPorClienteUseCase`
  - `POST /api/v1/clientes/{id_cliente}/forma-pago` â†’ `RegistrarFormaPagoClienteUseCase`
  - `PUT /api/v1/clientes/{id_cliente}/actualizar-forma-pago` â†’ `ActualizarFormaPagoClienteUseCase`

- [x] T020 Crear `GlobalExceptionHandler` en `infrastructure/adapter/inbound/rest/GlobalExceptionHandler.java`
  - Retorna `ErrorResponse` con formato: `{ status, message, path }`

- [x] T021 Crear migraciÃ³n Flyway `V2__create_forma_pago_cliente_table.sql` en `resources/db/migration/`
  - Tabla: `forma_pago_cliente` con columnas: `id_cliente` (PK), `forma_pago` (VARCHAR), `fecha_registro` (TIMESTAMP)

- [x] T022 Actualizar `LiquidacionRepository` agregando mÃ©todo `findByIdPedido(Long idPedido)`
  - Implementar en `LiquidacionRepositoryAdapter`

---

## Phase 4: Testing âœ“

**Goal:** Crear tests unitarios y de integraciÃ³n

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
| `ConsultarFormaPagoLiquidacionUseCase` | `liquidaciones_cliente` | Consulta forma de pago del pedido (histÃ³rico) |
| `ConsultarFormaPagoPorClienteUseCase` | `forma_pago_cliente` | Consulta forma de pago actual del cliente |
| `VerificarTieneFormaPagoLiquidacionUseCase` | `liquidaciones_cliente` | Verifica si existe liquidaciÃ³n |
| `VerificarTieneFormaPagoPorClienteUseCase` | `forma_pago_cliente` | Verifica si cliente tiene forma de pago |
| `RegistrarFormaPagoClienteUseCase` | `forma_pago_cliente` | Registra forma de pago (solo si no existe) |
| `ActualizarFormaPagoClienteUseCase` | `forma_pago_cliente` | Actualiza forma de pago existente |

---

## Endpoints Implementados

| MÃ©todo | Endpoint | Use Case | Respuesta Exitosa |
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

**CÃ³digos de error por HTTP Status:**
- 400 Bad Request â†’ ValidaciÃ³n fallida, forma de pago invÃ¡lida
- 404 Not Found â†’ Cliente, pedido o forma de pago no encontrada
- 409 Conflict â†’ Cliente ya tiene forma de pago registrada
- 500 Internal Server Error â†’ Errores inesperados de Spring Boot

---

## Flujo de Negocio

1. **Asesor comercial** registra forma de pago del cliente â†’ `POST /clientes/{id}/forma-pago`
2. **Asesor comercial** actualiza forma de pago â†’ `PUT /clientes/{id}/actualizar-forma-pago`
3. **Al generar liquidaciÃ³n** â†’ Se copia forma de pago actual a `liquidaciones_cliente.formaPago`
4. **MÃ³dulo transporte** consulta forma de pago del pedido â†’ `liquidaciones_cliente`

---

## Success Criteria

- [x] **SC-001:** El sistema debe retornar la forma de pago en menos de 500ms
- [x] **SC-002:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultÃ¡neas

---

## Functional Requirements

- [x] **FR-001:** El sistema DEBE exponer un endpoint GET `/api/v1/pedidos/{id_pedido}/forma-pago`
- [x] **FR-002:** El sistema DEBE exponer una funciÃ³n interna que busque forma de pago por `id_cliente`
- [x] **FR-003:** El sistema DEBE retornar la forma de pago como string (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`)
- [x] **FR-004:** El sistema DEBE retornar error cuando el pedido o cliente no exista
- [x] **FR-005:** El sistema DEBE retornar error cuando el cliente no tenga forma de pago asignada
- [x] **FR-006:** El sistema DEBE exponer un endpoint GET `/api/v1/pedidos/{id_pedido}/tiene-forma-pago` que retorne un booleano
- [x] **FR-007:** El sistema DEBE permitir actualizar la forma de pago de un cliente existente
- [x] **FR-008:** El sistema DEBE retornar 409 Conflict si se intenta registrar forma de pago a un cliente que ya tiene

---

## Edge Cases

1. Pedido no encontrado â†’ `PedidoNotFoundException` â†’ HTTP 404
2. Cliente no encontrado â†’ `ClienteNotFoundException` â†’ HTTP 404
3. Cliente sin forma de pago â†’ `FormaPagoNotFoundException` â†’ HTTP 404
4. Cliente ya tiene forma de pago (POST) â†’ `FormaPagoAlreadyExistsException` â†’ HTTP 409
5. Forma de pago invÃ¡lida (enum) â†’ Error de deserializaciÃ³n Jackson â†’ HTTP 400
6. ID de cliente invÃ¡lido â†’ `ClienteNotFoundException`
7. Fallo en base de datos â†’ `log.error()` + respuesta genÃ©rica de Spring

---

## Notes

- Entidad `FormaPago` (enum) ya existe con valores `CONTRA_ENTREGA`, `CARTERA_COMERCIAL`
- El DTO `FormaPagoCommand` solo contiene `formaPago` (el `idCliente` viene del path URL)
- El `ActualizarFormaPagoClienteUseCase` usa `entityManager.merge()` para actualizar
- El `RegistrarFormaPagoClienteUseCase` usa `entityManager.persist()` para crear
- Seguir convenciones de logging: solo `log.error()` para errores tÃ©cnicos (no para casos de negocio)
- ProgramaciÃ³n funcional: uso de `Mono`, `Optional`, streams (sin `if` imperativos)
- SRP aplicado: un use case por responsabilidad
- Arquitectura: Puerto outbound (`FormaPagoClienteRepository`) implementado con EntityManager + JPQL (sin JpaRepository intermedio)
