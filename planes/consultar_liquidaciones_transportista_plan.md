# Implementation Plan: Consultar Liquidaciones del Transportista

**Date:** 24-04-2026  
**Spec:** especificaciones/consultar_liquidaciones_transportista.md

---

## Summary

El sistema debe proveer una consulta paginada de liquidaciones para un transportista identificÃ¡ndolo de manera Ãºnica por su identificador personal. Los datos retornados deben presentarse en orden cronolÃ³gico descendente.

**Technical Approach:** ModificaciÃ³n del controlador existente de liquidaciones para abrir un nuevo recurso de red, delegando en un caso de uso puro. La persistencia debe asegurar la ordenaciÃ³n y paginaciÃ³n de manera directa en la consulta de base de datos sin lÃ³gica condicional.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Lombok, MapStruct  
**Storage:** PostgreSQL  
**Programming Style:** ProgramaciÃ³n en prosa, evitando el uso de condicionales de ramificaciÃ³n. Uso de inyecciÃ³n de dependencias. Valores por defecto manejados mediante anotaciones.  
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
- **SC-003:** La informaciÃ³n muestra los datos del transportista, pedido, monto y fechas.
- **SC-004:** La respuesta estÃ¡ paginada por defecto en veinte elementos.

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
â”‚                   â”‚   â””â”€â”€ model/
â”‚                   â”‚       â””â”€â”€ LiquidacionTransportista.java
â”‚                   â”‚
â”‚                   â”œâ”€â”€ application/
â”‚                   â”‚   â”œâ”€â”€ dto/
â”‚                   â”‚   â”‚   â””â”€â”€ response/
â”‚                   â”‚   â”‚       â””â”€â”€ LiquidacionTransportistaResponse.java
â”‚                   â”‚   â”œâ”€â”€ repository/
â”‚                   â”‚   â”‚   â””â”€â”€ LiquidacionRepository.java
â”‚                   â”‚   â””â”€â”€ service/
â”‚                   â”‚       â””â”€â”€ liquidacion/
â”‚                   â”‚           â””â”€â”€ ConsultarLiquidacionesTransportistaUseCase.java
â”‚                   â”‚
â”‚                   â””â”€â”€ infrastructure/
â”‚                       â”œâ”€â”€ adapter/
â”‚                       â”‚   â”œâ”€â”€ inbound/
â”‚                       â”‚   â”‚   â””â”€â”€ rest/
â”‚                       â”‚   â”‚       â””â”€â”€ LiquidacionController.java
â”‚                       â”‚   â””â”€â”€ outbound/
â”‚                       â”‚       â””â”€â”€ persistence/
â”‚                       â”‚           â””â”€â”€ LiquidacionRepositoryAdapter.java
â”‚                       â””â”€â”€ persistence/
â”‚                           â””â”€â”€ mapper/
â”‚                               â””â”€â”€ LiquidacionEntityMapper.java
```

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**PropÃ³sito:** Establecer el modelo de dominio principal para las liquidaciones del transportista.
- [ ] **T001:** Crear la clase `LiquidacionTransportista` en la capa de modelo de dominio.
- [ ] **T002:** Asignar los atributos identificador, identificador de pedido, identificador de transportista, monto calculado y fecha de liquidaciÃ³n.

### Phase 2: Capa de AplicaciÃ³n (Application)
**PropÃ³sito:** Estructurar el caso de uso y el contrato de persistencia.
- [ ] **T003:** Crear el registro `LiquidacionTransportistaResponse` para definir el contrato de salida de la aplicaciÃ³n.
- [ ] **T004:** Agregar el contrato de bÃºsqueda en la interfaz `LiquidacionRepository`. Este debe recibir el identificador del transportista, pÃ¡gina y lÃ­mite de elementos.
- [ ] **T005:** Crear la clase `ConsultarLiquidacionesTransportistaUseCase` y en su mÃ©todo principal ejecutar la delegaciÃ³n de la consulta al repositorio inyectado.

### Phase 3: Capa de Infraestructura (Infrastructure)
**PropÃ³sito:** Habilitar el punto de acceso de red y garantizar la persistencia.
- [ ] **T006:** Implementar la bÃºsqueda en `LiquidacionRepositoryAdapter`, asegurando el orden cronolÃ³gico descendente mediante la definiciÃ³n estricta de la consulta a la base de datos.
- [ ] **T007:** Modificar el recurso de red en `LiquidacionController`.
- [ ] **T008:** Exponer la ruta para el transportista y recibir los parÃ¡metros de consulta de pÃ¡gina y tamaÃ±o de pÃ¡gina.
- [ ] **T009:** Asignar mediante anotaciones en la definiciÃ³n de la ruta el valor predeterminado de veinte para el tamaÃ±o de la pÃ¡gina.
- [ ] **T010:** Mapear el modelo de dominio devuelto por el caso de uso utilizando `LiquidacionEntityMapper` hacia el registro de respuesta.

---

## Notes
- **Regla estricta:** Evitar el uso de condicionales. El manejo de valores por defecto como la paginaciÃ³n debe realizarse en las anotaciones del controlador en lugar de validar variables.
- **Estructura en prosa:** Toda la lÃ³gica ha sido planeada de forma descriptiva paso por paso, favoreciendo la lectura natural y estructurada como se solicita.

