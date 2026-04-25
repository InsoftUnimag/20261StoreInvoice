# Feature Specification - Generar Liquidación de Cliente

**Created:** 03-03-2026  
**Status:** In Development

## Descripción del Flujo

> **Nota:** El trigger de esta función es el evento publicado por el Módulo de Transporte. Ver spec: `recibir_estado_final_modulo_transporte.md`. Los datos del pedido fueron recibidos previamente del Módulo de Inventario (ver spec `recibir_datos_pedido_modulo_inventario.md`).

**Datos previos recibidos:**
- Los datos del pedido (id_pedido, id_cliente, total_pedido) ya fueron recibidos del Módulo de Inventario
- Los productos se consultan posteriormente para generar el PDF

**Evento del Módulo de Transporte:**
Al recibir el evento, el Sistema Financiero obtiene:
- `id_pedido`
- `estado_final`
- `tasa_efectividad`

**Pasos para generar la liquidación:**

1. **Guardar estado_final**: Se actualiza el registro del pedido con el `estado_final` recibido del Módulo de Transporte
2. **Consultar productos**: Se llama al endpoint `GET /api/v1/pedidos/{id_pedido}/productos` del Módulo de Inventario
3. **Consultar datos del cliente**: Se llama al Módulo de Gestión de Clientes para obtener los datos del cliente
4. **Generar PDF**: Se invoca la función interna `generar_pdf_liquidacion_cliente.md` que retorna la URI del PDF
5. **Guardar liquidación**: Se guarda el registro de liquidación con la URI del PDF

**Fórmula de cálculo:**
- `total_calculado = (precio_pedido + tarifa_envío) × (tasa_efectividad / 100)`

---

## User Scenarios & Testing (mandatory)

### User Story 1 - Generación de liquidación según estado final de entrega (Priority: P1)

Yo como Sistema Financiero necesito generar una liquidación para un cliente cuando el Módulo de Gestión de Transporte reporte llegada a un estado final de entrega. Para registrar el monto a cobrar y generar el PDF de la liquidación.

**Why this priority:** Es una función crítica para la conciliación financiera.

**Acceptance Scenarios:**

1. **Scenario:** Generación exitosa de liquidación
   - **Given:** El Módulo de Gestión de Transporte envía el estado final con el id_pedido
   - **When:** Se desea generar la liquidación del cliente
   - **Then:** El sistema genera el PDF con la información de la liquidación y guarda la URI.

2. **Scenario:** Error al generar PDF
   - **Given:** El sistema no puede generar el PDF
   - **When:** Se desea generar la liquidación del cliente
   - **Then:** El sistema retorna error.

---

### User Story 2 - Validación de datos requeridos (Priority: P1)

Yo como Sistema Financiero necesito validar que existan todos los datos requeridos antes de generar la liquidación. Para evitar registros contables erróneos.

**Why this priority:** Protege la integridad de la base de datos y previene facturas incompletas.

**Independent Test:** Intentar generar liquidaciones con datos faltantes y verificar que el sistema las rechace.

**Acceptance Scenarios:**

1. **Scenario:** Intento de liquidación sin precio del pedido
   - **Given:** Se intenta generar liquidación pero al consultar el id_pedido no se obtiene el precio
   - **When:** Cuando se desea generar la liquidación del cliente
   - **Then:** El sistema bloquea la operación y muestra error: "Falta precio del pedido".

2. **Scenario:** Intento de liquidación sin forma de pago registrada
   - **Given:** Se intenta generar liquidación para un cliente sin forma de pago asignada
   - **When:** Cuando se desea generar la liquidación
   - **Then:** El sistema bloquea la operación y muestra error: "Cliente sin forma de pago registrada".

3. **Scenario:** Intento de liquidación sin estado de entrega
   - **Given:** El Módulo de Gestión de Transporte no ha enviado el estado final del pedido
   - **When:** Cuando se desea generar la liquidación
   - **Then:** El sistema bloquea la operación y muestra error: "Estado de entrega no disponible".

---



### Edge Cases

- **¿Qué pasa si el pedido no existe en la base de datos?**
  - El sistema debe retornar error: "Pedido no encontrado"

- **¿Qué pasa si no se pueden obtener los productos del Módulo de Inventario?**
  - El sistema debe retornar error: "Error al consultar los productos del pedido"

- **¿Qué pasa si no se pueden obtener los datos del cliente?**
  - El sistema debe retornar error: "Error al consultar los datos del cliente"

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir del evento del Módulo de Transporte el id_pedido, estado_final y tasa_efectividad (ver spec `recibir_estado_final_modulo_transporte.md`).
- **FR-002:** El sistema DEBE obtener los datos del pedido desde la BD (recibidos del Módulo de Inventario, ver spec `recibir_datos_pedido_modulo_inventario.md`).
- **FR-003:** El sistema DEBE obtener la forma de pago del registro del pedido (ya consultada y guardada al recibir los datos del Módulo de Inventario).
- **FR-004:** El sistema DEBE guardar el estado_final en el registro del pedido.
- **FR-005:** El sistema DEBE consultar los productos del pedido del Módulo de Inventario.
- **FR-006:** El sistema DEBE consultar los datos del cliente del Módulo de Gestión de Clientes.
- **FR-007:** El sistema DEBE invocar la función interna de generación de PDF (spec `generar_pdf_liquidacion_cliente.md`) y guardar la URI retornada.
- **FR-008:** El sistema DEBE mantener un registro inmutable de todas las liquidaciones generadas.

### Key Entities *(include if feature involves data)*

- **Liquidacion_Cliente:** 
  - [id_liquidacion, id_pedido, id_cliente, precio_pedido, tarifa_envio, total_calculado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf]
  - Entidad que registra el monto a cobrar al cliente.

**Notas de columnas:**
- `precio_pedido`: Se obtiene de los datos recibidos del Módulo de Inventario
- `forma_pago`: Se obtiene del registro del pedido (ya consultada al recibir los datos del Módulo de Inventario)
- `estado_final`: Se recibe del evento del Módulo de Transporte
- `uri_pdf`: Se genera mediante la función `generar_pdf_liquidacion_cliente.md`

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe generar una liquidación en menos de 2 segundos después de recibir el estado final.

- **SC-002:** Todas las liquidaciones deben ser rastreables e inmutables para auditoría.

- **SC-003:** El sistema debe procesar al menos 1,000 liquidaciones diarias sin fallos críticos. |



