# Implementation Plan: Consultar Total del Pedido

**Date:** 23-04-2026 **Spec:** especificaciones/consultar_total_pedido.md

## Summary

Exponer endpoint para que el Modulo de Transporte consulte el total de un pedido. El total se obtiene de la tabla `liquidacion_cliente` columna `monto_liquidado` filtrando por `id_pedido`.

## Technical Context

**Language/Version:** Java 21 (LTS)
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
**Storage:** PostgreSQL con arquitectura hexagonal (puertos y adaptadores)
**Programming Style:** Programacion reactiva, Optional, streams, lambdas, excepciones de dominio, logging estructurado, Bean Validation
**Architecture:** Clean Architecture (domain, application, infrastructure) con principios SOLID
**Testing:** JUnit 5 + Mockito + TestContainers
**Target:** <500ms tiempo de respuesta, <200ms p95

---

## Project Structure

```
src/
├── main/
│   └── java/com/storeinvoice/storeinvoiceapi/
│       ├── domain/
│       │   └── exception/
│       │       └── PedidoNotFoundException.java
│       ├── application/
│       │   ├── repository/
│       │   │   └── LiquidacionRepository.java
│       │   ├── dto/
│       │   │   └── response/
│       │   │       └── TotalPedidoResponse.java
│       │   └── service/
│       │       └── pedido/
│       │           └── ConsultarTotalPedidoUseCase.java
│       └── infrastructure/
│           ├── adapter/
│           │   ├── inbound/
│           │   │   └── rest/
│           │   │       └── PedidoController.java
│           │   └── outbound/
│           │       └── persistence/
│           │           └── LiquidacionRepositoryAdapter.java
│           └── persistence/
│               ├── entity/
│               │   └── LiquidacionClienteJpaEntity.java
│               └── mapper/
│                   └── LiquidacionJpaMapper.java
```

---

## Phase 1: Domain Layer

Purpose: Definir excepciones para el caso de uso

Tasks:

- [x] T001 Verificar/crear excepcion PedidoNotFoundException en domain/exception/
- [x] T002 Verificar que existe LiquidacionCliente en domain/model/
- [x] T003 Verificar que existen enums FormaPago y EstadoLiquidacion en domain/model/

Dependencies: Phase 2 del plan general

---

## Phase 2: Application Layer

Purpose: Implementar logica de negocio, puertos y DTOs

Tasks:

- [x] T004 Crear TotalPedidoResponse en application/dto/response/
- [x] T005 Crear ConsultarTotalPedidoUseCase en application/service/pedido/
- [x] T006 Verificar que existe LiquidacionRepository en application/repository/

Dependencies: Phase 1

---

## Phase 3: Infrastructure Layer

Purpose: Implementar adaptadores y endpoints

Tasks:

- [x] T007 Verificar que existe LiquidacionJpaMapper en infrastructure/persistence/mapper/
- [x] T008 Agregar metodo findMontoLiquidadoByIdPedido en LiquidacionRepositoryAdapter si no existe
- [x] T009 Agregar endpoint GET /api/v1/pedidos/{id_pedido}/total en PedidoController
- [x] T010 Implementar manejo de error 404 cuando no se encuentra liquidacion

Dependencies: Phase 2

---

## Phase 4: Testing

Purpose: Validar la funcionalidad con tests

Tasks:

- [x] T011 Crear test unitario para ConsultarTotalPedidoUseCase
- [x] T012 Crear test de integracion para el endpoint

Dependencies: Phase 3

---

## Phase 5: Polish & Integration

Purpose: Ajustes finales

Tasks:

- [x] T013 Agregar logging para operaciones de consulta (log.error solo para errores tecnicos)
- [x] T014 Validar performance: tiempo de respuesta < 500ms

Dependencies: Phase 4

---

## Key Entities

### TotalPedidoResponse
```java
public record TotalPedidoResponse(
    Long idPedido,
    BigDecimal totalPedido
) {}
```

### Endpoint
```
GET /api/v1/pedidos/{id_pedido}/total
```

### Response
```json
{
  "id_pedido": 123,
  "total_pedido": 150000
}
```

### Error Response
- 404: "Pedido no encontrado"

---

## Success Criteria

| Criteria | Validation |
|----------|------------|
| Retorna monto_liquidado correcto | Test con liquidacion existente |
| Tiempo de respuesta < 500ms | Performance test |
| 404 cuando no existe liquidacion | Test caso de error |

---

## Dependencies

- Phase 2 del plan general (Foundational) debe estar completo
- Entidad LiquidacionCliente (ya existe)
- LiquidacionRepository adapter (ya existe)
- Enums FormaPago y EstadoLiquidacion (ya existen)

---

## Notes

- Follow principios SOLID en cada fase
- Usar Optional para valores opcionales
- Logging: log.error() solo para errores tecnicos
- Controller convierte Domain Model -> Response DTO
- UseCase retorna Domain Models (no DTOs)
- Si no existe liquidacion para el pedido, retornar 404
- No usar caracteres especiales del espanol (ñ, acentos) en codigo
