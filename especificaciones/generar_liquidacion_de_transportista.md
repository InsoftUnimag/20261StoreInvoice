# Feature Specification - Generar Liquidación de transportista

> **Nota:** El trigger de esta función es el evento publicado por el Módulo de Transporte. Ver spec: `recibir_estado_final_modulo_transporte.md`


**Created:** 24-02-2026
## User Scenarios & Testing (mandatory)

### User Story 1 - Cálculo automatizado de liquidación según estado final y tasa de efectividad recibida (Priority: P1)

Yo como Sistema Financiero necesito calcular el monto a pagar al transportista basándose en una tarifa fija y el resultado de la entrega. Si el transportista pierde el pedido (tasa_efectividad = -100), debe asumir el valor completo del pedido. Esto con el objetivo de asegurar el pago a los aliados de transporte según su desempeño.

**Why this priority:** Es una función importante del módulo financiero; sin esto, no se puede pagar a los transportistas.

**Fórmula de cálculo:**
- Si `tasa_efectividad = -100` (pedido perdido): `monto = precio_pedido`
- Si no: `monto = tarifa_base × (% según matriz del estado_final)`

**Matriz de porcentaje por estado final:**
| Estado Final | % Pago Logístico |
|-------------|------------------|
| Entregado Completo | 100% |
| Rechazo Parcial | 80% |
| Devolución (Error Empresa) | 0% |
| Faltante de Inventario | -100% |

> **Nota:** La tarifa_base es un valor fijo pendiente de definir.

**Acceptance Scenarios:** 

1. Scenario: Liquidación con Entregado Completo (tasa 100)
  	- **Given:** El sistema obtiene exitosamente el precio del pedido desde la BD local, el estado_final es "Entregado Completo" y tasa_efectividad de 100.
  	- **When:** Cuando se desea generar la liquidación del transportista
  	- **Then:** El sistema calcula: tarifa_base × 100%, y genera una liquidación a pagar.

2. Scenario: Liquidación con Rechazo Parcial (tasa 80)
 	- **Given**: El sistema obtiene exitosamente el precio del pedido desde la BD local, el estado_final es "Rechazo Parcial" y tasa_efectividad de 80.
 	- **When:** Cuando se desea generar la liquidación del transportista
 	- **Then:** El sistema calcula: tarifa_base × 80%, y genera una liquidación a pagar.

3. Scenario: Liquidación con Devolución (tasa 0)
 	- **Given**: El sistema obtiene exitosamente el precio del pedido desde la BD local, el estado_final es "Devolución (Error Empresa)" y tasa_efectividad de 0.
 	- **When:** Cuando se desea generar la liquidación del transportista
 	- **Then:** El sistema calcula: tarifa_base × 0% = 0, y registra el costo del flete como pérdida operativa.

4. Scenario: Liquidación con Pedido Perdido (tasa -100)
 	- **Given**: El sistema obtiene exitosamente el precio del pedido desde la BD local, el estado_final es "Faltante de Inventario" y tasa_efectividad de -100.
 	- **When:** Cuando se desea generar la liquidación del transportista
 	- **Then:** El sistema calcula: precio_pedido (el transportista asume el valor completo del pedido).

### User Story 2 - Validación de datos cruzados entre módulos (Priority: P1)

Yo como Sistema Financiero necesito validar que existan los datos de entrada requeridos, precio del pedido y tasa de efectividad antes de intentar procesar el cálculo. Para evitar registros contables erróneos.

**Why this priority:** Protege la integridad de la base de datos y garantiza que el módulo financiero no falle por culpa de datos incompletos de los módulos de Logistica o de Gestión de Inventario.

**Independent Test:** Forzar el cálculo de una liquidación para un pedido que no tiene registrado su precio en el sistema de inventario o un reporte del modulo de losgitica donde envien una tasa de efectividad nula o por fuera de los parametros.

**Acceptance Scenarios:**

1. **Scenario:** Intento de liquidación con datos incompletos
 	- **Given:** El sistema intenta generar la liquidación pero no encuentra el precio del pedido en la BD local.
 	- **When:** Cuando se desea generar la liquidación del transportista.
 	- **Then:** El sistema bloquea la operación, no genera la liquidación y muestra un error de "Falta precio del pedido".

2. **Scenario:** Intento de liquidación con datos incompletos
 	- **Given:** El sistema obtiene exitosamente el precio del pedido desde la BD local, pero no se encuentra la Tasa de efectividad o estado_final registrada por el Módulo de Gestión de Transporte.
 	- **When:** Cuando se desea generar la liquidación del transportista.
 	- **Then:** El sistema bloquea la operación, no genera la liquidación y muestra un error de "Falta tasa de efectividad o estado final del pedido".

### Edge Cases

- ¿Qué pasa si la tasa de efectividad recibida es 0%?
- El sistema calcula la tarifa_base × 0% = 0, resultando en liquidación nula, y registra el costo del flete como pérdida operativa.

- ¿Qué pasa si la tasa de efectividad recibida está fuera del rango -100 a 100?
- El sistema debe rechazar el dato, bloquear el cálculo y emitir una alerta de "Dato de efectividad inválido".

- ¿Qué pasa si un pedido cambia de estado después de haber sido liquidado (ejemplo, el cliente hace un reclamo posterior a la entrega)?
- **Pendiente de definir:** El sistema no debe modificar ni sobrescribir la liquidación original, ya que esto rompería la auditoría contable. Se debe evaluar la implementación de un mecanismo de Nota de Ajuste o Corrección Financiera. Ver spec separado para este caso.

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: El sistema DEBE obtener el precio total del pedido desde la BD (recibido del Módulo de Inventario).
- **FR-002**: El sistema DEBE obtener la `tasa_efectividad` y `estado_final` del evento publicado por el Módulo de Transporte (ver spec `recibir_estado_final_modulo_transporte.md`).
- **FR-003**: El sistema DEBE aplicar la fórmula de cálculo: Si tasa_efectividad = -100, monto = precio_pedido. Si no, monto = tarifa_base × (% según matriz del estado_final).
- **FR-004**: El sistema DEBE usar la matriz de porcentaje según el estado_final: Entregado Completo (100%), Rechazo Parcial (80%), Devolución (0%), Faltante de Inventario (-100%).
- **FR-005**: El sistema DEBE bloquear y cancelar la generación de la liquidación si el precio del pedido es nulo, cero, o si no hay `tasa_efectividad`.

### Key Entities *(include if feature involves data)*

- **PedidoRecibido**: [id_pedido, id_cliente, total_pedido] (Recibido del Módulo de Inventario).
- **Liquidacion_Transportista**: [id_liquidacion, id_pedido, id_transportista, monto_calculado, fecha_liquidacion] (La entidad y registro final resultante).
  
## Success Criteria (mandatory)

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: El 100% de las liquidaciones generadas reflejan exactamente la fórmula: Si tasa_efectividad = -100, monto = precio_pedido. Si no, monto = tarifa_base × (% según matriz).
- **SC-002**: El sistema no genera ninguna liquidación con campos vacíos o nulos por falta de datos de entrada.
- **SC-003**: El sistema no genera ninguna liquidación si la `tasa_efectividad` está fuera de los rangos permitidos o el precio es nulo/cero.
