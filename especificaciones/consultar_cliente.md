# Feature Specification: Consultar Cliente - Consumo desde Sistema Financiero

**Created:** 07-03-2026  
**Status:** In Development

> **Nota:** Esta especificación documenta cómo el Sistema Financiero consume los endpoints del Módulo de Gestión de Clientes. Los endpoints son proporcionados por el Módulo de Clientes y el Sistema Financiero los consume.

---

## Endpoint Consumido del Módulo de Gestión de Clientes

### Consultar Cliente por ID Nacional (síncrono)

**Endpoint:** `GET /api/v1/clientes/nacional/{id_nacional}`

**Propósito:** Obtener el ID de base de datos del cliente usando su ID Nacional (número de documento) para poder guardar la forma de pago o asociar liquidaciones.

**Parámetros de consulta:**
- `id_nacional` (path, requerido): Número de documento del cliente (cédula, etc.)

**Respuesta exitosa:**
```json
{
  "id_cliente": "100",
  "id_nacional": "12345678",
  "nombre": "Juan Perez",
  "telefono": "3001234567",
  "direccion": "Calle 123 #45-67"
}
```

> **Nota:** Los campos del cliente están pendientes de definición por parte del Módulo de Gestión de Clientes.

**Casos de error:**
- Cliente no encontrado: `"Cliente no encontrado con el ID Nacional proporcionado"`
- ID Nacional vacío: `"El ID Nacional es requerido"`

---

### Consultar Cliente por ID de BD (síncrono)

**Endpoint:** `GET /api/v1/clientes/{id_cliente}`

**Propósito:** Obtener los datos completos del cliente usando su ID de base de datos. Para usar en procesos internos como generación de liquidaciones.

**Parámetros de consulta:**
- `id_cliente` (path, requerido): ID del cliente en la base de datos

**Respuesta exitosa:**
```json
{
  "id_cliente": "100",
  "id_nacional": "12345678",
  "nombre": "Juan Perez",
  "telefono": "3001234567",
  "direccion": "Calle 123 #45-67"
}
```

> **Nota:** Los campos del cliente están pendientes de definición por parte del Módulo de Gestión de Clientes.

**Casos de error:**
- Cliente no encontrado: `"Cliente no encontrado con el ID proporcionado"`
- ID inválido: `"ID de cliente inválido"`

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de cliente por ID Nacional (Priority: P1)

**Endpoint:** `GET /api/v1/clientes/nacional/{id_nacional}` (síncrono)

Yo como Asesor Comercial necesito consultar un cliente utilizando su ID Nacional (número de documento) para obtener su ID de base de datos. Para poder guardar la forma de pago asociada al cliente correcto en la base de datos.

**Why this priority:** Es necesario para mantener la concordancia entre módulos; el ID de BD es el identificador interno que se usa para todas las operaciones de base de datos.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de cliente por ID Nacional
   - **Given:** Existe un cliente registrado en el sistema con ID Nacional "12345678"
   - **When:** El Sistema Financiero envía una solicitud de consulta con ese ID Nacional
   - **Then:** El sistema retorna el ID de base de datos del cliente (ej: "100")

2. **Scenario:** Cliente no encontrado por ID Nacional
    - **Given:** No existe ningún cliente registrado con el ID Nacional proporcionado
    - **When:** Se envía una solicitud de consulta con ese ID Nacional
    - **Then:** El sistema retorna un error indicando que el cliente no fue encontrado

---

### User Story 2 - Consulta de cliente por ID de BD (Priority: P1)

**Endpoint:** `GET /api/v1/clientes/{id_cliente}` (síncrono)

Yo como Sistema Financiero necesito consultar los datos de un cliente utilizando su ID de base de datos para generar liquidaciones y procesos internos.

**Why this priority:** Necesario para recuperar los datos del cliente (nombre, dirección, teléfono) cuando se genera una liquidación.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de cliente por ID de BD
    - **Given:** Existe un cliente registrado en el sistema con ID de BD "100"
    - **When:** El Sistema Financiero envía una solicitud de consulta con ese ID
    - **Then:** El sistema retorna los datos completos del cliente (id_cliente, id_nacional, nombre, telefono, direccion)

2. **Scenario:** Cliente no encontrado por ID de BD
    - **Given:** No existe ningún cliente registrado con el ID de BD proporcionado
    - **When:** Se envía una solicitud de consulta con ese ID
    - **Then:** El sistema retorna un error indicando que el cliente no fue encontrado

---

### Edge Cases

- **¿Qué pasa si el ID Nacional está vacío?**
  - El endpoint debe retornar un error: "El ID Nacional es requerido"

- **¿Qué pasa si la consulta al Módulo de Clientes falla?**
  - El Sistema Financiero debe manejar el error de conexión: "Error al consultar el Módulo de Clientes. Intente más tarde"

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe retornar el ID de BD del cliente en menos de 500ms después de recibir la solicitud.

- **SC-002:** El 100% de las consultas con ID Nacional válido de clientes existentes deben retornar el ID de BD correcto.

- **SC-003:** El sistema debe retornar un mensaje de error apropiado cuando el cliente no existe.

- **SC-004:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultáneas.

---

## Notas

- **ID Nacional vs ID de BD:**
  - ID Nacional: Número de documento del cliente (cédula, etc.) - usado para buscar
  - ID de BD: Identificador interno del cliente en la base de datos - usado para relaciones
