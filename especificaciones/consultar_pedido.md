# Feature Specification: Consultar Pedido - Consumo desde Sistema Financiero

**Created:** 07-03-2026  
**Status:** In Development

> **Nota:** Esta especificación documenta cómo el Sistema Financiero consume los endpoints del Módulo de Gestión de Inventario. Los endpoints son proporcionados por el Módulo de Inventario y el Sistema Financiero los consume.

---

## Endpoints Consumidos del Módulo de Gestión de Inventario

### 1. Consultar Productos del Pedido

**Endpoint:** `GET /api/v1/pedidos/{id_pedido}/productos`

**Propósito:** Obtener la lista de productos asociados a un pedido para generar la factura PDF.

**Parámetros de consulta:**
- `id_pedido` (path, requerido): ID del pedido

**Respuesta exitosa:**
```json
{
  "productos": [
    {
      "id_producto": "501",
      "nombre": "Gaseosa 1L",
      "cantidad": 10,
      "precio_unitario": 5000,
      "subtotal": 50000
    },
    {
      "id_producto": "502",
      "nombre": "Agua 1L",
      "cantidad": 5,
      "precio_unitario": 3000,
      "subtotal": 15000
    }
  ]
}
```

**DTO de respuesta (ProductoPedidoDTO):**
- `id_producto`: String - Identificador del producto
- `nombre`: String - Nombre del producto
- `cantidad`: Integer - Cantidad solicitada
- `precio_unitario`: Integer - Precio por unidad
- `subtotal`: Integer - Cantidad × precio_unitario

> **Nota para el Sistema Financiero:** Este DTO debe ser mapeado a un DTO interno del Sistema Financiero para evitar acoplamiento con el Módulo de Inventario.

**Casos de error:**
- Pedido no encontrado: `"Pedido no encontrado con el ID proporcionado"`
- ID inválido: `"ID de pedido inválido"`

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de productos del pedido para facturación (Priority: P1)

Yo como Módulo de Facturación necesito consultar los productos asociados a un pedido específico utilizando su ID. Para obtener los datos de los productos como lista de DTOs y usarlos como parámetros para generar el PDF de la factura.

**Why this priority:** Es una función crítica para generar facturas válidas; sin los productos no se puede emitir la facturación.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de productos del pedido (lista de DTOs)
   - **Given:** Existe un pedido con ID válido en la base de datos del Módulo de Gestión de Inventario
   - **When:** El Sistema Financiero envía una solicitud de consulta con ese ID de pedido
   - **Then:** El sistema retorna una lista de DTOs con los datos de cada producto (id_producto, nombre, cantidad, precio_unitario, subtotal)

2. **Scenario:** Pedido con múltiples productos
   - **Given:** Un pedido contiene múltiples productos diferentes
   - **When:** Se consulta el pedido por su ID
   - **Then:** El sistema retorna todos los productos como lista de DTOs

---

### User Story 2 - Consulta de valor total del pedido (Priority: P1)

Yo como Sistema Financiero necesito conocer el valor total del pedido para calcular la liquidación. Para garantizar que la liquidación refleje el valor exacto a cobrar.

**Why this priority:** Es fundamental para la precisión de la liquidación financiera.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa del valor total
   - **Given:** Existe un pedido con productos y precios definidos
   - **When:** Se consulta el total del pedido por su ID
   - **Then:** El sistema retorna el valor total del pedido

---


### User Story 3 - Validación de existencia del pedido (Priority: P1)

Yo como Sistema Financiero necesito validar que el pedido existe antes de intentar procesarlo. Para evitar errores en el flujo de liquidación.

**Why this priority:** Previene errores en el proceso de liquidación.

**Acceptance Scenarios:**

1. **Scenario:** Pedido no encontrado
   - **Given:** Se intenta consultar un pedido con un ID que no existe en la base de datos
   - **When:** El Sistema Financiero envía la solicitud de consulta
   - **Then:** El sistema retorna un error indicando que el pedido no fue encontrado

---

### Edge Cases

- **¿Qué pasa si el ID del pedido es inválido (no es un número)?**
  - El endpoint debe retornar un error: "ID de pedido inválido"

- **¿Qué pasa si la consulta al Módulo de Inventario falla?**
  - El Sistema Financiero debe manejar el error de conexión: "Error al consultar el Módulo de Inventario. Intente más tarde"

- **¿Qué pasa si el pedido no tiene productos (pedido vacío)?**
  - El endpoint debe retornar una lista vacía o un error indicando que el pedido no tiene productos

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El Sistema Financiero debe obtener los productos del pedido en menos de 1 segundo después de recibir la solicitud.

- **SC-002:** El 100% de las consultas con IDs válidos deben retornar la información correcta.

- **SC-003:** El sistema debe retornar un mensaje de error apropiado en menos de 500ms cuando el pedido no existe.

- **SC-004:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultáneas.

---

