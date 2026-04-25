# Implementation Plan: Consultar Liquidaciones del Transportista

**Date:** 24-04-2026  
**Spec:** especificaciones/consultar_liquidaciones_transportista.md

---

## Summary

El sistema debe proveer una consulta paginada de liquidaciones para un transportista identificándolo de manera única por su identificador personal. Los datos retornados deben presentarse en orden cronológico descendente.

**Technical Approach:** Modificación del controlador existente de liquidaciones para abrir un nuevo recurso de red, delegando en un caso de uso puro. La persistencia debe asegurar la ordenación y paginación de manera directa en la consulta de base de datos sin lógica condicional.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Lombok, MapStruct  
**Storage:** PostgreSQL  
**Programming Style:** Programación en prosa, evitando el uso de condicionales de ramificación. Uso de inyección de dependencias. Valores por defecto manejados mediante anotaciones.  
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - Consulta de liquidaciones (Priority: P1)
Yo como transportista necesito consultar mis liquidaciones de forma paginada para poder revisar mis ganancias.

**Consumo interno:** `GET /api/v1/transportistas/{id_transportista}/liquidaciones`

---

## Success Criteria

- **SC-001:** El sistema lista el cien por ciento de las liquidaciones para el transportista especificado.
- **SC-002:** El tiempo de respuesta de la consulta debe mantenerse por debajo de tres segundos.
- **SC-003:** La información muestra los datos del transportista, pedido, monto y fechas.
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
│                   │       └── LiquidacionTransportista.java
│                   │
│                   ├── application/
│                   │   ├── dto/
│                   │   │   └── response/
│                   │   │       └── LiquidacionTransportistaResponse.java
│                   │   ├── repository/
│                   │   │   └── LiquidacionRepository.java
│                   │   └── service/
│                   │       └── liquidacion/
│                   │           └── ConsultarLiquidacionesTransportistaUseCase.java
│                   │
│                   └── infrastructure/
│                       ├── adapter/
│                       │   ├── inbound/
│                       │   │   └── rest/
│                       │   │       └── LiquidacionController.java
│                       │   └── outbound/
│                       │       └── persistence/
│                       │           └── LiquidacionRepositoryAdapter.java
│                       └── persistence/
│                           └── mapper/
│                               └── LiquidacionEntityMapper.java
```

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**Propósito:** Establecer el modelo de dominio principal para las liquidaciones del transportista.
- [ ] **T001:** Crear la clase `LiquidacionTransportista` en la capa de modelo de dominio.
- [ ] **T002:** Asignar los atributos identificador, identificador de pedido, identificador de transportista, monto calculado y fecha de liquidación.

### Phase 2: Capa de Aplicación (Application)
**Propósito:** Estructurar el caso de uso y el contrato de persistencia.
- [ ] **T003:** Crear el registro `LiquidacionTransportistaResponse` para definir el contrato de salida de la aplicación.
- [ ] **T004:** Agregar el contrato de búsqueda en la interfaz `LiquidacionRepository`. Este debe recibir el identificador del transportista, página y límite de elementos.
- [ ] **T005:** Crear la clase `ConsultarLiquidacionesTransportistaUseCase` y en su método principal ejecutar la delegación de la consulta al repositorio inyectado.

### Phase 3: Capa de Infraestructura (Infrastructure)
**Propósito:** Habilitar el punto de acceso de red y garantizar la persistencia.
- [ ] **T006:** Implementar la búsqueda en `LiquidacionRepositoryAdapter`, asegurando el orden cronológico descendente mediante la definición estricta de la consulta a la base de datos.
- [ ] **T007:** Modificar el recurso de red en `LiquidacionController`.
- [ ] **T008:** Exponer la ruta para el transportista y recibir los parámetros de consulta de página y tamaño de página.
- [ ] **T009:** Asignar mediante anotaciones en la definición de la ruta el valor predeterminado de veinte para el tamaño de la página.
- [ ] **T010:** Mapear el modelo de dominio devuelto por el caso de uso utilizando `LiquidacionEntityMapper` hacia el registro de respuesta.

---

## Notes
- **Regla estricta:** Evitar el uso de condicionales. El manejo de valores por defecto como la paginación debe realizarse en las anotaciones del controlador en lugar de validar variables.
- **Estructura en prosa:** Toda la lógica ha sido planeada de forma descriptiva paso por paso, favoreciendo la lectura natural y estructurada como se solicita.
