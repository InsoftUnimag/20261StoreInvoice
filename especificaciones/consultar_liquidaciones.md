# Feature Specification: Consultar Liquidaciones (Para Contador)

**Created:** 21-02-2026  
**Status:** In Development

## Descripción del Flujo

El contador consulta las liquidaciones generadas desde la interfaz (web/app). El sistema retorna una lista paginada con filtros disponibles y permite descargar el PDF de cada liquidación.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Necesidad del Contador (Negocio)

Yo como contador necesito revisar todas las liquidaciones en la base de datos. Para fines legales y contables.

**Why this priority:** Transparencia de información para auditoría.

**Acceptance Scenarios:**

1. **Scenario:** Consulta general de liquidaciones
   - **Given:** El contador accede al módulo de consultas
   - **When:** Solicita ver todas las liquidaciones
   - **Then:** Se muestra una lista paginada (20 por página) ordenada cronológicamente de forma descendente

2. **Scenario:** Consulta por cliente específico
   - **Given:** El contador conoce el ID nacional del cliente
   - **When:** Filtra por cliente
   - **Then:** Se muestran las liquidaciones de ese cliente

3. **Scenario:** Consulta por rango de fechas
   - **Given:** El contador necesita información de un período contable
   - **When:** Selecciona fecha inicio y fecha fin
   - **Then:** Se muestran las liquidaciones dentro de ese rango

4. **Scenario:** Consulta por cliente + rango de fechas
   - **Given:** El contador necesita liquidaciones de un cliente en un período específico
   - **When:** Filtra por cliente y rango de fechas
   - **Then:** Se muestran las liquidaciones que coinciden con ambos filtros

5. **Scenario:** Cliente sin liquidaciones
   - **Given:** El cliente no tiene liquidaciones asociadas
   - **When:** Se consulta ese cliente
   - **Then:** Se muestra un mensaje indicando que no tiene liquidaciones

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE mostrar todas las liquidaciones ordenadas cronológicamente de forma descendente.
- **FR-002:** El sistema DEBE permitir filtrar por ID nacional del cliente.
- **FR-003:** El sistema DEBE permitir filtrar por rango de fechas (fecha inicio y fecha fin).
- **FR-004:** El sistema DEBE permitir combinar filtros (cliente + fechas).
- **FR-005:** El sistema DEBE paginar los resultados (20 registros por página por defecto).
- **FR-006:** El sistema DEBE mostrar los detalles de cada liquidación: id_liquidacion, id_pedido, id_cliente, total_calculado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf.
- **FR-007:** El sistema DEBE permitir acceder al PDF de cada liquidación mediante la uri_pdf.

### Key Entities *(include if data)*

**Liquidacion_Cliente:**
- [id_liquidacion, id_pedido, id_cliente, precio_pedido, tarifa_envio, total_calculado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf]

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe listar el 100% de las liquidaciones que coincidan con los filtros aplicados.

- **SC-002:** El tiempo de respuesta al consultar liquidaciones debe ser menor a 2 segundos.

- **SC-003:** La consulta por rango de fechas debe incluir liquidaciones desde las 00:00 de la fecha inicial hasta las 23:59 de la fecha final.
