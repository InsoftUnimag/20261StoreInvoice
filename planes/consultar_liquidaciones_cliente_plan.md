# Implementation Plan: Consultar Liquidaciones del Cliente (WebApp)

**Date:** 22-04-2026
**Spec:** especificaciones/consultar_liquidaciones_cliente.md

## Summary

Permitir al cliente consultar sus liquidaciones desde una interfaz web mediante identificacion por ID de cliente, con paginacion de 20 registros por pagina y descarga de PDF.

## Technical Context

**Language/Version:** Java 21 (LTS)
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
**Storage:** PostgreSQL con arquitectura hexagonal (puertos y adaptadores)
**Programming Style:** Programacion reactiva, Optional, streams, lambdas, excepciones de dominio, logging estructurado, Bean Validation
**Architecture:** Clean Architecture (domain, application, infrastructure) con principios SOLID
**Testing:** JUnit 5 + Mockito + TestContainers
**Target:** <3s tiempo de respuesta, <200ms p95

---

## Project Structure

```
src/
├── main/
│   ├── java/com/storeinvoice/storeinvoiceapi/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── LiquidacionCliente.java
│   │   │   │   ├── FormaPago.java
│   │   │   │   └── EstadoLiquidacion.java
│   │   │   └── exception/
│   │   │       └── LiquidacionNotFoundException.java
│   │   ├── application/
│   │   │   ├── service/
│   │   │   │   └── liquidacion/
│   │   │   │       └── ConsultarLiquidacionesClienteUseCase.java
│   │   │   ├── repository/
│   │   │   │   └── LiquidacionRepository.java
│   │   │   └── dto/
│   │   │       ├── query/
│   │   │       │   └── ConsultarLiquidacionesQuery.java
│   │   │       └── response/
│   │   │           └── LiquidacionClienteResponse.java
│   │   └── infrastructure/
│   │       ├── adapter/
│   │       │   ├── inbound/
│   │       │   │   └── rest/
│   │       │   │       └── LiquidacionController.java
│   │       │   └── outbound/
│   │       │       └── persistence/
│   │       │           └── LiquidacionRepositoryAdapter.java
│   │       ├── persistence/
│   │       │   ├── entity/
│   │       │   │   └── LiquidacionClienteJpaEntity.java
│   │       │   └── mapper/
│   │       │       └── LiquidacionEntityMapper.java
│   │       └── exception/
│   │           └── GlobalExceptionHandler.java
│   └── resources/
│       └── db/migration/
│           └── V1__create_liquidacion_cliente_table.sql
└── test/
    └── java/com/storeinvoice/storeinvoiceapi/
        ├── application/service/liquidacion/
        │   └── ConsultarLiquidacionesClienteUseCaseTest.java
        └── infrastructure/adapter/inbound/rest/
            └── LiquidacionControllerTest.java
```

---

## Phase 1: Domain Layer

Purpose: Definir el modelo de dominio y excepciones para liquidaciones de cliente

Tasks:

- [x] T001 Crear entidad LiquidacionCliente en domain/model/LiquidacionCliente.java con validaciones (Bean Validation)
- [x] T001a Crear enum FormaPago en domain/model/FormaPago.java (CONTRA_ENTREGA, CARTERA_COMERCIAL)
- [x] T001b Crear enum EstadoLiquidacion en domain/model/EstadoLiquidacion.java (PENDIENTE, PAGADA, CANCELADA)
- [x] T002 Crear excepcion LiquidacionNotFoundException en domain/exception/

Dependencies: Infraestructura base (Phase 2 del plan general)

---

## Phase 2: Application Layer

Purpose: Implementar logica de negocio, puertos y DTOs para consulta de liquidaciones

Tasks:

- [x] T004 Crear interface LiquidacionRepository en application/repository/LiquidacionRepository.java
- [x] T005 Crear query ConsultarLiquidacionesQuery en application/dto/query/
- [x] T006 Crear response LiquidacionClienteResponse en application/dto/response/
- [x] T007 Implementar ConsultarLiquidacionesClienteUseCase en application/service/liquidacion/
- [x] T008 Implementar paginacion en LiquidacionRepository

Dependencies: Phase 1

---

## Phase 3: Infrastructure Layer

Purpose: Implementar adaptadores, controllers y configuracion de persistencia

Tasks:

- [x] T009 Crear LiquidacionClienteJpaEntity en infrastructure/persistence/entity/ (con enums)
- [x] T010 Crear LiquidacionEntityMapper en infrastructure/persistence/mapper/
- [x] T011 Implementar LiquidacionRepositoryAdapter en infrastructure/adapter/outbound/persistence/
- [x] T012 Crear endpoint GET /api/v1/clientes/{idCliente}/liquidaciones en LiquidacionController
- [x] T013 Implementar conversion Domain Model → LiquidacionClienteResponse usando MapStruct
- [x] T014 Crear script Flyway V1__create_liquidacion_cliente_table.sql
- [x] T015 Configurar paginacion (20 registros por defecto) y ordenamiento descendente por fecha

Dependencies: Phase 2

---

## Phase 4: Testing

Purpose: Validar la funcionalidad con tests unitarios y de integracion

Tasks:

- [x] T016 Crear test unitario ConsultarLiquidacionesClienteUseCaseTest
- [x] T017 Crear test de integracion LiquidacionControllerTest con Mockito
- [x] T018 Validar escenario: cliente sin liquidaciones retorna lista vacia
- [x] T019 Validar escenario: paginacion de 20 registros por pagina
- [x] T020 Validar escenario: ordenamiento cronologico descendente

Dependencies: Phase 3

---

## Phase 5: Polish & Integration

Purpose: Ajustes finales e integracion con sistema existente

Tasks:

- [ ] T021 Agregar logging para operaciones de consulta (log.error solo para errores tecnicos)
- [ ] T022 Actualizar GlobalExceptionHandler para manejar LiquidacionNotFoundException
- [ ] T023 Validar performance: tiempo de respuesta < 3 segundos
- [ ] T024 Documentar endpoint en Swagger/OpenAPI

Dependencies: Phase 4

---

## Key Entities (Enums)

### FormaPago
```java
public enum FormaPago {
    CONTRA_ENTREGA,
    CARTERA_COMERCIAL
}
```

### EstadoLiquidacion
```java
public enum EstadoLiquidacion {
    PENDIENTE,
    PAGADA,
    CANCELADA
}
```

### LiquidacionCliente
```java
public class LiquidacionCliente {
    private Long idLiquidacion;
    private Long idPedido;
    private Long idCliente;
    private FormaPago formaPago;
    private EstadoLiquidacion estadoLiquidacion;
    private LocalDateTime fechaLiquidacion;
    private String uriPdf;
    private BigDecimal montoLiquidado;
}
```

---

## Functional Requirements Mapped

| FR | Task | Description |
|----|------|-------------|
| FR-001 | T012 | Identificar cliente por ID de cliente en endpoint |
| FR-002 | T015 | Ordenar liquidaciones cronologicamente descendente |
| FR-003 | T008, T015 | Paginar resultados (20 por pagina por defecto) |
| FR-004 | T006 | Mostrar detalles: idLiquidacion, idPedido, idCliente, formaPago, estadoLiquidacion, fechaLiquidacion, uriPdf, montoLiquidado |
| FR-005 | T015 | Exponer uriPdf para acceso a PDF |

---

## Success Criteria

| SC | Criteria | Validation |
|----|----------|------------|
| SC-001 | Listar 100% de liquidaciones del cliente | Test con multiples liquidaciones |
| SC-002 | Tiempo de respuesta < 3 segundos | Performance test |
| SC-003 | Descarga de PDF en < 3 clics | Validar uriPdf accesible |

---

## User Story Acceptance Criteria

1. **Consulta exitosa**: Dado cliente con liquidaciones -> cuando consulta -> mostrar lista paginada con datos y enlace PDF
2. **Sin liquidaciones**: Dado cliente sin liquidaciones -> cuando consulta -> mostrar mensaje "no tiene liquidaciones"
3. **Navegacion entre paginas**: Dado cliente con >20 liquidaciones -> cuando navega -> mostrar siguientes 20 registros
4. **Descarga PDF**: Dado cliente visualizando liquidaciones -> cuando hace clic en uriPdf -> descargar PDF

---

## Dependencies

- Phase 2 del plan general (Foundational) debe estar completo
- Entidad LiquidacionCliente (ya existe)
- LiquidacionRepository adapter en infrastructure
- Enums FormaPago y EstadoLiquidacion en dominio

---

## Notes

- Follow principios SOLID en cada fase
- Usar Optional para valores opcionales
- Logging: log.error() solo para errores tecnicos (no para casos de negocio como "sin liquidaciones")
- Controller convierte Domain Model -> Response DTO usando MapStruct
- UseCase retorna Domain Models (no DTOs)
- Repository usa paginacion con offset/limit
- JPA Entity usa enums (con @Enumerated(EnumType.STRING)) - trade-off aceptado por simplicidad
- Mapper generado por MapStruct en build/generated/
- No usar caracteres especiales del español (ñ, acentos) en codigo