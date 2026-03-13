# Feature Specification: Consultar Total del Pedido

**Created:** 12-03-2026  
**Status:** In Development

## Descripción del Flujo

El Sistema Financiero expone un endpoint para que el Módulo de Transporte consulte el total de un pedido. La consulta es síncrona.

---

## Endpoint del Sistema Financiero

### Consultar Total del Pedido (síncrono)

**Endpoint:** `GET /api/v1/pedidos/{id_pedido}/total`

**Parámetros:**
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
