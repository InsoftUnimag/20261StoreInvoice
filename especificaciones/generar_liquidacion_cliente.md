# Feature Specification - Generar LiquidaciÃ³n de Cliente

**Created:** 03-03-2026  
**Status:** In Development

## DescripciÃ³n del Flujo

> **Nota:** El trigger de esta funciÃ³n es el evento publicado por el **MÃ³dulo de Inventario** (ver spec `recibir_datos_pedido_modulo_inventario.md`). Al recibir los datos del pedido, el sistema consulta los productos del MÃ³dulo de Inventario, consulta los datos del cliente del MÃ³dulo de GestiÃ³n de Clientes, genera el PDF de liquidaciÃ³n y lo guarda en el registro de `liquidaciones_cliente`.

**Datos recibidos del MÃ³dulo de Inventario:**
- `id_pedido`
- `id_cliente`
- `total_pedido`

**Pasos para generar la liquidaciÃ³n:**

1. **Recibir datos**: Se reciben los datos del pedido desde el MÃ³dulo de Inventario (vÃ­a cola asÃ­ncrona)
2. **Consultar forma de pago**: Con el `id_cliente`, se consulta la forma de pago usando `FormaPagoClienteRepository`
3. **Consultar productos**: Se llama al endpoint del MÃ³dulo de Inventario via `InventarioServicePort` para obtener los productos del pedido
4. **Consultar datos del cliente**: Se llama al MÃ³dulo de GestiÃ³n de Clientes via `ClienteServicePort` para obtener los datos del cliente
5. **Generar PDF**: Se invoca la funciÃ³n interna de generaciÃ³n de PDF (spec `generar_pdf_liquidacion_cliente.md`) que retorna la URI del PDF
6. **Guardar liquidaciÃ³n**: Se guarda el registro de liquidaciÃ³n con estado `PENDIENTE` y la URI del PDF

> **Nota:** El `monto_liquidado` es igual al `total_pedido` recibido del MÃ³dulo de Inventario. No se aplica fÃ³rmula de cÃ¡lculo adicional.

---

## User Scenarios & Testing (mandatory)

### User Story 1 - GeneraciÃ³n de liquidaciÃ³n al recibir pedido (Priority: P1)

Yo como Sistema Financiero necesito generar una liquidaciÃ³n para un cliente cuando el MÃ³dulo de Inventario envÃ­e los datos del pedido. Para registrar el monto a cobrar y generar el PDF de la liquidaciÃ³n.

**Why this priority:** Es una funciÃ³n crÃ­tica para la conciliaciÃ³n financiera.

**Acceptance Scenarios:**

1. **Scenario:** GeneraciÃ³n exitosa de liquidaciÃ³n
   - **Given:** El MÃ³dulo de Inventario envÃ­a los datos del pedido con id_pedido
   - **When:** Se desea generar la liquidaciÃ³n del cliente
   - **Then:** El sistema consulta los productos, consulta el cliente, genera el PDF y guarda la URI.

2. **Scenario:** Error al generar PDF
   - **Given:** El sistema no puede generar el PDF
   - **When:** Se desea generar la liquidaciÃ³n del cliente
   - **Then:** El sistema retorna error y no guarda la liquidaciÃ³n incompleta.

---

### User Story 2 - ValidaciÃ³n de datos requeridos (Priority: P1)

Yo como Sistema Financiero necesito validar que existan todos los datos requeridos antes de generar la liquidaciÃ³n. Para evitar registros contables errÃ³neos.

**Why this priority:** Protege la integridad de la base de datos y previene facturas incompletas.

**Independent Test:** Intentar generar liquidaciones con datos faltantes y verificar que el sistema las rechace.

**Acceptance Scenarios:**

1. **Scenario:** Intento de liquidaciÃ³n sin precio del pedido
   - **Given:** Se intenta generar liquidaciÃ³n pero el total_pedido es nulo o negativo
   - **When:** Cuando se desea generar la liquidaciÃ³n del cliente
   - **Then:** El sistema bloquea la operaciÃ³n y muestra error: "Total del pedido no puede ser negativo".

2. **Scenario:** Intento de liquidaciÃ³n sin forma de pago registrada
   - **Given:** Se intenta generar liquidaciÃ³n para un cliente sin forma de pago asignada
   - **When:** Cuando se desea generar la liquidaciÃ³n
   - **Then:** El sistema bloquea la operaciÃ³n y muestra error: "Cliente sin forma de pago registrada".

3. **Scenario:** Intento de liquidaciÃ³n sin productos
   - **Given:** El MÃ³dulo de Inventario retorna lista vacÃ­a de productos para el pedido
   - **When:** Cuando se desea generar la liquidaciÃ³n
   - **Then:** El sistema bloquea la operaciÃ³n y muestra error: "No se encontraron productos para el pedido".

---

### Edge Cases

- **Â¿QuÃ© pasa si el pedido no existe en la base de datos?**
  - El sistema debe retornar error: "Pedido no encontrado"

- **Â¿QuÃ© pasa si no se pueden obtener los productos del MÃ³dulo de Inventario?**
  - El sistema debe retornar error: "Error al consultar los productos del pedido"

- **Â¿QuÃ© pasa si no se pueden obtener los datos del cliente?**
  - El sistema debe retornar error: "Error al consultar los datos del cliente"

- **Â¿QuÃ© pasa si falla la subida del PDF a la nube?**
  - El sistema debe retornar error: "Error al subir el PDF a la nube. Intente mÃ¡s tarde"

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir del evento del MÃ³dulo de Inventario el `id_pedido`, `id_cliente` y `total_pedido`.
- **FR-002:** El sistema DEBE consultar la forma de pago del cliente usando `FormaPagoClienteRepository`.
- **FR-003:** El sistema DEBE consultar los productos del pedido del MÃ³dulo de Inventario via `InventarioServicePort`.
- **FR-004:** El sistema DEBE consultar los datos del cliente del MÃ³dulo de GestiÃ³n de Clientes via `ClienteServicePort`.
- **FR-005:** El sistema DEBE invocar la funciÃ³n interna de generaciÃ³n de PDF (spec `generar_pdf_liquidacion_cliente.md`) y guardar la URI retornada.
- **FR-006:** El sistema DEBE guardar el registro de liquidaciÃ³n con estado `PENDIENTE`, fecha actual y URI del PDF.
- **FR-007:** El sistema DEBE validar que todos los parÃ¡metros requeridos estÃ©n presentes antes de generar la liquidaciÃ³n.
- **FR-008:** El sistema DEBE mantener un registro inmutable de todas las liquidaciones generadas.

### Key Entities *(include if feature involves data)*

- **Liquidacion_Cliente:** 
  - [id_liquidacion, id_pedido, id_cliente, monto_liquidado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf]
  - Entidad que registra el monto a cobrar al cliente.

**Notas de columnas:**
- `monto_liquidado`: Se obtiene del `total_pedido` recibido del MÃ³dulo de Inventario
- `forma_pago`: Se consulta por `id_cliente` via `FormaPagoClienteRepository`
- `estado_liquidacion`: Siempre `PENDIENTE` al crear
- `uri_pdf`: Se genera mediante la funciÃ³n `generar_pdf_liquidacion_cliente.md`
- `fecha_liquidacion`: Fecha actual del sistema

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe generar una liquidaciÃ³n completa (recepciÃ³n + consultas + PDF + almacenamiento) en menos de 2 segundos.

- **SC-002:** Todas las liquidaciones deben ser rastreables e inmutables para auditorÃ­a.

- **SC-003:** El sistema debe procesar al menos 1,000 liquidaciones diarias sin fallos crÃ­ticos.

- **SC-004:** El sistema debe manejar errores de manera apropiada sin detener el procesamiento de mensajes siguientes (pipeline reactivo).

---

## Notas de ImplementaciÃ³n

### SimplificaciÃ³n respecto al diseÃ±o original

Esta spec fue simplificada durante la implementaciÃ³n. Originalmente el trigger era el evento del MÃ³dulo de Transporte con campos adicionales (`estado_final`, `tasa_efectividad`, `tarifa_envio`). La implementaciÃ³n actual:

- Usa el **evento del MÃ³dulo de Inventario** como trigger Ãºnico
- No procesa eventos del MÃ³dulo de Transporte
- No utiliza `estado_final`, `tasa_efectividad`, `tarifa_envio`
- No aplica fÃ³rmula de cÃ¡lculo: `monto_liquidado = total_pedido`
- Genera el PDF inmediatamente al recibir el mensaje del Inventario
- Guarda la URI del PDF en el registro inicial (no `null`)

El flujo estÃ¡ implementado en:
- `ProcesarPedidoInventarioUseCase` (orquestador reactivo)
- `PedidoEventConsumer` (consumer reactivo Function<T, Mono<Void>>)

