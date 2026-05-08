# Resumen del Flujo del Proyecto StoreInvoice API

## 1. Vision General

**StoreInvoice API** es un microservicio de facturacion y liquidaciones que forma parte de un ecosistema de tienda. Su proposito principal es:

- Gestionar **liquidaciones de clientes** (pagos/devoluciones segun forma de pago y estado de entrega)
- Gestionar **liquidaciones de transportistas** (pagos segun tasa de efectividad de entrega)
- Administrar **formas de pago** de los clientes
- Generar y almacenar **PDFs de liquidacion**
- Actuar como orquestador entre los modulos de **Inventario**, **Clientes** y **Transporte**

---

## 2. Stack Tecnologico

| Capa | Tecnologia |
|------|-----------|
| Framework | Spring Boot 3.4.0 |
| Lenguaje | Java 21 |
| Programacion | Reactiva (Spring WebFlux - Mono/Flux) |
| Persistencia | Spring Data JPA + PostgreSQL |
| Migraciones | Flyway |
| Mensajeria | Spring Cloud Stream + RabbitMQ |
| Generacion PDF | OpenPDF (Open Source) |
| Almacenamiento PDF | Supabase Storage (prod) / Local (dev/test) |
| Llamadas externas | WebClient reactivo |
| Resiliencia | Resilience4j (Circuit Breaker + Retry) |
| Mapeo | MapStruct |
| Boilerplate | Lombok |
| Testing | JUnit 5 + Mockito + TestContainers |

---

## 3. Arquitectura: Hexagonal (Ports & Adapters)

El proyecto sigue una arquitectura hexagonal estricta, respetando los principios SOLID:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           INFRASTRUCTURE (ADAPTERS)                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐  │
│  │  REST Controllers│  │  JPA Repositories│  │  External WebClients       │  │
│  │  (Inbound/Driving)│  │  (Outbound/Driven)│  │  (Outbound/Driven)        │  │
│  └────────┬────────┘  └────────┬────────┘  └─────────────┬───────────────┘  │
│           │                    │                          │                  │
├───────────┼────────────────────┼──────────────────────────┼──────────────────┤
│           │         APPLICATION LAYER (Use Cases + Ports) │                  │
│           │  ┌─────────────────────────────────────────┐  │                  │
│           │  │  Use Cases (Servicios de Aplicacion)    │  │                  │
│           │  │  Ports (Interfaces Inbound/Outbound)    │  │                  │
│           │  │  Repository Interfaces                  │  │                  │
│           │  └─────────────────────────────────────────┘  │                  │
│           │                    │                          │                  │
├───────────┼────────────────────┼──────────────────────────┼──────────────────┤
│           │         DOMAIN LAYER (Modelos + Reglas)       │                  │
│           │  ┌─────────────────────────────────────────┐  │                  │
│           │  │  Entities / Domain Models               │  │                  │
│           │  │  Value Objects (ej: TasaEfectividad)    │  │                  │
│           │  │  Domain Exceptions                      │  │                  │
│           │  └─────────────────────────────────────────┘  │                  │
└───────────┴────────────────────┴──────────────────────────┴──────────────────┘
```

**Regla de oro:** La capa de dominio no depende de ninguna otra. La aplicacion depende solo de dominio. La infraestructura depende de aplicacion + dominio.

---

## 4. Modelos de Dominio Principales

### 4.1 LiquidacionCliente
Representa el cobro/devolucion a un cliente por un pedido.
- `idLiquidacion`, `idPedido`, `idCliente`
- `formaPago`: CONTRA_ENTREGA | CARTERA_COMERCIAL
- `estadoLiquidacion`: PENDIENTE | PAGADA | CANCELADA
- `fechaLiquidacion`, `uriPdf`, `montoLiquidado`

### 4.2 LiquidacionTransportista
Representa el pago a un transportista. Contiene **logica de negocio** para calcular el monto:
```
Formula: Monto = (Precio Pedido × 10%) × (tasa_efectividad / 100)
Redondeo: al entero mas cercano (HALF_UP)
```

### 4.3 LiquidacionContable
Vista unificada de liquidaciones (clientes y transportistas) para el area contable.

### 4.4 FormaPagoCliente
Registra la forma de pago preferida de cada cliente.
- Valores validos: `CONTRA_ENTREGA`, `CARTERA_COMERCIAL`

### 4.5 Pedido
Copia local de pedidos recibidos desde el Modulo de Inventario. Se usa para calcular liquidaciones de transportistas.

### 4.6 EventoRecibido
Seguimiento de eventos del Modulo de Transporte. Estados: `PENDIENTE`, `PROCESADO`, `ERROR`.

### 4.7 Cliente (Record)
Entidad inmutable con validaciones Bean Validation: `idCliente`, `idNacional`, `nombre`, `telefono`, `direccion`.

### 4.8 TasaEfectividad (Value Object)
Valida que la tasa este en el rango `[-100, 100]`. Lanza `InvalidTasaEfectividadException` si esta fuera de rango.

---

## 5. Endpoints REST

### 5.1 LiquidacionController (`/api/v1`)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/clientes/{idCliente}/liquidaciones` | Listar liquidaciones de un cliente (paginado) |
| GET | `/transportistas/{idTransportista}/liquidaciones` | Listar liquidaciones de un transportista (paginado) |
| POST | `/clientes/liquidaciones` | Crear liquidacion de cliente |
| PUT | `/clientes/liquidaciones/{idLiquidacion}/estado` | Actualizar estado de liquidacion cliente |
| POST | `/transportistas/liquidaciones` | Crear liquidacion de transportista |
| PUT | `/transportistas/liquidaciones/{idLiquidacion}/monto` | Actualizar monto de liquidacion transportista |

### 5.2 LiquidacionContableController (`/api/v1`)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/liquidaciones` | Consultar liquidaciones contables con filtros (tipo, idSujeto, rango de fechas) |

### 5.3 ClienteController (`/api/v1/clientes`)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/nacional/{idNacional}` | Consultar cliente por ID nacional (reactivo) |
| GET | `/{idCliente}` | Consultar cliente por ID interno (reactivo) |

### 5.4 PedidoController (`/api/v1/pedidos`)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/{id_pedido}/total` | Obtener el precio total de un pedido |

### 5.5 FormaPagoClienteController (`/api/v1`)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/pedidos/{id_pedido}/forma-pago` | Consultar forma de pago a partir de un pedido |
| GET | `/pedidos/{id_pedido}/tiene-forma-pago` | Verificar si el cliente del pedido tiene forma de pago |
| GET | `/clientes/{id_cliente}/forma-pago` | Consultar forma de pago de un cliente |
| GET | `/clientes/{id_cliente}/tiene-forma-pago` | Verificar si un cliente tiene forma de pago registrada |
| POST | `/clientes/{id_cliente}/forma-pago` | Registrar forma de pago de un cliente |
| PUT | `/clientes/{id_cliente}/actualizar-forma-pago` | Actualizar forma de pago de un cliente |

---

## 6. Flujos de Ejecucion Principales

### FLUJO 1: Procesamiento de Pedido desde Inventario (Asincrono)

**Trigger:** Mensaje en la cola `pedidos.inventario` (publicado por el Modulo de Inventario)

```
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────────────────┐
│ Modulo Inventario│────▶│ RabbitMQ: pedidos.   │────▶│ PedidoEventConsumer         │
│ (Producer)       │     │ inventario           │     │ (Inbound Adapter)           │
└─────────────────┘     └──────────────────────┘     └─────────────┬───────────────┘
                                                                   │
                                                                   ▼
                                              ┌────────────────────────────────────┐
                                              │ ProcesarPedidoInventarioUseCase    │
                                              │ (Orquestador)                      │
                                              └─────────────┬──────────────────────┘
                                                            │
                            ┌───────────────────────────────┼───────────────────────────────┐
                            │                               │                               │
                            ▼                               ▼                               ▼
              ┌─────────────────────┐        ┌─────────────────────┐        ┌─────────────────────┐
              │ 1. Validar mensaje  │        │ 2. Consultar forma  │        │ 3. Consultar        │
              │    (idPedido,       │        │    de pago del      │        │    productos en     │
              │     idCliente,      │        │    cliente (BD      │        │    Modulo Inventario│
              │     totalPedido)    │        │    local)           │        │    (WebClient)      │
              └─────────────────────┘        └─────────────────────┘        └─────────────────────┘
                                                                              │
                                                                              ▼
                                                              ┌─────────────────────┐
                                                              │ 4. Consultar datos  │
                                                              │    del cliente en   │
                                                              │    Modulo Clientes  │
                                                              │    (WebClient)      │
                                                              └──────────┬──────────┘
                                                                         │
                                                                         ▼
                                                              ┌─────────────────────┐
                                                              │ 5. Generar PDF de   │
                                                              │    liquidacion      │
                                                              │    (OpenPDF)        │
                                                              └──────────┬──────────┘
                                                                         │
                                                                         ▼
                                                              ┌─────────────────────┐
                                                              │ 6. Subir PDF a      │
                                                              │    almacenamiento   │
                                                              │    (Supabase /      │
                                                              │    Local)           │
                                                              └──────────┬──────────┘
                                                                         │
                                                                         ▼
                                                              ┌─────────────────────┐
                                                              │ 7. Guardar          │
                                                              │    LiquidacionCliente│
                                                              │    en BD (estado    │
                                                              │    PENDIENTE + URI) │
                                                              └─────────────────────┘
```

**Notas:**
- Si falla algun paso, se loguea el error y el mensaje se descarta (no se reintenta automaticamente en este consumer)
- El PDF contiene: datos del cliente, tabla de productos, total del pedido y forma de pago

---

### FLUJO 2: Liquidacion de Transportista (Asincrono)

**Trigger:** Mensaje en la cola `estado.final.transporte` (publicado por el Modulo de Transporte)

```
┌────────────────────┐     ┌──────────────────────────┐     ┌─────────────────────────┐
│ Modulo Transporte  │────▶│ RabbitMQ: estado.final.  │────▶│ EstadoFinalEventConsumer│
│ (Producer)         │     │ transporte               │     │ (Inbound Adapter)       │
└────────────────────┘     └──────────────────────────┘     └────────────┬────────────┘
                                                                         │
                                                                         ▼
                                                           ┌─────────────────────────────┐
                                                           │ ProcesarEstadoFinalUseCase  │
                                                           │ (Caso de Uso)               │
                                                           └─────────────┬───────────────┘
                                                                         │
                    ┌────────────────────────────────────────────────────┼────────────────────────────────────┐
                    │                                                    │                                    │
                    ▼                                                    ▼                                    ▼
      ┌─────────────────────────┐                          ┌─────────────────────────┐            ┌─────────────────────────┐
      │ 1. Validar campos       │                          │ 2. Verificar            │            │ 3. Registrar evento     │
      │    obligatorios         │                          │    idempotencia         │            │    como PENDIENTE       │
      │    (idPedido, tasa,     │                          │    (evitar duplicados   │            │    en BD                │
      │     idTransportista)    │                          │     PROCESADO)          │            │                         │
      └─────────────────────────┘                          └─────────────────────────┘            └────────────┬────────────┘
                                                                                                                │
                                                                                                                ▼
                                                                                                  ┌─────────────────────────┐
                                                                                                  │ 4. Obtener precio del   │
                                                                                                  │    pedido desde BD      │
                                                                                                  │    local (tabla pedidos)│
                                                                                                  └────────────┬────────────┘
                                                                                                               │
                                                                                                               ▼
                                                                                                  ┌─────────────────────────┐
                                                                                                  │ 5. Validar tasa con     │
                                                                                                  │    Value Object         │
                                                                                                  │    TasaEfectividad      │
                                                                                                  │    (rango [-100, 100])  │
                                                                                                  └────────────┬────────────┘
                                                                                                               │
                                                                                                               ▼
                                                                                                  ┌─────────────────────────┐
                                                                                                  │ 6. Calcular monto:      │
                                                                                                  │    (Precio × 0.10) ×    │
                                                                                                  │    (tasa / 100)         │
                                                                                                  │    redondeado a entero  │
                                                                                                  └────────────┬────────────┘
                                                                                                               │
                                                                                                               ▼
                                                                                                  ┌─────────────────────────┐
                                                                                                  │ 7. Si tasa = 0:         │
                                                                                                  │    Reportar PERDIDA     │
                                                                                                  │    OPERATIVA (log)      │
                                                                                                  └────────────┬────────────┘
                                                                                                               │
                                                                                                               ▼
                                                                                                  ┌─────────────────────────┐
                                                                                                  │ 8. Guardar              │
                                                                                                  │    LiquidacionTransporti│
                                                                                                  │    sta en BD            │
                                                                                                  │ 9. Marcar evento como   │
                                                                                                  │    PROCESADO            │
                                                                                                  └─────────────────────────┘
```

**Manejo de errores:**
- Si ocurre un error durante el procesamiento, el evento se marca como `ERROR`
- La DLQ (Dead Letter Queue) permite reintentos automaticos (`max-attempts: 3`)
- Solo los eventos `PROCESADO` exitosamente se ignoran en futuras ejecuciones (idempotencia)

---

### FLUJO 3: Consulta de Liquidaciones (Sincrono)

```
Cliente/Frontend
       │
       ▼
GET /api/v1/clientes/{id}/liquidaciones?page=0&size=20
       │
       ▼
┌─────────────────────┐
│ LiquidacionController│
│ (Inbound Adapter)    │
└──────────┬──────────┘
           │
           ▼
┌────────────────────────────────────┐
│ ConsultarLiquidacionesClienteUseCase│
│ (Caso de Uso)                      │
└──────────┬─────────────────────────┘
           │
           ▼
┌─────────────────────┐
│ LiquidacionRepository│
│ (Port / Interface)   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────┐
│ LiquidacionRepositoryAdapter │
│ (JPA Implementation)         │
└──────────┬──────────────────┘
           │
           ▼
┌─────────────────────────────┐
│ PostgreSQL (liquidaciones_  │
│ cliente table)              │
└─────────────────────────────┘
```

**Respuesta:** Lista paginada de `LiquidacionClienteResponse` con datos del dominio mapeados a DTOs.

---

## 7. Integraciones Externas

### 7.1 Modulo de Clientes (`http://localhost:8081`)
- **Port:** `ClienteServicePort`
- **Implementacion prod:** `ClienteWebClient` (WebClient reactivo)
- **Implementacion dev/test:** `ClienteMockAdapter`
- **Endpoints consumidos:**
  - `GET /api/v1/clientes/{id_cliente}`
  - `GET /api/v1/clientes/nacional/{id_nacional}`
- **Resiliencia:** Circuit Breaker + Retry (Resilience4j) con nombre `clienteService`

### 7.2 Modulo de Inventario (`http://localhost:8082`)
- **Port:** `InventarioServicePort`
- **Implementacion prod:** `InventarioWebClient` (WebClient reactivo)
- **Implementacion dev/test:** `InventarioMockAdapter`
- **Endpoints consumidos:**
  - `GET /api/v1/pedidos/{id_pedido}/productos`
- **Manejo de errores:**
  - `404` -> `PedidoNotFoundException`
  - `4xx/5xx` -> `ServiceConnectionException`
  - Errores de red -> `ServiceConnectionException`

### 7.3 Almacenamiento de PDFs
- **Port:** `PdfStoragePort`
- **Implementacion prod:** `SupabaseStorageAdapter` (usa WebClient para subir a Supabase Storage)
- **Implementacion local/test:** `LocalFileStorageAdapter` (guarda en disco: `/tmp/store-invoice/pdfs`)

### 7.4 Generacion de PDFs
- **Port:** `PdfGeneratorPort`
- **Implementacion:** `OpenPdfGeneratorAdapter` (OpenPDF / iText LGPL)
- El PDF incluye: titulo, datos del cliente, tabla de productos, total y forma de pago

---

## 8. Modelo de Datos (PostgreSQL)

### Tablas principales:

| Tabla | Proposito |
|-------|-----------|
| `liquidaciones_cliente` | Liquidaciones generadas para clientes |
| `liquidaciones_transportista` | Liquidaciones calculadas para transportistas |
| `forma_pago_cliente` | Forma de pago registrada por cliente |
| `pedidos` | Copia local de pedidos (datos del Modulo de Inventario) |
| `evento_recibido` | Seguimiento de eventos del Modulo de Transporte |

**Migraciones:** Flyway gestiona el esquema (`classpath:db/migration`). DDL-auto: `validate` (solo valida, no crea).

---

## 9. Mensajeria y Eventos

| Cola / Topic | Rol | Consumer | Proposito |
|-------------|-----|----------|-----------|
| `pedidos.inventario` | Entrante | `recibirPedido` | Recibir datos de pedidos nuevos |
| `estado.final.transporte` | Entrante | `processFinalState` | Recibir estado final de entregas |

**Configuracion de RabbitMQ:**
- Grupos de consumidores definidos
- DLQ (Dead Letter Queue) habilitada para ambas colas
- Reintentos: max 3 intentos
- Republish-to-dlq: activo

---

## 10. Manejo de Excepciones

El `GlobalExceptionHandler` convierte excepciones de dominio en respuestas HTTP estandarizadas:

| Excepcion de Dominio | HTTP Status | Mensaje al Cliente |
|---------------------|-------------|-------------------|
| `ClienteNotFoundException` | 404 | Cliente no encontrado |
| `PedidoNotFoundException` | 404 | Pedido no encontrado |
| `LiquidacionNotFoundException` | 404 | Liquidacion no encontrada |
| `FormaPagoNotFoundException` | 404 | El cliente no tiene forma de pago registrada |
| `FormaPagoAlreadyExistsException` | 409 | El cliente ya tiene forma de pago registrada |
| `InvalidTasaEfectividadException` | 400 | La tasa debe estar entre -100 y 100 |
| `InvalidFormaPagoException` | 400 | Forma de pago invalida |
| `LiquidacionException` | 422 | No se puede generar la liquidacion |
| `ServiceConnectionException` | 503 | Servicio temporalmente no disponible |
| `ErrorGeneracionPdfException` | 500 | Error interno al generar el PDF |
| `ErrorSubidaPdfException` | 503 | Error al almacenar el PDF |

**Logging:**
- `log.error()` solo para errores tecnicos reales (conexiones, configuracion, validaciones de input que indican bugs)
- NO se usa `log.error()` para flujos de negocio esperados (cliente no encontrado, etc.)

---

## 11. Resumen de Responsabilidades por Capa

### Domain
- Entidades con comportamiento (ej: `LiquidacionTransportista.calcularMonto()`)
- Value Objects con validaciones (ej: `TasaEfectividad`)
- Excepciones de negocio
- **Sin dependencias externas**

### Application
- Casos de uso (orquestan el flujo de negocio)
- DTOs (Records, inmutables)
- Puertos (interfaces): `ClienteServicePort`, `InventarioServicePort`, `PdfGeneratorPort`, `PdfStoragePort`
- Repositorios (interfaces): `LiquidacionRepository`, `PedidoRepository`, etc.
- **Depende solo de Domain**

### Infrastructure
- **Inbound (Driving):** REST Controllers, Message Consumers
- **Outbound (Driven):** JPA Adapters, WebClient Adapters, PDF Generator, Storage Adapters
- Mappers (MapStruct): Entity <-> Domain <-> DTO
- Configuracion: Mensajeria, Base de datos, WebFlux
- **Depende de Application + Domain**

---

## 12. Convenciones Importantes del Proyecto

1. **Sin caracteres especiales del espanol** en codigo: no se usan n, tildes, etc.
2. **Nombres en espanol** pero con caracteres ASCII (ej: `Liquidacion`, `FormaPago`)
3. **Paquete base:** `com.storeinvoice.storeinvoiceapi`
4. **Programacion reactiva:** Uso obligatorio de `Mono`/`Flux` para operaciones no bloqueantes
5. **Optional sobre null checks:** Preferir `map/filter/orElse` en lugar de `if (x != null)`
6. **Java Records para DTOs** (inmutables)
7. **Lombok `@Data` + `@Builder` para Entities** JPA
8. **MapStruct para mapeos** (generacion en tiempo de compilacion)

---

*Documento generado el: 2026-04-25*
