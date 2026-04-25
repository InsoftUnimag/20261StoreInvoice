# Feature Specification: Recibir Datos del Pedido desde MÃ³dulo de Inventario

**Created:** 12-03-2026  
**ComunicaciÃ³n:** asÃ­ncrona usando una cola para almacenar pedidos.

---

## DescripciÃ³n del Flujo

El MÃ³dulo de GestiÃ³n de Inventario envÃ­a los datos del pedido al Sistema Financiero cuando se crea un nuevo pedido. El Sistema Financiero guarda esta informaciÃ³n para posteriormente generar la liquidaciÃ³n cuando el MÃ³dulo de Transporte reporte el estado final.

---

## Datos Esperados del MÃ³dulo de Inventario

| Campo | Tipo | Requerido | DescripciÃ³n |
|-------|------|-----------|-------------|
| `id_pedido` | Integer | SÃ­ | ID del pedido |
| `id_cliente` | Integer | SÃ­ | ID del cliente |
| `total_pedido` | Integer | SÃ­ | Valor total del pedido |


> **Nota:** 
> - Los productos NO se reciben ahora. Se consultarÃ¡n posteriormente cuando se genere el PDF (al recibir el estado final del MÃ³dulo de Transporte).
> - El campo `direccion` se recibe pero **no se persiste** en la base de datos de finanzas.
> - Los datos del pedido **no se guardan en una tabla de pedidos**; se registran directamente en la tabla `liquidaciones_cliente`.

---

## Proceso en Sistema Financiero

1. **Recibir datos**: Se reciben los datos del pedido desde MÃ³dulo de Inventario (vÃ­a cola asÃ­ncrona)
2. **Consultar forma de pago**: Con el `id_cliente`, se consulta la forma de pago usando la funciÃ³n interna `buscar_forma_pago_por_id_cliente`
3. **Guardar en BD**: Se crea un registro en **`liquidaciones_cliente`** con los datos recibidos y la forma de pago asociada:
   - `id_pedido` â† `id_pedido` del mensaje
   - `id_cliente` â† `id_cliente` del mensaje
   - `monto_liquidado` â† `total_pedido` del mensaje
   - `forma_pago` â† forma de pago consultada
   - `estado_liquidacion` â† `PENDIENTE`
   - `fecha_liquidacion` â† fecha actual
   - `uri_pdf` â† `null` (se genera al recibir estado final)
4. **Esperar estado final**: La liquidaciÃ³n queda en estado `PENDIENTE` hasta que el MÃ³dulo de Transporte envÃ­e el evento

> **Nota:** Los productos se consultan posteriormente cuando se genere el PDF (ver spec `generar_liquidacion_cliente.md`).

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - RecepciÃ³n de datos del pedido (Priority: P1)

**Endpoint:** Evento/Cola (asÃ­ncrono)

**Como Sistema financiero**, requiero saber detalles de un pedido para usar los datos en la liquidaciÃ³n final, por tal motivo es primordial recibir los datos de un pedido cada vez que se cree uno nuevo.

**Why this priority:** La solicitud por parte del la logÃ­stica financiera es crucial su funcionamiento, saber los detalles del pedido se vuelve un dato clave en el flujo de entrega al cliente.

**Acceptance Scenarios:**

1. **Scenario:** EnvÃ­o de datos exitoso
   - **Given:** Se crea recientemente un pedido
   - **When:** El asesor crea un pedido para un cliente
   - **Then:** Luego de que el pedido se crea, se envÃ­an los datos al Ã¡rea de finanzas
   - **And:** Ãrea de finanzas los recibe exitosamente
   - **And:** Confirman que estÃ¡ todo bien

2. **Scenario:** EnvÃ­o de datos con inconveniente
   - **Given:** Se crea recientemente un pedido
   - **When:** El asesor crea un pedido para un cliente
   - **Then:** Luego de que el pedido se crea, se envÃ­an los datos al Ã¡rea de finanzas
   - **And:** Existen datos errÃ³neos o faltan datos del pedido en la logÃ­stica de finanzas
   - **And:** No confirman la recepciÃ³n exitosa
   - **And:** Se revisa el pedido con sus datos y se reintenta el envÃ­o de los datos

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-048:** El sistema DEBE enviar los datos solicitados a logÃ­stica de finanzas luego de crear el pedido.
- **FR-049:** El sistema espera la verificaciÃ³n del buen recibido.
- **FR-051:** El sistema puede volver a enviar los datos si ocurre algÃºn problema y no llega notificaciÃ³n de recibido.

### Key Entities *(include if data)*

**LiquidacionCliente (creado al recibir el evento):**
- `id_liquidacion` (autogenerado)
- `id_pedido` (del mensaje)
- `id_cliente` (del mensaje)
- `monto_liquidado` â† `total_pedido` del mensaje
- `forma_pago` (consultada por `id_cliente`)
- `estado_liquidacion` = `PENDIENTE`
- `fecha_liquidacion` = fecha actual
- `uri_pdf` = `null`

> **Nota:** Los productos no se guardan en BD. Se consultan al generar el PDF. No se crea una tabla `pedido`; los datos del pedido se almacenan directamente en `liquidaciones_cliente`.

---

## Success Criteria *(mandatory)*

- **SC-023:** Debe haber 0% de pedidos no enviados a finanzas luego de haber sido creados.

