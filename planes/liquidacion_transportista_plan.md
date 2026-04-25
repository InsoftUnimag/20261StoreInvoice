# Implementation Plan: RecepciÃ³n de Estado Final y GeneraciÃ³n de LiquidaciÃ³n de Transportista

**Date:** 24-04-2026  
**Spec:** especificaciones/recibir_estado_final_modulo_transporte.md, especificaciones/generar_liquidacion_de_transportista.md

---

## Summary

El flujo inicia de manera asÃ­ncrona cuando el MÃ³dulo de Transporte emite un evento indicando el estado final de una entrega. Este evento es capturado por un adaptador de entrada (Consumer) en la capa de infraestructura del Sistema Financiero. Una vez recibido, se registrarÃ¡ el evento para garantizar su seguimiento e idempotencia. Luego, se consultarÃ¡ el precio del pedido previamente guardado, se aplicarÃ¡n las reglas de negocio para calcular el pago al transportista (con su respectivo redondeo y reglas de excepciÃ³n como pÃ©rdidas operativas), y se generarÃ¡ y guardarÃ¡ la entidad correspondiente, actualizando finalmente el estado del evento.

**Technical Approach:** Se implementarÃ¡ la lÃ³gica de cÃ¡lculo puro en la capa de dominio sin dependencias externas, introduciendo la entidad de seguimiento de eventos. Se generarÃ¡n los casos de uso respectivos para la gestiÃ³n de liquidaciones de transportistas (CRUD) y procesamiento de eventos respetando estrictamente el principio de responsabilidad Ãºnica. La delegaciÃ³n se harÃ¡ a travÃ©s del consumidor de eventos y la inyecciÃ³n de dependencias utilizarÃ¡ `@RequiredArgsConstructor`. La persistencia asegurarÃ¡ que se guarde correctamente cada liquidaciÃ³n procesada y el historial de procesamiento de eventos.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Spring Cloud Stream, Lombok, MapStruct  
**Storage:** PostgreSQL  
**Programming Style:** ProgramaciÃ³n en prosa, favoreciendo la lectura natural sin condicionales anidados. Uso imperativo de la anotaciÃ³n `@RequiredArgsConstructor` para la inyecciÃ³n de dependencias. Cada caso de uso representarÃ¡ una Ãºnica acciÃ³n.  
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - CÃ¡lculo automatizado de liquidaciÃ³n segÃºn tasa de efectividad (P1)
Yo como Sistema Financiero necesito calcular el monto a pagar al transportista tomando como base el 10% del precio total del pedido y aplicando la tasa de efectividad recibida del MÃ³dulo de GestiÃ³n de Transporte, para generar una liquidaciÃ³n confiable.

**Consumo interno:** Evento asÃ­ncrono vÃ­a `Spring Cloud Stream` (RabbitMQ).

---

## Success Criteria

- **SC-001:** El cien por ciento de los datos vÃ¡lidos recibidos deben ser procesados.
- **SC-002:** El procesamiento de cada evento debe completarse en menos de cinco segundos.
- **SC-003:** El cien por ciento de las liquidaciones generadas reflejan exactamente el cÃ¡lculo de la tarifa base por la tasa de efectividad.
- **SC-004:** El sistema maneja errores de manera que puedan ser reintentados y ninguna liquidaciÃ³n se genera si los datos son invÃ¡lidos o nulos.

---

## Project Structure

```text
src/
â”œâ”€â”€ main/
â”‚   â””â”€â”€ java/
â”‚       â””â”€â”€ com/
â”‚           â””â”€â”€ storeinvoice/
â”‚               â””â”€â”€ storeinvoiceapi/
â”‚                   â”œâ”€â”€ domain/
â”‚                   â”‚   â”œâ”€â”€ entity/
â”‚                   â”‚   â”‚   â””â”€â”€ EventoRecibido.java
â”‚                   â”‚   â””â”€â”€ valueobject/
â”‚                   â”‚       â””â”€â”€ TasaEfectividad.java
â”‚                   â”œâ”€â”€ application/
â”‚                   â”‚   â””â”€â”€ dto/
â”‚                   â”‚       â””â”€â”€ command/
â”‚                   â”‚           â””â”€â”€ ProcesarEstadoFinalCommand.java
â”‚                   â””â”€â”€ infrastructure/
â”‚                       â””â”€â”€ adapter/
â”‚                           â””â”€â”€ inbound/
â”‚                               â””â”€â”€ messaging/
â”‚                                   â””â”€â”€ EstadoFinalEventConsumer.java
```

*(Nota: Solo se listan los archivos nuevos a crear en esta especificaciÃ³n. Los archivos existentes a modificar, como `LiquidacionTransportista` y `CrearLiquidacionTransportistaUseCase`, no se incluyen en el Ã¡rbol para mantener la claridad de los entregables nuevos).*

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**PropÃ³sito:** Establecer las reglas de negocio puras, seguimiento de eventos y la fÃ³rmula matemÃ¡tica.
- [x] **T001:** Crear el Value Object `TasaEfectividad` para validar que los valores estÃ©n dentro del rango permitido (-100 a 100).
- [x] **T002:** Actualizar la entidad existente `LiquidacionTransportista` en la capa de modelo de dominio para aÃ±adir el mÃ©todo de cÃ¡lculo que obtenga el monto aplicando la fÃ³rmula `(Precio Pedido Ã— 10%) Ã— (tasa_efectividad / 100)`, aplicando redondeo al nÃºmero entero mÃ¡s cercano.
- [x] **T003:** Crear la entidad `EventoRecibido` (con campos: `id_pedido`, `tasa_efectividad`, `id_transportista`, `status`, `fecha_recibido`, `fecha_procesado`) para rastrear el procesamiento de cada estado final y permitir manejo de duplicados.

### Phase 2: Capa de AplicaciÃ³n (Application)
**PropÃ³sito:** Procesar la orden de generar la liquidaciÃ³n, garantizando validaciones de negocio e idempotencia.
- [x] **T004:** Crear el objeto de transferencia de datos `ProcesarEstadoFinalCommand` en `application/dto/command/` para encapsular los datos de entrada validados provenientes del evento.
- [x] **T005:** Actualizar/crear el caso de uso `ProcesarEstadoFinalUseCase` (o refactorizar `CrearLiquidacionTransportistaUseCase`) que orqueste lo siguiente:
    - Consultar si el `id_pedido` ya existe en `EventoRecibido` para prevenir eventos duplicados (Idempotencia).
    - Registrar el inicio del evento como "pendiente".
    - Consultar el precio del pedido y bloquear la operaciÃ³n si el precio es nulo o estrictamente 0.
    - Delegar a la entidad `LiquidacionTransportista` el cÃ¡lculo del monto.
    - Si la `tasa_efectividad` es 0%, registrar el costo del flete en un reporte de "pÃ©rdida operativa".
    - Guardar la liquidaciÃ³n generada y actualizar el estado de `EventoRecibido` a "procesado".

### Phase 3: Capa de Infraestructura (Infrastructure)
**PropÃ³sito:** Habilitar el consumo de eventos asÃ­ncronos y persistencia de seguimiento.
- [x] **T006:** Implementar el adaptador `EstadoFinalEventConsumer` utilizando `@RequiredArgsConstructor`, mapeando el evento JSON a `ProcesarEstadoFinalCommand` e invocando al caso de uso correspondiente.
- [x] **T007:** Configurar la gestiÃ³n de reintentos y colas de mensajes no procesables (Dead Letter Queue) en las propiedades para manejar fallos de manera resiliente.
- [x] **T008:** Crear adaptadores de persistencia para `EventoRecibido` que permitan la inserciÃ³n y validaciÃ³n de existencia (para el control de duplicados).

---

## Notes
- **Regla estricta:** Solo se crearÃ¡n e implementarÃ¡n los adaptadores y comandos estrictamente necesarios para el evento de transporte detallado. Los repositorios de otras entidades, u otros casos de uso que no tienen que ver con este flujo, no serÃ¡n creados ni forman parte de este desarrollo.
- **InyecciÃ³n de dependencias:** Todos los consumidores de eventos deben estar decorados obligatoriamente con `@RequiredArgsConstructor` de Lombok para garantizar una inyecciÃ³n limpia mediante constructores generados de forma automÃ¡tica.
- **Estructura en prosa:** Toda la lÃ³gica de planificaciÃ³n ha sido descrita de forma narrativa y estructurada, paso por paso, favoreciendo el entendimiento humano y directo como fue requerido.

