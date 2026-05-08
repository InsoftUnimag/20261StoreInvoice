# Endpoints Internos del Sistema Financiero (Frontend)

**Creado:** 07-04-2026
**Ultima actualizacion:** 26-04-2026
**Estado:** Actualizado con implementacion real

---

## Descripcion General

Este documento define todos los endpoints que expone el Sistema Financiero (Modulo 3) para consumo del **frontend web** de los diferentes actores del sistema:

- **Clientes**: Consultar sus liquidaciones y descargar PDFs
- **Transportistas**: Consultar sus liquidaciones y pagos
- **Contador**: Consultar todas las liquidaciones con filtros
- **Asesores Comerciales**: Registrar y actualizar formas de pago de clientes

---

## Endpoints para el Frontend

### 1. Consultas de Liquidaciones del Cliente (WebApp)

#### 1.1 Consultar Liquidaciones del Cliente
**Endpoint:** `GET /api/v1/clientes/{idCliente}/liquidaciones`

**Caso de Uso:** El cliente visualiza sus liquidaciones desde la interfaz web.

**Parametros:**
- `idCliente` (path, requerido): ID del cliente
- `pagina` (query, opcional): Numero de pagina (default: 0)
- `tamanoPagina` (query, ocional): Registros por pagina (default: 20)

**Respuesta Exitosa (200):**
```json
[
  {
    "idLiquidacion": 1001,
    "idPedido": 123,
    "idCliente": 1,
    "formaPago": "CARTERA_COMERCIAL",
    "estadoLiquidacion": "PAGADA",
    "fechaLiquidacion": "2026-03-15T10:30:00Z",
    "uriPdf": "https://storage.example.com/liquidaciones/1001.pdf",
    "montoLiquidado": 150000.00
  }
]
```

**Casos de Error:**
- `404 - "Cliente no encontrado"`
- `200 - []` (respuesta vacia si no tiene liquidaciones)

---

### 2. Consultas de Liquidaciones del Transportista (WebApp)

#### 2.1 Consultar Liquidaciones del Transportista
**Endpoint:** `GET /api/v1/transportistas/{idTransportista}/liquidaciones`

**Caso de Uso:** El transportista visualiza sus liquidaciones desde la interfaz web.

**Parametros:**
- `idTransportista` (path, requerido): ID del transportista
- `pagina` (query, opcional): Numero de pagina (default: 0)
- `tamanoPagina` (query, ocional): Registros por pagina (default: 20)

**Respuesta Exitosa (200):**
```json
[
  {
    "idLiquidacion": 2001,
    "idPedido": 123,
    "idTransportista": 50,
    "montoCalculado": 15000.00,
    "fechaLiquidacion": "2026-03-15T10:30:00Z"
  }
]
```

**Casos de Error:**
- `404 - "Transportista no encontrado"`
- `200 - []` (respuesta vacia si no tiene liquidaciones)

---

### 3. Consultas de Liquidaciones para Contador (WebApp)

#### 3.1 Consultar Liquidaciones con Filtros
**Endpoint:** `GET /api/v1/liquidaciones`

**Caso de Uso:** El contador consulta todas las liquidaciones con multiples filtros para auditoria contable.

**Parametros de Consulta:**
- `tipo` (query, opcional): "cliente", "transportista" o null (todos)
- `idSujeto` (query, opcional): Filtrar por ID de cliente o transportista
- `fechaDesde` (query, ocional): Formato ISO 8601 (YYYY-MM-DD)
- `fechaHasta` (query, ocional): Formato ISO 8601 (YYYY-MM-DD)
- `pagina` (query, ocional): Numero de pagina (default: 0)
- `tamanoPagina` (query, ocional): Registros por pagina (default: 20)

**Respuesta Exitosa (200):**
```json
[
  {
    "idLiquidacion": 1001,
    "tipoLiquidacion": "cliente",
    "idPedido": 123,
    "idSujeto": 1,
    "monto": 150000.00,
    "fechaLiquidacion": "2026-03-15T10:30:00Z",
    "uriDocumento": "https://storage.example.com/liquidaciones/1001.pdf"
  },
  {
    "idLiquidacion": 2001,
    "tipoLiquidacion": "transportista",
    "idPedido": 123,
    "idSujeto": 50,
    "monto": 15000.00,
    "fechaLiquidacion": "2026-03-15T10:30:00Z",
    "uriDocumento": null
  }
]
```

**Casos de Error:**
- `400 - "Rango de fechas invalido"`
- `400 - "Tipo de liquidacion no valido"`

---

### 4. Gestion de Formas de Pago (Asesores Comerciales)

#### 4.1 Registrar Forma de Pago del Cliente
**Endpoint:** `POST /api/v1/clientes/{id_cliente}/forma-pago`

**Caso de Uso:** El asesor comercial registra la forma de pago de un cliente.

**Parametros:**
- `id_cliente` (path, requerido): ID del cliente
- `formaPago` (body, requerido): Forma de pago (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`)

**Request Body:**
```json
{
  "formaPago": "CARTERA_COMERCIAL"
}
```

**Respuesta Exitosa (201):**
```json
{
  "idCliente": 1,
  "formaPago": "CARTERA_COMERCIAL",
  "fechaRegistro": "2026-03-15T10:30:00Z"
}
```

**Casos de Error:**
- `404 - "Cliente no encontrado"`
- `400 - "Forma de pago invalida"`
- `409 - "El cliente ya tiene forma de pago registrada"`

---

#### 4.2 Actualizar Forma de Pago del Cliente
**Endpoint:** `PUT /api/v1/clientes/{id_cliente}/actualizar-forma-pago`

**Caso de Uso:** El asesor comercial actualiza la forma de pago de un cliente.

**Parametros:**
- `id_cliente` (path, requerido): ID del cliente
- `formaPago` (body, requerido): Nueva forma de pago (`CONTRA_ENTREGA` o `CARTERA_COMERCIAL`)

**Request Body:**
```json
{
  "formaPago": "CONTRA_ENTREGA"
}
```

**Respuesta Exitosa (200):**
```json
{
  "idCliente": 1,
  "formaPago": "CONTRA_ENTREGA",
  "fechaRegistro": "2026-03-15T10:30:00Z"
}
```

**Casos de Error:**
- `404 - "Cliente no encontrado"`
- `404 - "El cliente no tiene forma de pago registrada"`
- `400 - "Forma de pago invalida"`

---

#### 4.3 Consultar Cliente por ID Nacional
**Endpoint:** `GET /api/v1/clientes/nacional/{idNacional}`

**Caso de Uso:** El asesor comercial consulta un cliente por su numero de documento para obtener su ID de base de datos.

**Parametros:**
- `idNacional` (path, requerido): Numero de documento del cliente

**Respuesta Exitosa (200):**
```json
{
  "idCliente": "100",
  "idNacional": "12345678",
  "nombre": "Juan Perez",
  "telefono": "3001234567",
  "direccion": "Calle 123 #45-67"
}
```

**Casos de Error:**
- `404 - "Cliente no encontrado con el ID Nacional proporcionado"`
- `400 - "El ID Nacional es requerido"`

---

### 5. Consultas de Pedidos y Formas de Pago






#### 5.4 Consultar Forma de Pago por Cliente
**Endpoint:** `GET /api/v1/clientes/{id_cliente}/forma-pago`

**Caso de Uso:** Consultar la forma de pago actual de un cliente.

**Parametros:**
- `id_cliente` (path, requerido): ID del cliente

**Respuesta Exitosa (200):**
```json
{
  "idCliente": 1,
  "formaPago": "CARTERA_COMERCIAL",
  "fechaRegistro": "2026-03-15T10:30:00Z"
}
```

**Casos de Error:**
- `404 - "Cliente no encontrado"`
- `404 - "El cliente no tiene forma de pago registrada"`

---







## Resumen de Endpoints para Frontend

### Para Clientes:
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/v1/clientes/{idCliente}/liquidaciones` | Ver mis liquidaciones |
| GET | `/api/v1/clientes/{id_cliente}/forma-pago` | Ver mi forma de pago |

### Para Transportistas:
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/v1/transportistas/{idTransportista}/liquidaciones` | Ver mis liquidaciones |

### Para Contador:
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/v1/liquidaciones` | Consultar todas las liquidaciones con filtros |

### Para Asesores Comerciales:
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/v1/clientes/nacional/{idNacional}` | Buscar cliente por documento |
| POST | `/api/v1/clientes/{id_cliente}/forma-pago` | Registrar forma de pago |
| PUT | `/api/v1/clientes/{id_cliente}/actualizar-forma-pago` | Actualizar forma de pago |



## Notas Importantes

1. **Seguridad:** Los endpoints deben incluir autenticacion y autorizacion segun el rol del usuario.
2. **Rate Limiting:** Implementar limites de tasa para prevenir abuso.
3. **Validacion:** Todas las entradas deben validarse en servidor (no confiar en cliente).
4. **Logging:** Registrar todas las operaciones para auditoria.
5. **DTOs:** Usar DTOs especificos del Sistema Financiero para desacoplar de otros modulos.
6. **Manejo de Errores:** Implementar codigos HTTP estandar y respuestas de error consistentes.
7. **Paginacion:** Implementar pagina/tamanoPagina para todas las consultas de lista.
8. **Timestamps:** Usar ISO 8601 para fechas en todas las respuestas.
9. **Reactividad:** La mayoria de los endpoints usan Spring WebFlux (Mono/Flux) para operaciones no bloqueantes.

---

*Documento actualizado el: 2026-04-26*
