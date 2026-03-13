# Feature Specification: Recibir Estado Final de Entrega del Módulo de Transporte (Arquitectura de Eventos)

**Created:** 12-03-2026  
**Status:** In Development

## Descripción del Flujo

El Módulo de Gestión de Transporte publica un evento cuando un pedido alcanza un estado final de entrega. El Sistema Financiero consume este evento y procesa:
1. Generación de liquidación del cliente (spec `generar_liquidacion_cliente.md`)
2. Generación de liquidación del transportista (spec `generar_liquidacion_de_transportista.md`)

> **Nota:** Esta comunicación es asíncrona mediante un broker de mensajes. No hay llamada REST directa.

---

## Arquitectura de Eventos

```
┌─────────────────────┐     ┌─────────────────┐     ┌──────────────────────┐
│ Módulo Transporte   │────▶│  Broker de      │────▶│ Sistema Financiero   │
│ (Productor)        │     │  Mensajes      │     │ (Consumidor)        │
└─────────────────────┘     │  (Cola/Topic)   │     └──────────────────────┘
                           └─────────────────┘              │
                                    │                        │
                                    │                        ▼
                           ┌─────────────────┐     ┌──────────────────────┐
                           │ Lista de        │◀────│ Generar Liquidaciones│
                           │ Eventos         │     │ (Cliente y Transportista)
                           └─────────────────┘     └──────────────────────┘
```

---

## Evento: EstadoFinalEntrega

### Topic/Cola
`estado_final_entrega`

### Estructura del Evento
```json
{
  "event_id": "uuid-unico",
  "event_type": "EstadoFinalEntrega",
  "timestamp": "2026-03-12T10:30:00Z",
  "source": "modulo_transporte",
  "payload": {
    "id_pedido": 123,
    "estado_final": "Entregado Completo",
    "tasa_efectividad": 100,
    "id_transportista": 50
  }
}
```

### Campos del Payload
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id_pedido` | Integer | Sí | ID del pedido |
| `estado_final` | String | Sí | Estado final de entrega (para liquidación del cliente) |
| `tasa_efectividad` | Integer | Sí | Porcentaje de efectividad 0-100 (para liquidación del transportista) |
| `id_transportista` | Integer | Sí | ID del transportista |

> **Nota:** 
> - El `id_cliente` y datos del pedido ya fueron recibidos previamente del Módulo de Inventario (ver spec `recibir_datos_pedido_modulo_inventario.md`)
> - `estado_final` se usa para la liquidación del cliente (matriz de cliente)
> - `tasa_efectividad` se usa para la liquidación del transportista (matriz de transportista)

### Estados finales válidos
- Entregado Completo
- Rechazo Parcial
- Devolución (Error Empresa)
- Faltante de Inventario
- No Entregado

---

## Proceso

1. **Publicación**: Módulo Transporte publica el evento en el broker de mensajes
2. **Consumo**: Sistema Financiero consume el evento de la cola
3. **Validación**: Se valida que el evento contenga todos los datos requeridos
4. **Registro**: Se registra el evento en la lista de eventos para seguimiento
5. **Procesamiento**: Se invocan las funciones:
   - Generar liquidación del cliente (`generar_liquidacion_cliente.md`)
   - Generar liquidación del transportista (`generar_liquidacion_de_transportista.md`)
6. **Actualización**: Se actualiza el estado del evento (procesado/error)

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recepción de evento de estado final (Priority: P1)

Yo como Módulo de Gestión de Transporte necesito publicar un evento cuando un pedido alcance un estado final de entrega. Para que el Sistema Financiero procese:
1. Liquidación del cliente (spec `generar_liquidacion_cliente.md`)
2. Liquidación del transportista (spec `generar_liquidacion_de_transportista.md`)

**Why this priority:** Es el disparador de todo el proceso de liquidación.

**Acceptance Scenarios:**

1. **Scenario:** Evento publicado correctamente
   - **Given:** El Módulo de Transporte publica un evento válido
   - **When:** El Sistema Financiero consume el evento
   - **Then:** Se validan los datos y se procesan:
     - Liquidación del cliente (`generar_liquidacion_cliente.md`)
     - Liquidación del transportista (`generar_liquidacion_de_transportista.md`)

2. **Scenario:** Evento con datos incompletos
   - **Given:** El Módulo de Transporte publica un evento sin todos los campos requeridos
   - **When:** El Sistema Financiero consume el evento
   - **Then:** Se marca el evento como error y se registra el motivo

---

### User Story 2 - Seguimiento de eventos (Priority: P2)

Yo como Sistema Financiero necesito consultar la lista de eventos recibidos para hacer seguimiento del procesamiento. Para verificar el estado de las liquidaciones.

**Acceptance Scenarios:**

1. **Scenario:** Consulta de eventos pendientes
   - **Given:** Existen eventos en la cola pendientes de procesar
   - **When:** Se consulta la lista de eventos
   - **Then:** Se retornan los eventos con su estado (pendiente/procesado/error)

---

## Lista de Eventos

El sistema debe mantener un registro de todos los eventos recibidos para seguimiento.

### Endpoint para consultar eventos
`GET /api/v1/eventos/estado-entrega`

**Respuesta:**
```json
{
  "eventos": [
    {
      "event_id": "uuid-1",
      "id_pedido": 123,
      "estado_final": "Entregado Completo",
      "tasa_efectividad": 100,
      "id_transportista": 50,
      "status": "procesado",
      "fecha_recibido": "2026-03-12T10:30:00Z",
      "fecha_procesado": "2026-03-12T10:30:05Z"
    }
  ]
}
```

---

## Edge Cases

- **¿Qué pasa si el broker de mensajes no está disponible?**
  - El sistema debe manejar la reconnectividad y reintentar el consumo

- **¿Qué pasa si el evento tiene un estado_final inválido?**
  - Se marca el evento como error y se registra el motivo

- **¿Qué pasa si falla el procesamiento de una liquidación?**
  - El evento permanece como "pendiente" o "error" para reintento

- **¿Qué pasa si llega un evento duplicado?**
  - Se verifica si ya fue procesado y se ignora o marca como duplicado

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001:** El sistema DEBE consumir eventos del topic/cola `estado_final_entrega` del broker de mensajes.
- **FR-002:** El sistema DEBE validar que el payload del evento contenga: id_pedido, estado_final, tasa_efectividad, id_transportista.
- **FR-003:** El sistema DEBE validar que el estado_final sea uno de los estados válidos.
- **FR-004:** El sistema DEBE registrar cada evento recibido en la lista de eventos.
- **FR-005:** El sistema DEBE invocar la función de generación de liquidación del cliente (`generar_liquidacion_cliente.md`).
- **FR-006:** El sistema DEBE invocar la función de generación de liquidación del transportista (`generar_liquidacion_de_transportista.md`).
- **FR-007:** El sistema DEBE actualizar el estado del evento después del procesamiento.
- **FR-008:** El sistema DEBE exponer un endpoint para consultar la lista de eventos.

### Key Entities *(include if data)*

**EventoRecibido:**
- [event_id, event_type, timestamp, source, payload, status, fecha_recibido, fecha_procesado]

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El 100% de los eventos válidos deben ser procesados.

- **SC-002:** Los eventos deben ser consumidos y procesados en menos de 5 segundos después de publicados.

- **SC-003:** El sistema debe manejar errores de manera que los eventos puedan ser reintentados.

- **SC-004:** La lista de eventos debe mostrar el estado correcto de cada evento.
