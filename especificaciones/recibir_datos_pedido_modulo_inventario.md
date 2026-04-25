# Feature Specification: Recibir Datos del Pedido desde Módulo de Inventario

**Created:** 12-03-2026  
**Comunicación:** asíncrona usando una cola para almacenar pedidos.

---

## Descripción del Flujo

El Módulo de Gestión de Inventario envía los datos del pedido al Sistema Financiero cuando se crea un nuevo pedido. El Sistema Financiero guarda esta información para posteriormente generar la liquidación cuando el Módulo de Transporte reporte el estado final.

---

## Datos Esperados del Módulo de Inventario

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id_pedido` | Integer | Sí | ID del pedido |
| `id_cliente` | Integer | Sí | ID del cliente |
| `total_pedido` | Integer | Sí | Valor total del pedido |


> **Nota:** 
> - Los productos NO se reciben ahora. Se consultarán posteriormente cuando se genere el PDF (al recibir el estado final del Módulo de Transporte).
> - El campo `direccion` se recibe pero **no se persiste** en la base de datos de finanzas.
> - Los datos del pedido **no se guardan en una tabla de pedidos**; se registran directamente en la tabla `liquidaciones_cliente`.

---

## Proceso en Sistema Financiero

1. **Recibir datos**: Se reciben los datos del pedido desde Módulo de Inventario (vía cola asíncrona)
2. **Consultar forma de pago**: Con el `id_cliente`, se consulta la forma de pago usando la función interna `buscar_forma_pago_por_id_cliente`
3. **Guardar en BD**: Se crea un registro en **`liquidaciones_cliente`** con los datos recibidos y la forma de pago asociada:
   - `id_pedido` ← `id_pedido` del mensaje
   - `id_cliente` ← `id_cliente` del mensaje
   - `monto_liquidado` ← `total_pedido` del mensaje
   - `forma_pago` ← forma de pago consultada
   - `estado_liquidacion` ← `PENDIENTE`
   - `fecha_liquidacion` ← fecha actual
   - `uri_pdf` ← `null` (se genera al recibir estado final)
4. **Esperar estado final**: La liquidación queda en estado `PENDIENTE` hasta que el Módulo de Transporte envíe el evento

> **Nota:** Los productos se consultan posteriormente cuando se genere el PDF (ver spec `generar_liquidacion_cliente.md`).

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recepción de datos del pedido (Priority: P1)

**Endpoint:** Evento/Cola (asíncrono)

**Como Sistema financiero**, requiero saber detalles de un pedido para usar los datos en la liquidación final, por tal motivo es primordial recibir los datos de un pedido cada vez que se cree uno nuevo.

**Why this priority:** La solicitud por parte del la logística financiera es crucial su funcionamiento, saber los detalles del pedido se vuelve un dato clave en el flujo de entrega al cliente.

**Acceptance Scenarios:**

1. **Scenario:** Envío de datos exitoso
   - **Given:** Se crea recientemente un pedido
   - **When:** El asesor crea un pedido para un cliente
   - **Then:** Luego de que el pedido se crea, se envían los datos al área de finanzas
   - **And:** Área de finanzas los recibe exitosamente
   - **And:** Confirman que está todo bien

2. **Scenario:** Envío de datos con inconveniente
   - **Given:** Se crea recientemente un pedido
   - **When:** El asesor crea un pedido para un cliente
   - **Then:** Luego de que el pedido se crea, se envían los datos al área de finanzas
   - **And:** Existen datos erróneos o faltan datos del pedido en la logística de finanzas
   - **And:** No confirman la recepción exitosa
   - **And:** Se revisa el pedido con sus datos y se reintenta el envío de los datos

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-048:** El sistema DEBE enviar los datos solicitados a logística de finanzas luego de crear el pedido.
- **FR-049:** El sistema espera la verificación del buen recibido.
- **FR-051:** El sistema puede volver a enviar los datos si ocurre algún problema y no llega notificación de recibido.

### Key Entities *(include if data)*

**LiquidacionCliente (creado al recibir el evento):**
- `id_liquidacion` (autogenerado)
- `id_pedido` (del mensaje)
- `id_cliente` (del mensaje)
- `monto_liquidado` ← `total_pedido` del mensaje
- `forma_pago` (consultada por `id_cliente`)
- `estado_liquidacion` = `PENDIENTE`
- `fecha_liquidacion` = fecha actual
- `uri_pdf` = `null`

> **Nota:** Los productos no se guardan en BD. Se consultan al generar el PDF. No se crea una tabla `pedido`; los datos del pedido se almacenan directamente en `liquidaciones_cliente`.

---

## Success Criteria *(mandatory)*

- **SC-023:** Debe haber 0% de pedidos no enviados a finanzas luego de haber sido creados.
