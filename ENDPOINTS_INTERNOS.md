# Endpoints Internos del Sistema Financiero

**Creado:** 07-04-2026  
**Estado:** DefiniciÃ³n inicial basada en casos de uso

---

## ðŸ“‹ DescripciÃ³n General

Este documento define todos los endpoints que expone internamente el Sistema Financiero (MÃ³dulo 3) para consumo de:
- Otros mÃ³dulos del sistema (GestiÃ³n de Clientes, GestiÃ³n de Inventario, GestiÃ³n de Transporte)
- Interfaces web (clientes, transportistas, contador)

---

## ðŸ” Endpoints PÃºblicos del Sistema Financiero

### Consultas de Liquidaciones

#### 4. Consultar Liquidaciones del Cliente (WebApp)
**Endpoint:** `GET /api/v1/liquidaciones/cliente/{id_cliente}`

**Caso de Uso:** `consultar_liquidaciones_cliente.md`

**PropÃ³sito:** El cliente visualiza sus liquidaciones desde la interfaz web.

**ParÃ¡metros:**
- `id_cliente` (path, requerido): ID del cliente
- `page` (query, opcional): NÃºmero de pÃ¡gina (default: 1)
- `size` (query, opcional): Registros por pÃ¡gina (default: 20)

**Respuesta Exitosa (200):**
```json
{
  "total": 45,
  "page": 1,
  "size": 20,
  "liquidaciones": [
    {
      "id_liquidacion": 1001,
      "id_pedido": 123,
      "id_cliente": 1,
      "forma_pago": "CARTERA_COMERCIAL",
      "estado_liquidacion": "Pagada",
      "fecha_liquidacion": "2026-03-15T10:30:00Z",
      "uri_pdf": "https://storage.example.com/liquidaciones/1001.pdf"
    }
  ]
}
```

**Casos de Error:**
- `404 - "Cliente no encontrado"`
- `200 - "El cliente no tiene liquidaciones"` (respuesta vacÃ­a)

---

#### 5. Consultar Liquidaciones del Transportista (WebApp)
**Endpoint:** `GET /api/v1/liquidaciones/transportista/{id_transportista}`

**Caso de Uso:** `consultar_liquidaciones_transportista.md`

**PropÃ³sito:** El transportista visualiza sus liquidaciones desde la interfaz web.

**ParÃ¡metros:**
- `id_transportista` (path, requerido): ID del transportista
- `page` (query, opcional): NÃºmero de pÃ¡gina (default: 1)
- `size` (query, opcional): Registros por pÃ¡gina (default: 20)

**Respuesta Exitosa (200):**
```json
{
  "total": 30,
  "page": 1,
  "size": 20,
  "liquidaciones": [
    {
      "id_liquidacion": 2001,
      "id_pedido": 123,
      "id_transportista": 50,
      "monto_calculado": 15000,
      "fecha_liquidacion": "2026-03-15T10:30:00Z",
    }
  ]
}
```

**Casos de Error:**
- `404 - "Transportista no encontrado"`
- `200 - "El transportista no tiene liquidaciones"` (respuesta vacÃ­a)

---

#### 6. Consultar Liquidaciones (Contador - con Filtros)
**Endpoint:** `GET /api/v1/liquidaciones`

**Caso de Uso:** `consultar_liquidaciones_contador.md`

**PropÃ³sito:** El contador consulta todas las liquidaciones con mÃºltiples filtros para auditorÃ­a contable.

**ParÃ¡metros de Consulta:**
- `tipo` (query, opcional): "cliente", "transportista" o "todos" (default: "todos")
- `id_cliente` (query, opcional): Filtrar por ID de cliente
- `id_transportista` (query, opcional): Filtrar por ID de transportista
- `fecha_inicio` (query, opcional): Formato ISO 8601 (YYYY-MM-DD)
- `fecha_fin` (query, opcional): Formato ISO 8601 (YYYY-MM-DD)
- `page` (query, opcional): NÃºmero de pÃ¡gina (default: 1)
- `size` (query, opcional): Registros por pÃ¡gina (default: 20)

**Respuesta Exitosa (200):**
```json
{
  "total": 75,
  "page": 1,
  "size": 20,
  "liquidaciones": [
    {
      "id_liquidacion": 1001,
      "tipo": "cliente",
      "id_pedido": 123,
      "id_cliente": 1,
      "id_transportista": null,
      "forma_pago": "CARTERA_COMERCIAL",
      "estado_liquidacion": "Pagada",
      "monto_calculado": null,
      "fecha_liquidacion": "2026-03-15T10:30:00Z",
      "uri_pdf": "https://storage.example.com/liquidaciones/1001.pdf"
    },
    {
      "id_liquidacion": 2001,
      "tipo": "transportista",
      "id_pedido": 123,
      "id_cliente": null,
      "id_transportista": 50,
      "forma_pago": null,
      "estado_liquidacion": null,
      "monto_calculado": 15000,
      "fecha_liquidacion": "2026-03-15T10:30:00Z",
    }
  ]
}
```

**Casos de Error:**
- `400 - "Rango de fechas invÃ¡lido"`
- `400 - "Tipo de liquidaciÃ³n no vÃ¡lido"`

---

## ðŸ“¤ Endpoints de RecepciÃ³n de Eventos (AsÃ­ncrono)


## ðŸ”§ Funciones Internas (Sin Endpoint)

### FunciÃ³n 1: Buscar Forma de Pago por ID de Cliente
**Nombre:** `buscar_forma_pago_por_id_cliente(id_cliente)`

**Caso de Uso:** `Ingresar_forma_de_pago_cliente.md`, `recibir_datos_pedido_modulo_inventario.md`

**PropÃ³sito:** Consultar la forma de pago almacenada para un cliente especÃ­fico.

**ParÃ¡metros:**
- `id_cliente` (Integer): ID del cliente

**Retorno (Success):**
```json
{
  "id_cliente": 1,
  "forma_pago": "CARTERA_COMERCIAL"
}
```

**Excepciones:**
- `ClienteNoEncontradoException`: Cliente no existe
- `FormaPagoNoRegistradaException`: Cliente sin forma de pago

---

### FunciÃ³n 2: Generar PDF de LiquidaciÃ³n del Cliente
**Nombre:** `generar_pdf_liquidacion_cliente(productos, total_pedido, forma_pago, cliente, id_pedido)`

**Caso de Uso:** `generar_pdf_liquidacion_cliente.md`

**PropÃ³sito:** FunciÃ³n interna que genera el PDF de la liquidaciÃ³n del cliente y lo sube a almacenamiento en nube.

**ParÃ¡metros de Entrada:**
```
productos: List[ProductoPedidoDTO]
  - id_producto (String)
  - nombre (String)
  - cantidad (Integer)
  - precio_unitario (Integer)
  - subtotal (Integer)

total_pedido: Integer
forma_pago: String ("CONTRA_ENTREGA" | "CARTERA_COMERCIAL")
cliente: ClienteDTO
  - id_cliente (Integer)
  - id_nacional (String)
  - nombre (String)
  - telefono (String)
  - direccion (String)
id_pedido: Integer
```

**Retorno (Success):**
```json
{
  "uri_pdf": "https://storage.example.com/liquidaciones/1001.pdf",
  "generado_exitosamente": true
}
```

**Excepciones:**
- `ErrorGenerarPDFException`: Error al generar el PDF
- `ErrorSubirArchivoException`: Error al subir archivo a almacenamiento

---

### FunciÃ³n 3: Generar LiquidaciÃ³n de Cliente
**Nombre:** `generar_liquidacion_cliente(id_pedido, estado_final, tasa_efectividad)`

**Caso de Uso:** `generar_liquidacion_cliente.md`

**PropÃ³sito:** Se invoca cuando se recibe el evento del MÃ³dulo de Transporte. Genera la liquidaciÃ³n para el cliente.

**ParÃ¡metros:**
- `id_pedido` (Integer): ID del pedido
- `estado_final` (String): Estado final recibido del transportista
- `tasa_efectividad` (Integer): Tasa de efectividad

**Proceso Interno:**
1. Consultar endpoint GET `/api/v1/pedidos/{id_pedido}/productos` (MÃ³dulo de Inventario)
2. Consultar datos del cliente desde MÃ³dulo de GestiÃ³n de Clientes
3. Invocar `generar_pdf_liquidacion_cliente()`
4. Guardar registro de `Liquidacion_Cliente` con uri_pdf

**Retorno (Success):**
```json
{
  "id_liquidacion": 1001,
  "id_pedido": 123,
  "estado": "Generada",
  "uri_pdf": "https://storage.example.com/liquidaciones/1001.pdf"
}
```

---

### FunciÃ³n 4: Generar LiquidaciÃ³n de Transportista
**Nombre:** `generar_liquidacion_transportista(id_pedido, id_transportista, tasa_efectividad)`

**Caso de Uso:** `generar_liquidacion_de_transportista.md`

**PropÃ³sito:** Se invoca cuando se recibe el evento del MÃ³dulo de Transporte. Genera la liquidaciÃ³n del transportista.

**ParÃ¡metros:**
- `id_pedido` (Integer): ID del pedido
- `id_transportista` (Integer): ID del transportista
- `tasa_efectividad` (Integer): Tasa de efectividad (-100 a 100)

**FÃ³rmula de CÃ¡lculo:**
```
base_tarifa = total_pedido Ã— 0.1  (10% del pedido)
monto_calculado = base_tarifa Ã— (tasa_efectividad / 100)
```

**Validaciones:**
- Si `tasa_efectividad < 0`: Se genera como deuda del transportista
- Si `tasa_efectividad = 0`: Se genera como pÃ©rdida operativa (monto = 0)
- Si hay decimales largos: redondear al entero mÃ¡s cercano

**Retorno (Success):**
```json
{
  "id_liquidacion": 2001,
  "id_pedido": 123,
  "id_transportista": 50,
  "monto_calculado": 15000,
  "estado": "Generada"
}
```

**Excepciones:**
- `ErrorAlConsultarTotalException`: No se puede obtener el total del pedido
- `TasaEfectividadInvalidaException`: Tasa fuera de rango

---

## ðŸ“Š Resumen de Endpoints

| MÃ©todo | Endpoint | Tipo | Caso de Uso |
|--------|----------|------|------------|
| GET | `/api/v1/pedidos/{id_pedido}/forma-pago` | Sync | Ingresar forma pago |
| GET | `/api/v1/clientes/{id_cliente}/tiene-forma-pago` | Sync | Validar forma pago |
| GET | `/api/v1/pedidos/{id_pedido}/total` | Sync | Consultar total pedido |
| GET | `/api/v1/liquidaciones/cliente/{id_cliente}` | Sync | Cliente consulta liquidaciones |
| GET | `/api/v1/liquidaciones/transportista/{id_transportista}` | Sync | Transportista consulta liquidaciones |
| GET | `/api/v1/liquidaciones` | Sync | Contador consulta liquidaciones |
| POST | `/api/v1/pedidos/eventos/recibir-datos` | Async | Recibir datos de inventario |
| POST | `/api/v1/pedidos/eventos/estado-final-transporte` | Async | Recibir estado de transporte |

---

## ðŸ”‘ Notas Importantes

1. **Seguridad:** Los endpoints deben incluir autenticaciÃ³n y autorizaciÃ³n segÃºn el rol del usuario.
2. **Rate Limiting:** Implementar lÃ­mites de tasa para prevenir abuso.
3. **ValidaciÃ³n:** Todas las entradas deben validarse en servidor (no confiar en cliente).
4. **Logging:** Registrar todas las operaciones para auditorÃ­a.
5. **DTOs:** Usar DTOs especÃ­ficos del Sistema Financiero para desacoplar de otros mÃ³dulos.
6. **Manejo de Errores:** Implementar cÃ³digos HTTP estÃ¡ndar y respuestas de error consistentes.
7. **PaginaciÃ³n:** Implementar offset/limit o page/size para todas las consultas de lista.
8. **Timestamps:** Usar ISO 8601 para fechas en todas las respuestas.

---


