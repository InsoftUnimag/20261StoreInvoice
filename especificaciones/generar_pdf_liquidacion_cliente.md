# Feature Specification: Generar PDF de Liquidación del Cliente

**Created:** 12-03-2026  
**Status:** In Development

## Descripción del Flujo

El Sistema Financiero genera un PDF con la información de la liquidación del cliente. Este PDF se sube a un sistema de almacenamiento en la nube y se guarda la URI en el registro de la liquidación.

**Esta es una función interna del sistema, no expone un endpoint.**

---

## Entradas de la Función

La función recibe los siguientes parámetros:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `productos` | List[ProductoPedidoDTO] | Lista de productos del pedido (consultados del Módulo de Inventario) |
| `total_pedido` | Integer | Valor total del pedido |
| `forma_pago` | String | Forma de pago del cliente (CONTRA_ENTREGA o CARTERA_COMERCIAL) |
| `cliente` | ClienteDTO | Datos del cliente (ver nota) |
| `id_pedido` | Integer | ID del pedido |

> **Nota sobre ClienteDTO:** Los campos del cliente se obtendrán del Módulo de Gestión de Clientes. Pendiente definir: id_cliente, id_nacional, nombre, apellido, dirección, teléfono.

---

## Proceso

1. **Construir contenido del PDF**: Se genera el contenido del PDF con los datos de la liquidación
2. **Generar archivo PDF**: Se crea el documento en formato PDF
3. **Subir a nube**: Se sube el archivo al sistema de almacenamiento en la nube
4. **Retornar URI**: Se retorna la URL/path donde señala al archivo

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generación de PDF de liquidación (Priority: P1)

Yo como Sistema Financiero necesito generar un PDF con la información de la liquidación para que el cliente pueda visualizar o descargar su factura.

**Why this priority:** Es necesario para que el cliente tenga un documento físico de su liquidación.

**Acceptance Scenarios:**

1. **Scenario:** Generación exitosa del PDF
   - **Given:** Se tienen todos los datos necesarios (productos, total, cliente, forma de pago)
   - **When:** Se invoca la función para generar el PDF
   - **Then:** El sistema genera el PDF, lo sube a la nube y retorna la URI

2. **Scenario:** PDF con múltiples productos
   - **Given:** El pedido tiene múltiples productos
   - **When:** Se genera el PDF
   - **Then:** El PDF contiene todos los productos con su cantidad, precio unitario y subtotal

---

### Edge Cases

- **¿Qué pasa si la subir a la nube falla?**
  - El sistema debe retornar un error: "Error al subir el PDF a la nube. Intente más tarde"

- **¿Qué pasa si algún parámetro está vacío o es inválido?**
  - El sistema debe validar los parámetros y retornar error si faltan datos requeridos

- **¿Qué pasa si el contenido del PDF es muy grande?**
  - El sistema debe manejar el tamaño máximo permitido por el almacenamiento en la nube

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir como parámetros la lista de productos, total del pedido, forma de pago, datos del cliente y ID del pedido.
- **FR-002:** El sistema DEBE generar un PDF con el contenido de la liquidación.
- **FR-003:** El sistema DEBE subir el PDF generado a un sistema de almacenamiento en la nube.
- **FR-004:** El sistema DEBE retornar la URI donde señala al PDF.
- **FR-005:** El sistema DEBE validar que todos los parámetros requeridos estén presentes antes de generar el PDF.
- **FR-006:** El sistema DEBE manejar errores al subir a la nube y retornar un mensaje apropiado.

### Key Entities *(include if data)*

**ProductoPedidoDTO (entrada):**
- [id_producto, nombre, cantidad, precio_unitario, subtotal]

**ClienteDTO (entrada):**
- [Pendiente de definir campos]

**Salida:**
- [uri_pdf]: URL/path donde señala al PDF

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El PDF debe generarse en menos de 3 segundos.

- **SC-002:** El PDF debe contener todos los productos del pedido con su información correcta.

- **SC-003:** La URI retornada debe permitir acceder al PDF en la nube.

- **SC-004:** El sistema debe manejar errores de manera apropiada y retornar mensajes claros.
