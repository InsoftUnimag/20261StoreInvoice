# Feature Specification: Ingresar tasa de efectividad de la distribución del pedido

**Created:** 21-02-2026  
**Status:** In Development

## Descripción del Flujo

El Módulo de Gestión de Transporteavisará al Sistema Financiero cuando un pedido alcance un estado final de entrega. En ese momento, el Sistema Financiero recibirá:
- `id_pedido`
- `estado_final` (ver matriz de correspondencia)
- `tasa_efectividad` (porcentaje 0-100%)

**Estados finales válidos (Matriz de Correspondencia):**

| Estado Final de Entrega | Descripción |
| :--- | :--- |
| **Entregado Completo** | El pedido fue entregado exitosamente al cliente |
| **Rechazo Parcial** | El cliente rechazó algunos productos |
| **Devolución (Error Empresa)** | El pedido fue devuelto por error de la empresa |
| **Faltante de Inventario** | No había suficientes productos en inventario |
| **No Entregado** | El pedido no pudo ser entregado por el transportista |

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Recepción de tasa de efectividad desde el Módulo de Gestión de Transporte (Priority: P1)

Yo como Sistema Financiero necesito recibir la tasa de efectividad, el estado final y el ID del pedido desde el Módulo de Gestión de Transporte. Para almacenar la información necesaria para calcular la liquidación del transportista y del cliente.

**Why this priority**: Es el punto de entrada de datos para todo el proceso de liquidación.

**Independent Test**: Verificar que el sistema reciba correctamente todos los parámetros enviados por el Módulo de Gestión de Transporte.

**Acceptance Scenarios**:

1. **Scenario:** Recepción exitosa de datos
   - **Given:** El Módulo de Gestión de Transporte envía id_pedido, tasa_efectividad y estado_final válidos
   - **When:** Se recibe la información de tasa de efectividad
   - **Then:** El sistema guarda la tasa de efectividad asociada al pedido en la base de datos

2. **Scenario:** Estado final válido
   - **Given:** El Módulo de Gestión de Transporte envía un estado_final de la matriz de correspondencia
   - **When:** Se recibe la información
   - **Then:** El sistema valida que el estado sea uno de los válidos y lo almacena


---


---

### Edge Cases

- **¿Qué pasa si el Módulo de Gestión de Transporte no envía la información?**
  - El sistema debe registrar error y no permitir liquidación.

- **¿Qué pasa si la tasa es mayor a 100% o menor a 0%?**
  - El sistema debe rechazar el valor y mostrar: "Tasa de efectividad inválida. Debe estar entre 0% y 100%".

- **¿Qué pasa si un pedido cambia de estado después de liquidado?**
  - Debe generarse ajuste o nota de corrección.

- **¿Qué pasa si se envían datos incompletos?**
  - El sistema debe devolver un error: "Datos incompletos. Se requiere: id_pedido, tasa_efectividad, estado_final".

- **¿Qué pasa si el estado_final no está en la matriz de correspondencia?**
  - El sistema debe rechazar y mostrar: "Estado final no reconocido".

- **¿Qué pasa si el id_pedido no existe en el sistema?**
  - El sistema debe rechazar y mostrar: "Pedido no encontrado".


## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE recibir del Módulo de Gestión de Transporte los siguientes datos: id_pedido, tasa_efectividad, estado_final.
- **FR-002**: El sistema DEBE almacenar la tasa de efectividad asociada al pedido.
- **FR-003**: El sistema DEBE validar que la tasa_efectividad esté entre 0% y 100%.
- **FR-004**: El sistema DEBE validar que el estado_final sea uno de los estados válidos de la matriz de correspondencia.
- **FR-005**: El sistema DEBE validar que el id_pedido exista en el sistema antes de guardar la tasa.
- **FR-006**: El sistema DEBE rechazar datos incompletos y devolver error si falta algún parámetro requerido.
- **FR-007**: El sistema DEBE permitir actualizar la tasa de efectividad solo si el pedido no ha sido liquidado anteriormente.

### Key Entities *(include if feature involves data)*

- **Tasa_Efectividad**:
  - [id_tasa_efectividad, id_pedido, tasa_efectividad, estado_final, fecha_registro]
  - Representa el registro de la tasa de efectividad del pedido.
  - Entidad donde se almacena la información recibida del Módulo de Gestión de Transporte.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El 100% de las tasas de efectividad recibidas del Módulo de Gestión de Transporte deben almacenarse correctamente en la base de datos sin pérdida de información.

- **SC-002:** El sistema debe validar y guardar la tasa de efectividad en menos de 1 segundo después de recibir los datos.

- **SC-003:** El sistema debe rechazar el 100% de las tasas inválidas (mayores a 100% o menores a 0%).

- **SC-004:** El sistema debe rechazar el 100% de los estados finales que no estén en la matriz de correspondencia.

- **SC-005:** El sistema debe procesar al menos 500 registros de tasa de efectividad diarias sin fallos críticos.

