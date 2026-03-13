# Feature Specification: Consultar Forma de Pago del Cliente

**Created:** 11-03-2026  
**Status:** In Development

## Descripción del Flujo

El Sistema Financiero guarda la forma de pago de cada cliente en su propia base de datos. Los datos se almacenan con:
- `id_cliente`: ID de base de datos del cliente
- `forma_pago`: Forma de pago del cliente (CONTRA_ENTREGA o CARTERA_COMERCIAL)

> **Nota:** Este endpoint es consumido por el **Módulo de Gestión de Transporte** para determinar cómo realizar el cobro al momento de la entrega (efectivo vs crédito).
> 
> *El Sistema Financiero usa internamente la función del servicio, no este endpoint.*
>
> Este endpoint también es consumido por el **Módulo de Inventario** para verificar si el cliente tiene forma de pago registrada antes de crear un pedido.

---

## Endpoint del Sistema Financiero

### Consultar Forma de Pago del Cliente

**Endpoint:** `GET /api/v1/clientes/{id_cliente}/forma-pago`

**Propósito:** Obtener la forma de pago asociada a un cliente (Contra Entrega o Cartera Comercial).

**Parámetros de consulta:**
- `id_cliente` (path, requerido): ID de base de datos del cliente

**Respuesta exitosa:**
```json
{
  "id_cliente": "100",
  "forma_pago": "CARTERA_COMERCIAL"
}
```

**Formas de pago válidas:**
- `CONTRA_ENTREGA` - Pago contra entrega
- `CARTERA_COMERCIAL` - Cartera comercial (crédito)

**Casos de error:**
- Cliente no encontrado: `"Cliente no encontrado con el ID proporcionado"`
- Cliente sin forma de pago asignada: `"El cliente no tiene forma de pago registrada"`

---

### Verificar si Cliente tiene Forma de Pago

**Endpoint:** `GET /api/v1/clientes/{id_cliente}/tiene-forma-pago`

**Propósito:** Verificar si un cliente tiene forma de pago registrada. Consumido por el Módulo de Inventario antes de crear un pedido.

**Parámetros de consulta:**
- `id_cliente` (path, requerido): ID de base de datos del cliente

**Respuesta exitosa:**
```json
{
  "id_cliente": 100,
  "tiene_forma_pago": true
}
```

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Verificar forma de pago desde Módulo de Inventario (Priority: P1)

Yo como Módulo de Inventario necesito verificar si un cliente tiene forma de pago registrada antes de crear un pedido. Para evitar crear pedidos de clientes sin forma de pago asignada.

**Acceptance Scenarios:**

1. **Scenario:** Cliente tiene forma de pago
   - **Input:** GET `/api/v1/clientes/100/tiene-forma-pago`
   - **Output:** `200 OK` → `{ "id_cliente": 100, "tiene_forma_pago": true }`

2. **Scenario:** Cliente no tiene forma de pago
   - **Input:** GET `/api/v1/clientes/100/tiene-forma-pago`
   - **Output:** `200 OK` → `{ "id_cliente": 100, "tiene_forma_pago": false }`

3. **Scenario:** Cliente no encontrado
   - **Input:** GET `/api/v1/clientes/999/tiene-forma-pago`
   - **Output:** `404 Not Found` → `"Cliente no encontrado con el ID proporcionado"`

---

### User Story 2 - Contrato del Endpoint

**Expectativas del Endpoint:**

- **Input (Qué espero recibir):**
  - Path: `id_cliente` (Integer) - ID del cliente en la base de datos

- **Output (Qué voy a responder):**
  - Éxito (200): `{ "id_cliente": "100", "forma_pago": "CARTERA_COMERCIAL" }`
  - Error (404): `"Cliente no encontrado con el ID proporcionado"`
  - Error (404): `"El cliente no tiene forma de pago registrada"`

**Acceptance Scenarios:**

1. **Scenario:** Cliente con forma de pago Contra Entrega
   - **Input:** GET `/api/v1/clientes/100/forma-pago`
   - **Output:** `200 OK` → `{ "id_cliente": "100", "forma_pago": "CONTRA_ENTREGA" }`

2. **Scenario:** Cliente con forma de pago Cartera Comercial
   - **Input:** GET `/api/v1/clientes/100/forma-pago`
   - **Output:** `200 OK` → `{ "id_cliente": "100", "forma_pago": "CARTERA_COMERCIAL" }`

3. **Scenario:** Cliente no encontrado
   - **Input:** GET `/api/v1/clientes/999/forma-pago`
   - **Output:** `404 Not Found` → `"Cliente no encontrado con el ID proporcionado"`

4. **Scenario:** Cliente sin forma de pago asignada
   - **Input:** GET `/api/v1/clientes/100/forma-pago`
   - **Output:** `404 Not Found` → `"El cliente no tiene forma de pago registrada"`

### User Story 3 - Consulta interna de forma de pago para liquidación (Priority: P1)

Yo como Sistema Financiero necesito consultar la forma de pago de un cliente para registrar cómo debe realizarse el cobro en la liquidación. Para identificar si es recaudo en efectivo (Contra Entrega) o factura a crédito (Cartera Comercial).

**Why this priority:** Es necesario para registrar la forma de pago en la liquidación y gestionar el flujo de caja.

**Nota:** Esta consulta se realiza mediante función interna del servicio, no a través del endpoint.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de forma de pago (Contra Entrega)
   - **Given:** Existe un cliente registrado con forma de pago "Contra Entrega"
   - **When:** Se consulta la forma de pago con el id_cliente
   - **Then:** El sistema retorna `"forma_pago": "CONTRA_ENTREGA"`

2. **Scenario:** Consulta exitosa de forma de pago (Cartera Comercial)
   - **Given:** Existe un cliente registrado con forma de pago "Cartera Comercial"
   - **When:** Se consulta la forma de pago con el id_cliente
   - **Then:** El sistema retorna `"forma_pago": "CARTERA_COMERCIAL"`

3. **Scenario:** Cliente sin forma de pago asignada
   - **Given:** Existe un cliente registrado pero sin forma de pago asignada
   - **When:** Se consulta la forma de pago con el id_cliente
   - **Then:** El sistema retorna un error indicando que el cliente no tiene forma de pago registrada

---

### Edge Cases

- **¿Qué pasa si el ID del cliente es inválido?**
  - El sistema debe retornar un error: "Cliente no encontrado con el ID proporcionado"

- **¿Qué pasa si la consulta a la base de datos falla?**
  - El sistema debe manejar el error y retornar: "Error al consultar la forma de pago. Intente más tarde"

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE exponer un endpoint GET que reciba el `id_cliente` y retorne la forma de pago registrada (para consumo del Módulo de Transporte).
- **FR-002:** El sistema DEBE exponer una función interna de servicio que consulte la forma de pago por `id_cliente` (para uso del Sistema Financiero).
- **FR-003:** El sistema DEBE retornar la forma de pago como string (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`).
- **FR-004:** El sistema DEBE retornar un error cuando el cliente no exista en la base de datos.
- **FR-005:** El sistema DEBE retornar un error cuando el cliente no tenga forma de pago asignada.
- **FR-006:** El sistema DEBE exponer un endpoint GET `/api/v1/clientes/{id_cliente}/tiene-forma-pago` que retorne un booleano (para consumo del Módulo de Inventario).

### Key Entities *(include if feature involves data)*

- **Forma_Pago_Cliente:**
  - [id_cliente, forma_pago]
  - Entidad que almacena la forma de pago de cada cliente.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe retornar la forma de pago en menos de 500ms después de recibir la solicitud.

- **SC-002:** El 100% de las consultas con ID de cliente válido deben retornar la forma de pago correcta.

- **SC-003:** El sistema debe retornar un mensaje de error apropiado cuando el cliente no existe o no tiene forma de pago.

- **SC-004:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultáneas.
