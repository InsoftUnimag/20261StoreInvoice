# Implementation Plan: Ingresar Forma de Pago Cliente

**Date:** 24-04-2026
**Spec:** `Ingresar_forma_de_pago_cliente.md`
**Status:** Completed ✓

## Summary

Permitir al Asesor Comercial registrar la forma de pago (Contra Entrega o Cartera Comercial) de un cliente. Proceso interno que primero consulta el cliente por ID Nacional para obtener su ID de base de datos y luego guarda la forma de pago asociada. También permite actualizar la forma de pago de un cliente existente.

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
├── application/
│   ├── service/
│   │   └── formapago/
│   │       ├── RegistrarFormaPagoClienteUseCase.java   # POST
│   │       └── ActualizarFormaPagoClienteUseCase.java   # PUT
│   └── dto/
│       ├── command/
│       │   └── FormaPagoCommand.java                    # Body para POST/PUT
│       └── response/
│           └── FormaPagoClienteResponse.java
└── infrastructure/
    └── adapter/
        └── inbound/
            └── rest/
                └── FormaPagoClienteController.java
```

---

## Phase 1: Domain Layer (Exceptions) ✓

**Goal:** Crear excepciones necesarias para el registro de forma de pago

Tasks:

- [x] T001 Crear excepción `FormaPagoAlreadyExistsException` en `domain/exception/FormaPagoAlreadyExistsException.java`

- [x] T002 Crear excepción `InvalidFormaPagoException` en `domain/exception/InvalidFormaPagoException.java`

---

## Phase 2: Application Layer (DTOs, Use Cases) ✓

**Goal:** Crear DTOs de comando y casos de uso para registrar y actualizar forma de pago

Tasks:

- [x] T003 Crear `FormaPagoCommand` record en `application/dto/command/FormaPagoCommand.java`
  - Campo: `formaPago` (FormaPago enum)
  - Validaciones: `@NotNull` en formaPago
  - Nota: El `idCliente` viene del path URL, no del body

- [x] T004 Reutilizar `FormaPagoClienteResponse` de `consultar_forma_pago_cliente_plan.md`
  - Campos: `idCliente` (Long), `formaPago` (String), `fechaRegistro` (LocalDateTime)

- [x] T005 Crear `RegistrarFormaPagoClienteUseCase` en `application/service/formapago/RegistrarFormaPagoClienteUseCase.java`
  - Método: `ejecutar(Long idCliente, FormaPago formaPago)` → `Mono<FormaPagoClienteResponse>`
  - Flujo:
    1. Verificar que el cliente no tenga ya forma de pago (existsByIdCliente)
    2. Crear y guardar FormaPagoCliente
    3. Retornar respuesta
  - Error: `FormaPagoAlreadyExistsException` (409) si ya existe

- [x] T006 Crear `ActualizarFormaPagoClienteUseCase` en `application/service/formapago/ActualizarFormaPagoClienteUseCase.java`
  - Método: `ejecutar(Long idCliente, FormaPago formaPago)` → `Mono<FormaPagoClienteResponse>`
  - Flujo:
    1. Buscar forma de pago existente por idCliente
    2. Actualizar formaPago y fechaRegistro
    3. Guardar y retornar respuesta
  - Error: `FormaPagoNotFoundException` (404) si no existe

---

## Phase 3: Infrastructure Layer (Controller) ✓

**Goal:** Implementar endpoints REST para registrar y actualizar forma de pago

Tasks:

- [x] T007 Crear endpoint `POST /api/v1/clientes/{id_cliente}/forma-pago` en `FormaPagoClienteController.java`
  - Body: `{ "formaPago": "CONTRA_ENTREGA" | "CARTERA_COMERCIAL" }`
  - Retornar 201 Created con `FormaPagoClienteResponse`
  - Error: 409 Conflict si el cliente ya tiene forma de pago

- [x] T008 Crear endpoint `PUT /api/v1/clientes/{id_cliente}/actualizar-forma-pago` en `FormaPagoClienteController.java`
  - Body: `{ "formaPago": "CONTRA_ENTREGA" | "CARTERA_COMERCIAL" }`
  - Retornar 200 OK con `FormaPagoClienteResponse`
  - Error: 404 Not Found si el cliente no tiene forma de pago

---

## Phase 4: Testing ✓

**Goal:** Crear tests unitarios y de integración

Tasks:

- [x] T009 Crear `RegistrarFormaPagoClienteUseCaseTest` en `test/application/service/formapago/RegistrarFormaPagoClienteUseCaseTest.java`
  - Test registro exitoso con forma de pago válida
  - Test error cuando cliente ya tiene forma de pago

- [x] T010 Crear `ActualizarFormaPagoClienteUseCaseTest` en `test/application/service/formapago/ActualizarFormaPagoClienteUseCaseTest.java`
  - Test actualización exitosa
  - Test error cuando cliente no existe
  - Test cambio de CONTRA_ENTREGA a CARTERA_COMERCIAL
  - Test cambio de CARTERA_COMERCIAL a CONTRA_ENTREGA

---

## Endpoints Implementados

| Método | Endpoint | Use Case | Descripción |
|--------|----------|----------|-------------|
| POST | `/api/v1/clientes/{id_cliente}/forma-pago` | RegistrarFormaPagoClienteUseCase | Registrar (solo si no existe) |
| PUT | `/api/v1/clientes/{id_cliente}/actualizar-forma-pago` | ActualizarFormaPagoClienteUseCase | Actualizar (solo si existe) |

---

## Body de los Endpoints

**POST/PUT - Body:**
```json
{
  "formaPago": "CONTRA_ENTREGA"
}
```
- Valores válidos: `CONTRA_ENTREGA`, `CARTERA_COMERCIAL`
- El `idCliente` se toma del path URL

**Respuesta exitosa:**
```json
{
  "idCliente": 1,
  "formaPago": "CONTRA_ENTREGA",
  "fechaRegistro": "2026-04-24T10:30:00.000000"
}
```

**Respuesta de error:**
```json
{
  "status": 409,
  "message": "El cliente ya tiene forma de pago registrada",
  "path": "/api/v1/clientes/1/forma-pago"
}
```

---

## Flujo de Negocio

1. Asesor consulta cliente por ID Nacional → obtiene `idCliente`
2. Selecciona forma de pago (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`)
3. **Primera vez:** POST a `/api/v1/clientes/{id_cliente}/forma-pago` → guarda en `forma_pago_cliente`
4. **Cambiar forma de pago:** PUT a `/api/v1/clientes/{id_cliente}/actualizar-forma-pago`
5. Al generar liquidación → se copia esta forma de pago a `liquidaciones_cliente.formaPago`

---

## Success Criteria

- [x] **SC-001:** El sistema debe guardar la forma de pago en menos de 500ms
- [x] **SC-002:** El cambio de forma de pago debe estar disponible inmediatamente para nuevos pedidos
- [x] **SC-003:** El sistema debe permitir actualizar la forma de pago existente

---

## Functional Requirements

- [x] **FR-001:** El sistema DEBE permitir registrar la forma de pago de un cliente
- [x] **FR-002:** El sistema DEBE validar que el cliente exista en la base de datos
- [x] **FR-003:** El sistema DEBE guardar la forma de pago asociada al ID de BD del cliente
- [x] **FR-004:** El sistema DEBE validar que la forma de pago sea `CONTRA_ENTREGA` o `CARTERA_COMERCIAL`
- [x] **FR-005:** El sistema DEBE permitir actualizar la forma de pago de un cliente existente
- [x] **FR-006:** El sistema DEBE retornar 409 Conflict si se intenta registrar un cliente que ya tiene forma de pago

---

## Edge Cases

1. Cliente no encontrado → `ClienteNotFoundException` + respuesta HTTP 404
2. Forma de pago inválida → Error de deserialización Jackson + respuesta HTTP 400
3. Cliente ya tiene forma de pago (POST) → `FormaPagoAlreadyExistsException` + respuesta HTTP 409
4. Cliente no tiene forma de pago (PUT) → `FormaPagoNotFoundException` + respuesta HTTP 404
5. Body vacío/null → Error de validación Spring + respuesta HTTP 400

---

## Key Entity

### Forma_Pago_Cliente

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id_cliente` | Integer | Sí | ID del cliente en la base de datos (PK) |
| `forma_pago` | String | Sí | Forma de pago: `CONTRA_ENTREGA` o `CARTERA_COMERCIAL` |
| `fecha_registro` | DateTime | Sí | Fecha y hora del último registro/actualización |

---

## Notes

- Entidad `FormaPago` (enum) ya existe con valores `CONTRA_ENTREGA`, `CARTERA_COMERCIAL`
- El flujo de negocio requiere consultar primero el cliente por ID Nacional (spec `consultar_cliente.md`) para obtener el `idCliente` de BD antes de registrar la forma de pago
- Seguir convenciones de logging: solo `log.error()` para errores técnicos
- Programación funcional usada: `Mono`, `Optional`, streams, lambda expressions
- SRP aplicado: `RegistrarFormaPagoClienteUseCase` y `ActualizarFormaPagoClienteUseCase` tienen responsabilidades separadas
- POST = "registrar por primera vez" (fallará si ya existe)
- PUT = "actualizar existente" (fallará si no existe)
- Ambos usan el mismo DTO de body (`FormaPagoCommand`) con el enum `FormaPago`