# Feature Specification - Ingresar forma de pago del cliente

**Status:** Terminada  
**Created:** 24-02-2026

---

## Endpoint del Sistema Financiero

### Registrar Forma de Pago del Cliente

**Endpoint:** `POST /api/v1/clientes/{id_cliente}/forma-pago`

**Propósito:** Registrar o actualizar la forma de pago de un cliente.

**Parámetros:**
- `id_cliente` (path, requerido): ID del cliente en la base de datos
- Body (JSON):
```json
{
  "forma_pago": "CARTERA_COMERCIAL"
}
```

**Formas de pago válidas:**
- `CONTRA_ENTREGA` - Pago contra entrega
- `CARTERA_COMERCIAL` - Cartera comercial (crédito)

**Respuesta exitosa (200):**
```json
{
  "id_cliente": 100,
  "forma_pago": "CARTERA_COMERCIAL",
  "mensaje": "Forma de pago actualizada exitosamente"
}
```

**Casos de error:**
- Cliente no encontrado: `404 - "Cliente no encontrado con el ID proporcionado"`
- Forma de pago inválida: `400 - "Forma de pago inválida. Valores permitidos: CONTRA_ENTREGA, CARTERA_COMERCIAL"`

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Asignación de modalidad de pago a cliente nuevo o existente (Priority: P1)

Yo como Asesor Comercial necesito registrar la forma de pago (Contra Entrega o Cartera Comercial) que tendrá un cliente. Para establecer las reglas de recaudo antes de generar pedidos.

**Why this priority:** Define si el conductor recibe el efectivo o se tiene en cuenta para credito  
**Independent Test**: Seleccionar un cliente en la base de datos y asignarle "Cartera Comercial", verificando que la actualización sea exitosa.

**Acceptance Scenarios:**
1. Scenario: Asignación exitosa a cliente existente
	- **Given:** Un cliente previamente registrado en el sistema (primero se consulta su ID de BD usando el endpoint del spec `consultar_cliente.md` con el ID Nacional)
	- **When:** El asesor selecciona la opción Pago Contra Entrega y guarda los cambios
	- **Then:** El perfil del cliente se actualiza con la nueva forma de pago

2. Scenario: Cliente no registrado
	- **Given**: Un intento de asignar forma de pago
	- **When:** El asesor busca un ID Nacional y no existe en la base de datos (al consultar con el spec `consultar_cliente.md`)
	- **Then:** El sistema solicita ejecutar Registrar cliente antes de continuar

### Edge Cases

- El asesor ingresa caracteres no numéricos o espacios en blanco en el campo de búsqueda de ID Nacional?
- El sistema debe mostrar un mensaje de error en lugar de fallar.

- El cliente no existe al consultar con ID Nacional?
- El sistema debe mostrar error y solicitar registrar al cliente primero (usar el endpoint del spec `consultar_cliente.md` para validar).

---


## Requirements (mandatory)

### Functional Requirements

- **FR-001**: System MUST permitir la selección entre al menos dos formas de pago: Pago Contra Entrega y Cartera Comercial.
- **FR-002:** System MUST requerir que el cliente exista en la base de datos antes de guardar la forma de pago.
- **FR-003:** System MUST primero consultar el ID de BD del cliente usando el endpoint del spec `consultar_cliente.md` con el ID Nacional antes de guardar la forma de pago.
- **FR-004:** System MUST guardar la forma de pago asociada al ID de BD del cliente.

## Key Entities *(include if feature involves data)*

### Forma_Pago_Cliente

Entidad que almacena la forma de pago de cada cliente en el Sistema Financiero.

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id_cliente` | Integer | Sí | ID del cliente en la base de datos del Sistema Financiero |
| `forma_pago` | String | Sí | Forma de pago del cliente: `CONTRA_ENTREGA` o `CARTERA_COMERCIAL` |
| `fecha_registro` | DateTime | Sí | Fecha y hora del registro de la forma de pago |

> **Nota:** El `id_cliente` se obtiene consultando primero el ID de BD del cliente mediante el endpoint del Módulo de Gestión de Clientes (spec `consultar_cliente.md`), usando el ID Nacional del cliente.

---

## Success Criteria (mandatory)

### Measurable Outcomes

- **SC-001:** Todos los clientes en la base de datos deben tener una forma de pago obligatoria asociada para poder generarles un pedido.
- **SC-002:** El cambio de forma de pago debe actualizarse en tiempo real para no afectar los pedidos que se generen.
