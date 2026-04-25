Implementation Plan: Recibir Datos del Pedido desde Modulo de Inventario

Date: 2026-04-24
Spec: [especificaciones/recibir_datos_pedido_modulo_inventario.md](especificaciones/recibir_datos_pedido_modulo_inventario.md)

## Summary

Implementar la recepcion asincrona de datos de pedidos desde el Modulo de Inventario mediante RabbitMQ y Spring Cloud Stream. Al recibir un evento con `id_pedido`, `id_cliente`, `total_pedido` y `direccion`, el sistema consulta la forma de pago del cliente y crea directamente un registro en `liquidaciones_cliente` con estado `PENDIENTE`. No se crea tabla `pedido`; los datos del pedido se almacenan en la liquidacion. El campo `direccion` se recibe pero no se persiste. Los productos se consultaran posteriormente al generar el PDF cuando llegue el evento del Modulo de Transporte.

## Technical Context

- **Language/Version**: Java 21 (LTS)
- **Primary Dependencies**: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Spring Cloud Stream (para eventos), Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
- **Storage**: PostgreSQL con modelo puertos y adaptadores (arquitectura limpia) para no acoplarse a una BD especifica
- **Programming style**: Usar programacion reactiva, funcional, usar Optional, streams, lambdas. Usar excepciones particulares del dominio. Priorizar la codificacion en prosa. Lombok para evitar boilerplate. Records para DTOs.
- **Arquitectura**: Arquitectura limpia (domain, application, infrastructure) con principios SOLID, sin acoplamiento entre capas
- **Testing**: Test unitarios con Mockito, test de integracion con TestContainers
- **Project Type**: Backend API (sistema financiero que se comunica mediante eventos y APIs REST)
- **Performance Goals**: <2s para recepcion y almacenamiento del pedido, soportar 100 solicitudes concurrentes

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── storeinvoice/
│   │           └── storeinvoiceapi/
│   │               ├── domain/
│   │               │   └── exception/
│   │               │       ├── DatosPedidoInvalidosException.java
│   │               │       └── FormaPagoClienteNoEncontradaException.java
│   │               │
│   │               ├── application/
│   │               │   ├── dto/
│   │               │   │   └── messaging/
│   │               │   │       └── DatosPedidoInventarioMessage.java
│   │               │   └── service/
│   │               │       └── liquidacion/
│   │               │           └── cliente/
│   │               │               └── RegistrarLiquidacionDesdeInventarioUseCase.java
│   │               │
│   │               └── infrastructure/
│   │                   └── adapter/
│   │                       └── inbound/
│   │                           └── messaging/
│   │                               └── PedidoEventConsumer.java
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
    │               │               └── RegistrarLiquidacionDesdeInventarioUseCaseTest.java
    │               ├── infrastructure/
    │               │   └── adapter/
    │               │       └── inbound/
    │               │           └── messaging/
    │               │               └── PedidoEventConsumerTest.java
    │               └── integration/
    │                   └── PedidoRecepcionIntegrationTest.java
```

## Phase 1: Infrastructure Setup

**Purpose**: Configurar dependencias de mensajeria y aplicacion para soportar la recepcion de eventos.

**Prerequisites**: Fase 2 del plan general completada (entidades base, configuracion BD, excepciones globales, Cliente, FormaPago y Liquidacion existentes).

    [x] T001 Agregar dependencia del binder RabbitMQ de Spring Cloud Stream en `build.gradle` (`spring-cloud-stream-binder-rabbit`)
    [x] T002 Configurar propiedades de Spring Cloud Stream y RabbitMQ en `application.yml` (bindings, destination, group, content-type: application/json)
    [x] T003 Configurar DLQ y politica de reintentos en `application.yml` (auto-bind-dlq, republish-to-dlq)
    [x] T004 Verificar que `ddl-auto: validate` no genere errores al iniciar la aplicacion

## Phase 2: Domain Layer

**Purpose**: Definir las excepciones especificas para validacion de datos del mensaje y forma de pago no encontrada.

    [x] T005 Crear `DatosPedidoInvalidosException.java` en `domain/exception/` extendiendo `DomainException` para errores de validacion de datos del pedido recibido
    [x] T006 Crear `FormaPagoClienteNoEncontradaException.java` en `domain/exception/` extendiendo `DomainException` para cuando el `id_cliente` del mensaje no tiene forma de pago registrada
    [x] T007 Actualizar `DomainException.java` para incluir las nuevas excepciones en el `permits` del sealed class

## Phase 3: Application Layer

**Purpose**: Definir el DTO de mensaje y el caso de uso que orquesta la recepcion y persistencia en liquidaciones_cliente.

    [x] T008 Crear `DatosPedidoInventarioMessage.java` en `application/dto/messaging/` como Java Record con campos: idPedido, idCliente, totalPedido, direccion. Incluir validaciones Bean Validation.
    [x] T009 Crear `RegistrarLiquidacionDesdeInventarioUseCase.java` en `application/service/liquidacion/cliente/`. Orquestar: validar mensaje → consultar forma de pago via `FormaPagoClienteRepository` → construir `LiquidacionCliente` con estado PENDIENTE → persistir via `LiquidacionRepository`. Retornar `LiquidacionCliente` (domain model).

## Phase 4: Infrastructure Layer - Messaging

**Purpose**: Implementar el consumer de eventos de RabbitMQ y la integracion con el caso de uso.

    [x] T010 Crear `PedidoEventConsumer.java` en `infrastructure/adapter/inbound/messaging/` como `@Configuration` con funcion Spring Cloud Stream (`Consumer<DatosPedidoInventarioMessage>`). Recibir el mensaje y delegar a `RegistrarLiquidacionDesdeInventarioUseCase`.
    [x] T011 Implementar validacion de datos del mensaje recibido (idPedido > 0, idCliente > 0, totalPedido >= 0). Lanzar `DatosPedidoInvalidosException` si falla validacion.
    [x] T012 Agregar logging estructurado en `PedidoEventConsumer` y `RegistrarLiquidacionDesdeInventarioUseCase`: `log.info()` para inicio de operacion, `log.error()` unicamente para fallos tecnicos.

## Phase 5: Testing

**Purpose**: Garantizar funcionalidad correcta, manejo de errores y cumplimiento de criterios de aceptacion.

**Independent Test**: El sistema debe recibir y almacenar exitosamente los datos del pedido (`id_pedido`, `id_cliente`, `total_pedido`) incluyendo la `forma_pago` consultada, en menos de 2 segundos. No deben perderse pedidos (SC-023: 0% de pedidos no enviados a finanzas luego de haber sido creados).

    [x] T013 Crear test unitario `RegistrarLiquidacionDesdeInventarioUseCaseTest.java` usando Mockito: verificar que dado un `DatosPedidoInventarioMessage` valido, consulta forma de pago, construye el `LiquidacionCliente` y llama a `LiquidacionRepository.saveCliente()`.
    [x] T014 Crear test unitario `PedidoEventConsumerTest.java` usando Mockito: verificar que al recibir un mensaje valido, se delega al use case.
    [x] T015 Crear test de integracion `PedidoRecepcionIntegrationTest.java` con TestContainers (PostgreSQL + RabbitMQ): publicar mensaje en cola, verificar que se persiste en BD con forma de pago correcta.
    [x] T016 Crear test de integracion para escenario de error: mensaje con `id_cliente` inexistente debe generar excepcion y el mensaje debe ir a DLQ.
    [x] T017 Crear test de integracion para escenario de reintento: mensaje con datos invalidos debe reintentar segun configuracion y luego ir a DLQ.

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Refinamiento, documentacion y validacion final de la feature.

    [x] T018 Documentar el formato esperado del mensaje RabbitMQ y el nombre de la cola
    [x] T019 Verificar que la feature cumple SC-023: 0% de pedidos perdidos (revision de DLQ, persistencia confirmada antes de ACK)
    [x] T020 Revisar logs: eliminar logs de error innecesarios para casos de negocio
    [x] T021 Verificar manejo de ACK/NACK en RabbitMQ
    [x] T022 Ejecutar `./gradlew build` y `./gradlew test` para confirmar que todos los tests pasan
    [x] T023 Ejecutar `./gradlew bootRun` para verificar que la aplicacion inicia correctamente
    [x] T024 Revisar coverage de tests y agregar casos faltantes si es menor al 80%

## Dependencies & Execution Order

### Phase Dependencies

    Phase 1 (Infrastructure Setup): No dependencies directos de codigo, pero requiere Fase 2 del plan general completa (Cliente, FormaPago, Liquidacion, BD configurada).
    Phase 2 (Domain Layer): Depends on Phase 1.
    Phase 3 (Application Layer): Depends on Phase 2 (excepciones definidas).
    Phase 4 (Messaging Infrastructure): Depends on Phase 3 (use case definido).
    Phase 5 (Testing): Depends on Phases 3 y 4 (implementacion completa para testear).
    Phase 6 (Polish): Depends on all previous phases.

### Execution Order Within Phases

    Excepciones de dominio antes de use cases.
    Use cases antes de adaptadores (infraestructura depende de aplicacion).
    Core implementation antes de tests.
    Tests unitarios antes de tests de integracion.

## Notes

- **Reutilizacion**: Se reutiliza la entidad `LiquidacionCliente` y `LiquidacionRepository` existentes. No se crea tabla `pedido`.
- **No direccion**: El campo `direccion` del mensaje se recibe pero no se persiste en BD.
- **No productos**: Los productos del pedido NO se reciben ni almacenan en esta feature. Se consultaran directamente al modulo de inventario cuando se genere la liquidacion/PDF.
- **Estado inicial**: Al recibir el pedido, `estado_liquidacion` = `PENDIENTE` y `uri_pdf` = `null`. Se completaran al recibir el evento del Modulo de Transporte.
- **Mensajeria**: Spring Cloud Stream con binder RabbitMQ. Consumer funcional con API de funciones.
- **DLQ**: Configurada para garantizar que los mensajes que fallan no se pierdan (FR-051).
- **Trazabilidad**: Usar `log.info()` al inicio del procesamiento del mensaje con `id_pedido`.
- **Performance**: El procesamiento completo debe ser menor a 2 segundos.

## Success Criteria Mapping

| Criterio | Task(s) |
|----------|---------|
| SC-023: 0% de pedidos no enviados a finanzas luego de haber sido creados | T003 (DLQ), T015-T017 (tests de no-perdida), T019 (verificacion) |
| FR-048: Enviar datos a logistica de finanzas luego de crear pedido | T010-T012 (consumer + procesamiento) |
| FR-049: Esperar verificacion del buen recibido | T015 (test de integracion exitoso) |
| FR-051: Reenviar datos si ocurre problema y no llega notificacion de recibido | T003 (DLQ + reintentos), T017 (test de reintento) |
