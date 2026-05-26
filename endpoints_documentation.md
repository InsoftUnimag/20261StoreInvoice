# Documentación de Endpoints del Módulo de Liquidación y Pagos (Store Invoice)

Esta documentación describe todos los endpoints disponibles en el backend que son consumidos por el frontend de este módulo. Se incluye el método HTTP, la ruta, los parámetros necesarios y la estructura de los payloads de solicitud (request) y respuesta (response).

Todas las rutas parten del path base: `/api/v1`

---

## 1. Módulo de Pedidos

### 1.1 Consultar Total de un Pedido
Obtiene el total a pagar de un pedido específico.

- **Método**: `GET`
- **Ruta**: `/pedidos/{id_pedido}/total`
- **Parámetros de Ruta**:
  - `id_pedido` (Long, requerido): ID único del pedido.

**Respuesta Exitosa (200 OK)**
```json
{
  "idPedido": 1,
  "totalPedido": 1500.50
}
```

### 1.2 Consultar Pago de Transporte de un Pedido
Obtiene la forma de pago y el valor a contra entrega para el transporte de un pedido.

- **Método**: `GET`
- **Ruta**: `/pedidos/{id_pedido}/pago-transporte`
- **Parámetros de Ruta**:
  - `id_pedido` (Long, requerido): ID único del pedido.

**Respuesta Exitosa (200 OK)**
```json
{
  "idPedido": 1,
  "formaPago": "CONTRA_ENTREGA",
  "valorContraEntrega": 150.00
}
```

---

## 2. Módulo de Forma de Pago Cliente

### 2.1 Consultar Forma de Pago por Pedido
Consulta la forma de pago de un cliente a través del ID del pedido (usado desde la liquidación).

- **Método**: `GET`
- **Ruta**: `/pedidos/{id_pedido}/forma-pago`
- **Parámetros de Ruta**:
  - `id_pedido` (Long, requerido): ID único del pedido.

**Respuesta Exitosa (200 OK)**
```json
{
  "idCliente": 5,
  "formaPago": "EFECTIVO",
  "fechaRegistro": "2026-05-25T10:00:00"
}
```

### 2.2 Verificar si Cliente tiene Forma de Pago por Pedido
Verifica si el cliente asociado a un pedido ya tiene una forma de pago registrada.

- **Método**: `GET`
- **Ruta**: `/pedidos/{id_pedido}/tiene-forma-pago`
- **Parámetros de Ruta**:
  - `id_pedido` (Long, requerido): ID único del pedido.

**Respuesta Exitosa (200 OK)**
```json
{
  "idCliente": 5,
  "tieneFormaPago": true
}
```

### 2.3 Consultar Forma de Pago por Cliente
Consulta la forma de pago actual registrada para un cliente específico.

- **Método**: `GET`
- **Ruta**: `/clientes/{id_cliente}/forma-pago`
- **Parámetros de Ruta**:
  - `id_cliente` (Long, requerido): ID único del cliente.

**Respuesta Exitosa (200 OK)**
```json
{
  "idCliente": 5,
  "formaPago": "TARJETA_CREDITO",
  "fechaRegistro": "2026-05-25T10:00:00"
}
```

### 2.4 Verificar si Cliente tiene Forma de Pago
Verifica si un cliente en específico tiene una forma de pago registrada.

- **Método**: `GET`
- **Ruta**: `/clientes/{id_cliente}/tiene-forma-pago`
- **Parámetros de Ruta**:
  - `id_cliente` (Long, requerido): ID único del cliente.

**Respuesta Exitosa (200 OK)**
```json
{
  "idCliente": 5,
  "tieneFormaPago": true
}
```

### 2.5 Registrar Forma de Pago de Cliente
Registra una nueva forma de pago para un cliente.

- **Método**: `POST`
- **Ruta**: `/clientes/{id_cliente}/forma-pago`
- **Parámetros de Ruta**:
  - `id_cliente` (Long, requerido): ID único del cliente.

**Cuerpo de la Solicitud (Request Body)**
```json
{
  "formaPago": "TRANSFERENCIA"
}
```

**Respuesta Exitosa (201 Created)**
```json
{
  "idCliente": 5,
  "formaPago": "TRANSFERENCIA",
  "fechaRegistro": "2026-05-25T11:30:00"
}
```

### 2.6 Actualizar Forma de Pago de Cliente
Actualiza la forma de pago existente de un cliente.

- **Método**: `PUT`
- **Ruta**: `/clientes/{id_cliente}/actualizar-forma-pago`
- **Parámetros de Ruta**:
  - `id_cliente` (Long, requerido): ID único del cliente.

**Cuerpo de la Solicitud (Request Body)**
```json
{
  "formaPago": "EFECTIVO"
}
```

**Respuesta Exitosa (200 OK)**
```json
{
  "idCliente": 5,
  "formaPago": "EFECTIVO",
  "fechaRegistro": "2026-05-25T12:00:00"
}
```

---

## 3. Módulo de Liquidaciones

### 3.1 Consultar Liquidaciones de un Cliente
Obtiene la lista de liquidaciones de un cliente de forma paginada.

- **Método**: `GET`
- **Ruta**: `/clientes/{idCliente}/liquidaciones`
- **Parámetros de Ruta**:
  - `idCliente` (Long, requerido): ID del cliente.
- **Parámetros de Query (Opcionales)**:
  - `pagina` (int): Número de página (desde 0, por defecto 0).
  - `tamanoPagina` (int): Tamaño de la página (por defecto 20).

**Respuesta Exitosa (200 OK)**
```json
[
  {
    "idLiquidacion": 101,
    "idPedido": 10,
    "idCliente": 5,
    "formaPago": "TRANSFERENCIA",
    "estadoLiquidacion": "PAGADO",
    "fechaLiquidacion": "2026-05-25T09:00:00",
    "uriPdf": "https://storage.com/docs/liq_101.pdf",
    "montoLiquidado": 1500.50
  }
]
```

### 3.2 Crear Liquidación de Cliente
Crea una nueva liquidación para un cliente.

- **Método**: `POST`
- **Ruta**: `/clientes/liquidaciones`

**Cuerpo de la Solicitud (Request Body)**
```json
{
  "idPedido": 10,
  "idCliente": 5,
  "formaPago": "EFECTIVO",
  "montoLiquidado": 1500.50
}
```

**Respuesta Exitosa (200 OK)**
```json
{
  "idLiquidacion": 102,
  "idPedido": 10,
  "idCliente": 5,
  "formaPago": "EFECTIVO",
  "estadoLiquidacion": "PENDIENTE",
  "fechaLiquidacion": "2026-05-25T14:00:00",
  "uriPdf": null,
  "montoLiquidado": 1500.50
}
```

### 3.3 Actualizar Estado de Liquidación de Cliente
Actualiza el estado de una liquidación de cliente existente.

- **Método**: `PUT`
- **Ruta**: `/clientes/liquidaciones/{idLiquidacion}/estado`
- **Parámetros de Ruta**:
  - `idLiquidacion` (Long, requerido): ID de la liquidación.

**Cuerpo de la Solicitud (Request Body)**
```json
{
  "estadoLiquidacion": "COMPLETADO"
}
```

**Respuesta Exitosa (200 OK)**
```json
{
  "idLiquidacion": 102,
  "idPedido": 10,
  "idCliente": 5,
  "formaPago": "EFECTIVO",
  "estadoLiquidacion": "COMPLETADO",
  "fechaLiquidacion": "2026-05-25T14:00:00",
  "uriPdf": "https://storage.com/docs/liq_102.pdf",
  "montoLiquidado": 1500.50
}
```

### 3.4 Consultar Liquidaciones de un Transportista
Obtiene la lista de liquidaciones de un transportista de forma paginada.

- **Método**: `GET`
- **Ruta**: `/transportistas/{idTransportista}/liquidaciones`
- **Parámetros de Ruta**:
  - `idTransportista` (Long, requerido): ID del transportista.
- **Parámetros de Query (Opcionales)**:
  - `pagina` (int): Número de página (desde 0, por defecto 0).
  - `tamanoPagina` (int): Tamaño de la página (por defecto 20).

**Respuesta Exitosa (200 OK)**
```json
[
  {
    "idLiquidacion": 201,
    "idPedido": 15,
    "idTransportista": 8,
    "montoCalculado": 250.00,
    "fechaLiquidacion": "2026-05-24T18:00:00"
  }
]
```

### 3.5 Crear Liquidación de Transportista
Crea una nueva liquidación para un transportista.

- **Método**: `POST`
- **Ruta**: `/transportistas/liquidaciones`

**Cuerpo de la Solicitud (Request Body)**
```json
{
  "idPedido": 15,
  "idTransportista": 8,
  "montoCalculado": 250.00
}
```

**Respuesta Exitosa (200 OK)**
```json
{
  "idLiquidacion": 202,
  "idPedido": 15,
  "idTransportista": 8,
  "montoCalculado": 250.00,
  "fechaLiquidacion": "2026-05-25T15:00:00"
}
```

### 3.6 Actualizar Monto de Liquidación de Transportista
Actualiza el monto de una liquidación de transportista existente.

- **Método**: `PUT`
- **Ruta**: `/transportistas/liquidaciones/{idLiquidacion}/monto`
- **Parámetros de Ruta**:
  - `idLiquidacion` (Long, requerido): ID de la liquidación.

**Cuerpo de la Solicitud (Request Body)**
```json
{
  "montoCalculado": 300.00
}
```

**Respuesta Exitosa (200 OK)**
```json
{
  "idLiquidacion": 202,
  "idPedido": 15,
  "idTransportista": 8,
  "montoCalculado": 300.00,
  "fechaLiquidacion": "2026-05-25T15:30:00"
}
```

---

## 4. Módulo de Liquidaciones Contables (Contador)

### 4.1 Consultar Liquidaciones Contables
Obtiene la lista general de liquidaciones (de clientes y/o transportistas) con filtros opcionales. Ideal para las vistas del módulo contable.

- **Método**: `GET`
- **Ruta**: `/liquidaciones`
- **Parámetros de Query (Opcionales)**:
  - `tipo` (String): Tipo de sujeto (`CLIENTE` o `TRANSPORTISTA`).
  - `idSujeto` (Long): ID del cliente o transportista.
  - `fechaDesde` (String, formato `YYYY-MM-DD`): Fecha de inicio del rango.
  - `fechaHasta` (String, formato `YYYY-MM-DD`): Fecha de fin del rango.
  - `pagina` (int): Número de página (desde 0, por defecto 0).
  - `tamanoPagina` (int): Tamaño de la página (por defecto 20).

**Respuesta Exitosa (200 OK)**
```json
[
  {
    "idLiquidacion": 101,
    "idPedido": 10,
    "tipoLiquidacion": "CLIENTE",
    "idSujeto": 5,
    "monto": 1500.50,
    "fechaLiquidacion": "2026-05-25T09:00:00",
    "uriDocumento": "https://storage.com/docs/liq_101.pdf"
  },
  {
    "idLiquidacion": 201,
    "idPedido": 15,
    "tipoLiquidacion": "TRANSPORTISTA",
    "idSujeto": 8,
    "monto": 250.00,
    "fechaLiquidacion": "2026-05-24T18:00:00",
    "uriDocumento": null
  }
]
```
