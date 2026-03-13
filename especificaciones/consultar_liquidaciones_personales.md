# Feature Specification: Consultar Liquidaciones Personales del Cliente

**Created:** 21-02-2026  
**Status:** In Development


## User Scenarios & Testing *(mandatory)*

### User Story 1 - Necesidad del Cliente (P1)

Yo como cliente deseo consultar mis liquidaciones personales en una lista paginada. Para tener información de mis pagos y poder descargar el PDF de cada liquidación.

**Why this priority:** Transparencia de información para el cliente.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de liquidaciones
   - **Given:** Usuario autenticado; Existen liquidaciones generadas para el cliente
   - **When:** El cliente accede a "Mis liquidaciones"
   - **Then:** Se muestra una lista paginada (20 registros por página) ordenada cronológicamente de forma descendente, con los datos de cada liquidación y el enlace al PDF

2. **Scenario:** Cliente sin liquidaciones
   - **Given:** Cliente sin liquidaciones asociadas
   - **When:** El cliente consulta sus liquidaciones
   - **Then:** Se muestra un mensaje indicando que no tiene liquidaciones

3. **Scenario:** Navegación entre páginas
   - **Given:** El cliente tiene más de 20 liquidaciones
   - **When:** El cliente navega a la siguiente página
   - **Then:** Se muestran los siguientes 20 registros

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE mostrar las liquidaciones del cliente ordenadas cronológicamente de forma descendente.
- **FR-002:** El sistema DEBE paginar los resultados (20 registros por página por defecto).
- **FR-003:** El sistema DEBE mostrar los detalles de cada liquidación: id_liquidacion, id_pedido, total_calculado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf.
- **FR-004:** El sistema DEBE mostrar un mensaje cuando el cliente no tenga liquidaciones asociadas.

### Key Entities *(include if data)*

**Liquidacion_Cliente:**
- [id_liquidacion, id_pedido, id_cliente, precio_pedido, tarifa_envio, total_calculado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf]

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe listar el 100% de las liquidaciones asociadas al cliente.

- **SC-002:** El tiempo de respuesta al consultar "Mis liquidaciones" debe ser menor a 3 segundos.

- **SC-003:** El 90% de los clientes debe poder acceder al PDF de su liquidación en menos de 3 clics.

- **SC-004:** El PDF descargado debe contener los mismos datos que se muestran en la interfaz web.
