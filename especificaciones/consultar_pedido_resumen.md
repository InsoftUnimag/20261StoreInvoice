# Resumen de Implementación: Consultar Productos del Pedido

**Fecha:** 23-04-2026  
**Branch:** `feature/consultar_pedido`  
**Estado:** ✅ Completado

---

## ¿Qué se implementó?

Se construyó el mecanismo para que el Sistema Financiero pueda consultar los productos de un pedido al Módulo de Gestión de Inventario, siguiendo estrictamente la Arquitectura Hexagonal (Puertos y Adaptadores).

---

## Archivos Creados / Modificados

### ✅ Dominio (`domain/`)
| Archivo | Tipo | Descripción |
|---|---|---|
| `domain/model/Producto.java` | Nuevo | Entidad pura de dominio. Record de Java, sin dependencias externas. |
| `domain/exception/ServiceConnectionException.java` | Nuevo | Excepción de infraestructura para errores de conexión con servicios externos. |

### ✅ Aplicación (`application/`)
| Archivo | Tipo | Descripción |
|---|---|---|
| `application/port/InventarioServicePort.java` | Nuevo | Puerto de salida (Outbound Port). Define el contrato `consultarProductosPorPedido()`. |
| `application/service/pedido/ConsultarProductosPedidoUseCase.java` | Nuevo | Caso de uso. Valida el ID, llama al puerto y lanza `PedidoNotFoundException` si no hay resultados. |

### ✅ Infraestructura (`infrastructure/`)
| Archivo | Tipo | Descripción |
|---|---|---|
| `infrastructure/adapter/outbound/external/dto/ProductoExternalDTO.java` | Nuevo | DTO externo individual. Record que mapea el JSON de la API de Inventario. |
| `infrastructure/adapter/outbound/external/dto/ProductosPedidoResponseExternalDTO.java` | Nuevo | DTO envoltorio con la clave `"productos"` del JSON de respuesta. |
| `infrastructure/adapter/outbound/external/mapper/ProductoExternalMapper.java` | Nuevo | Mapper MapStruct: `ProductoExternalDTO` → `Producto` (Dominio). |
| `infrastructure/adapter/outbound/external/InventarioWebClient.java` | Nuevo | Adaptador real (`@Profile("prod")`). Llama a `GET /api/v1/pedidos/{id_pedido}/productos` via WebClient. |
| `infrastructure/adapter/outbound/external/InventarioMockAdapter.java` | Nuevo | Adaptador Mock (`@Profile("!prod")`). Devuelve 3 productos fijos sin llamadas reales. |
| `infrastructure/adapter/inbound/rest/GlobalExceptionHandler.java` | Modificado | Se agregó handler para `ServiceConnectionException` → responde con HTTP 503. |

### ✅ Tests
| Archivo | Tipo | Descripción |
|---|---|---|
| `application/service/pedido/ConsultarProductosPedidoUseCaseTest.java` | Nuevo | 5 tests con Mockito + `StepVerifier`. Cubre camino feliz, no encontrado, ID nulo/vacío, error propagado. |
| `infrastructure/adapter/outbound/external/InventarioWebClientTest.java` | Nuevo | 3 tests con `MockWebServer`. Cubre respuesta exitosa, 404 y error 500. |

### ✅ Dependencias (`build.gradle`)
| Dependencia | Descripción |
|---|---|
| `com.squareup.okhttp3:mockwebserver:4.12.0` | Agregada para los tests del WebClient real. |

---

## Arquitectura del Flujo

```
Caso de Uso (interno)
     │
     ▼
InventarioServicePort (Contrato / Puerto de Salida)
     │
     ├──[@Profile("!prod")]── InventarioMockAdapter  ──→ Lista hardcodeada (dev/test)
     │
     └──[@Profile("prod")]─── InventarioWebClient    ──→ GET /api/v1/pedidos/{id}/productos
                                      │
                                      ▼
                         ProductosPedidoResponseExternalDTO (JSON)
                                      │
                                      ▼
                         ProductoExternalMapper (MapStruct)
                                      │
                                      ▼
                              List<Producto> (Dominio)
```

---

## Decisiones Técnicas Importantes

- **`Producto` como Record:** Se usó `record` de Java en lugar de `@Getter/@AllArgsConstructor` de Lombok porque los Records son inmutables nativamente, tienen `equals`, `hashCode` y `toString` automáticos, y son perfectos para modelar datos puros del dominio.
- **`@Profile("prod")` en el WebClient:** El adaptador real sólo se activa en producción. En cualquier otro entorno, Spring inyecta automáticamente el `InventarioMockAdapter`, lo que permite desarrollo y pruebas sin dependencias externas.
- **Barrera anti-corrupción con MapStruct:** Los DTOs externos (`ProductoExternalDTO`) nunca salen de la carpeta `infrastructure/`. El mapper los transforma a objetos `Producto` del dominio antes de entregarlos al caso de uso.
- **Manejo granular de errores HTTP en WebClient:** Se usan dos handlers `.onStatus()` separados: uno específico para 404 (que lanza la excepción de dominio `PedidoNotFoundException`) y otro genérico para 4xx/5xx restantes.
