# Feature Specification: Recibir Tasa de Efectividad del MÃ³dulo de Transporte

**Created:** 12-03-2026  
**Status:** In Development

## DescripciÃ³n del Flujo

El MÃ³dulo de Transporte envÃ­a la tasa de efectividad de entrega de un pedido al Sistema Financiero (de forma asÃ­ncrona). El Sistema Financiero recibe esta informaciÃ³n y procesa:
1. GeneraciÃ³n de liquidaciÃ³n del transportista (spec `generar_liquidacion_de_transportista.md`)

---

## Datos Recibidos

### Estructura

```json
{
  "id_pedido": 123,
  "tasa_efectividad": 100,
  "id_transportista": 50
}
```

### Campos

| Campo | Tipo | Requerido | DescripciÃ³n |
|-------|------|-----------|-------------|
| `id_pedido` | Integer | SÃ­ | ID del pedido |
| `tasa_efectividad` | Integer | SÃ­ | Porcentaje de efectividad -100 a 100 |
| `id_transportista` | Integer | SÃ­ | ID del transportista |


> **Nota:** El `id_cliente` y datos del pedido ya fueron recibidos previamente del MÃ³dulo de Inventario.

---

## Proceso

1. **Recibir datos**: El Sistema Financiero recibe los datos del MÃ³dulo de Transporte
2. **Validar**: Se valida que contenga todos los datos requeridos
3. **Registrar**: Se registra para seguimiento
4. **Procesar**: Se invocan las funciones de liquidaciÃ³n
5. **Actualizar**: Se actualiza el estado del procesamiento

---

## Edge Cases

- **Â¿QuÃ© pasa si falla el procesamiento de una liquidaciÃ³n?**
  - El registro permanece como "pendiente" o "error" para reintento

- **Â¿QuÃ© pasa si llega un estado final duplicado?**
  - Se verifica si ya fue procesado y se ignora o marca como duplicado

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir la tasa de efectividad del MÃ³dulo de Transporte.
- **FR-002:** El sistema DEBE validar que los datos contengan: id_pedido, tasa_efectividad, id_transportista.
- **FR-004:** El sistema DEBE registrar cada tasa de efectividad recibida para seguimiento.
- **FR-006:** El sistema DEBE invocar la funciÃ³n de generaciÃ³n de liquidaciÃ³n del transportista.
- **FR-007:** El sistema DEBE actualizar el estado despuÃ©s del procesamiento.

### Key Entities

**EventoRecibido:**
- [id_pedido, tasa_efectividad, id_transportista, status, fecha_recibido, fecha_procesado]

---

## Success Criteria

- **SC-001:** El 100% de los datos vÃ¡lidos deben ser procesados.
- **SC-002:** El procesamiento debe completarse en menos de 5 segundos.
- **SC-003:** El sistema debe manejar errores de manera que puedan ser reintentados.
- **SC-004:** El registro debe mostrar el estado correcto de cada procesamiento.

