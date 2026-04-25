# Implementation Plan: Recepción de Estado Final y Generación de Liquidación de Transportista

**Date:** 24-04-2026  
**Spec:** especificaciones/recibir_estado_final_modulo_transporte.md, especificaciones/generar_liquidacion_de_transportista.md

---

## Summary

El flujo inicia de manera asíncrona cuando el Módulo de Transporte emite un evento indicando el estado final de una entrega. Este evento es capturado por un adaptador de entrada (Consumer) en la capa de infraestructura del Sistema Financiero. Una vez recibido, se registrará el evento para garantizar su seguimiento e idempotencia. Luego, se consultará el precio del pedido previamente guardado, se aplicarán las reglas de negocio para calcular el pago al transportista (con su respectivo redondeo y reglas de excepción como pérdidas operativas), y se generará y guardará la entidad correspondiente, actualizando finalmente el estado del evento.

**Technical Approach:** Se implementará la lógica de cálculo puro en la capa de dominio sin dependencias externas, introduciendo la entidad de seguimiento de eventos. Se generarán los casos de uso respectivos para la gestión de liquidaciones de transportistas (CRUD) y procesamiento de eventos respetando estrictamente el principio de responsabilidad única. La delegación se hará a través del consumidor de eventos y la inyección de dependencias utilizará `@RequiredArgsConstructor`. La persistencia asegurará que se guarde correctamente cada liquidación procesada y el historial de procesamiento de eventos.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Spring Cloud Stream, Lombok, MapStruct  
**Storage:** PostgreSQL  
**Programming Style:** Programación en prosa, favoreciendo la lectura natural sin condicionales anidados. Uso imperativo de la anotación `@RequiredArgsConstructor` para la inyección de dependencias. Cada caso de uso representará una única acción.  
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - Cálculo automatizado de liquidación según tasa de efectividad (P1)
Yo como Sistema Financiero necesito calcular el monto a pagar al transportista tomando como base el 10% del precio total del pedido y aplicando la tasa de efectividad recibida del Módulo de Gestión de Transporte, para generar una liquidación confiable.

**Consumo interno:** Evento asíncrono vía `Spring Cloud Stream` (RabbitMQ).

---

## Success Criteria

- **SC-001:** El cien por ciento de los datos válidos recibidos deben ser procesados.
- **SC-002:** El procesamiento de cada evento debe completarse en menos de cinco segundos.
- **SC-003:** El cien por ciento de las liquidaciones generadas reflejan exactamente el cálculo de la tarifa base por la tasa de efectividad.
- **SC-004:** El sistema maneja errores de manera que puedan ser reintentados y ninguna liquidación se genera si los datos son inválidos o nulos.

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
│                   │   ├── entity/
│                   │   │   └── EventoRecibido.java
│                   │   └── valueobject/
│                   │       └── TasaEfectividad.java
│                   ├── application/
│                   │   └── dto/
│                   │       └── command/
│                   │           └── ProcesarEstadoFinalCommand.java
│                   └── infrastructure/
│                       └── adapter/
│                           └── inbound/
│                               └── messaging/
│                                   └── EstadoFinalEventConsumer.java
```

*(Nota: Solo se listan los archivos nuevos a crear en esta especificación. Los archivos existentes a modificar, como `LiquidacionTransportista` y `CrearLiquidacionTransportistaUseCase`, no se incluyen en el árbol para mantener la claridad de los entregables nuevos).*

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**Propósito:** Establecer las reglas de negocio puras, seguimiento de eventos y la fórmula matemática.
- [ ] **T001:** Crear el Value Object `TasaEfectividad` para validar que los valores estén dentro del rango permitido (-100 a 100).
- [ ] **T002:** Actualizar la entidad existente `LiquidacionTransportista` en la capa de modelo de dominio para añadir el método de cálculo que obtenga el monto aplicando la fórmula `(Precio Pedido × 10%) × (tasa_efectividad / 100)`, aplicando redondeo al número entero más cercano.
- [ ] **T003:** Crear la entidad `EventoRecibido` (con campos: `id_pedido`, `tasa_efectividad`, `id_transportista`, `status`, `fecha_recibido`, `fecha_procesado`) para rastrear el procesamiento de cada estado final y permitir manejo de duplicados.

### Phase 2: Capa de Aplicación (Application)
**Propósito:** Procesar la orden de generar la liquidación, garantizando validaciones de negocio e idempotencia.
- [ ] **T004:** Crear el objeto de transferencia de datos `ProcesarEstadoFinalCommand` en `application/dto/command/` para encapsular los datos de entrada validados provenientes del evento.
- [ ] **T005:** Actualizar/crear el caso de uso `ProcesarEstadoFinalUseCase` (o refactorizar `CrearLiquidacionTransportistaUseCase`) que orqueste lo siguiente:
    - Consultar si el `id_pedido` ya existe en `EventoRecibido` para prevenir eventos duplicados (Idempotencia).
    - Registrar el inicio del evento como "pendiente".
    - Consultar el precio del pedido y bloquear la operación si el precio es nulo o estrictamente 0.
    - Delegar a la entidad `LiquidacionTransportista` el cálculo del monto.
    - Si la `tasa_efectividad` es 0%, registrar el costo del flete en un reporte de "pérdida operativa".
    - Guardar la liquidación generada y actualizar el estado de `EventoRecibido` a "procesado".

### Phase 3: Capa de Infraestructura (Infrastructure)
**Propósito:** Habilitar el consumo de eventos asíncronos y persistencia de seguimiento.
- [ ] **T006:** Implementar el adaptador `EstadoFinalEventConsumer` utilizando `@RequiredArgsConstructor`, mapeando el evento JSON a `ProcesarEstadoFinalCommand` e invocando al caso de uso correspondiente.
- [ ] **T007:** Configurar la gestión de reintentos y colas de mensajes no procesables (Dead Letter Queue) en las propiedades para manejar fallos de manera resiliente.
- [ ] **T008:** Crear adaptadores de persistencia para `EventoRecibido` que permitan la inserción y validación de existencia (para el control de duplicados).

---

## Notes
- **Regla estricta:** Solo se crearán e implementarán los adaptadores y comandos estrictamente necesarios para el evento de transporte detallado. Los repositorios de otras entidades, u otros casos de uso que no tienen que ver con este flujo, no serán creados ni forman parte de este desarrollo.
- **Inyección de dependencias:** Todos los consumidores de eventos deben estar decorados obligatoriamente con `@RequiredArgsConstructor` de Lombok para garantizar una inyección limpia mediante constructores generados de forma automática.
- **Estructura en prosa:** Toda la lógica de planificación ha sido descrita de forma narrativa y estructurada, paso por paso, favoreciendo el entendimiento humano y directo como fue requerido.
