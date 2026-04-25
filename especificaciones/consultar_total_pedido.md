# Feature Specification: Consultar Total del Pedido

**Created:** 12-03-2026  
**Status:** In Development

## DescripciÃ³n del Flujo

El Sistema Financiero expone un endpoint para que el MÃ³dulo de Transporte consulte el total de un pedido. La consulta es sÃ­ncrona.

---

## Endpoint del Sistema Financiero

### Consultar Total del Pedido (sÃ­ncrono)

**Endpoint:** `GET /api/v1/pedidos/{id_pedido}/total`

El total pedido se obtiene de la tabla `liquidacion_cliente`, columna `monto_liquidado`, filtrando por el `id_pedido` recibido. La tabla `liquidacion_cliente` tiene la siguiente estructura:

| Campo | Tipo | DescripciÃ³n |
|------|------|-------------|
| id_liquidacion | BIGSERIAL | PK |
| id_pedido | BIGINT | ID del pedido |
| id_cliente | BIGINT | ID del cliente |
| forma_pago | VARCHAR(50) | Forma de pago |
| estado_liquidacion | VARCHAR(50) | Estado de la liquidaciÃ³n |
| fecha_liquidacion | TIMESTAMP | Fecha de liquidaciÃ³n |
| uri_pdf | VARCHAR(500) | URI del PDF |
| monto_liquidado | DECIMAL(15,2) | **Monto lÃ­quido del pedido** |

**ParÃ¡metros:**
- `id_pedido` (path, requerido): ID del pedido

**Respuesta:**
```json
{
  "id_pedido": 123,
  "total_pedido": 150000
}
```

**Casos de error:**
- Pedido no encontrado: `404 - "Pedido no encontrado"`

