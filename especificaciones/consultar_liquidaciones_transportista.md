# Feature Specification: Consultar Liquidaciones del Transportista (WebApp)

**Created:** 13-03-2026  
**Status:** In Development

## Descripción del Flujo

El transportista accede a una interfaz web donde puede consultar sus liquidaciones. El sistema identifica al transportista por su ID y le muestra sus liquidaciones.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de liquidaciones desde WebApp (P1)

Yo como transportista deseo consultar mis liquidaciones desde una interfaz web. Para ver el historial de mis pagos y descargar los comprobantes en PDF.

**Why this priority:** Transparencia de información para el transportista.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de liquidaciones
   - **Given:** El transportista accede a la sección "Mis liquidaciones" en la web
   - **When:** El sistema identifica al transportista por su ID
   - **Then:** Se muestra una lista paginada (20 por página) con los datos de cada liquidación y el enlace al PDF

2. **Scenario:** Transportista sin liquidaciones
   - **Given:** El transportista no tiene liquidaciones asociadas
   - **When:** Accede a la sección "Mis liquidaciones"
   - **Then:** Se muestra un mensaje indicando que no tiene liquidaciones

3. **Scenario:** Navegación entre páginas
   - **Given:** El transportista tiene más de 20 liquidaciones
   - **When:** Navega a la siguiente página
   - **Then:** Se muestran los siguientes 20 registros

4. **Scenario:** Descarga de PDF
   - **Given:** El transportista visualiza sus liquidaciones
   - **When:** Hace clic en el enlace del PDF de una liquidación
   - **Then:** Se descarga el PDF de la liquidación

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE identificar al transportista por su ID.
- **FR-002:** El sistema DEBE mostrar las liquidaciones ordenadas cronológicamente de forma descendente.
- **FR-003:** El sistema DEBE paginar los resultados (20 registros por página por defecto).
- **FR-004:** El sistema DEBE mostrar los detalles de cada liquidación: id_liquidacion, id_pedido, id_transportista, monto_calculado, fecha_liquidacion.

### Key Entities

- **Liquidacion_Transportista:** [id_liquidacion, id_pedido, id_transportista, monto_calculado, fecha_liquidacion]

---

## Success Criteria

- **SC-001:** El sistema debe listar el 100% de las liquidaciones del transportista.
- **SC-002:** El tiempo de respuesta debe ser menor a 3 segundos.
- **SC-003:** El transportista debe poder descargar el PDF en menos de 3 clics.
