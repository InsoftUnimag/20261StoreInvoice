# Feature Specification: Generar PDF de LiquidaciÃ³n del Cliente

**Created:** 12-03-2026  
**Status:** In Development

## DescripciÃ³n del Flujo

El Sistema Financiero genera un PDF con la informaciÃ³n de la liquidaciÃ³n del cliente. Este PDF se sube a un sistema de almacenamiento en la nube y se guarda la URI en el registro de la liquidaciÃ³n.

**Esta es una funciÃ³n interna del sistema, no expone un endpoint.**

> **Nota:** Los datos del pedido (`id_pedido`, `id_cliente`, `total_pedido`, `forma_pago`) se reciben como parÃ¡metros. Los productos se consultan en tiempo real al MÃ³dulo de Inventario via `InventarioServicePort`. Los datos del cliente se consultan al MÃ³dulo de GestiÃ³n de Clientes via `ClienteServicePort`. Esta funciÃ³n recibe todo como parÃ¡metros para construir el contenido del PDF.

---

## Entradas de la FunciÃ³n

La funciÃ³n recibe los siguientes parÃ¡metros:

| ParÃ¡metro | Tipo | DescripciÃ³n |
|-----------|------|-------------|
| `productos` | List[ProductoPedidoDTO] | Lista de productos del pedido (consultados del MÃ³dulo de Inventario) |
| `total_pedido` | Integer | Valor total del pedido |
| `forma_pago` | String | Forma de pago del cliente (CONTRA_ENTREGA o CARTERA_COMERCIAL) |
| `cliente` | ClienteLiquidacionDTO | Datos del cliente consultados del MÃ³dulo de GestiÃ³n de Clientes |
| `id_pedido` | Integer | ID del pedido |

> **Nota sobre ClienteLiquidacionDTO:** Los campos del cliente se obtienen del MÃ³dulo de GestiÃ³n de Clientes via `ClienteServicePort`. Campos definidos:
> - `idCliente` (Long): ID del cliente
> - `idNacional` (String): Documento de identidad nacional
> - `nombre` (String): Nombre completo del cliente
> - `telefono` (String): TelÃ©fono de contacto
> - `direccion` (String): DirecciÃ³n de entrega

---

## Proceso

1. **Construir contenido del PDF**: Se genera el contenido del PDF con los datos de la liquidaciÃ³n
2. **Generar archivo PDF**: Se crea el documento en formato PDF
3. **Subir a Supabase Storage**: Se sube el archivo a Supabase Storage (bucket pÃºblico `liquidaciones-pdf`)
4. **Retornar URI**: Se retorna la URL/path donde seÃ±ala al archivo

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - GeneraciÃ³n de PDF de liquidaciÃ³n (Priority: P1)

Yo como Sistema Financiero necesito generar un PDF con la informaciÃ³n de la liquidaciÃ³n para que el cliente pueda visualizar o descargar su factura.

**Why this priority:** Es necesario para que el cliente tenga un documento fÃ­sico de su liquidaciÃ³n.

**Acceptance Scenarios:**

1. **Scenario:** GeneraciÃ³n exitosa del PDF
   - **Given:** Se tienen todos los datos necesarios (productos, total, cliente, forma de pago)
   - **When:** Se invoca la funciÃ³n para generar el PDF
   - **Then:** El sistema genera el PDF, lo sube a la nube y retorna la URI

2. **Scenario:** PDF con mÃºltiples productos
   - **Given:** El pedido tiene mÃºltiples productos
   - **When:** Se genera el PDF
   - **Then:** El PDF contiene todos los productos con su cantidad, precio unitario y subtotal

---

### Edge Cases

- **Â¿QuÃ© pasa si la subir a la nube falla?**
  - El sistema debe retornar un error: "Error al subir el PDF a la nube. Intente mÃ¡s tarde"

- **Â¿QuÃ© pasa si algÃºn parÃ¡metro estÃ¡ vacÃ­o o es invÃ¡lido?**
  - El sistema debe validar los parÃ¡metros y retornar error si faltan datos requeridos

- **Â¿QuÃ© pasa si el contenido del PDF es muy grande?**
  - El sistema debe manejar el tamaÃ±o mÃ¡ximo permitido por el almacenamiento en la nube

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir como parÃ¡metros la lista de productos, total del pedido, forma de pago, datos del cliente y ID del pedido.
- **FR-002:** El sistema DEBE generar un PDF con el contenido de la liquidaciÃ³n.
- **FR-003:** El sistema DEBE subir el PDF generado a un sistema de almacenamiento en la nube.
- **FR-004:** El sistema DEBE retornar la URI donde seÃ±ala al PDF.
- **FR-005:** El sistema DEBE validar que todos los parÃ¡metros requeridos estÃ©n presentes antes de generar el PDF.
- **FR-006:** El sistema DEBE manejar errores al subir a la nube y retornar un mensaje apropiado.

### Key Entities *(include if data)*

**ProductoPedidoDTO (entrada):**
- `idProducto` (Long): ID del producto
- `nombre` (String): Nombre del producto
- `cantidad` (Integer): Cantidad ordenada
- `precioUnitario` (BigDecimal): Precio unitario
- `subtotal` (BigDecimal): Subtotal (cantidad Ã— precio unitario)

**ClienteLiquidacionDTO (entrada):**
- `idCliente` (Long): ID del cliente
- `idNacional` (String): Documento de identidad nacional
- `nombre` (String): Nombre completo del cliente
- `telefono` (String): TelÃ©fono de contacto
- `direccion` (String): DirecciÃ³n de entrega

**Salida:**
- [uri_pdf]: URL/path donde seÃ±ala al PDF

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El PDF debe generarse en menos de 3 segundos.

- **SC-002:** El PDF debe contener todos los productos del pedido con su informaciÃ³n correcta.

- **SC-003:** La URI retornada debe permitir acceder al PDF en la nube.

- **SC-004:** El sistema debe manejar errores de manera apropiada y retornar mensajes claros.

