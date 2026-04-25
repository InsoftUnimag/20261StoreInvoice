# Implementation Plan: Consultar Liquidaciones del Contador

**Date:** 24-04-2026  
**Spec:** especificaciones/consultar_liquidaciones_contador.md

---

## Summary

El sistema debe proveer una consulta paginada de todas las liquidaciones para fines contables, unificando la informaciÃ³n de clientes y transportistas. Los datos retornados deben presentarse en orden cronolÃ³gico descendente, permitiendo al contador aplicar filtros por tipo, identificador de cliente, identificador de transportista y rangos de fechas.

**Technical Approach:** CreaciÃ³n de una nueva entidad unificada de solo lectura y gestiÃ³n para el contador. Se generarÃ¡n los casos de uso respectivos para su mantenimiento (CRUD) respetando estrictamente el principio de responsabilidad Ãºnica. La delegaciÃ³n se harÃ¡ a travÃ©s del controlador usando la inyecciÃ³n de dependencias mediante `@RequiredArgsConstructor`. La persistencia asegurarÃ¡ la paginaciÃ³n y filtrado dinÃ¡mico directamente en la base de datos.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring Data JPA, Lombok, MapStruct  
**Storage:** PostgreSQL  
**Programming Style:** ProgramaciÃ³n en prosa, favoreciendo la lectura natural sin condicionales anidados. Uso imperativo de la anotaciÃ³n `@RequiredArgsConstructor` para la inyecciÃ³n de dependencias. Cada caso de uso representarÃ¡ una Ãºnica acciÃ³n.  
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - Consulta de liquidaciones para fines contables (P1)
Yo como contador necesito revisar las liquidaciones de clientes y transportistas en la base de datos de forma unificada, para fines legales, contables y auditorÃ­a.

**Consumo interno:** `GET /api/v1/liquidaciones`

---

## Success Criteria

- **SC-001:** El sistema lista el cien por ciento de las liquidaciones que coincidan con los filtros aplicados.
- **SC-002:** El tiempo de respuesta de la consulta debe mantenerse por debajo de dos segundos.
- **SC-003:** La consulta por rango de fechas incluye rigurosamente desde las cero horas de la fecha inicial hasta el final del dÃ­a de la fecha final.
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
â”‚                   â”‚       â””â”€â”€ LiquidacionContable.java
â”‚                   â”œâ”€â”€ application/
â”‚                   â”‚   â”œâ”€â”€ dto/
â”‚                   â”‚   â”‚   â”œâ”€â”€ query/
â”‚                   â”‚   â”‚   â”‚   â””â”€â”€ ConsultarLiquidacionesQuery.java
â”‚                   â”‚   â”‚   â””â”€â”€ response/
â”‚                   â”‚   â”‚       â””â”€â”€ LiquidacionContadorResponse.java
â”‚                   â”‚   â”œâ”€â”€ repository/
â”‚                   â”‚   â”‚   â””â”€â”€ LiquidacionContableRepository.java
â”‚                   â”‚   â””â”€â”€ service/
â”‚                   â”‚       â””â”€â”€ liquidacion/
â”‚                   â”‚           â”œâ”€â”€ CrearLiquidacionContableUseCase.java
â”‚                   â”‚           â”œâ”€â”€ ConsultarLiquidacionContableUseCase.java
â”‚                   â”‚           â”œâ”€â”€ ActualizarLiquidacionContableUseCase.java
â”‚                   â”‚           â””â”€â”€ EliminarLiquidacionContableUseCase.java
â”‚                   â”‚
â”‚                   â””â”€â”€ infrastructure/
â”‚                       â”œâ”€â”€ adapter/
â”‚                       â”‚   â”œâ”€â”€ inbound/
â”‚                       â”‚   â”‚   â””â”€â”€ rest/
â”‚                       â”‚   â”‚       â””â”€â”€ LiquidacionContableController.java
â”‚                       â”‚   â””â”€â”€ outbound/
â”‚                       â”‚       â””â”€â”€ persistence/
â”‚                       â”‚           â””â”€â”€ LiquidacionContableRepositoryAdapter.java
â”‚                       â””â”€â”€ persistence/
â”‚                           â””â”€â”€ mapper/
â”‚                               â””â”€â”€ LiquidacionContableMapper.java
```

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**PropÃ³sito:** Establecer el modelo de dominio unificado para las liquidaciones del contador y sus operaciones de ciclo de vida.
- [ ] **T001:** Crear la entidad `LiquidacionContable` en la capa de modelo de dominio para consolidar la informaciÃ³n.
- [ ] **T002:** Asignar a la entidad los atributos necesarios: identificador de liquidaciÃ³n, identificador de pedido, tipo de liquidaciÃ³n, sujeto involucrado, monto, fecha de liquidaciÃ³n y la direcciÃ³n de recurso uniforme del documento digital.
- [ ] **T003:** Al crear esta nueva entidad, establecer la base para su ciclo de vida respetando el principio de responsabilidad Ãºnica.

### Phase 2: Capa de AplicaciÃ³n (Application)
**PropÃ³sito:** Estructurar los casos de uso requeridos para la entidad creada y los contratos de persistencia. Se debe utilizar la anotaciÃ³n `@RequiredArgsConstructor` para la inyecciÃ³n de dependencias.
- [ ] **T004:** Implementar el caso de uso `CrearLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para manejar la creaciÃ³n de los registros.
- [ ] **T005:** Implementar el caso de uso `ConsultarLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para gestionar las bÃºsquedas con filtros.
- [ ] **T006:** Implementar el caso de uso `ActualizarLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para la modificaciÃ³n de los registros.
- [ ] **T007:** Implementar el caso de uso `EliminarLiquidacionContableUseCase` anotado con `@RequiredArgsConstructor` para el borrado de los registros.
- [ ] **T008:** Crear el objeto de transferencia de datos `ConsultarLiquidacionesQuery` para encapsular los filtros de bÃºsqueda (tipo, identificador, fechas).
- [ ] **T009:** Definir el contrato de bÃºsqueda y mantenimiento en la interfaz `LiquidacionContableRepository`.

### Phase 3: Capa de Infraestructura (Infrastructure)
**PropÃ³sito:** Habilitar los puntos de acceso de red y garantizar la persistencia de los datos unificados.
- [ ] **T010:** Implementar el adaptador de persistencia `LiquidacionContableRepositoryAdapter` utilizando `@RequiredArgsConstructor`, traduciendo los filtros dinÃ¡micos en una consulta estructurada a la base de datos asegurando el orden cronolÃ³gico descendente.
- [ ] **T011:** Crear el recurso de red `LiquidacionContableController` inyectando los casos de uso mediante la anotaciÃ³n `@RequiredArgsConstructor`.
- [ ] **T012:** Exponer la ruta para el contador y recibir los parÃ¡metros de consulta de pÃ¡gina, lÃ­mite y filtros asociados.
- [ ] **T013:** Asignar mediante anotaciones en la definiciÃ³n de la ruta el valor predeterminado de veinte para el tamaÃ±o de la pÃ¡gina, evitando la validaciÃ³n manual o condicional.
- [ ] **T014:** Mapear el modelo de dominio devuelto por los casos de uso utilizando `LiquidacionContableMapper` hacia la estructura de respuesta definida.

---

## Notes
- **Regla estricta:** Al haberse creado la entidad `LiquidacionContable`, se han definido sus cuatro casos de uso (CRUD) de manera independiente (`Crear`, `Consultar`, `Actualizar` y `Eliminar`), cumpliendo estrictamente con el principio de responsabilidad Ãºnica.
- **InyecciÃ³n de dependencias:** Todos los servicios, adaptadores y controladores deben estar decorados obligatoriamente con `@RequiredArgsConstructor` de Lombok para garantizar una inyecciÃ³n limpia mediante constructores generados de forma automÃ¡tica.
- **Estructura en prosa:** Toda la lÃ³gica de planificaciÃ³n ha sido descrita de forma narrativa y estructurada, paso por paso, favoreciendo el entendimiento humano y directo como fue requerido.

