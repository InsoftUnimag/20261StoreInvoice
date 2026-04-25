Implementation Plan: Recibir Datos del Pedido desde Modulo de Inventario

Date: 2026-04-24
Spec: [especificaciones/recibir_datos_pedido_modulo_inventario.md](especificaciones/recibir_datos_pedido_modulo_inventario.md)

## Summary

Implementar la recepcion asincrona de datos de pedidos desde el Modulo de Inventario mediante RabbitMQ y Spring Cloud Stream. Al recibir un evento con `id_pedido`, `id_cliente`, `total_pedido` y `direccion`, el sistema:

1. Consulta la forma de pago del cliente
2. Consulta los productos del pedido al Modulo de Inventario
3. Consulta los datos del cliente al Modulo de Gestion de Clientes
4. Genera el PDF de liquidacion con todos los datos
5. Guarda el registro en `liquidaciones_cliente` con estado `PENDIENTE` y la URI del PDF

No se crea tabla `pedido`; los datos del pedido se almacenan en la liquidacion. El campo `direccion` se recibe pero no se persiste en BD.

## Technical Context

- **Language/Version**: Java 21 (LTS)
- **Primary Dependencies**: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Spring Cloud Stream (para eventos), Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
- **Storage**: PostgreSQL con modelo puertos y adaptadores (arquitectura limpia)
- **Programming style**: Programacion reactiva y funcional, Optional, streams, lambdas. Excepciones particulares del dominio. Priorizar codificacion en prosa. Lombok para evitar boilerplate. Records para DTOs.
- **Arquitectura**: Arquitectura limpia (domain, application, infrastructure) con principios SOLID, sin acoplamiento entre capas
- **Testing**: Test unitarios con Mockito, test de integracion con TestContainers
- **Project Type**: Backend API (sistema financiero que se comunica mediante eventos y APIs REST)
- **Performance Goals**: <2s para recepcion, generacion de PDF y almacenamiento completo, soportar 100 solicitudes concurrentes

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── storeinvoice/
│   │           └── storeinvoiceapi/
│   │               ├── domain/
│   │               │   ├── model/
│   │               │   │   ├── Cliente.java
│   │               │   │   ├── Producto.java
│   │               │   │   └── LiquidacionCliente.java
│   │               │   └── exception/
│   │               │       ├── DomainException.java
│   │               │       ├── DatosPedidoInvalidosException.java
│   │               │       ├── FormaPagoClienteNoEncontradaException.java
│   │               │       ├── ProductosNoEncontradosException.java
│   │               │       ├── ErrorConsultaProductosException.java
│   │               │       └── ErrorConsultaClienteException.java
│   │               │
│   │               ├── application/
│   │               │   ├── dto/
│   │               │   │   ├── messaging/
│   │               │   │   │   └── DatosPedidoInventarioMessage.java
│   │               │   │   ├── ProductoPedidoDTO.java
│   │               │   │   └── ClienteLiquidacionDTO.java
│   │               │   ├── port/
│   │               │   │   ├── InventarioServicePort.java
│   │               │   │   ├── ClienteServicePort.java
│   │               │   │   ├── PdfGeneratorPort.java
│   │               │   │   └── PdfStoragePort.java
│   │               │   ├── repository/
│   │               │   │   ├── LiquidacionRepository.java
│   │               │   │   └── FormaPagoClienteRepository.java
│   │               │   └── service/
│   │               │       └── liquidacion/
│   │               │           ├── cliente/
│   │               │           │   ├── RegistrarLiquidacionDesdeInventarioUseCase.java
│   │               │           │   └── ProcesarPedidoInventarioUseCase.java
│   │               │           └── mapper/
│   │               │               ├── ProductoMapper.java
│   │               │               └── ClienteMapper.java
│   │               │
│   │               └── infrastructure/
│   │                   └── adapter/
│   │                       ├── inbound/
│   │                       │   └── messaging/
│   │                       │       └── PedidoEventConsumer.java
│   │                       └── outbound/
│   │                           ├── external/
│   │                           │   ├── InventarioWebClient.java
│   │                           │   ├── InventarioMockAdapter.java
│   │                           │   ├── ClienteWebClient.java
│   │                           │   └── ClienteMockAdapter.java
│   │                           ├── pdf/
│   │                           │   └── OpenPdfGeneratorAdapter.java
│   │                           └── storage/
│   │                               ├── LocalFileStorageAdapter.java
│   │                               └── SupabaseStorageAdapter.java
│   │
│   └── resources/
│       └── application.yml
│
└── test/
    ├── java/
    │   └── com/
    │       └── storeinvoice/
    │           └── storeinvoiceapi/
    │               ├── application/
    │               │   └── service/
    │               │       └── liquidacion/
    │               │           └── cliente/
    │               │               ├── RegistrarLiquidacionDesdeInventarioUseCaseTest.java
    │               │               └── ProcesarPedidoInventarioUseCaseTest.java
    │               ├── infrastructure/
    │               │   └── adapter/
    │               │       └── inbound/
    │               │           └── messaging/
    │               │               └── PedidoEventConsumerTest.java
    │               └── integration/
    │                   ├── PedidoRecepcionIntegrationTest.java
    │                   └── ProcesarPedidoInventarioIntegrationTest.java
```

## Phase 1: Infrastructure Setup

**Purpose**: Configurar dependencias de mensajeria y aplicacion para soportar la recepcion de eventos.

**Prerequisites**: Fase 2 del plan general completada (entidades base, configuracion BD, excepciones globales, Cliente, FormaPago y Liquidacion existentes).

    [x] T001 Agregar dependencia del binder RabbitMQ de Spring Cloud Stream en `build.gradle` (`spring-cloud-stream-binder-rabbit`)
    [x] T002 Configurar propiedades de Spring Cloud Stream y RabbitMQ en `application.yml` (bindings, destination, group, content-type: application/json)
    [x] T003 Configurar DLQ y politica de reintentos en `application.yml` (auto-bind-dlq, republish-to-dlq)
    [x] T004 Agregar dependencia OpenPDF en `build.gradle` (`com.github.librepdf:openpdf:2.0.3`)
    [x] T005 Configurar propiedades de Supabase Storage en `application.yml` (supabase.url, supabase.api-key, supabase.bucket)
    [x] T006 Configurar propiedades de almacenamiento local en `application.yml` (pdf.storage.local.path)

## Phase 2: Domain Layer

**Purpose**: Definir las excepciones especificas para validacion de datos del mensaje y errores de consulta externa.

    [x] T007 Crear `DatosPedidoInvalidosException.java` en `domain/exception/` extendiendo `DomainException` para errores de validacion de datos del pedido recibido
    [x] T008 Crear `FormaPagoClienteNoEncontradaException.java` en `domain/exception/` extendiendo `DomainException` para cuando el `id_cliente` del mensaje no tiene forma de pago registrada
    [x] T009 Crear `ProductosNoEncontradosException.java` en `domain/exception/` extendiendo `DomainException` para cuando el Modulo de Inventario retorna lista vacia de productos
    [x] T010 Crear `ErrorConsultaProductosException.java` en `domain/exception/` extendiendo `DomainException` para errores tecnicos al consultar productos
    [x] T011 Crear `ErrorConsultaClienteException.java` en `domain/exception/` extendiendo `DomainException` para errores tecnicos al consultar datos del cliente
    [x] T012 Actualizar `DomainException.java` para incluir las nuevas excepciones en el `permits` del sealed class

## Phase 3: Application Layer - Mappers

**Purpose**: Crear mappers para convertir tipos externos a DTOs internos.

    [x] T013 Crear `ProductoMapper.java` en `application/service/liquidacion/mapper/`. Convertir `List<Producto>` (del Modulo de Inventario) a `List<ProductoPedidoDTO>`.
        - Manejar conversion `String idProducto` -> `Long idProducto` (Long.parseLong)
        - Manejar conversion `double precioUnitario/subtotal` -> `BigDecimal`
        - Manejar `NumberFormatException` con `ErrorConsultaProductosException`
    [x] T014 Crear `ClienteMapper.java` en `application/service/liquidacion/mapper/`. Convertir `Cliente` (del Modulo de Clientes) a `ClienteLiquidacionDTO`.
        - Manejar conversion `String idCliente` -> `Long idCliente` (Long.parseLong)
        - Manejar `NumberFormatException` con `ClienteNotFoundException`

## Phase 4: Application Layer - Use Case Orquestador

**Purpose**: Implementar el caso de uso orquestador que coordina todo el flujo reactivo.

    [x] T015 Crear `ProcesarPedidoInventarioUseCase.java` en `application/service/liquidacion/cliente/`. Orquestar el flujo completo reactivo:
        1. Validar mensaje (declarativo con Optional)
        2. Consultar forma de pago via `FormaPagoClienteRepository`
        3. Consultar productos via `InventarioServicePort`
        4. Consultar cliente via `ClienteServicePort`
        5. Mapear a DTOs con los mappers
        6. Generar PDF via `GenerarPdfLiquidacionClienteUseCase`
        7. Construir `LiquidacionCliente` con estado PENDIENTE y URI del PDF
        8. Persistir via `LiquidacionRepository` envuelto en `TransactionTemplate` para manejar transaccion JPA en pipeline reactivo
        9. Retornar `Mono<Void>`
    [x] T016 Inyectar dependencias: `FormaPagoClienteRepository`, `InventarioServicePort`, `ClienteServicePort`, `GenerarPdfLiquidacionClienteUseCase`, `LiquidacionRepository`, `TransactionTemplate`
    [x] T017 Manejo de errores: `.onErrorResume` para no matar el pipeline, errores por mensaje aislados
    [x] T018 Logging: `log.info()` para inicio/exitoso, `log.error()` unicamente para fallos tecnicos

## Phase 5: Infrastructure Layer - Messaging

**Purpose**: Implementar el consumer de eventos de RabbitMQ reactivo.

    [x] T019 Refactorizar `PedidoEventConsumer.java` en `infrastructure/adapter/inbound/messaging/`.
        - Cambiar de `Consumer<DatosPedidoInventarioMessage>` a `Function<DatosPedidoInventarioMessage, Mono<Void>>`
        - Inyectar `ProcesarPedidoInventarioUseCase` (no `RegistrarLiquidacionDesdeInventarioUseCase`)
        - Pipeline reactivo con `.doOnSuccess`, `.onErrorResume(e -> Mono.empty())`
    [x] T020 Aislamiento de errores: Error en un mensaje no debe afectar el procesamiento de mensajes siguientes
    [x] T021 Mantener `RegistrarLiquidacionDesdeInventarioUseCase` intacto (principio Open/Closed) para uso futuro o tests independientes

## Phase 6: Infrastructure Layer - PDF y Storage

**Purpose**: Implementar generacion de PDF y almacenamiento.

    [x] T022 Crear `OpenPdfGeneratorAdapter.java` en `infrastructure/adapter/outbound/pdf/`. Generar PDF con tabla de productos, total, forma de pago, datos del cliente.
    [x] T023 Crear `LocalFileStorageAdapter.java` en `infrastructure/adapter/outbound/storage/` (perfil local/test). Guardar PDF en directorio local, retornar URI.
    [x] T024 Crear `SupabaseStorageAdapter.java` en `infrastructure/adapter/outbound/storage/` (perfil default/prod). Subir PDF a Supabase Storage via WebClient, retornar URL publica.

## Phase 7: Testing

**Purpose**: Garantizar funcionalidad correcta, manejo de errores y cumplimiento de criterios de aceptacion.

**Independent Test**: El sistema debe recibir los datos del pedido, generar el PDF y almacenar la liquidacion con URI en menos de 2 segundos.

    [x] T025 Crear test unitario `ProcesarPedidoInventarioUseCaseTest.java` usando Mockito + StepVerifier:
        - Flujo exitoso completo (mensaje valido -> forma pago -> productos -> cliente -> PDF -> guardado)
        - Mensaje nulo
        - idPedido invalido (cero)
        - idCliente invalido (cero)
        - totalPedido negativo
        - Forma de pago no encontrada
        - Productos vacios
        - Error al consultar productos
        - Cliente no encontrado
        - Error al consultar cliente
        - Error al generar PDF
    [x] T026 Crear test unitario `PedidoEventConsumerTest.java` usando Mockito + StepVerifier:
        - Mensaje procesado exitosamente retorna Mono<Void>
        - Error en procesamiento NO mata el consumer
    [x] T027 Crear test de integracion `ProcesarPedidoInventarioIntegrationTest.java` con TestContainers (PostgreSQL + RabbitMQ):
        - Publicar mensaje, verificar que se persiste en BD con forma de pago correcta y URI del PDF no nula
        - Mensaje invalido no guarda en BD
    [x] T028 Mantener tests existentes de `RegistrarLiquidacionDesdeInventarioUseCase` y `PedidoRecepcionIntegrationTest` funcionando

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Refinamiento, documentacion y validacion final de la feature.

    [x] T029 Documentar el formato esperado del mensaje RabbitMQ y el nombre de la cola
    [x] T030 Verificar que la feature cumple SC-023: 0% de pedidos perdidos (revision de DLQ, persistencia confirmada antes de ACK)
    [x] T031 Revisar logs: eliminar logs de error innecesarios para casos de negocio
    [x] T032 Verificar manejo de ACK/NACK en RabbitMQ
    [x] T033 Ejecutar `./gradlew build` y `./gradlew test` para confirmar que todos los tests pasan
    [x] T034 Ejecutar `./gradlew bootRun` para verificar que la aplicacion inicia correctamente
    [x] T035 Limpiar BD local de desarrollo si es necesario (eliminar `application.properties` conflictivo)

## Dependencies & Execution Order

### Phase Dependencies

    Phase 1 (Infrastructure Setup): No dependencies directos de codigo, pero requiere Fase 2 del plan general completa.
    Phase 2 (Domain Layer): Depends on Phase 1.
    Phase 3 (Mappers): Depends on Phase 2.
    Phase 4 (Use Case Orquestador): Depends on Phases 2 y 3.
    Phase 5 (Messaging Infrastructure): Depends on Phase 4.
    Phase 6 (PDF y Storage): Depends on Phase 4.
    Phase 7 (Testing): Depends on Phases 4, 5 y 6.
    Phase 8 (Polish): Depends on all previous phases.

### Execution Order Within Phases

    Excepciones de dominio antes de use cases.
    Mappers antes de use case orquestador.
    Use cases antes de adaptadores (infraestructura depende de aplicacion).
    Core implementation antes de tests.
    Tests unitarios antes de tests de integracion.

## Cambios Respecto al Plan Original

| Aspecto | Plan Original | Implementacion Actual |
|---------|--------------|----------------------|
| **Generacion de PDF** | Se generaba al recibir estado final del Modulo de Transporte | Se genera **inmediatamente** al recibir el mensaje del Inventario |
| **uri_pdf** | Se guardaba como `null` inicialmente | Se guarda con la **URI del PDF generado** |
| **Consulta de productos** | Se consultaban posteriormente (al generar PDF) | Se consultan **inmediatamente** via `InventarioServicePort` |
| **Consulta de cliente** | No estaba en el flujo original | Se consulta **inmediatamente** via `ClienteServicePort` |
| **Use case principal** | `RegistrarLiquidacionDesdeInventarioUseCase` | `ProcesarPedidoInventarioUseCase` (orquestador reactivo) |
| **Consumer** | `Consumer<DatosPedidoInventarioMessage>` (sincrono) | `Function<DatosPedidoInventarioMessage, Mono<Void>>` (reactivo) |
| **Persistencia** | JPA sincrono con `@Transactional` | JPA envuelto en `Mono.fromCallable` + `TransactionTemplate` para pipeline reactivo |

## Notes

- **Reutilizacion**: Se reutiliza la entidad `LiquidacionCliente` y `LiquidacionRepository` existentes. No se crea tabla `pedido`.
- **No direccion**: El campo `direccion` del mensaje se recibe pero no se persiste en BD.
- **Estado inicial**: Al recibir el pedido, `estado_liquidacion` = `PENDIENTE`. El PDF se genera inmediatamente, no se espera al Modulo de Transporte.
- **Mensajeria**: Spring Cloud Stream con binder RabbitMQ. Consumer funcional reactivo con API de funciones (`Function<T, Mono<Void>>`).
- **DLQ**: Configurada para garantizar que los mensajes que fallan no se pierden (FR-051).
- **Trazabilidad**: Usar `log.info()` al inicio del procesamiento del mensaje con `id_pedido`.
- **Performance**: El procesamiento completo (recepcion + consultas externas + generacion PDF + almacenamiento) debe ser menor a 2 segundos.
- **Reactividad**: Todo el pipeline es reactivo (`Mono`). No se usa `.block()` en ningun punto.
- **TransactionTemplate**: Necesario para manejar transacciones JPA dentro de un pipeline reactivo que ejecuta en `Schedulers.boundedElastic()`.

## Success Criteria Mapping

| Criterio | Task(s) |
|----------|---------|
| SC-023: 0% de pedidos no enviados a finanzas luego de haber sido creados | T003 (DLQ), T027-T028 (tests de integracion), T030 (verificacion) |
| FR-048: Enviar datos a logistica de finanzas luego de crear pedido | T019-T021 (consumer + procesamiento) |
| FR-049: Esperar verificacion del buen recibido | T027 (test de integracion exitoso) |
| FR-051: Reenviar datos si ocurre problema y no llega notificacion de recibido | T003 (DLQ + reintentos), T027 (test de integracion con error) |

## Archivos Creados/Modificados

### Nuevos archivos:
- `domain/exception/ProductosNoEncontradosException.java`
- `domain/exception/ErrorConsultaProductosException.java`
- `domain/exception/ErrorConsultaClienteException.java`
- `application/service/liquidacion/mapper/ProductoMapper.java`
- `application/service/liquidacion/mapper/ClienteMapper.java`
- `application/service/liquidacion/cliente/ProcesarPedidoInventarioUseCase.java`
- `test/.../ProcesarPedidoInventarioUseCaseTest.java`
- `test/.../ProcesarPedidoInventarioIntegrationTest.java`

### Archivos modificados:
- `domain/exception/DomainException.java` (agregar excepciones al sealed)
- `infrastructure/adapter/inbound/messaging/PedidoEventConsumer.java` (refactor a reactivo)
- `src/main/resources/application.yml` (password de BD)
- `src/main/resources/application.properties` (eliminado por conflicto)

### Archivos mantenidos intactos (Open/Closed):
- `application/service/liquidacion/cliente/RegistrarLiquidacionDesdeInventarioUseCase.java`
- `test/.../RegistrarLiquidacionDesdeInventarioUseCaseTest.java`
- `test/.../PedidoRecepcionIntegrationTest.java`
