# Feature Specification: Consultar Liquidaciones del Transportista (WebApp)

**Created:** 13-03-2026  
**Status:** In Development

## DescripciÃ³n del Flujo

El transportista accede a una interfaz web donde puede consultar sus liquidaciones. El sistema identifica al transportista por su ID y le muestra sus liquidaciones.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de liquidaciones desde WebApp (P1)

Yo como transportista deseo consultar mis liquidaciones desde una interfaz web. Para ver el historial de mis pagos y descargar los comprobantes en PDF.

**Why this priority:** Transparencia de informaciÃ³n para el transportista.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de liquidaciones
   - **Given:** El transportista accede a la secciÃ³n "Mis liquidaciones" en la web
   - **When:** El sistema identifica al transportista por su ID
   - **Then:** Se muestra una lista paginada (20 por pÃ¡gina) con los datos de cada liquidaciÃ³n y el enlace al PDF

2. **Scenario:** Transportista sin liquidaciones
   - **Given:** El transportista no tiene liquidaciones asociadas
   - **When:** Accede a la secciÃ³n "Mis liquidaciones"
   - **Then:** Se muestra un mensaje indicando que no tiene liquidaciones

3. **Scenario:** NavegaciÃ³n entre pÃ¡ginas
   - **Given:** El transportista tiene mÃ¡s de 20 liquidaciones
   - **When:** Navega a la siguiente pÃ¡gina
   - **Then:** Se muestran los siguientes 20 registros


---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE identificar al transportista por su ID.
- **FR-002:** El sistema DEBE mostrar las liquidaciones ordenadas cronolÃ³gicamente de forma descendente.
- **FR-003:** El sistema DEBE paginar los resultados (20 registros por pÃ¡gina por defecto).
- **FR-004:** El sistema DEBE mostrar los detalles de cada liquidaciÃ³n: id_liquidacion, id_pedido, id_transportista, monto_calculado, fecha_liquidacion.

### Key Entities

- **Liquidacion_Transportista:** [id_liquidacion, id_pedido, id_transportista, monto_calculado, fecha_liquidacion]

---

## Success Criteria

- **SC-001:** El sistema debe listar el 100% de las liquidaciones del transportista.
- **SC-002:** El tiempo de respuesta debe ser menor a 3 segundos.
- **SC-003:** El transportista debe poder descargar el PDF en menos de 3 clics.

