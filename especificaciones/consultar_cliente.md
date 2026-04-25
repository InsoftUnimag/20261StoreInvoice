# Feature Specification: Consultar Cliente - Consumo desde Sistema Financiero

**Created:** 07-03-2026  
**Status:** In Development

> **Nota:** Esta especificaciÃ³n documenta cÃ³mo el Sistema Financiero consume los endpoints del MÃ³dulo de GestiÃ³n de Clientes. Los endpoints son proporcionados por el MÃ³dulo de Clientes y el Sistema Financiero los consume.

---

## Endpoint Consumido del MÃ³dulo de GestiÃ³n de Clientes

### Consultar Cliente por ID Nacional (sÃ­ncrono)

**Endpoint:** `GET /api/v1/clientes/nacional/{id_nacional}`

**PropÃ³sito:** Obtener el ID de base de datos del cliente usando su ID Nacional (nÃºmero de documento) para poder guardar la forma de pago o asociar liquidaciones.

**ParÃ¡metros de consulta:**
- `id_nacional` (path, requerido): NÃºmero de documento del cliente (cÃ©dula, etc.)

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

> **Nota:** Los campos del cliente estÃ¡n pendientes de definiciÃ³n por parte del MÃ³dulo de GestiÃ³n de Clientes.

**Casos de error:**
- Cliente no encontrado: `"Cliente no encontrado con el ID Nacional proporcionado"`
- ID Nacional vacÃ­o: `"El ID Nacional es requerido"`

---

### Consultar Cliente por ID de BD (sÃ­ncrono)

**Endpoint:** `GET /api/v1/clientes/{id_cliente}`

**PropÃ³sito:** Obtener los datos completos del cliente usando su ID de base de datos. Para usar en procesos internos como generaciÃ³n de liquidaciones.

**ParÃ¡metros de consulta:**
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

> **Nota:** Los campos del cliente estÃ¡n pendientes de definiciÃ³n por parte del MÃ³dulo de GestiÃ³n de Clientes.

**Casos de error:**
- Cliente no encontrado: `"Cliente no encontrado con el ID proporcionado"`
- ID invÃ¡lido: `"ID de cliente invÃ¡lido"`

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta de cliente por ID Nacional (Priority: P1)

**Endpoint:** `GET /api/v1/clientes/nacional/{id_nacional}` (sÃ­ncrono)

Yo como Asesor Comercial necesito consultar un cliente utilizando su ID Nacional (nÃºmero de documento) para obtener su ID de base de datos. Para poder guardar la forma de pago asociada al cliente correcto en la base de datos.

**Why this priority:** Es necesario para mantener la concordancia entre mÃ³dulos; el ID de BD es el identificador interno que se usa para todas las operaciones de base de datos.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de cliente por ID Nacional
   - **Given:** Existe un cliente registrado en el sistema con ID Nacional "12345678"
   - **When:** El Sistema Financiero envÃ­a una solicitud de consulta con ese ID Nacional
   - **Then:** El sistema retorna el ID de base de datos del cliente (ej: "100")

2. **Scenario:** Cliente no encontrado por ID Nacional
    - **Given:** No existe ningÃºn cliente registrado con el ID Nacional proporcionado
    - **When:** Se envÃ­a una solicitud de consulta con ese ID Nacional
    - **Then:** El sistema retorna un error indicando que el cliente no fue encontrado

---

### User Story 2 - Consulta de cliente por ID de BD (Priority: P1)

**Endpoint:** `GET /api/v1/clientes/{id_cliente}` (sÃ­ncrono)

Yo como Sistema Financiero necesito consultar los datos de un cliente utilizando su ID de base de datos para generar liquidaciones y procesos internos.

**Why this priority:** Necesario para recuperar los datos del cliente (nombre, direcciÃ³n, telÃ©fono) cuando se genera una liquidaciÃ³n.

**Acceptance Scenarios:**

1. **Scenario:** Consulta exitosa de cliente por ID de BD
    - **Given:** Existe un cliente registrado en el sistema con ID de BD "100"
    - **When:** El Sistema Financiero envÃ­a una solicitud de consulta con ese ID
    - **Then:** El sistema retorna los datos completos del cliente (id_cliente, id_nacional, nombre, telefono, direccion)

2. **Scenario:** Cliente no encontrado por ID de BD
    - **Given:** No existe ningÃºn cliente registrado con el ID de BD proporcionado
    - **When:** Se envÃ­a una solicitud de consulta con ese ID
    - **Then:** El sistema retorna un error indicando que el cliente no fue encontrado

---

### Edge Cases

- **Â¿QuÃ© pasa si el ID Nacional estÃ¡ vacÃ­o?**
  - El endpoint debe retornar un error: "El ID Nacional es requerido"

- **Â¿QuÃ© pasa si la consulta al MÃ³dulo de Clientes falla?**
  - El Sistema Financiero debe manejar el error de conexiÃ³n: "Error al consultar el MÃ³dulo de Clientes. Intente mÃ¡s tarde"

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001:** El sistema debe retornar el ID de BD del cliente en menos de 500ms despuÃ©s de recibir la solicitud.

- **SC-002:** El 100% de las consultas con ID Nacional vÃ¡lido de clientes existentes deben retornar el ID de BD correcto.

- **SC-003:** El sistema debe retornar un mensaje de error apropiado cuando el cliente no existe.

- **SC-004:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultÃ¡neas.

---

## Notas

- **ID Nacional vs ID de BD:**
  - ID Nacional: NÃºmero de documento del cliente (cÃ©dula, etc.) - usado para buscar
  - ID de BD: Identificador interno del cliente en la base de datos - usado para relaciones

