# Feature Specification - Generar Liquidación de Cliente

**Created:** 03-03-2026  
**Status:** In Development

## Descripción del Flujo

> **Nota:** El trigger de esta función es el evento publicado por el **Módulo de Inventario** (ver spec `recibir_datos_pedido_modulo_inventario.md`). Al recibir los datos del pedido, el sistema consulta los productos del Módulo de Inventario, consulta los datos del cliente del Módulo de Gestión de Clientes, genera el PDF de liquidación y lo guarda en el registro de `liquidaciones_cliente`.

**Datos recibidos del Módulo de Inventario:**
- `id_pedido`
- `id_cliente` (número de documento/idNacional, debe resolverse al idCliente de BD)
- `total_pedido`

**Pasos para generar la liquidación:**

1. **Recibir datos**: Se reciben los datos del pedido desde el Módulo de Inventario (vía cola asíncrona)
2. **Resolver cliente**: Con el `id_cliente` del mensaje (número de documento/idNacional), se consulta al Módulo de Gestión de Clientes via `ClienteServicePort.findByIdNacional()` para obtener el `idCliente` real de la base de datos
3. **Consultar forma de pago**: Con el `idCliente` de BD resuelto, se consulta la forma de pago usando `FormaPagoClienteRepository`
4. **Consultar productos**: Se llama al endpoint del Módulo de Inventario via `InventarioServicePort` para obtener los productos del pedido
5. **Consultar datos del cliente**: Se usa el `Cliente` resuelto en el paso 2 (ya se tienen los datos del cliente, no需要进行额外的外部调用)
6. **Generar PDF**: Se invoca la función interna de generación de PDF (spec `generar_pdf_liquidacion_cliente.md`) que retorna la URI del PDF
7. **Guardar liquidación**: Se guarda el registro de liquidación con estado `PENDIENTE` y la URI del PDF

> **Nota:** El `id_cliente` del mensaje es el número de documento (idNacional). La resolución al idCliente de BD es **obligatoria** antes de consultar la forma de pago o guardar cualquier registro en `liquidaciones_cliente`.

---

## User Scenarios & Testing (mandatory)

### User Story 1 - Generación de liquidación al recibir pedido (Priority: P1)

Yo como Sistema Financiero necesito generar una liquidación para un cliente cuando el Módulo de Inventario envíe los datos del pedido. Para registrar el monto a cobrar y generar el PDF de la liquidación.

**Why this priority:** Es una función crítica para la conciliación financiera.

**Acceptance Scenarios:**

1. **Scenario:** Generación exitosa de liquidación
   - **Given:** El Módulo de Inventario envía los datos del pedido con id_pedido
   - **When:** Se desea generar la liquidación del cliente
   - **Then:** El sistema consulta los productos, consulta el cliente, genera el PDF y guarda la URI.

2. **Scenario:** Error al generar PDF
   - **Given:** El sistema no puede generar el PDF
   - **When:** Se desea generar la liquidación del cliente
   - **Then:** El sistema retorna error y no guarda la liquidación incompleta.

---

### User Story 2 - Validación de datos requeridos (Priority: P1)

Yo como Sistema Financiero necesito validar que existan todos los datos requeridos antes de generar la liquidación. Para evitar registros contables erróneos.

**Why this priority:** Protege la integridad de la base de datos y previene facturas incompletas.

**Independent Test:** Intentar generar liquidaciones con datos faltantes y verificar que el sistema las rechace.

**Acceptance Scenarios:**

1. **Scenario:** Intento de liquidación sin precio del pedido
   - **Given:** Se intenta generar liquidación pero el total_pedido es nulo o negativo
   - **When:** Cuando se desea generar la liquidación del cliente
   - **Then:** El sistema bloquea la operación y muestra error: "Total del pedido no puede ser negativo".

2. **Scenario:** Intento de liquidación sin forma de pago registrada
   - **Given:** Se intenta generar liquidación para un cliente sin forma de pago asignada
   - **When:** Cuando se desea generar la liquidación
   - **Then:** El sistema bloquea la operación y muestra error: "Cliente sin forma de pago registrada".

3. **Scenario:** Intento de liquidación sin productos
   - **Given:** El Módulo de Inventario retorna lista vacía de productos para el pedido
   - **When:** Cuando se desea generar la liquidación
   - **Then:** El sistema bloquea la operación y muestra error: "No se encontraron productos para el pedido".

---

### Edge Cases

- **¿Qué pasa si el pedido no existe en la base de datos?**
  - El sistema debe retornar error: "Pedido no encontrado"

- **¿Qué pasa si no se pueden obtener los productos del Módulo de Inventario?**
  - El sistema debe retornar error: "Error al consultar los productos del pedido"

- **¿Qué pasa si no se pueden obtener los datos del cliente?**
  - El sistema debe retornar error: "Error al consultar los datos del cliente"

- **¿Qué pasa si falla la subida del PDF a la nube?**
  - El sistema debe retornar error: "Error al subir el PDF a la nube. Intente más tarde"

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir del evento del Módulo de Inventario el `id_pedido`, `id_cliente` y `total_pedido`.
- **FR-002:** El sistema DEBE consultar la forma de pago del cliente usando `FormaPagoClienteRepository`.
- **FR-003:** El sistema DEBE consultar los productos del pedido del Módulo de Inventario via `InventarioServicePort`.
- **FR-004:** El sistema DEBE consultar los datos del cliente del Módulo de Gestión de Clientes via `ClienteServicePort`.
- **FR-005:** El sistema DEBE invocar la función interna de generación de PDF (spec `generar_pdf_liquidacion_cliente.md`) y guardar la URI retornada.
- **FR-006:** El sistema DEBE guardar el registro de liquidación con estado `PENDIENTE`, fecha actual y URI del PDF.
- **FR-007:** El sistema DEBE validar que todos los parámetros requeridos estén presentes antes de generar la liquidación.
- **FR-008:** El sistema DEBE mantener un registro inmutable de todas las liquidaciones generadas.

### Key Entities *(include if feature involves data)*

- **Liquidacion_Cliente:** 
  - [id_liquidacion, id_pedido, id_cliente, monto_liquidado, forma_pago, estado_liquidacion, fecha_liquidacion, uri_pdf]
  - Entidad que registra el monto a cobrar al cliente.

**Notas de columnas:**
- `id_cliente`: Se resolve del idNacional (número de documento recibido en el mensaje) al idCliente de BD via `ClienteServicePort.findByIdNacional()`. El valor almacenado es el **idCliente de BD**, no el idNacional.
- `monto_liquidado`: Se obtiene del `total_pedido` recibido del Módulo de Inventario
- `forma_pago`: Se consulta por **idCliente de BD** via `FormaPagoClienteRepository`
- `estado_liquidacion`: Siempre `PENDIENTE` al crear
- `uri_pdf`: Se genera mediante la función `generar_pdf_liquidacion_cliente.md`
- `fecha_liquidacion`: Fecha actual del sistema

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe generar una liquidación completa (recepción + consultas + PDF + almacenamiento) en menos de 2 segundos.

- **SC-002:** Todas las liquidaciones deben ser rastreables e inmutables para auditoría.

- **SC-003:** El sistema debe procesar al menos 1,000 liquidaciones diarias sin fallos críticos.

- **SC-004:** El sistema debe manejar errores de manera apropiada sin detener el procesamiento de mensajes siguientes (pipeline reactivo).

---

## Notas de Implementación

### Resolución de idCliente (ID Nacional vs ID de BD)

El sistema maneja dos identificadores de cliente:

| Identificador | Descripción | Uso |
|--------------|-------------|-----|
| `idNacional` | Número de documento del cliente (cédula, etc.) | Recibido en el mensaje del Módulo de Inventario |
| `idCliente` | Identificador interno en la base de datos | Usado para consultas a `forma_pago_cliente` y almacenamiento en `liquidaciones_cliente` |

**Flujo obligatorio:**
1. El mensaje del Inventario recebe `id_cliente` = idNacional (número de documento)
2. Se llama a `ClienteServicePort.findByIdNacional(idNacional)` para obtener el `Cliente` completo
3. Con el `idCliente` de BD (del `Cliente` resuelto) se consulta la forma de pago
4. Se guarda en `liquidaciones_cliente.id_cliente` el idCliente de BD, no el idNacional

### Simplificación respecto al diseño original

Esta spec fue simplificada durante la implementación. Originalmente el trigger era el evento del Módulo de Transporte con campos adicionales (`estado_final`, `tasa_efectividad`, `tarifa_envio`). La implementación actual:

- Usa el **evento del Módulo de Inventario** como trigger único
- No procesa eventos del Módulo de Transporte
- No utiliza `estado_final`, `tasa_efectividad`, `tarifa_envio`
- No aplica fórmula de cálculo: `monto_liquidado = total_pedido`
- Genera el PDF inmediatamente al recibir el mensaje del Inventario
- Guarda la URI del PDF en el registro inicial (no `null`)

El flujo está implementado en:
- `ProcesarPedidoInventarioUseCase` (orquestador reactivo)
- `PedidoEventConsumer` (consumer reactivo Function<T, Mono<Void>>)
