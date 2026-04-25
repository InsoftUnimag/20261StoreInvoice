# Feature Specification: Recibir Tasa de Efectividad del Módulo de Transporte

**Created:** 12-03-2026  
**Status:** In Development

## Descripción del Flujo

El Módulo de Transporte envía la tasa de efectividad de entrega de un pedido al Sistema Financiero (de forma asíncrona). El Sistema Financiero recibe esta información y procesa:
1. Generación de liquidación del transportista (spec `generar_liquidacion_de_transportista.md`)

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

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id_pedido` | Integer | Sí | ID del pedido |
| `tasa_efectividad` | Integer | Sí | Porcentaje de efectividad -100 a 100 |
| `id_transportista` | Integer | Sí | ID del transportista |


> **Nota:** El `id_cliente` y datos del pedido ya fueron recibidos previamente del Módulo de Inventario.

---

## Proceso

1. **Recibir datos**: El Sistema Financiero recibe los datos del Módulo de Transporte
2. **Validar**: Se valida que contenga todos los datos requeridos
3. **Registrar**: Se registra para seguimiento
4. **Procesar**: Se invocan las funciones de liquidación
5. **Actualizar**: Se actualiza el estado del procesamiento

---

## Edge Cases

- **¿Qué pasa si falla el procesamiento de una liquidación?**
  - El registro permanece como "pendiente" o "error" para reintento

- **¿Qué pasa si llega un estado final duplicado?**
  - Se verifica si ya fue procesado y se ignora o marca como duplicado

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE recibir la tasa de efectividad del Módulo de Transporte.
- **FR-002:** El sistema DEBE validar que los datos contengan: id_pedido, tasa_efectividad, id_transportista.
- **FR-004:** El sistema DEBE registrar cada tasa de efectividad recibida para seguimiento.
- **FR-006:** El sistema DEBE invocar la función de generación de liquidación del transportista.
- **FR-007:** El sistema DEBE actualizar el estado después del procesamiento.

### Key Entities

**EventoRecibido:**
- [id_pedido, tasa_efectividad, id_transportista, status, fecha_recibido, fecha_procesado]

---

## Success Criteria

- **SC-001:** El 100% de los datos válidos deben ser procesados.
- **SC-002:** El procesamiento debe completarse en menos de 5 segundos.
- **SC-003:** El sistema debe manejar errores de manera que puedan ser reintentados.
- **SC-004:** El registro debe mostrar el estado correcto de cada procesamiento.
