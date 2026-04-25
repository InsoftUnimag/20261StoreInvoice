# Implementation Plan: Consultar Productos del Pedido (Consumo Externo)

**Date:** 23-04-2026  
**Spec:** especificaciones/consultar_pedido.md

---

## Summary

El Sistema Financiero debe consumir el endpoint del Módulo de Gestión de Inventario para consultar los productos asociados a un pedido específico utilizando su ID. Esta información será utilizada internamente para generar la liquidación y la factura (PDF). **No hay persistencia en base de datos para los productos en este sistema**; solo se consultan en tiempo real bajo demanda.

**Technical Approach:** Implementación de `WebClient` reactivo (`InventarioWebClient`) para consumir el endpoint `GET /api/v1/pedidos/{id_pedido}/productos` del Módulo de Inventario de manera asíncrona y no bloqueante, protegiendo el dominio local de los DTOs externos mediante mappers.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring WebFlux (WebClient), Lombok 1.18.36, MapStruct  
**Storage:** No aplica (consumo de API externa en tiempo real, no requiere persistencia de productos en PostgreSQL)  
**Programming Style:** Programación reactiva (Project Reactor: Mono/Flux), manejo de errores con WebFlux (`onErrorResume`, `switchIfEmpty`), arquitectural hexagonal limpia, mapeo de DTOs a Entidades de Dominio puro.
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - Consulta de productos del pedido para facturación (Priority: P1)
Yo como Módulo de Facturación necesito consultar los productos asociados a un pedido específico utilizando su ID. Para obtener los datos de los productos como lista de DTOs y usarlos como parámetros para generar el PDF de la factura.

**Consumo externo:** `GET /api/v1/pedidos/{id_pedido}/productos`

### User Story 2 - Validación de existencia del pedido (Priority: P1)
Validar que el pedido existe y manejar adecuadamente los errores 404 provenientes del Módulo de Inventario para evitar liquidaciones falsas o en blanco.

---

## Success Criteria

- **SC-001:** El Sistema Financiero debe obtener los productos del pedido en menos de 1 segundo.
- **SC-002:** El 100% de las consultas con IDs válidos deben retornar la información correcta mapeada al modelo de dominio.
- **SC-003:** El sistema debe retornar un mensaje de error apropiado en menos de 500ms cuando el pedido no existe.
- **SC-004:** Tolerancia a la carga de 100 solicitudes simultáneas gracias a la naturaleza no bloqueante de WebFlux.

---

## Project Structure

A continuación se muestra la arquitectura propuesta y cómo se guardarán los archivos siguiendo estrictamente los principios de Arquitectura Hexagonal que se usan en el proyecto:

```text
src/
├── main/
│   └── java/
│       └── com/
│           └── storeinvoice/
│               └── storeinvoiceapi/
│                   ├── domain/
│                   │   ├── model/
│                   │   │   └── Producto.java                      # Entidad de Dominio Pura (POJO)
│                   │   └── exception/
│                   │       └── PedidoNotFoundException.java       # Excepción de dominio (si no existe)
│                   │
│                   ├── application/
│                   │   ├── dto/
│                   │   │   └── product/
│                   │   │       ├── ProductoExternalDTO.java # Representa el producto individual JSON
│                   │   │       └── ProductosPedidoResponseExternalDTO.java # Envoltorio del JSON {"productos": [...]}
│                   │   ├── port/
│                   │   │   └── InventarioServicePort.java         # Puerto de Salida (Outbound Port)
│                   │   └── service/
│                   │       └── pedido/
│                   │           └── ConsultarProductosPedidoUseCase.java # Lógica de Negocio
│                   │
│                   └── infrastructure/
│                       ├── adapter/
│                       │   └── outbound/
│                       │       └── external/
│                       │           ├── InventarioWebClient.java   # Adaptador de Salida (WebClient)
│                       │           └── InventarioMockAdapter.java # Adaptador Mock para desarrollo local (!prod)
│                       └── persistence/
│                           └── mapper/
│                               └── ProductoExternalMapper.java # MapStruct: ExternalDTO -> Domain Model
│
└── test/
    └── java/
        └── com/
            └── storeinvoice/
                └── storeinvoiceapi/
                    ├── application/
                    │   └── service/
                    │       └── pedido/
                    │           └── ConsultarProductosPedidoUseCaseTest.java # Usando Mockito y StepVerifier
                    └── infrastructure/
                        └── adapter/
                            └── outbound/
                                └── external/
                                    └── InventarioWebClientTest.java # Usando MockWebServer
```

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**Propósito:** Definir el modelo puro y las excepciones del negocio sin dependencias.
- [x] **T001:** Crear clase `Producto.java` en `domain/model/` (campos: idProducto, nombre, cantidad, precioUnitario, subtotal) sin anotaciones de BD ni librerías externas.
- [x] **T002:** Verificar existencia de `PedidoNotFoundException` en `domain/exception/` o crearla si no existe.

### Phase 2: Capa de Aplicación (Application)
**Propósito:** Definir el contrato (Port) y la lógica de negocio (UseCase).
- [x] **T003:** Crear interfaz `InventarioServicePort.java` en `application/port/` que defina el método `Mono<List<Producto>> consultarProductosPorPedido(String idPedido)`.
- [x] **T004:** Crear `ConsultarProductosPedidoUseCase.java` en `application/service/pedido/`. Este inyectará el `InventarioServicePort`.
- [x] **T005:** Implementar pruebas unitarias para `ConsultarProductosPedidoUseCase` usando Mockito y `StepVerifier`.

### Phase 3: Capa de Infraestructura (Infrastructure)
**Propósito:** Implementar la comunicación real por HTTP con el Módulo de Inventario.
- [x] **T006:** Crear DTOs de lectura en `application/dto/product/` (`ProductoExternalDTO` y el envoltorio `ProductosPedidoResponseExternalDTO` acordes al JSON de la spec).
- [x] **T007:** Crear mapeador `ProductoExternalMapper.java` usando MapStruct para transformar la lista de DTOs en una lista del modelo de Dominio puro (`Producto`).
- [x] **T008:** Crear adaptador `InventarioWebClient.java` implementando `InventarioServicePort`. Debe configurarse un `WebClient` apuntando a `GET /api/v1/pedidos/{id_pedido}/productos`.
- [x] **T009:** Crear adaptador simulado `InventarioMockAdapter.java` implementando `InventarioServicePort` anotado con `@Profile("!prod")` para devolver una lista de productos hardcodeados en entornos locales sin depender del Módulo de Inventario.
- [x] **T010:** Manejar correctamente el `404 Not Found` en el `WebClient` (mediante `.onStatus(HttpStatusCode::is4xxClientError, ...)`), lanzando `PedidoNotFoundException`.
- [x] **T011:** Implementar pruebas unitarias para `InventarioWebClient` usando `MockWebServer` de OkHttp para simular las respuestas REST.

### Phase 4: Validaciones y Tareas Cruzadas
**Propósito:** Asegurar la solidez del sistema.
- [x] **T012:** Validar en el use case o en la inyección de entrada que el ID del pedido no venga nulo o en blanco antes de hacer la petición HTTP.
- [x] **T013:** Asegurar que `GlobalExceptionHandler` maneje `PedidoNotFoundException` y `ServiceConnectionException`.

---

## Notes
- **Desacoplamiento Estricto:** Es crítico mapear los DTOs que devuelve la API externa del Inventario a nuestro propio objeto `Producto` de Dominio. No permitas que el DTO escape de la capa de infraestructura.
- **Uso de WebClient:** Se utilizará WebClient en lugar de RestTemplate/Feign para cumplir con el ecosistema reactivo (`Mono/Flux`) y asegurar un uso eficiente de hilos.
- **Nomenclatura:** Aunque la especificación se llama "consultar_pedido", el propósito real del Sistema Financiero aquí es traer los **productos** para imprimir la factura. Por esto, la semántica del caso de uso apunta a `ConsultarProductosPedidoUseCase` para no chocar con un hipotético "Pedido" genérico, manteniendo alta cohesión y código auto-explicativo.
