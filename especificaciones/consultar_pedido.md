# Feature Specification: Consultar Pedido - Consumo desde Sistema Financiero

**Created:** 07-03-2026  
**Status:** In Development

> **Nota:** Esta especificaciÃ³n documenta cÃ³mo el Sistema Financiero consume los endpoints del MÃ³dulo de GestiÃ³n de Inventario. Los endpoints son proporcionados por el MÃ³dulo de Inventario y el Sistema Financiero los consume.

---

## Endpoints Consumidos del MÃ³dulo de GestiÃ³n de Inventario

### 1. Consultar Productos del Pedido

**Endpoint:** `GET /api/v1/pedidos/{id_pedido}/productos`

**PropÃ³sito:** Obtener la lista de productos asociados a un pedido para generar la factura PDF.

**ParÃ¡metros de consulta:**
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
- `subtotal`: Integer - Cantidad Ã— precio_unitario

> **Nota para el Sistema Financiero:** Este DTO debe ser mapeado a un DTO interno del Sistema Financiero para evitar acoplamiento con el MÃ³dulo de Inventario.

**Casos de error:**
- Pedido no encontrado: `"Pedido no encontrado con el ID proporcionado"`
- ID invÃ¡lido: `"ID de pedido invÃ¡lido"`

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de productos del pedido para facturaciÃ³n (Priority: P1)

Yo como MÃ³dulo de FacturaciÃ³n necesito consultar los productos asociados a un pedido especÃ­fico utilizando su ID. Para obtener los datos de los productos como lista de DTOs y usarlos como parÃ¡metros para generar el PDF de la factura.

**Why this priority:** Es una funciÃ³n crÃ­tica para generar facturas vÃ¡lidas; sin los productos no se puede emitir la facturaciÃ³n.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de productos del pedido (lista de DTOs)
   - **Given:** Existe un pedido con ID vÃ¡lido en la base de datos del MÃ³dulo de GestiÃ³n de Inventario
   - **When:** El Sistema Financiero envÃ­a una solicitud de consulta con ese ID de pedido
   - **Then:** El sistema retorna una lista de DTOs con los datos de cada producto (id_producto, nombre, cantidad, precio_unitario, subtotal)

2. **Scenario:** Pedido con mÃºltiples productos
   - **Given:** Un pedido contiene mÃºltiples productos diferentes
   - **When:** Se consulta el pedido por su ID
   - **Then:** El sistema retorna todos los productos como lista de DTOs

---

### User Story 2 - ValidaciÃ³n de existencia del pedido (Priority: P1)

Yo como Sistema Financiero necesito validar que el pedido existe antes de intentar procesarlo. Para evitar errores en el flujo de liquidaciÃ³n.

**Why this priority:** Previene errores en el proceso de liquidaciÃ³n.

**Acceptance Scenarios:**

1. **Scenario:** Pedido no encontrado
   - **Given:** Se intenta consultar un pedido con un ID que no existe en la base de datos
   - **When:** El Sistema Financiero envÃ­a la solicitud de consulta
   - **Then:** El sistema retorna un error indicando que el pedido no fue encontrado

---

### Edge Cases

- **Â¿QuÃ© pasa si el ID del pedido es invÃ¡lido (no es un nÃºmero)?**
  - El endpoint debe retornar un error: "ID de pedido invÃ¡lido"

- **Â¿QuÃ© pasa si la consulta al MÃ³dulo de Inventario falla?**
  - El Sistema Financiero debe manejar el error de conexiÃ³n: "Error al consultar el MÃ³dulo de Inventario. Intente mÃ¡s tarde"

- **Â¿QuÃ© pasa si el pedido no tiene productos (pedido vacÃ­o)?**
  - El endpoint debe retornar una lista vacÃ­a o un error indicando que el pedido no tiene productos

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El Sistema Financiero debe obtener los productos del pedido en menos de 1 segundo despuÃ©s de recibir la solicitud.

- **SC-002:** El 100% de las consultas con IDs vÃ¡lidos deben retornar la informaciÃ³n correcta.

- **SC-003:** El sistema debe retornar un mensaje de error apropiado en menos de 500ms cuando el pedido no existe.

- **SC-004:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultÃ¡neas.

---


