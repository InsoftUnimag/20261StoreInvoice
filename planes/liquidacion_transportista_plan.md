# Implementation Plan: Recepcion de Estado Final y Generacion de Liquidacion de Transportista

**Date:** 24-04-2026  
**Updated:** 22-05-2026  
**Spec:** especificaciones/recibir_estado_final_modulo_transporte.md, especificaciones/generar_liquidacion_de_transportista.md

---

## Summary

El flujo inicia de manera asincrona cuando el Modulo de Transporte emite un evento indicando el estado final de una entrega. Este evento es capturado por un adaptador de entrada (Consumer) en la capa de infraestructura del Sistema Financiero. Una vez recibido, se registrara el evento para garantizar su seguimiento e idempotencia. Luego, se consultara el precio del pedido desde la liquidacion del cliente (ya que fue procesada previamente desde el Modulo de Inventario), se aplicaran las reglas de negocio para calcular el pago al transportista (con su respectivo redondeo y reglas de excepcion como perdidas operativas), y se generara y guardara la entidad correspondiente, actualizando finalmente el estado del evento.

**Technical Approach:** Se implementara la logica de calculo puro en la capa de dominio sin dependencias externas, introduciendo la entidad de seguimiento de eventos. Se generaran los casos de uso respectivos para la gestion de liquidaciones de transportistas (CRUD) y procesamiento de eventos respetando estrictamente el principio de responsabilidad unica. La delegacion se hara a traves del consumidor de eventos y la inyeccion de dependencias utilizara `@RequiredArgsConstructor`. La persistencia asegurara que se guarde correctamente cada liquidacion procesada y el historial de procesamiento de eventos.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Spring Cloud Stream, Lombok, MapStruct  
**Storage:** PostgreSQL  
**Programming Style:** Programacion en prosa, favoreciendo la lectura natural sin condicionales anidados. Uso imperativo de la anotacion `@RequiredArgsConstructor` para la inyeccion de dependencias. Cada caso de uso representara una unica accion.  
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - Calculo automatizado de liquidacion segun tasa de efectividad (P1)
Yo como Sistema Financiero necesito calcular el monto a pagar al transportista tomando como base el 10% del precio total del pedido y aplicando la tasa de efectividad recibida del Modulo de Gestion de Transporte, para generar una liquidacion confiable.

**Consumo interno:** Evento asincrono via `Spring Cloud Stream` (RabbitMQ).

---

## Success Criteria

- **SC-001:** El cien por ciento de los datos validos recibidos deben ser procesados.
- **SC-002:** El procesamiento de cada evento debe completarse en menos de cinco segundos.
- **SC-003:** El cien por ciento de las liquidaciones generadas reflejan exactamente el calculo de la tarifa base por la tasa de efectividad.
- **SC-004:** El sistema maneja errores de manera que puedan ser reintentados y ninguna liquidacion se genera si los datos son invalidos o nulos.

---

## Project Structure

```text
src/
├── main/
│   └── java/
│       └── com/
│           └── storeinvoice/
│               └── storeinvoiceapi/
│                   ├── domain/
│                   │   ├── model/
│                   │   │   ├── EventoRecibido.java
│                   │   │   ├── LiquidacionTransportista.java
│                   │   │   └── TasaEfectividad.java
│                   │   └── valueobject/
│                   │       └── TasaEfectividad.java
│                   ├── application/
│                   │   ├── dto/
│                   │   │   └── command/
│                   │   │       └── ProcesarEstadoFinalCommand.java
│                   │   ├── repository/
│                   │   │   ├── EventoRecibidoRepository.java
│                   │   │   └── LiquidacionRepository.java
│                   │   └── service/
│                   │       └── liquidacion/
│                   │           └── transportista/
│                   │               └── ProcesarEstadoFinalUseCase.java
│                   └── infrastructure/
│                       ├── adapter/
│                       │   ├── inbound/
│                       │   │   └── messaging/
│                       │   │       └── EstadoFinalEventConsumer.java
│                       │   └── outbound/
│                       │       └── persistence/
│                       │           ├── EventoRecibidoRepositoryAdapter.java
│                       │           └── LiquidacionRepositoryAdapter.java
│                       └── config/
│                           └── TransactionConfig.java
│
├── test/
│   └── java/
│       └── com/
│           └── storeinvoice/
│               └── storeinvoiceapi/
│                   ├── application/
│                   │   └── service/
│                   │       └── liquidacion/
│                   │           └── transportista/
│                   │               └── ProcesarEstadoFinalUseCaseTest.java
│                   └── infrastructure/
│                       └── adapter/
│                           └── inbound/
│                               └── messaging/
│                                   └── EstadoFinalEventConsumerTest.java
```

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**Proposito:** Establecer las reglas de negocio puras, seguimiento de eventos y la formula matematica.
- [x] **T001:** Crear el Value Object `TasaEfectividad` para validar que los valores esten dentro del rango permitido (-100 a 100).
- [x] **T002:** Actualizar la entidad existente `LiquidacionTransportista` en la capa de modelo de dominio para anadir el metodo de calculo que obtenga el monto aplicando la formula `(Precio Pedido × 10%) × (tasa_efectividad / 100)`, aplicando redondeo al numero entero mas cercano.
- [x] **T003:** Crear la entidad `EventoRecibido` (con campos: `id_pedido`, `tasa_efectividad`, `id_transportista`, `estado`, `fecha_recibido`, `fecha_procesado`) para rastrear el procesamiento de cada estado final y permitir manejo de duplicados.

### Phase 2: Capa de Aplicacion (Application)
**Proposito:** Procesar la orden de generar la liquidacion, garantizando validaciones de negocio e idempotencia.
- [x] **T004:** Crear el objeto de transferencia de datos `ProcesarEstadoFinalCommand` en `application/dto/command/` para encapsular los datos de entrada validados provenientes del evento.
- [x] **T005:** Crear el caso de uso `ProcesarEstadoFinalUseCase` que orqueste lo siguiente:
    - Validar comando (no nulo, campos requeridos).
    - Consultar si el `id_pedido` ya existe en `EventoRecibido` con estado PROCESADO para prevenir eventos duplicados (Idempotencia).
    - Registrar el inicio del evento como "PENDIENTE".
    - Consultar el precio del pedido desde `LiquidacionRepository.findByIdPedido()` (obtiene `montoLiquidado` de `LiquidacionCliente`).
    - Bloquear la operacion si el precio es nulo o estrictamente 0.
    - Delegar a la entidad `LiquidacionTransportista` el calculo del monto.
    - Guardar la liquidacion generada y actualizar el estado de `EventoRecibido` a "PROCESADO".
    - En caso de error, actualizar estado a "ERROR" para permitir reintento (DLQ).

### Phase 3: Capa de Infraestructura (Infrastructure)
**Proposito:** Habilitar el consumo de eventos asincronos y persistencia de seguimiento.
- [x] **T006:** Implementar el adaptador `EstadoFinalEventConsumer` utilizando `@RequiredArgsConstructor`, mapeando el evento JSON a `ProcesarEstadoFinalCommand` e invocando al caso de uso correspondiente.
- [x] **T007:** Configurar la gestion de reintentos y colas de mensajes no procesables (Dead Letter Queue) en las propiedades para manejar fallos de manera resiliente.
- [x] **T008:** Crear adaptadores de persistencia para `EventoRecibido` que permitan la insercion y validacion de existencia (para el control de duplicados).
- [x] **T009:** Crear `TransactionConfig` con `@EnableTransactionManagement` para configurar explicitamente `TransactionTemplate` y `JpaTransactionManager`.

### Phase 4: Refactorizacion y Correcciones
**Proposito:** Resolver problemas de ejecucion y simplificar el flujo.
- [x] **T010:** Eliminar `PedidoRepository` y todas sus dependencias (`PedidoRepositoryAdapter`, `PedidoJpaEntity`, `PedidoJpaMapper`, `Pedido` domain model) ya que el precio del pedido se obtiene de `LiquidacionCliente`.
- [x] **T011:** Actualizar `ConsultarTotalPedidoUseCase` para usar `LiquidacionRepository.findByIdPedido()` en lugar de `PedidoRepository`.
- [x] **T012:** Actualizar `V4__create_pedidos_and_evento_recibido_table.sql` para eliminar la tabla `pedidos` (ya no se usa).
- [x] **T013:** Corregir `ProcesarEstadoFinalUseCase.execute()` para ejecutar directamente `transactionTemplate.executeWithoutResult()` sin wrapping reactivo (`Mono.fromCallable` o `Mono.fromRunnable`), ya que estos causaban que el codigo no se ejecutara en el contexto de Spring Cloud Stream.

### Phase 5: Testing
**Proposito:** Garantizar funcionalidad correcta y manejo de errores.
- [x] **T014:** Actualizar `ProcesarEstadoFinalUseCaseTest`:
    - Cambiar `@Mock TransactionTemplate` por `@Spy TransactionTemplate` para simular la ejecucion real del callback.
    - Actualizar tests para reflejar que el precio ahora viene de `LiquidacionCliente`.
    - Agregar test para monto cero.
- [x] **T015:** Actualizar `ConsultarTotalPedidoUseCaseTest` para usar `LiquidacionRepository`.

---

## Notes
- **Regla estricta:** Solo se crearan e implementaran los adaptadores y comandos estrictamente necesarios para el evento de transporte detallado. Los repositorios de otras entidades, u otros casos de uso que no tienen que ver con este flujo, no seran creados ni forman parte de este desarrollo.
- **Inyeccion de dependencias:** Todos los consumidores de eventos deben estar decorados obligatoriamente con `@RequiredArgsConstructor` de Lombok para garantizar una inyeccion limpia mediante constructores generados de forma automatica.
- **Estructura en prosa:** Toda la logica de planificacion ha sido descrita de forma narrativa y estructurada, paso a paso, favoreciendo el entendimiento humano y directo como fue requerido.
- **Patron de ejecucion:** El uso de `Mono.fromCallable()` o `Mono.fromRunnable()` con `subscribeOn()` dentro de un `Function<Command, Mono<Void>>` de Spring Cloud Stream causaba que el codigo dentro del callable/runnable no se ejecutara. La solucion fue ejecutar directamente `transactionTemplate.executeWithoutResult()` (imperativo) y retornar `Mono.empty()` al final, manteniendo la compatibilidad con la firma del consumer.
- **Fuente de precio del pedido:** El precio total del pedido ya no se consulta de una tabla `pedidos` independiente. Se obtiene de `liquidaciones_cliente.monto_liquidado`, que fue guardado cuando se proceso el pedido desde el Modulo de Inventario.
- **Eliminacion de codigo muerto:** Se eliminaron `PedidoRepository`, `PedidoRepositoryAdapter`, `PedidoJpaEntity`, `PedidoJpaMapper`, y `Pedido` (domain model) porque su funcionalidad fue absorbida por `LiquidacionRepository` y `LiquidacionCliente`.

---

## Success Criteria Mapping

| Criterio | Task(s) |
|----------|---------|
| SC-001: El cien por ciento de los datos validos recibidos deben ser procesados. | T005 (idempotencia), T013 (ejecucion directa sin wrapping reactivo problematico) |
| SC-002: El procesamiento de cada evento debe completarse en menos de cinco segundos. | T005 (flujo sincrono directo), T007 (DLQ para reintentos) |
| SC-003: El cien por ciento de las liquidaciones generadas reflejan exactamente el calculo de la tarifa base por la tasa de efectividad. | T002 (formula en dominio), T005 (uso de dominio) |
| SC-004: El sistema maneja errores de manera que puedan ser reintentados. | T005 (estado ERROR en EventoRecibido), T007 (DLQ) |

---

## Archivos Creados/Modificados

### Nuevos archivos:
- `domain/model/EventoRecibido.java`
- `domain/model/LiquidacionTransportista.java`
- `domain/valueobject/TasaEfectividad.java`
- `domain/exception/EstadoFinalInvalidoException.java`
- `application/dto/command/ProcesarEstadoFinalCommand.java`
- `application/repository/EventoRecibidoRepository.java`
- `application/service/liquidacion/transportista/ProcesarEstadoFinalUseCase.java`
- `infrastructure/adapter/inbound/messaging/EstadoFinalEventConsumer.java`
- `infrastructure/adapter/outbound/persistence/EventoRecibidoRepositoryAdapter.java`
- `infrastructure/persistence/entity/EventoRecibidoJpaEntity.java`
- `infrastructure/persistence/mapper/EventoRecibidoMapper.java`
- `infrastructure/config/TransactionConfig.java`
- `test/.../ProcesarEstadoFinalUseCaseTest.java`
- `test/.../EstadoFinalEventConsumerTest.java`

### Archivos modificados:
- `application/service/pedido/ConsultarTotalPedidoUseCase.java` (ahora usa `LiquidacionRepository`)
- `application/service/liquidacion/transportista/ProcesarEstadoFinalUseCase.java` (patron de ejecucion simplificado)
- `resources/db/migration/V4__create_pedidos_and_evento_recibido_table.sql` (eliminada tabla `pedidos`)

### Archivos eliminados:
- `application/repository/PedidoRepository.java`
- `application/service/pedido/ConsultarTotalPedidoUseCase.java` (logica actualizada)
- `infrastructure/adapter/outbound/persistence/PedidoRepositoryAdapter.java`
- `infrastructure/persistence/entity/PedidoJpaEntity.java`
- `infrastructure/persistence/mapper/PedidoJpaMapper.java`
- `domain/model/Pedido.java`
