# Feature Specification: Consultar Liquidaciones (Para Contador)

**Created:** 21-02-2026  
**Status:** In Development

## Descripción del Flujo

El contador consulta las liquidaciones de clientes y transportistas desde una interfaz. El sistema retorna una lista paginada con filtros disponibles y permite descargar el PDF de cada liquidación.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de liquidaciones para fines contables (P1)

Yo como contador necesito revisar las liquidaciones de clientes y transportistas en la base de datos. Para fines legales, contables y auditoría.

**Why this priority:** Transparencia de información para auditoría contable.

**Acceptance Scenarios:**

1. **Scenario:** Consulta general de todas las liquidaciones
   - **Given:** El contador accede al módulo de consultas
   - **When:** Solicita ver todas las liquidaciones
   - **Then:** Se muestra una lista paginada (20 por página) ordenada cronológicamente de forma descendente

2. **Scenario:** Consulta de liquidaciones de clientes
   - **Given:** El contador necesita solo las liquidaciones de clientes
   - **When:** Filtra por tipo "cliente"
   - **Then:** Se muestran solo las liquidaciones de clientes

3. **Scenario:** Consulta de liquidaciones de transportistas
   - **Given:** El contador necesita solo las liquidaciones de transportistas
   - **When:** Filtra por tipo "transportista"
   - **Then:** Se muestran solo las liquidaciones de transportistas

4. **Scenario:** Consulta por cliente específico
   - **Given:** El contador conoce el ID nacional del cliente
   - **When:** Filtra por cliente
   - **Then:** Se muestran las liquidaciones de ese cliente

5. **Scenario:** Consulta por transportista específico
   - **Given:** El contador conoce el ID del transportista
   - **When:** Filtra por transportista
   - **Then:** Se muestran las liquidaciones de ese transportista

6. **Scenario:** Consulta por rango de fechas
   - **Given:** El contador necesita información de un período contable
   - **When:** Selecciona fecha inicio y fecha fin
   - **Then:** Se muestran las liquidaciones dentro de ese rango

7. **Scenario:** Consulta combinada (tipo + rango de fechas)
   - **Given:** El contador necesita liquidaciones de un tipo específico en un período
   - **When:** Filtra por tipo y rango de fechas
   - **Then:** Se muestran las liquidaciones que coinciden con ambos filtros

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE mostrar todas las liquidaciones (cliente y transportista) ordenadas cronológicamente de forma descendente.
- **FR-002:** El sistema DEBE permitir filtrar por tipo de liquidación: "cliente", "transportista" o "todos".
- **FR-003:** El sistema DEBE permitir filtrar por ID de cliente (ID de BD).
- **FR-004:** El sistema DEBE permitir filtrar por ID del transportista.
- **FR-005:** El sistema DEBE permitir filtrar por rango de fechas (fecha inicio y fecha fin).
- **FR-006:** El sistema DEBE permitir combinar filtros (tipo + cliente/transportista + fechas).
- **FR-007:** El sistema DEBE paginar los resultados (20 registros por página por defecto).
- **FR-008:** El sistema DEBE mostrar los detalles de cada liquidación dependiendo del tipo.
- **FR-009:** El sistema DEBE permitir acceder al PDF de cada liquidación mediante la uri_pdf.

### Detalles de Liquidaciones por Tipo

**Liquidación de Cliente:**
- id_liquidacion, id_pedido, id_cliente, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf

**Liquidación de Transportista:**
- id_liquidacion, id_pedido, id_transportista, monto_calculado, fecha_liquidacion, uri_pdf

---

## Success Criteria

- **SC-001:** El sistema debe listar el 100% de las liquidaciones que coincidan con los filtros aplicados.
- **SC-002:** El tiempo de respuesta debe ser menor a 2 segundos.
- **SC-003:** La consulta por rango de fechas debe incluir liquidaciones desde las 00:00 de la fecha inicial hasta las 23:59 de la fecha final.
