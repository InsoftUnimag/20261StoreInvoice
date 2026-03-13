# Feature Specification: Recibir Datos del Pedido desde Módulo de Inventario

**Created:** 12-03-2026  
**Status:** Pendiente definir arquitectura (async/sync)

## Descripción del Flujo

El Módulo de Gestión de Inventario envía los datos del pedido al Sistema Financiero cuando se crea un nuevo pedido. El Sistema Financiero guarda esta información para posteriormente generar la liquidación cuando el Módulo de Transporte reporte el estado final.

> **Pendiente:** Definir si la comunicación será por evento (async) o endpoint (sync).

---

## Datos Esperados del Módulo de Inventario

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id_pedido` | Integer | Sí | ID del pedido |
| `id_cliente` | Integer | Sí | ID del cliente |
| `total_pedido` | Integer | Sí | Valor total del pedido |
| `fecha_despacho` | DateTime | Sí | Fecha de despacho del pedido |
| `direccion` | String | Sí | Dirección de entrega del pedido |

> **Nota:** 
> - Los productos NO se reciben ahora. Se consultarán posteriormente cuando se genere el PDF (al recibir el estado final del Módulo de Transporte).
> - El pedido se recibe cuando ya tiene ruta asignada.

---

## Proceso en Sistema Financiero

1. **Recibir datos**: Se reciben los datos del pedido desde Módulo de Inventario
2. **Consultar forma de pago**: Con el `id_cliente`, se consulta la forma de pago usando la función interna `buscar_forma_pago_por_id_cliente`
3. **Guardar en BD**: Se guarda el registro del pedido con los datos recibidos y la forma de pago asociada
4. **Esperar estado final**: Queda en espera hasta que el Módulo de Transporte envíe el evento

> **Nota:** Los productos se consultan posteriormente cuando se genere el PDF (ver spec `generar_liquidacion_cliente.md`).

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recepción de datos del pedido

**Pendiente:** Definir arquitectura (evento o endpoint)

**Acceptance Scenarios:**

1. **Scenario:** Recepción exitosa de datos del pedido
   - **Given:** El Módulo de Inventario envía datos válidos del pedido
   - **When:** Se reciben los datos
   - **Then:** Se guarda el registro en la BD y se consulta la forma de pago

2. **Scenario:** Cliente sin forma de pago
   - **Given:** El cliente no tiene forma de pago registrada
   - **When:** Se reciben los datos del pedido
   - **Then:** Se rechaza el pedido y se notifica que el cliente no tiene forma de pago

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir los datos del pedido desde el Módulo de Inventario (pendiente definir medio: evento o endpoint).
- **FR-002:** El sistema DEBE validar que el cliente tenga forma de pago registrada.
- **FR-003:** El sistema DEBE guardar el registro del pedido en la base de datos.
- **FR-004:** El sistema DEBE consultar la forma de pago del cliente mediante la función interna `buscar_forma_pago_por_id_cliente`.
- **FR-005:** El sistema DEBE rechazar el pedido si el cliente no tiene forma de pago asignada.

### Key Entities *(include if data)*

**PedidoRecibido:**
- [id_pedido, id_cliente, total_pedido, fecha_despacho, direccion, forma_pago, estado_final, uri_pdf]

> **Nota:** Los productos no se guardan en BD. Se consultan al generar el PDF.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe guardar el pedido en menos de 1 segundo.

- **SC-002:** El sistema debe validar la forma de pago antes de guardar.

- **SC-003:** El sistema debe rechazar pedidos de clientes sin forma de pago.
