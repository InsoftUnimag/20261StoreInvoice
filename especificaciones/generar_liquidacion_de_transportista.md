**Created:** 24-02-2026
## User Scenarios & Testing (mandatory)

### User Story 1 - CÃ¡lculo automatizado de liquidaciÃ³n segÃºn tasa de efectividad recibida (Priority: P1)

Yo como Sistema Financiero (MÃ³dulo 3) necesito calcular el monto a pagar al transportista tomando como base el 10% del precio total del pedido y aplicando la tasa de efectividad recibida directamente del MÃ³dulo de GestiÃ³n de Transporte. Esto con el objetivo de asegurar el pago a los aliados de transporte segÃºn su desempeÃ±o.

**Why this priority:**  Es una funciÃ³n importante del mÃ³dulo financiero; sin esto, no se puede pagar a los transportistas.

**Independent Test**: AÃ±adir en la base de datos pedidos con diferentes precios y enviar diferentes tasas de efectividad, verificando que el cÃ¡lculo final sea exacto: (precio Ã— 10%) Ã— tasa_efectividad.

**Acceptance Scenarios:** 

1. Scenario: LiquidaciÃ³n con Tasa de Efectividad del 100%
	- **Given:** El sistema obtiene exitosamente el precio del pedido consultando al MÃ³dulo de GestiÃ³n de Inventario, el MÃ³dulo de GestiÃ³n de Transporte ha reportado una `tasa_efectividad` de 100 para ese pedido.
	- **When:** Cuando se desea generar la liquidaciÃ³n del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de 100%, y genera una liquidaciÃ³n a pagar.

2. Scenario: LiquidaciÃ³n con Tasa de Efectividad del 80%
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al MÃ³dulo de GestiÃ³n de Inventario, el MÃ³dulo de GestiÃ³n de Transporte ha reportado una `tasa_efectividad` de 80 para ese pedido.
	- **When:** Cuando se desea generar la liquidaciÃ³n del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de 80%, y genera una liquidaciÃ³n a pagar.

3. Scenario: LiquidaciÃ³n con Tasa de Efectividad Negativa (-100%)
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al MÃ³dulo de GestiÃ³n de Inventario, el MÃ³dulo de GestiÃ³n de Transporte ha reportado una `tasa_efectividad` de -100 para ese pedido.
	- **When:** Cuando se desea generar la liquidaciÃ³n del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de -100%, y genera una liquidaciÃ³n en contra del transportista.

4. Scenario: LiquidaciÃ³n con Tasa de Efectividad del 0%
	- **Given**: El sistema obtiene exitosamente el precio del pedido consultando al MÃ³dulo de GestiÃ³n de Inventario, el MÃ³dulo de GestiÃ³n de Transporte ha reportado una `tasa_efectividad` de 0 para ese pedido.
	- **When:** Cuando se desea generar la liquidaciÃ³n del transportista
	- **Then:** El sistema calcula la tarifa (10% del precio), aplica directamente la tasa_efectividad de 0%, resultando en una liquidaciÃ³n nula, y registra el costo del flete como una pÃ©rdida operativa.

### User Story 2 - ValidaciÃ³n de datos cruzados entre mÃ³dulos (Priority: P1)

Yo como Sistema Financiero necesito validar que existan los datos de entrada 

### Edge Cases

- Â¿QuÃ© pasa si la tasa de efectividad recibida es 0%?
- El sistema calcula el 10% del pedido como tarifa base, pero al aplicar el 0%, la liquidaciÃ³n a pagar al transportista es de $0, y se debe generar un reporte como pÃ©rdida operativa.

- Â¿QuÃ© pasa si el cÃ¡lculo del 10% del pedido arroja decimales muy largos?
- El sistema debe redondear al precio entero mÃ¡s cercano para evitar problemas.

- Â¿QuÃ© pasa si la tasa de efectividad recibida desde el MÃ³dulo de GestiÃ³n de Transporte estÃ¡ fuera del rango 0-100%?
- El sistema debe rechazar la operaciÃ³n, bloquear el cÃ¡lculo y emitir una alerta de "Dato de efectividad invÃ¡lido".

- Â¿QuÃ© pasa si un pedido cambia de estado despuÃ©s de haber sido liquidado (ejemplo, el cliente hace un reclamo posterior a la entrega)?

Yo como Sistema Financiero necesito validar que existan los datos de entrada req
### Functional Requirements

- **FR-001**: El sistema DEBE obtener el precio total del pedido desde la BD (recibido del MÃ³dulo de Inventario).
- **FR-002**: El sistema DEBE obtener la `tasa_efectividad` directamente del evento publicado por el MÃ³dulo de Transporte (ver spec `recibir_estado_final_modulo_transporte.md`, rango vÃ¡lido 0-100 o valores negativos permitidos segÃºn lÃ³gica).
- **FR-003**: El sistema DEBE calcular la "tarifa base" del transportista extrayendo exactamente el 10% del precio total del pedido.
- **FR-004**: El sistema DEBE aplicar a la tarifa base la `tasa_efectividad` recibida directamente del evento. FÃ³rmula: Monto = (Precio Pedido Ã— 10%) Ã— (tasa_efectividad / 100).
- **FR-005**: El sistema DEBE bloquear y cancelar la generaciÃ³n de la liquidaciÃ³n si el precio del pedido es nulo, cero, o si no hay `tasa_efectividad`.

### Key Entities *(include if feature involves data)*

Yo como Sistema Financiero necesito validar que existan los datos de entrada req

### Measurable Outcomes

- **SC-001**: El 100% de las liquidaciones generadas reflejan exactamente el cÃ¡lculo: Monto = (Precio Pedido Ã— 10%) Ã— (tasa_efectividad).
- **SC-002**: El sistema no genera ninguna liquidaciÃ³n con campos vacÃ­os o nulos por falta de datos de entrada.
- **SC-003**: El sistema no genera ninguna liquidaciÃ³n si la `tasa_efectividad` estÃ¡ fuera de los rangos permitidos o el precio es nulo/cero.
