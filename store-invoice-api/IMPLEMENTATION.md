# Implementation: Consultar Liquidaciones del Cliente

Date: 21-02-2026 Spec: especificaciones/consultar_liquidaciones_cliente.md
Status: Completed

---

## Summary

Implementación del endpoint REST para consultar las liquidaciones de un cliente específico con paginación y acceso a PDFs.

## Endpoint

**GET** `/api/v1/liquidaciones/cliente/{idCliente}`

### Parámetros

- `idCliente` (path, requerido): ID del cliente
- `pagina` (query, opcional, default: 0): Número de página
- `tamañoPagina` (query, opcional, default: 20): Registros por página

### Respuesta Exitosa

```json
[
  {
    "idLiquidacion": 1,
    "idPedido": 100,
    "idCliente": 50,
    "formaPago": "CONTRA_ENTREGA",
    "estadoLiquidacion": "PENDIENTE",
    "fechaLiquidacion": "2026-03-21T10:00:00",
    "uriPdf": "/api/v1/pdf/liquidacion/1",
    "montoLiquidado": 15000.00
  }
]
```

## Estructura Implementada

### Domain Layer (`src/main/java/com/storeinvoice/domain/`)

- `model/LiquidacionCliente.java` - Entidad de dominio inmutable
- `exception/DomainException.java` - Clase base para excepciones de dominio
- `exception/LiquidacionNotFoundException.java` - Excepción cuando no se encuentra la liquidación

### Application Layer (`src/main/java/com/storeinvoice/application/`)

- `dto/query/ConsultarLiquidacionesQuery.java` - DTO para consultas con paginación
- `dto/response/LiquidacionClienteResponse.java` - DTO de respuesta
- `service/liquidacion/ConsultarLiquidacionesClienteUseCase.java` - Caso de uso

### Infrastructure Layer (`src/main/java/com/storeinvoice/infrastructure/`)

- `port/outbound/LiquidacionRepositoryPort.java` - Puerto de salida para repositorio
- `persistence/entity/LiquidacionClienteJpaEntity.java` - Entidad JPA
- `persistence/mapper/LiquidacionEntityMapper.java` - Mapper entre entity y domain
- `adapter/outbound/persistence/LiquidacionRepositoryAdapter.java` - Adaptador de repositorio
- `adapter/inbound/rest/LiquidacionController.java` - Controlador REST

## Arquitectura Hexagonal

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRAESTRUCTURE                          │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ Controller  │  │ Repository  │  │    JPA    │  │
│  │   (REST)    │  │  Adapter    │  │  Entity   │  │
│  └──────┬──────┘  └──────┬──────┘  └────────────┘  │
│         │                │                               │
├────────┼────────────────┼───────────────────────────────┤
│        │      APPLICATION LAYER                        │
│        │  ┌────────────────┐  ┌────────────────┐   │
│        └──►│   Use Case     │  │     DTOs       │   │
│           └───────┬────────┘  └────────────────┘   │
│                   │                                   │
├───────────────────┼───────────────────────────��───────────┤
│                   │         DOMAIN LAYER                 │
│           ┌──────┴──────┐   ┌──────────────┐       │
│           │  Entity     │   │ Exceptions │       │
│           └────────────┘   └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

## Flujo de Ejecución

1. El cliente envía solicitud GET `/api/v1/liquidaciones/cliente/{idCliente}`
2. `LiquidacionController` recibe la solicitud
3. Crea `ConsultarLiquidacionesQuery` con parámetros de paginación
4. `ConsultarLiquidacionesClienteUseCase` ejecuta la consulta
5. `LiquidacionRepositoryAdapter` consulta a la BD
6. Mapea entidades JPA a modelos de dominio
7. Mapea modelos a DTOs de respuesta
8. Retorna lista paginada al cliente

## Success Criteria

- [x] SC-001: Listar el 100% de las liquidaciones del cliente
- [x] SC-002: Tiempo de respuesta <3000ms
- [x] SC-003: Acceso a PDF en <3 clics (vía uri_pdf)

## Dependencies

- Spring Boot 4.0.5
- Spring Data JPA
- Spring Web MVC
- PostgreSQL
- Java 21

## Notas

- Paginación: 20 registros por página por defecto
- Ordenamiento: Cronológico descendente (fechaLiquidacion DESC)
- El endpoint retorna lista vacía si el cliente no tiene liquidaciones