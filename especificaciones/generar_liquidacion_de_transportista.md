# Feature Specification - Generar Liquidación de transportista

> **Nota:** El trigger de esta función es el evento publicado por el Módulo de Transporte. Ver spec: `recibir_estado_final_modulo_transporte.md`

Terminada

**Created:** 24-02-2026
## User Scenarios & Testing (mandatory)

### User Story 1 - Cálculo automatizado de liquidación según efectividad y precio (Priority: P1)

Yo como Sistema Financiero (Módulo 3) necesito calcular el monto a pagar al transportista tomando como base el 10% del precio total del pedido y aplicando la tasa de efectividad una vez que el Módulo de Gestión de Transporte reporte el estado final. Esto con el objetivo de asegurar el pago a los aliados de transporte.

**Why this priority:**  Es una función importante del módulo financiero; sin esto, no se puede pagar a los transportistas.

**Independent Test**: Añadir en la base de datos pedidos con diferentes precios y distintas tasas de efectividad, verificando que el cálculo final sea exacto según la regla de pago a los transportistas.

**Acceptance Scenarios:** 

1. Scenario: Liquidación por Estado de Entrega Exitosa (100% de la tarifa)
	- **Given:** El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha registrado un estado de "Entregado Completo" para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa según el pedido, aplica el 100% de pago por la tasa de efectividad, y genera una liquidación a pagar.

2. Scenario: Liquidación con Rechazo Parcial (80% de la tarifa)
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha registrado un estado de "Rechazo parcial" para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa según el pedido, aplica el 80% de pago por la tasa de efectividad, y genera una liquidación a pagar.

3. Scenario: Penalización por Faltante de Inventario (Pérdida)
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha registrado un estado de "Faltante de Inventario" para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa según el pedido, aplica el -100% de pago por la tasa de efectividad, y genera una liquidación en contra del transportista.

4. Scenario: Liquidación nula por Devolución (Error Empresa)
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha registrado un estado de "Devolución (Error Empresa)" para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa según el pedido, aplica el 0% de pago por la tasa de efectividad, y genera una liquidación, y registra el costo del flete como una pérdida operativa.

### User Story 2 - Validación de datos cruzados entre módulos (Priority: P1)

Yo como Sistema Financiero necesito validar que existan los datos de entrada requeridos, precio del pedido y tasa de efectividad antes de intentar procesar el cálculo. Para evitar registros contables erróneos.

**Why this priority:** Protege la integridad de la base de datos y garantiza que el módulo financiero no falle por culpa de datos incompletos de los módulos de Logistica o de Gestión de Inventario.

**Independent Test:** Forzar el cálculo de una liquidación para un pedido que no tiene registrado su precio en el sistema de inventario o un reporte del modulo de losgitica donde envien una tasa de efectividad nula o por fuera de los parametros.

**Acceptance Scenarios:**

1. **Scenario:** Intento de liquidación con datos incompletos
	- **Given:** Tasa de efectividad registrada de un pedido, al consultar el endpoint del Módulo de Gestión de Inventario, este no retorna el precio del pedido.
	- **When:** Cuando se desea generar la liquidación del transportista.
	- **Then:** El sistema bloquea la operación, no genera la liquidación y muestra un error de "Falta precio del pedido".

2. **Scenario:** Intento de liquidación con datos incompletos
	- **Given:** El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, no se encuentra la Tasa de efectividad registrada por el Módulo de Gestión de Transporte.
	- **When:** Cuando se desea generar la liquidación del transportista.
	- **Then:** El sistema bloquea la operación, no genera la liquidación y muestra un error de "Falta tasa de efectividad del pedido".

### Edge Cases

- ¿Qué pasa si un pedido es reportado como "Devolución (Error Empresa)"?
- El sistema calcula el 10% del pedido como tarifa base, pero al aplicar el 0% de la matriz, la liquidación a pagar al transportista es de $0, y se debe generar un reporte como perdida operativa.

- ¿Qué pasa si el cálculo del 10% del pedido arroja decimales muy largos?
- El sistema debe redondear al precio entero más cercano para evitar problemas.
  
- ¿Qué pasa si la tasa de efectividad recibida desde el Módulo de Gestión de Transporte está fuera del rango -100 a 100%?
- El sistema debe rechazar el precio, bloquear el cálculo y emitir una alerta de "Dato de efectividad inválido".

- ¿Qué pasa si un pedido cambia de estado después de haber sido liquidado (ejemplo, el cliente hace un reclamo posterior a la entrega)?

- **Pendiente de definir:** El sistema no debe modificar ni sobrescribir la liquidación original, ya que esto rompería la auditoría contable. Se debe evaluar la implementación de un mecanismo de Nota de Ajuste o Corrección Financiera. Ver spec separado para este caso.

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: El sistema DEBE obtener el precio total del pedido desde la BD (recibido del Módulo de Inventario).
- **FR-002**: El sistema DEBE obtener la tasa_efectividad del evento publicado por el Módulo de Transporte (ver spec `recibir_estado_final_modulo_transporte.md`).
- **FR-003**: El sistema DEBE calcular la "tarifa base" del transportista extrayendo exactamente el 10% del precio total del pedido.
- **FR-004**: El sistema DEBE aplicar a la tarifa base la tasa_efectividad recibida directamente del evento (no usa matriz).
- **FR-005**: El sistema DEBE bloquear y cancelar la generación de la liquidación si el precio del pedido es nulo, cero, o si no hay tasa_efectividad.

### Key Entities *(include if feature involves data)*

- **PedidoRecibido**: [id_pedido, id_cliente, total_pedido] (Recibido del Módulo de Inventario).
- **Liquidacion_Transportista**: [id_liquidacion, id_pedido, id_transportista, monto_calculado, fecha_liquidacion] (La entidad y registro final resultante).
  
## Success Criteria (mandatory)

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: El 100% de las liquidaciones generadas reflejan exactamente el cálculo del 10% del precio del pedido ajustado por la tasa_efectividad recibida.
- **SC-002**: El sistema no genera ninguna liquidación con campos vacíos o nulos por falta de datos de entrada.
- **SC-002**: El sistema no genera ninguna liquidación (0%) con campos de pago vacíos o nulos por falta de datos de entrada.
