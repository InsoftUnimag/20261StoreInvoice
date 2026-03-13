# Feature Specification - Registrar forma de pago del cliente

**Status:** Terminada  
**Created:** 24-02-2026

---

## Proceso Interno del Sistema Financiero

### Registrar Forma de Pago del Cliente

**Este es un proceso interno, no un endpoint.**

**Flujo:**

1. El Asesor Comercial consulta el cliente por ID Nacional usando el endpoint `GET /api/v1/clientes/{id_nacional}` del Módulo de Gestión de Clientes (ver spec `consultar_cliente.md`).
2. Obtiene el `id_cliente` (ID de base de datos) de la respuesta.
3. El Sistema Financiero guarda la forma de pago asociada al `id_cliente`.

**Parámetros de entrada:**
- `id_nacional` (String): Número de documento del cliente
- `forma_pago` (String): Forma de pago a registrar (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`)

**Formas de pago válidas:**
- `CONTRA_ENTREGA` - Pago contra entrega
- `CARTERA_COMERCIAL` - Cartera comercial (crédito)

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Asignación de forma de pago a cliente (Priority: P1)

Yo como Asesor Comercial necesito registrar la forma de pago (Contra Entrega o Cartera Comercial) de un cliente. Para establecer las reglas de recaudo antes de generar pedidos.

**Acceptance Scenarios:**

1. **Scenario:** Registro exitoso de forma de pago
   - **Given:** El asesor consulta el cliente por ID Nacional y obtiene el id_cliente
   - **When:** Selecciona "CARTERA_COMERCIAL" y guarda
   - **Then:** Se guarda la forma de pago asociada al cliente

2. **Scenario:** Cliente no encontrado
   - **Given:** El ID Nacional no existe en el sistema
   - **When:** Se intenta registrar la forma de pago
   - **Then:** Se muestra error "Cliente no encontrado"

3. **Scenario:** Forma de pago inválida
   - **Given:** Se intenta guardar una forma de pago que no es válida
   - **When:** Se envía el registro
   - **Then:** Se muestra error "Forma de pago inválida"

---

## Requirements (mandatory)

### Functional Requirements

- **FR-001:** El sistema DEBE permitir registrar la forma de pago de un cliente.
- **FR-002:** El sistema DEBE validar que el cliente exista en la base de datos (usando el endpoint del spec `consultar_cliente.md`).
- **FR-003:** El sistema DEBE guardar la forma de pago asociada al ID de BD del cliente.
- **FR-004:** El sistema DEBE validar que la forma de pago sea `CONTRA_ENTREGA` o `CARTERA_COMERCIAL`.

## Key Entities *(include if feature involves data)*

### Forma_Pago_Cliente

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id_cliente` | Integer | Sí | ID del cliente en la base de datos |
| `forma_pago` | String | Sí | Forma de pago: `CONTRA_ENTREGA` o `CARTERA_COMERCIAL` |
| `fecha_registro` | DateTime | Sí | Fecha y hora del registro |

---

## Success Criteria (mandatory)

### Measurable Outcomes

- **SC-001:** El sistema debe guardar la forma de pago en menos de 500ms.
- **SC-002:** El cambio de forma de pago debe estar disponible inmediatamente para nuevos pedidos.
