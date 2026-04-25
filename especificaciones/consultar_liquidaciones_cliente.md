# Feature Specification: Consultar Liquidaciones del Cliente (WebApp)

**Created:** 21-02-2026  
**Status:** In Development

## DescripciÃ³n del Flujo

El cliente accede a una interfaz web donde puede consultar sus liquidaciones. El sistema identifica al cliente por su ID de cliente y le muestra sus liquidaciones.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de liquidaciones desde WebApp (P1)

Yo como cliente deseo consultar mis liquidaciones desde una interfaz web. Para ver el historial de mis liquidaciones y descargar los comprobantes en PDF.

**Why this priority:** Transparencia de informaciÃ³n para el cliente.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de liquidaciones
   - **Given:** El cliente accede a la secciÃ³n "Mis liquidaciones" en la web
   - **When:** El sistema identifica al cliente por su ID
   - **Then:** Se muestra una lista paginada (20 por pÃ¡gina) con los datos de cada liquidaciÃ³n y el enlace al PDF

2. **Scenario:** Cliente sin liquidaciones
   - **Given:** El cliente no tiene liquidaciones asociadas
   - **When:** Accede a la secciÃ³n "Mis liquidaciones"
   - **Then:** Se muestra un mensaje indicando que no tiene liquidaciones

3. **Scenario:** NavegaciÃ³n entre pÃ¡ginas
   - **Given:** El cliente tiene mÃ¡s de 20 liquidaciones
   - **When:** Navega a la siguiente pÃ¡gina
   - **Then:** Se muestran los siguientes 20 registros

4. **Scenario:** Descarga de PDF
   - **Given:** El cliente visualiza sus liquidaciones
   - **When:** Hace clic en el enlace del PDF de una liquidaciÃ³n
   - **Then:** Se descarga el PDF de la liquidaciÃ³n

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE identificar al cliente por su ID de cliente.
- **FR-002:** El sistema DEBE mostrar las liquidaciones ordenadas cronolÃ³gicamente de forma descendente.
- **FR-003:** El sistema DEBE paginar los resultados (20 registros por pÃ¡gina por defecto).
- **FR-004:** El sistema DEBE mostrar los detalles de cada liquidaciÃ³n: idLiquidacion, idPedido, idCliente, formaPago, estadoLiquidacion, fechaLiquidacion, uriPdf, montoLiquidado.
- **FR-005:** El sistema DEBE permitir acceder al PDF mediante la uriPdf.

### Key Entities

- **Liquidacion_Cliente:** [idLiquidacion, idPedido, idCliente, formaPago, estadoLiquidacion, fechaLiquidacion, uriPdf, montoLiquidado]

### Enums (Domain)

#### FormaPago
```
- CONTRA_ENTREGA: Pago al momento de la entrega
- CARTERA_COMERCIAL: Pago a cuenta del comercio
```

#### EstadoLiquidacion
```
- PENDIENTE: Liquidacion generada, pendiente de pago
- PAGADA: Liquidacion pagada al cliente
- CANCELADA: Liquidacion cancelada
```

---

## Success Criteria

- **SC-001:** El sistema debe listar el 100% de las liquidaciones del cliente.
- **SC-002:** El tiempo de respuesta debe ser menor a 3 segundos.
- **SC-003:** El cliente debe poder descargar el PDF en menos de 3 clics.

