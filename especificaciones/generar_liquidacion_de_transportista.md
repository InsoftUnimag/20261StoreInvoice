**Created:** 24-02-2026
## User Scenarios & Testing (mandatory)

### User Story 1 - Cálculo automatizado de liquidación según tasa de efectividad recibida (Priority: P1)

Yo como Sistema Financiero (Módulo 3) necesito calcular el monto a pagar al transportista tomando como base el 10% del precio total del pedido y aplicando la tasa de efectividad recibida directamente del Módulo de Gestión de Transporte. Esto con el objetivo de asegurar el pago a los aliados de transporte según su desempeño.

**Why this priority:**  Es una función importante del módulo financiero; sin esto, no se puede pagar a los transportistas.

**Independent Test**: Añadir en la base de datos pedidos con diferentes precios y enviar diferentes tasas de efectividad, verificando que el cálculo final sea exacto: (precio × 10%) × tasa_efectividad.

**Acceptance Scenarios:** 

1. Scenario: Liquidación con Tasa de Efectividad del 100%
	- **Given:** El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha reportado una `tasa_efectividad` de 100 para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de 100%, y genera una liquidación a pagar.

2. Scenario: Liquidación con Tasa de Efectividad del 80%
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha reportado una `tasa_efectividad` de 80 para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de 80%, y genera una liquidación a pagar.

3. Scenario: Liquidación con Tasa de Efectividad Negativa (-100%)
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha reportado una `tasa_efectividad` de -100 para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de -100%, y genera una liquidación en contra del transportista.

4. Scenario: Liquidación con Tasa de Efectividad del 0%
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al Módulo de Gestión de Inventario, el Módulo de Gestión de Transporte ha reportado una `tasa_efectividad` de 0 para ese pedido.
	- **When:** Cuando se desea generar la liquidación del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de 0%, resultando en una liquidación nula, y registra el costo del flete como una pérdida operativa.

### User Story 2 - Validación de datos cruzados entre módulos (Priority: P1)

Yo como Sistema Financiero necesito validar que existan los datos de entrada 

### Edge Cases

- ¿Qué pasa si la tasa de efectividad recibida es 0%?
- El sistema calcula el 10% del pedido como tarifa base, pero al aplicar el 0%, la liquidación a pagar al transportista es de $0, y se debe generar un reporte como pérdida operativa.

- ¿Qué pasa si el cálculo del 10% del pedido arroja decimales muy largos?
- El sistema debe redondear al precio entero más cercano para evitar problemas.

- ¿Qué pasa si la tasa de efectividad recibida desde el Módulo de Gestión de Transporte está fuera del rango 0-100%?
- El sistema debe rechazar la operación, bloquear el cálculo y emitir una alerta de "Dato de efectividad inválido".

- ¿Qué pasa si un pedido cambia de estado después de haber sido liquidado (ejemplo, el cliente hace un reclamo posterior a la entrega)?

Yo como Sistema Financiero necesito validar que existan los datos de entrada req
### Functional Requirements

- **FR-001**: El sistema DEBE obtener el precio total del pedido desde la BD (recibido del Módulo de Inventario).
- **FR-002**: El sistema DEBE obtener la `tasa_efectividad` directamente del evento publicado por el Módulo de Transporte (ver spec `recibir_estado_final_modulo_transporte.md`, rango válido 0-100 o valores negativos permitidos según lógica).
- **FR-003**: El sistema DEBE calcular la "tarifa base" del transportista extrayendo exactamente el 10% del precio total del pedido.
- **FR-004**: El sistema DEBE aplicar a la tarifa base la `tasa_efectividad` recibida directamente del evento. Fórmula: Monto = (Precio Pedido × 10%) × (tasa_efectividad / 100).
- **FR-005**: El sistema DEBE bloquear y cancelar la generación de la liquidación si el precio del pedido es nulo, cero, o si no hay `tasa_efectividad`.

### Key Entities *(include if feature involves data)*

Yo como Sistema Financiero necesito validar que existan los datos de entrada req

### Measurable Outcomes

- **SC-001**: El 100% de las liquidaciones generadas reflejan exactamente el cálculo: Monto = (Precio Pedido × 10%) × (tasa_efectividad).
- **SC-002**: El sistema no genera ninguna liquidación con campos vacíos o nulos por falta de datos de entrada.
- **SC-003**: El sistema no genera ninguna liquidación si la `tasa_efectividad` está fuera de los rangos permitidos o el precio es nulo/cero.