# Implementation Plan: Consultar Liquidaciones del Contador

**Date:** 24-04-2026  
**Spec:** especificaciones/consultar_liquidaciones_contador.md

---

## Summary

El sistema debe proveer una consulta paginada de todas las liquidaciones para fines contables, unificando la información de clientes y transportistas. Los datos retornados deben presentarse en orden cronológico descendente, permitiendo al contador aplicar filtros por tipo, identificador de cliente, identificador de transportista y rangos de fechas.

**Technical Approach:** Creación de una nueva entidad unificada de solo lectura y gestión para el contador. Se generarán los casos de uso respectivos para su mantenimiento (CRUD) respetando estrictamente el principio de responsabilidad única. La delegación se hará a través del controlador usando la inyección de dependencias mediante `@RequiredArgsConstructor`. La persistencia asegurará la paginación y filtrado dinámico directamente en la base de datos.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Lombok, MapStruct  
**Storage:** PostgreSQL  
**Programming Style:** Programación en prosa, favoreciendo la lectura natural sin condicionales anidados. Uso imperativo de la anotación `@RequiredArgsConstructor` para la inyección de dependencias. Cada caso de uso representará una única acción.  
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - Consulta de liquidaciones para fines contables (P1)
Yo como contador necesito revisar las liquidaciones de clientes y transportistas en la base de datos de forma unificada, para fines legales, contables y auditoría.

**Consumo interno:** `GET /api/v1/liquidaciones`

---

## Success Criteria

- **SC-001:** El sistema lista el cien por ciento de las liquidaciones que coincidan con los filtros aplicados.
- **SC-002:** El tiempo de respuesta de la consulta debe mantenerse por debajo de dos segundos.
- **SC-003:** La consulta por rango de fechas incluye rigurosamente desde las cero horas de la fecha inicial hasta el final del día de la fecha final.
- **SC-004:** La respuesta está paginada por defecto en veinte elementos.

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
│                   │   └── model/
│                   │       └── LiquidacionContable.java
│                   ├── application/
│                   │   ├── dto/
│                   │   │   ├── query/
│                   │   │   │   └── ConsultarLiquidacionesQuery.java
│                   │   │   └── response/
│                   │   │       └── LiquidacionContadorResponse.java
│                   │   ├── repository/
│                   │   │   └── LiquidacionContableRepository.java
│                   │   └── service/
│                   │       └── liquidacion/
│                   │           ├── CrearLiquidacionContableUseCase.java
│                   │           ├── ConsultarLiquidacionContableUseCase.java
│                   │           ├── ActualizarLiquidacionContableUseCase.java
│                   │           └── EliminarLiquidacionContableUseCase.java
│                   │
│                   └── infrastructure/
│                       ├── adapter/
│                       │   ├── inbound/
│                       │   │   └── rest/
│                       │   │       └── LiquidacionContableController.java
│                       │   └── outbound/
│                       │       └── persistence/
│                       │           └── LiquidacionContableRepositoryAdapter.java
│                       └── persistence/
│                           └── mapper/
│                               └── LiquidacionContableMapper.java
```

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**Propósito:** Establecer el modelo de dominio unificado para las liquidaciones del contador y sus operaciones de ciclo de vida.
- [ ] **T001:** Crear la entidad `LiquidacionContable` en la capa de modelo de dominio para consolidar la información.
- [ ] **T002:** Asignar a la entidad los atributos necesarios: identificador de liquidación, identificador de pedido, tipo de liquidación, sujeto involucrado, monto, fecha de liquidación y la dirección de recurso uniforme del documento digital.
- [ ] **T003:** Al crear esta nueva entidad, establecer la base para su ciclo de vida respetando el principio de responsabilidad única.

### Phase 2: Capa de Aplicación (Application)
**Propósito:** Estructurar los casos de uso requeridos para la entidad creada y los contratos de persistencia. Se debe utilizar la anotación `@RequiredArgsConstructor` para la inyección de dependencias.
- [ ] **T004:** Implementar el caso de uso `CrearLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para manejar la creación de los registros.
- [ ] **T005:** Implementar el caso de uso `ConsultarLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para gestionar las búsquedas con filtros.
- [ ] **T006:** Implementar el caso de uso `ActualizarLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para la modificación de los registros.
- [ ] **T007:** Implementar el caso de uso `EliminarLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para el borrado de los registros.
- [ ] **T008:** Crear el objeto de transferencia de datos `ConsultarLiquidacionesQuery` para encapsular los filtros de búsqueda (tipo, identificador, fechas).
- [ ] **T009:** Definir el contrato de búsqueda y mantenimiento en la interfaz `LiquidacionContableRepository`.

### Phase 3: Capa de Infraestructura (Infrastructure)
**Propósito:** Habilitar los puntos de acceso de red y garantizar la persistencia de los datos unificados.
- [ ] **T010:** Implementar el adaptador de persistencia `LiquidacionContableRepositoryAdapter` utilizando `@RequiredArgsConstructor`, traduciendo los filtros dinámicos en una consulta estructurada a la base de datos asegurando el orden cronológico descendente.
- [ ] **T011:** Crear el recurso de red `LiquidacionContableController` inyectando los casos de uso mediante la anotación `@RequiredArgsConstructor`.
- [ ] **T012:** Exponer la ruta para el contador y recibir los parámetros de consulta de página, límite y filtros asociados.
- [ ] **T013:** Asignar mediante anotaciones en la definición de la ruta el valor predeterminado de veinte para el tamaño de la página, evitando la validación manual o condicional.
- [ ] **T014:** Mapear el modelo de dominio devuelto por los casos de uso utilizando `LiquidacionContableMapper` hacia la estructura de respuesta definida.

---

## Notes
- **Regla estricta:** Al haberse creado la entidad `LiquidacionContable`, se han definido sus cuatro casos de uso (CRUD) de manera independiente (`Crear`, `Consultar`, `Actualizar` y `Eliminar`), cumpliendo estrictamente con el principio de responsabilidad única.
- **Inyección de dependencias:** Todos los servicios, adaptadores y controladores deben estar decorados obligatoriamente con `@RequiredArgsConstructor` de Lombok para garantizar una inyección limpia mediante constructores generados de forma automática.
- **Estructura en prosa:** Toda la lógica de planificación ha sido descrita de forma narrativa y estructurada, paso por paso, favoreciendo el entendimiento humano y directo como fue requerido.
