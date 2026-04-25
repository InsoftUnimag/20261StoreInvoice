# Implementation: Consultar Liquidaciones del Cliente

Date: 21-02-2026 Spec: especificaciones/consultar_liquidaciones_cliente.md
Status: Completed

---

## Summary

ImplementaciÃ³n del endpoint REST para consultar las liquidaciones de un cliente especÃ­fico con paginaciÃ³n y acceso a PDFs.

## Endpoint

**GET** `/api/v1/liquidaciones/cliente/{idCliente}`

### ParÃ¡metros

- `idCliente` (path, requerido): ID del cliente
- `pagina` (query, opcional, default: 0): NÃºmero de pÃ¡gina
- `tamaÃ±oPagina` (query, opcional, default: 20): Registros por pÃ¡gina

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
- `exception/LiquidacionNotFoundException.java` - ExcepciÃ³n cuando no se encuentra la liquidaciÃ³n

### Application Layer (`src/main/java/com/storeinvoice/application/`)

- `dto/query/ConsultarLiquidacionesQuery.java` - DTO para consultas con paginaciÃ³n
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
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚                    INFRAESTRUCTURE                          â”‚
â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”‚
â”‚  â”‚ Controller  â”‚  â”‚ Repository  â”‚  â”‚    JPA    â”‚  â”‚
â”‚  â”‚   (REST)    â”‚  â”‚  Adapter    â”‚  â”‚  Entity   â”‚  â”‚
â”‚  â””â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â”‚
â”‚         â”‚                â”‚                               â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚        â”‚      APPLICATION LAYER                        â”‚
â”‚        â”‚  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”  â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”   â”‚
â”‚        â””â”€â”€â–ºâ”‚   Use Case     â”‚  â”‚     DTOs       â”‚   â”‚
â”‚           â””â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”˜  â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â”‚
â”‚                   â”‚                                   â”‚
â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ï¿½ï¿½â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
â”‚                   â”‚         DOMAIN LAYER                 â”‚
â”‚           â”Œâ”€â”€â”€â”€â”€â”€â”´â”€â”€â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”       â”‚
â”‚           â”‚  Entity     â”‚   â”‚ Exceptions â”‚       â”‚
â”‚           â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜       â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

## Flujo de EjecuciÃ³n

1. El cliente envÃ­a solicitud GET `/api/v1/liquidaciones/cliente/{idCliente}`
2. `LiquidacionController` recibe la solicitud
3. Crea `ConsultarLiquidacionesQuery` con parÃ¡metros de paginaciÃ³n
4. `ConsultarLiquidacionesClienteUseCase` ejecuta la consulta
5. `LiquidacionRepositoryAdapter` consulta a la BD
6. Mapea entidades JPA a modelos de dominio
7. Mapea modelos a DTOs de respuesta
8. Retorna lista paginada al cliente

## Success Criteria

- [x] SC-001: Listar el 100% de las liquidaciones del cliente
- [x] SC-002: Tiempo de respuesta <3000ms
- [x] SC-003: Acceso a PDF en <3 clics (vÃ­a uri_pdf)

## Dependencies

- Spring Boot 4.0.5
- Spring Data JPA
- Spring Web MVC
- PostgreSQL
- Java 21

## Notas

- PaginaciÃ³n: 20 registros por pÃ¡gina por defecto
- Ordenamiento: CronolÃ³gico descendente (fechaLiquidacion DESC)
- El endpoint retorna lista vacÃ­a si el cliente no tiene liquidaciones
