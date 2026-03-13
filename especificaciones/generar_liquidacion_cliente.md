# Feature Specification - Generar Liquidación de Cliente

**Created:** 03-03-2026  
**Status:** In Development

## Descripción del Flujo

> **Nota:** El trigger de esta función es el evento publicado por el Módulo de Transporte. Ver spec: `recibir_estado_final_modulo_transporte.md`. Los datos del pedido fueron recibidos previamente del Módulo de Inventario (ver spec `recibir_datos_pedido_modulo_inventario.md`).

**Datos previos recibidos:**
- Los datos del pedido (id_pedido, id_cliente, total_pedido, productos) ya fueron recibidos del Módulo de Inventario y guardados en BD (ver spec `recibir_datos_pedido_modulo_inventario.md`)

**Evento del Módulo de Transporte:**
Al recibir el evento, el Sistema Financiero obtiene:
- `id_pedido`
- `estado_final`
- `tasa_efectividad`

**Pasos para generar la liquidación:**

1. **Obtener datos del pedido**: Se consultan los datos guardados del pedido en BD (recibidos del Módulo de Inventario)
2. **Consultar productos**: Se llaman al endpoint `GET /api/v1/pedidos/{id_pedido}/productos` del Módulo de Inventario (necesario para generar el PDF)
3. **Obtener datos del cliente**: Se consultan los datos del cliente mediante función interna (necesario para el PDF)
4. **Consultar forma de pago**: Se consulta la forma de pago del cliente mediante función interna
4. **Calcular total**: Se calcula (precio_pedido + tarifa_envío) × % según la matriz de liquidación usando el `estado_final`
5. **Generar PDF**: Se genera el PDF de la liquidación invocando la función interna `generar_pdf_liquidacion_cliente.md`
6. **Actualizar registro**: Se actualiza el registro con estado_final, uri_pdf y se genera la liquidación

---

## User Scenarios & Testing (mandatory)

### User Story 1 - Generación de liquidación según estado final de entrega (Priority: P1)

Yo como Sistema Financiero necesito generar una liquidación para un cliente cuando el Módulo de Gestión de Transporte reporte llegada a un estado final de entrega. Para registrar el monto a cobrar según la efectividad de la distribución.

**Why this priority:** Es una función crítica para la conciliación financiera; sin esto, no se puede facturar ni cobrar a los clientes.

**Independent Test:** El Módulo de Gestión de Transporte envía diferentes estados finales y verificar que el cálculo del total sea correcto según la matriz.

**Acceptance Scenarios:**

1. **Scenario:** Liquidación con entrega exitosa (100% del monto)
   - **Given:** El Módulo de Gestión de Transporte envía el estado "Entregado Completo" con el id_pedido
   - **When:** Se desea generar la liquidación del cliente
   - **Then:** El sistema calcula el total = (Precio Pedido + Tarifa Envío) * 100% y genera una liquidación para el cliente.

2. **Scenario:** Liquidación con rechazo parcial (80% del monto)
   - **Given:** El Módulo de Gestión de Transporte envía el estado "Rechazo Parcial" con el id_pedido
   - **When:** Se desea generar la liquidación del cliente
   - **Then:** El sistema calcula el total = (Precio Pedido + Tarifa Envío) * 80%

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

- **¿Qué pasa si el precio del pedido tiene decimales muy largos?**
  - El sistema debe redondear al precio entero más cercano (Round Half Up).

- **¿Qué pasa si la tarifa de envío no está definida?**
  - El sistema debe rechazar y mostrar: "Tarifa de envío no configurada para la zona".

- **¿Qué pasa si el porcentaje de efectividad está fuera del rango 0-100%?**
  - El sistema debe rechazar y mostrar: "Porcentaje de efectividad inválido".

- **¿Qué pasa si el Módulo de Gestión de Transporte envía un estado que no existe en la matriz?**
  - El sistema debe rechazar y mostrar: "Estado de entrega no reconocido".

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir del evento del Módulo de Transporte el id_pedido y estado_final (ver spec `recibir_estado_final_modulo_transporte.md`).
- **FR-002:** El sistema DEBE obtener los datos del pedido desde la BD (recibidos del Módulo de Inventario, ver spec `recibir_datos_pedido_modulo_inventario.md`).
- **FR-003:** El sistema DEBE consultar la forma de pago del cliente mediante la función interna del servicio (no el endpoint), usando el `id_cliente` (Contra Entrega o Cartera Comercial).
- **FR-004:** El sistema DEBE aplicar una tarifa de envío base fija (valor pendiente por definir).
- **FR-005:** El sistema DEBE calcular el total a cobrar aplicando la fórmula: Total = (Precio Pedido + Tarifa Envío Base) * Porcentaje según matriz de liquidación del cliente.
- **FR-006:** El sistema DEBE bloquear la liquidación si falta precio del pedido, forma de pago o estado de entrega.
- **FR-007:** El sistema DEBE registrar la forma de pago en la liquidación.
- **FR-008:** El sistema DEBE guardar el precio_pedido como columna en el registro de liquidación.
- **FR-009:** El sistema DEBE mantener un registro inmutable de todas las liquidaciones generadas.
- **FR-010:** El sistema DEBE invocar la función interna de generación de PDF (spec `generar_pdf_liquidacion_cliente.md`) y guardar la URI retornada.

### Key Entities *(include if feature involves data)*

- **Liquidacion_Cliente:** 
  - [id_liquidacion, id_pedido, id_cliente, precio_pedido, tarifa_envio, total_calculado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf]
  - Entidad que registra el monto a cobrar al cliente.

**Notas de columnas:**
- `precio_pedido`: Se obtiene de los datos recibidos del Módulo de Inventario
- `tarifa_envio`: Tarifa base fija (valor pendiente por definir)
- `forma_pago`: Se obtiene consultando la función interna del servicio de forma de pago
- `uri_pdf`: Se genera mediante la función `generar_pdf_liquidacion_cliente.md`

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El 100% de las liquidaciones generadas deben reflejar correctamente el cálculo según la matriz de liquidación.

- **SC-002:** El sistema debe generar una liquidación en menos de 2 segundos después de recibir el estado final.

- **SC-003:** Todas las liquidaciones deben ser rastreables e inmutables para auditoría.

- **SC-004:** El sistema debe procesar al menos 1,000 liquidaciones diarias sin fallos críticos.

---

## Matriz de Liquidación de Cliente

| Estado Final de Entrega | % Por Cobrar |
| :--- | :--- |
| **Entregado Completo** | 100% |
| **Rechazo Parcial** | 80% |
| **Devolución (Error Empresa)** | 0% |
| **Faltante de Inventario** | Variable (solo productos entregados) |
| **No Entregado** | 0% |



