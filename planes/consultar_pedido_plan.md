# Implementation Plan: Consultar Productos del Pedido (Consumo Externo)

**Date:** 23-04-2026  
**Spec:** especificaciones/consultar_pedido.md

---

## Summary

El Sistema Financiero debe consumir el endpoint del MÃ³dulo de GestiÃ³n de Inventario para consultar los productos asociados a un pedido especÃ­fico utilizando su ID. Esta informaciÃ³n serÃ¡ utilizada internamente para generar la liquidaciÃ³n y la factura (PDF). **No hay persistencia en base de datos para los productos en este sistema**; solo se consultan en tiempo real bajo demanda.

**Technical Approach:** ImplementaciÃ³n de `WebClient` reactivo (`InventarioWebClient`) para consumir el endpoint `GET /api/v1/pedidos/{id_pedido}/productos` del MÃ³dulo de Inventario de manera asÃ­ncrona y no bloqueante, protegiendo el dominio local de los DTOs externos mediante mappers.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.4.0, Spring WebFlux (WebClient), Lombok 1.18.36, MapStruct  
**Storage:** No aplica (consumo de API externa en tiempo real, no requiere persistencia de productos en PostgreSQL)  
**Programming Style:** ProgramaciÃ³n reactiva (Project Reactor: Mono/Flux), manejo de errores con WebFlux (`onErrorResume`, `switchIfEmpty`), arquitectural hexagonal limpia, mapeo de DTOs a Entidades de Dominio puro.
**Architecture:** Arquitectura Hexagonal / Puertos y Adaptadores.

---

## User Stories & Scenarios

### User Story 1 - Consulta de productos del pedido para facturaciÃ³n (Priority: P1)
Yo como MÃ³dulo de FacturaciÃ³n necesito consultar los productos asociados a un pedido especÃ­fico utilizando su ID. Para obtener los datos de los productos como lista de DTOs y usarlos como parÃ¡metros para generar el PDF de la factura.

**Consumo externo:** `GET /api/v1/pedidos/{id_pedido}/productos`

### User Story 2 - ValidaciÃ³n de existencia del pedido (Priority: P1)
Validar que el pedido existe y manejar adecuadamente los errores 404 provenientes del MÃ³dulo de Inventario para evitar liquidaciones falsas o en blanco.

---

## Success Criteria

- **SC-001:** El Sistema Financiero debe obtener los productos del pedido en menos de 1 segundo.
- **SC-002:** El 100% de las consultas con IDs vÃ¡lidos deben retornar la informaciÃ³n correcta mapeada al modelo de dominio.
- **SC-003:** El sistema debe retornar un mensaje de error apropiado en menos de 500ms cuando el pedido no existe.
- **SC-004:** Tolerancia a la carga de 100 solicitudes simultÃ¡neas gracias a la naturaleza no bloqueante de WebFlux.

---

## Project Structure

A continuaciÃ³n se muestra la arquitectura propuesta y cÃ³mo se guardarÃ¡n los archivos siguiendo estrictamente los principios de Arquitectura Hexagonal que se usan en el proyecto:

```text
src/
â”œâ”€â”€ main/
â”‚   â””â”€â”€ java/
â”‚       â””â”€â”€ com/
â”‚           â””â”€â”€ storeinvoice/
â”‚               â””â”€â”€ storeinvoiceapi/
â”‚                   â”œâ”€â”€ domain/
â”‚                   â”‚   â”œâ”€â”€ model/
â”‚                   â”‚   â”‚   â””â”€â”€ Producto.java                      # Entidad de Dominio Pura (POJO)
â”‚                   â”‚   â””â”€â”€ exception/
â”‚                   â”‚       â””â”€â”€ PedidoNotFoundException.java       # ExcepciÃ³n de dominio (si no existe)
â”‚                   â”‚
â”‚                   â”œâ”€â”€ application/
â”‚                   â”‚   â”œâ”€â”€ dto/
â”‚                   â”‚   â”‚   â””â”€â”€ product/
â”‚                   â”‚   â”‚       â”œâ”€â”€ ProductoExternalDTO.java # Representa el producto individual JSON
â”‚                   â”‚   â”‚       â””â”€â”€ ProductosPedidoResponseExternalDTO.java # Envoltorio del JSON {"productos": [...]}
â”‚                   â”‚   â”œâ”€â”€ port/
â”‚                   â”‚   â”‚   â””â”€â”€ InventarioServicePort.java         # Puerto de Salida (Outbound Port)
â”‚                   â”‚   â””â”€â”€ service/
â”‚                   â”‚       â””â”€â”€ pedido/
â”‚                   â”‚           â””â”€â”€ ConsultarProductosPedidoUseCase.java # LÃ³gica de Negocio
â”‚                   â”‚
â”‚                   â””â”€â”€ infrastructure/
â”‚                       â”œâ”€â”€ adapter/
â”‚                       â”‚   â””â”€â”€ outbound/
â”‚                       â”‚       â””â”€â”€ external/
â”‚                       â”‚           â”œâ”€â”€ InventarioWebClient.java   # Adaptador de Salida (WebClient)
â”‚                       â”‚           â””â”€â”€ InventarioMockAdapter.java # Adaptador Mock para desarrollo local (!prod)
â”‚                       â””â”€â”€ persistence/
â”‚                           â””â”€â”€ mapper/
â”‚                               â””â”€â”€ ProductoExternalMapper.java # MapStruct: ExternalDTO -> Domain Model
â”‚
â””â”€â”€ test/
    â””â”€â”€ java/
        â””â”€â”€ com/
            â””â”€â”€ storeinvoice/
                â””â”€â”€ storeinvoiceapi/
                    â”œâ”€â”€ application/
                    â”‚   â””â”€â”€ service/
                    â”‚       â””â”€â”€ pedido/
                    â”‚           â””â”€â”€ ConsultarProductosPedidoUseCaseTest.java # Usando Mockito y StepVerifier
                    â””â”€â”€ infrastructure/
                        â””â”€â”€ adapter/
                            â””â”€â”€ outbound/
                                â””â”€â”€ external/
                                    â””â”€â”€ InventarioWebClientTest.java # Usando MockWebServer
```

---

## Implementation Tasks

### Phase 1: Capa de Dominio (Domain)
**PropÃ³sito:** Definir el modelo puro y las excepciones del negocio sin dependencias.
- [x] **T001:** Crear clase `Producto.java` en `domain/model/` (campos: idProducto, nombre, cantidad, precioUnitario, subtotal) sin anotaciones de BD ni librerÃ­as externas.
- [x] **T002:** Verificar existencia de `PedidoNotFoundException` en `domain/exception/` o crearla si no existe.

### Phase 2: Capa de AplicaciÃ³n (Application)
**PropÃ³sito:** Definir el contrato (Port) y la lÃ³gica de negocio (UseCase).
- [x] **T003:** Crear interfaz `InventarioServicePort.java` en `application/port/` que defina el mÃ©todo `Mono<List<Producto>> consultarProductosPorPedido(String idPedido)`.
- [x] **T004:** Crear `ConsultarProductosPedidoUseCase.java` en `application/service/pedido/`. Este inyectarÃ¡ el `InventarioServicePort`.
- [x] **T005:** Implementar pruebas unitarias para `ConsultarProductosPedidoUseCase` usando Mockito y `StepVerifier`.

### Phase 3: Capa de Infraestructura (Infrastructure)
**PropÃ³sito:** Implementar la comunicaciÃ³n real por HTTP con el MÃ³dulo de Inventario.
- [x] **T006:** Crear DTOs de lectura en `application/dto/product/` (`ProductoExternalDTO` y el envoltorio `ProductosPedidoResponseExternalDTO` acordes al JSON de la spec).
- [x] **T007:** Crear mapeador `ProductoExternalMapper.java` usando MapStruct para transformar la lista de DTOs en una lista del modelo de Dominio puro (`Producto`).
- [x] **T008:** Crear adaptador `InventarioWebClient.java` implementando `InventarioServicePort`. Debe configurarse un `WebClient` apuntando a `GET /api/v1/pedidos/{id_pedido}/productos`.
- [x] **T009:** Crear adaptador simulado `InventarioMockAdapter.java` implementando `InventarioServicePort` anotado con `@Profile("!prod")` para devolver una lista de productos hardcodeados en entornos locales sin depender del MÃ³dulo de Inventario.
- [x] **T010:** Manejar correctamente el `404 Not Found` en el `WebClient` (mediante `.onStatus(HttpStatusCode::is4xxClientError, ...)`), lanzando `PedidoNotFoundException`.
- [x] **T011:** Implementar pruebas unitarias para `InventarioWebClient` usando `MockWebServer` de OkHttp para simular las respuestas REST.

### Phase 4: Validaciones y Tareas Cruzadas
**PropÃ³sito:** Asegurar la solidez del sistema.
- [x] **T012:** Validar en el use case o en la inyecciÃ³n de entrada que el ID del pedido no venga nulo o en blanco antes de hacer la peticiÃ³n HTTP.
- [x] **T013:** Asegurar que `GlobalExceptionHandler` maneje `PedidoNotFoundException` y `ServiceConnectionException`.

---

## Notes
- **Desacoplamiento Estricto:** Es crÃ­tico mapear los DTOs que devuelve la API externa del Inventario a nuestro propio objeto `Producto` de Dominio. No permitas que el DTO escape de la capa de infraestructura.
- **Uso de WebClient:** Se utilizarÃ¡ WebClient en lugar de RestTemplate/Feign para cumplir con el ecosistema reactivo (`Mono/Flux`) y asegurar un uso eficiente de hilos.
- **Nomenclatura:** Aunque la especificaciÃ³n se llama "consultar_pedido", el propÃ³sito real del Sistema Financiero aquÃ­ es traer los **productos** para imprimir la factura. Por esto, la semÃ¡ntica del caso de uso apunta a `ConsultarProductosPedidoUseCase` para no chocar con un hipotÃ©tico "Pedido" genÃ©rico, manteniendo alta cohesiÃ³n y cÃ³digo auto-explicativo.

