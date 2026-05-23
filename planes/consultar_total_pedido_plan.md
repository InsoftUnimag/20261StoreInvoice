# Implementation Plan: Consultar Total del Pedido

**Date:** 23-04-2026  
**Updated:** 22-05-2026  
**Spec:** especificaciones/consultar_total_pedido.md

## Summary

Exponer endpoint para que el Modulo de Transporte consulte el total de un pedido. El total se obtiene de la tabla `liquidaciones_cliente` columna `monto_liquidado` filtrando por `id_pedido`.

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
│       │   ├── model/
│       │   │   ├── LiquidacionCliente.java
│       │   │   ├── FormaPago.java
│       │   │   └── EstadoLiquidacion.java
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
│                   └── LiquidacionEntityMapper.java
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
    - Usar `LiquidacionRepository.findByIdPedido()` para obtener `LiquidacionCliente`
    - Extraer `montoLiquidado` del resultado
    - Validar que `montoLiquidado` sea > 0
    - Lanzar `PedidoNotFoundException` si no existe o el monto es cero
- [x] T006 Verificar que existe LiquidacionRepository en application/repository/
    - Metodo `findByIdPedido(Long idPedido)` debe existir y retornar `Optional<LiquidacionCliente>`

Dependencies: Phase 1

---

## Phase 3: Infrastructure Layer

Purpose: Implementar adaptadores y endpoints

Tasks:

- [x] T007 Verificar que existe LiquidacionEntityMapper en infrastructure/persistence/mapper/
- [x] T008 Verificar que `LiquidacionRepositoryAdapter.findByIdPedido()` existe y funciona correctamente
- [x] T009 Agregar endpoint GET /api/v1/pedidos/{id_pedido}/total en PedidoController
- [x] T010 Implementar manejo de error 404 cuando no se encuentra liquidacion

Dependencies: Phase 2

---

## Phase 4: Refactorizacion (Eliminacion de PedidoRepository)

Purpose: Simplificar la arquitectura eliminando codigo redundante.

**Contexto:** Originalmente existia una tabla `pedidos` y un `PedidoRepository` para almacenar datos del pedido. Sin embargo, al recibir el evento del Modulo de Inventario, los datos del pedido (incluyendo el precio total) se guardan directamente en `liquidaciones_cliente.monto_liquidado`. Por lo tanto, la tabla `pedidos` y su repositorio son redundantes.

Tasks:

- [x] T011 Eliminar `PedidoRepository.java` (interfaz de application/repository/)
- [x] T012 Eliminar `PedidoRepositoryAdapter.java` (implementacion JPA)
- [x] T013 Eliminar `PedidoJpaEntity.java` (entidad JPA)
- [x] T014 Eliminar `PedidoJpaMapper.java` (mapper MapStruct)
- [x] T015 Eliminar `Pedido.java` (modelo de dominio)
- [x] T016 Actualizar `ConsultarTotalPedidoUseCase` para usar `LiquidacionRepository` en lugar de `PedidoRepository`
- [x] T017 Actualizar `V4__create_pedidos_and_evento_recibido_table.sql` para eliminar la creacion de tabla `pedidos`
- [x] T018 Actualizar `ConsultarTotalPedidoUseCaseTest` para mockear `LiquidacionRepository` y crear mocks de `LiquidacionCliente`

Dependencies: Phase 3

---

## Phase 5: Testing

Purpose: Validar la funcionalidad con tests

Tasks:

- [x] T019 Crear test unitario para ConsultarTotalPedidoUseCase
    - Flujo exitoso: retorna montoLiquidado cuando existe liquidacion
    - Caso de error: lanza PedidoNotFoundException cuando no existe liquidacion
    - Caso de error: lanza PedidoNotFoundException cuando montoLiquidado es cero
- [x] T020 Crear test de integracion para el endpoint

Dependencies: Phase 3 / Phase 4

---

## Phase 6: Polish & Integration

Purpose: Ajustes finales

Tasks:

- [x] T021 Agregar logging para operaciones de consulta (log.error solo para errores tecnicos)
- [x] T022 Validar performance: tiempo de respuesta < 500ms
- [x] T023 Ejecutar `./gradlew build` para confirmar compilacion exitosa

Dependencies: Phase 5

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
| 404 cuando monto_liquidado es cero | Test caso de borde |

---

## Dependencies

- Phase 2 del plan general (Foundational) debe estar completo
- Entidad LiquidacionCliente (ya existe)
- LiquidacionRepository adapter (ya existe)
- Enums FormaPago y EstadoLiquidacion (ya existen)
- Flujo de recepcion de pedidos desde Inventario (para que exista la liquidacion)

---

## Notes

- Follow principios SOLID en cada fase
- Usar Optional para valores opcionales
- Logging: log.error() solo para errores tecnicos
- Controller convierte Domain Model -> Response DTO
- UseCase retorna Domain Models (no DTOs)
- Si no existe liquidacion para el pedido, retornar 404
- No usar caracteres especiales del espanol (n, acentos) en codigo
- **Cambio importante:** El precio del pedido ya no se consulta de una tabla `pedidos` independiente. Se obtiene directamente de `LiquidacionCliente.montoLiquidado`.
