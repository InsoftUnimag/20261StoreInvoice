# Feature Specification: Consultar Forma de Pago del Cliente

**Created:** 11-03-2026  
**Status:** In Development

## Descripción del Flujo

El Sistema Financiero guarda la forma de pago de cada cliente en su propia base de datos.

---

## Endpoints del Sistema Financiero

### Consultar Forma de Pago por ID de Pedido (síncrono)

**Endpoint:** `GET /api/v1/pedidos/{id_pedido}/forma-pago`

**Parámetros:**
- `id_pedido` (path): ID del pedido

**Respuesta:**
```json
{
  "id_pedido": 123,
  "forma_pago": "CARTERA_COMERCIAL"
}
```

**Casos de error:**
- Pedido no encontrado: `"Pedido no encontrado"`
- Cliente sin forma de pago: `"El cliente no tiene forma de pago registrada"`

---

### Verificar si Cliente tiene Forma de Pago (síncrono)

**Endpoint:** `GET /api/v1/pedidos/{id_pedido}/tiene-forma-pago`

**Parámetros:**
- `id_pedido` (path): ID del pedido

**Respuesta:**
```json
{
  "id_pedido": 123, //debe ser id Cliente, corregir
  "tiene_forma_pago": true
}
```

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Buscar forma de pago por ID de Cliente (necesidad interna)

Yo como Sistema Financiero necesito consultar la forma de pago de un cliente por su ID. Para usar en funciones internas como la generación de liquidaciones.

**Why this priority:** Necesario para el proceso de liquidación del cliente.

**Acceptance Scenarios:**

1. **Scenario:** Cliente con forma de pago Contra Entrega
   - **Given:** Existe un cliente con forma de pago registrada
   - **When:** Se consulta por id_cliente
   - **Then:** Retorna "CONTRA_ENTREGA"

2. **Scenario:** Cliente con forma de pago Cartera Comercial
   - **Given:** Existe un cliente con forma de pago registrada
   - **When:** Se consulta por id_cliente
   - **Then:** Retorna "CARTERA_COMERCIAL"

3. **Scenario:** Cliente no encontrado
   - **Given:** Se consulta un cliente que no existe
   - **When:** Se consulta por id_cliente
   - **Then:** Retorna error

4. **Scenario:** Cliente sin forma de pago
   - **Given:** Existe un cliente sin forma de pago asignada
   - **When:** Se consulta por id_cliente
   - **Then:** Retorna error

---

### Edge Cases

- ¿Qué pasa si el ID del cliente es inválido?
  - Retorna error: Cliente no encontrado

- ¿Qué pasa si la consulta a la base de datos falla?
  - Retorna error: Error al consultar la forma de pago

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE exponer un endpoint GET `/api/v1/pedidos/{id_pedido}/forma-pago`.
- **FR-002:** El sistema DEBE exponer una función interna que busque forma de pago por `id_cliente`.
- **FR-003:** El sistema DEBE retornar la forma de pago como string (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`).
- **FR-004:** El sistema DEBE retornar error cuando el pedido o cliente no exista.
- **FR-005:** El sistema DEBE retornar error cuando el cliente no tenga forma de pago asignada.
- **FR-006:** El sistema DEBE exponer un endpoint GET `/api/v1/pedidos/{id_pedido}/tiene-forma-pago` que retorne un booleano.

### Key Entities

- **Forma_Pago_Cliente:** [id_cliente, forma_pago, fecha_registro]

---

## Success Criteria

- **SC-001:** El sistema debe retornar la forma de pago en menos de 500ms.
- **SC-002:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultáneas.
