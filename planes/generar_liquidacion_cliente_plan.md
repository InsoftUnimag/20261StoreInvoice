Implementation Plan: Generar Liquidacion de Cliente

Date: 2026-04-24
Spec: especificaciones/generar_liquidacion_cliente.md

Summary

Implementar la orquestacion reactiva del flujo completo de liquidacion de cliente. Al receber el evento del Modulo de Inventario, el sistema debe: validar el mensaje, resolver el idNacional (numero de documento) al idCliente de BD via `ClienteServicePort.findByIdNacional()`, consultar la forma de pago del cliente con el idCliente resuelto, consultar los productos del pedido al Modulo de Inventario, usar los datos del cliente obtenidos en la resolucion, generar el PDF de liquidacion, subirlo a almacenamiento en la nube, y guardar el registro de liquidacion con la URI del PDF y estado PENDIENTE.

**Flujo de resolucion de idCliente:**
- Mensaje recebe `id_cliente` = numero de documento (idNacional)
- Se llama a `ClienteServicePort.findByIdNacional(idNacional)` → retorna `Cliente` con `idCliente` (BD) y `idNacional`
- El `idCliente` de BD se usa para: consultar forma de pago (`FormaPagoClienteRepository.findByIdCliente`), guardar en `liquidaciones_cliente.id_cliente`
- Los datos del cliente (nombre, telefono, direccion) ya estan disponibles en el `Cliente` resuelto, no se necesita llamada adicional a `findById`

**Pasos del pipeline reactivo:**
1. Validar mensaje
2. Resolver cliente via `findByIdNacional()` (obtiene idCliente de BD + datos del cliente)
3. Consultar forma de pago con idCliente de BD
4. Consultar productos via `InventarioServicePort`
5. Mapear productos a DTOs y usar Cliente resuelto (sin llamada adicional a `findById`)
6. Generar PDF y guardar liquidacion con idCliente de BD

Nota: Esta implementacion SIMPLIFICA la spec original. No se procesa el evento del Modulo de Transporte ni se aplican campos como estado_final, tasa_efectividad, tarifa_envio, ni la formula de calculo. El flujo completo se ejecuta al receber el evento del Modulo de Inventario.

Technical Context

Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Spring Cloud Stream (para eventos), Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
Storage: PostgreSQL con modelo puertos y adaptadores (arquitectura limpia)
Programming style: Programacion reactiva y funcional, Optional, streams, lambdas, StringBuilder para concatenacion, excepciones de dominio especificas, logging estructurado con SLF4J, validacion Bean Validation
Arquitectura: Arquitectura limpia (hexagonal) - domain, application, infrastructure. Puertos en application, adaptadores en infrastructure
Testing: Test unitarios con Mockito, test de integracion con TestContainers
Target Platform: Linux server, EC2
Performance Goals: <2s para generacion completa de liquidacion (incluye consultas externas + PDF + subduida), soportar 100 solicitudes concurrentes
Constraints: <200ms p95 para endpoints criticos, alta disponibilidad para procesos financieros

Project Structure

Source Code (repository root)

src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── storeinvoice/
│   │           └── storeinvoiceapi/
│   │               ├── domain/
│   │               │   ├── model/
│   │               │   │   ├── Cliente.java                    # Ya existe
│   │               │   │   ├── Producto.java                   # Ya existe
│   │               │   │   ├── LiquidacionCliente.java         # Ya existe
│   │               │   │   └── FormaPagoCliente.java           # Ya existe
│   │               │   └── exception/
│   │               │       ├── DomainException.java            # Ya existe (sealed)
│   │               │       ├── ProductosNoEncontradosException.java
│   │               │       ├── ClienteNoEncontradoException.java
│   │               │       ├── ErrorConsultaProductosException.java
│   │               │       └── ErrorConsultaClienteException.java
│   │               ├── application/
│   │               │   ├── dto/
│   │               │   │   ├── messaging/
│   │               │   │   │   └── DatosPedidoInventarioMessage.java   # Ya existe
│   │               │   │   ├── ProductoPedidoDTO.java          # Ya existe
│   │               │   │   └── ClienteLiquidacionDTO.java      # Ya existe
│   │               │   ├── port/
│   │               │   │   ├── InventarioServicePort.java      # Ya existe
│   │               │   │   ├── ClienteServicePort.java         # Ya existe
│   │               │   │   ├── PdfGeneratorPort.java           # Ya existe
│   │               │   │   └── PdfStoragePort.java             # Ya existe
│   │               │   ├── repository/
│   │               │   │   ├── LiquidacionRepository.java      # Ya existe
│   │               │   │   └── FormaPagoClienteRepository.java # Ya existe
│   │               │   ├── service/
│   │               │   │   └── liquidacion/
│   │               │   │       ├── cliente/
│   │               │   │       │   ├── RegistrarLiquidacionDesdeInventarioUseCase.java   # Ya existe (NO modificar)
│   │               │   │       │   └── ProcesarPedidoInventarioUseCase.java              # NUEVO
│   │               │   │       └── mapper/
│   │               │   │           ├── ProductoMapper.java     # Ya existe
│   │               │   │           └── ClienteMapper.java      # Ya existe
│   │               │   └── service/
│   │               │       └── liquidacion/
│   │               │           └── cliente/
│   │               │               └── ProcesarPedidoInventarioUseCase.java  # MODIFICADO
│   │               └── infrastructure/
│   │                   ├── adapter/
│   │                   │   ├── inbound/
│   │                   │   │   └── messaging/
│   │                   │   │       └── PedidoEventConsumer.java            # Ya existe
│   │                   │   └── outbound/
│   │                   │       ├── persistence/
│   │                   │       │   └── LiquidacionRepositoryAdapter.java   # Ya existe
│   │                   │       ├── external/
│   │                   │       │   ├── InventarioWebClient.java            # Ya existe
│   │                   │       │   └── ClienteWebClient.java               # Ya existe
│   │                   │       ├── pdf/
│   │                   │       │   └── OpenPdfGeneratorAdapter.java        # Ya existe
│   │                   │       └── storage/
│   │                   │           ├── LocalFileStorageAdapter.java        # Ya existe
│   │                   │           └── SupabaseStorageAdapter.java         # Ya existe
│   │                   └── persistence/
│   │                       ├── entity/
│   │                       │   └── LiquidacionClienteJpaEntity.java        # Ya existe
│   │                       └── mapper/
│   │                           └── LiquidacionEntityMapper.java            # Ya existe
│   └── resources/
│       ├── application.yml                                     # Ya existe
│       └── db/migration/
│           ├── V1__create_liquidacion_cliente_table.sql        # Ya existe
│           └── (sin migraciones nuevas - no cambia schema)
│
└── test/
    └── java/
        └── com/
            └── storeinvoice/
                └── storeinvoiceapi/
                    ├── application/
                    │   └── service/
                    │       └── liquidacion/
                    │           └── cliente/
                    │               ├── RegistrarLiquidacionDesdeInventarioUseCaseTest.java   # Ya existe
                    │               └── ProcesarPedidoInventarioUseCaseTest.java              # Ya existe (MODIFICADO)
                    └── infrastructure/
                        └── adapter/
                            └── inbound/
                                └── messaging/
                                    └── PedidoEventConsumerTest.java                          # Ya existe

Phase 1: Domain Layer

[x] [T001] Crear excepciones de dominio para errores de consulta externa
    - ProductosNoEncontradosException: cuando el Modulo de Inventario retorna lista vacia
    - ClienteNoEncontradoException: se reutiliza ClienteNotFoundException existente
    - ErrorConsultaProductosException: error tecnico al llamar al Modulo de Inventario
    - ErrorConsultaClienteException: error tecnico al llamar al Modulo de Clientes
    - Todas heredan de DomainException (sealed class)
    - Actualizar DomainException.java para permitir las nuevas clases

Phase 2: Application Layer

[x] [T002] Crear mappers para conversion de tipos externos a DTOs internos
    - ProductoMapper: convierte List<Producto> (dominio del Inventario) -> List<ProductoPedidoDTO>
        * Manejar conversion String idProducto -> Long idProducto (Long.parseLong)
        * Manejar conversion double precioUnitario/subtotal -> BigDecimal
        * Manejar NumberFormatException con ErrorConsultaProductosException
    - ClienteMapper: convierte Cliente (dominio de Clientes) -> ClienteLiquidacionDTO
        * Manejar conversion String idCliente -> Long idCliente (Long.parseLong)
        * Manejar NumberFormatException con ClienteNotFoundException

[x] [T003] Implementar ProcesarPedidoInventarioUseCase (orquestador reactivo)
    - Ubicacion: application/service/liquidacion/cliente/ProcesarPedidoInventarioUseCase.java
    - Dependencias inyectadas (constructor):
        * FormaPagoClienteRepository
        * InventarioServicePort
        * ClienteServicePort
        * GenerarPdfLiquidacionClienteUseCase
        * LiquidacionRepository
    - Metodo publico: Mono<Void> ejecutar(DatosPedidoInventarioMessage mensaje)
    - Pipeline reactivo completo (encadenado con .flatMap):
        1. Validar mensaje (declarativo con Optional)
        2. Resolver cliente via `ClienteServicePort.findByIdNacional()` (obtiene idCliente de BD + datos)
        3. Consultar forma de pago (formaPagoRepository.findByIdCliente con idCliente de BD)
        4. Consultar productos (inventarioPort.consultarProductosPorPedido)
        5. Mapear productos a DTOs con ProductoMapper (usar Cliente resuelto, sin llamada a findById)
        6. Generar PDF (generarPdfUseCase.ejecutar)
        7. Construir LiquidacionCliente con idCliente de BD y URI del PDF
        8. Guardar en BD (Mono.fromCallable(() -> repository.saveCliente(...)))
        9. Retornar Mono<Void>
    - Manejo de errores:
        * .onErrorResume para errores de negocio (no matar el pipeline)
        * .doOnError para logging de errores tecnicos con log.error()
    - Logging: log.info() para inicio de procesamiento, log.error() solo para errores tecnicos
    - Decision tecnica: usar Mono.fromCallable para JPA (Opcion B aprobada)

Phase 3: Infrastructure Layer

[x] [T004] Refactorizar PedidoEventConsumer a reactivo
    - Cambiar de Consumer<DatosPedidoInventarioMessage> a Function<DatosPedidoInventarioMessage, Mono<Void>>
    - Inyectar ProcesarPedidoInventarioUseCase en lugar de RegistrarLiquidacionDesdeInventarioUseCase
    - Pipeline del consumer:
        * log.info("Mensaje recibido...")
        * Delegar a procesarPedidoUseCase.ejecutar(mensaje)
        * .doOnSuccess(v -> log.info("Mensaje procesado..."))
        * .onErrorResume(e -> { log.error(...); return Mono.empty(); })  // NO matar el consumer
    - El error en un mensaje no debe afectar el procesamiento de mensajes siguientes
    - NO modificar RegistrarLiquidacionDesdeInventarioUseCase (Opcion B aprobada)

Phase 4: Tests

[x] [T005] Crear tests unitarios para ProcesarPedidoInventarioUseCase
    - Mock de todas las dependencias (5 dependencias)
    - Test: flujo exitoso completo
        * mensaje valido -> resolucion de cliente (findByIdNacional) -> forma pago encontrada -> productos encontrados -> PDF generado -> URI retornada -> guardado en BD
    - Test: mensaje nulo -> completa Mono sin guardar
    - Test: idPedido invalido (cero) -> completa Mono sin guardar
    - Test: idCliente invalido (cero) -> completa Mono sin guardar
    - Test: totalPedido negativo -> completa Mono sin guardar
    - Test: cliente no encontrado por idNacional -> completa Mono sin guardar
    - Test: forma de pago no encontrada para idCliente de BD -> completa Mono sin guardar
    - Test: error al consultar productos del Inventario -> completa Mono sin guardar
    - Test: lista de productos vacia -> completa Mono sin guardar
    - Test: error al generar PDF -> completa Mono sin guardar
    - Usar StepVerifier para testear pipelines reactivos
    - Importante: mockear findByIdNacional en lugar de findById

[x] [T006] Crear tests unitarios para PedidoEventConsumer
    - Mock de ProcesarPedidoInventarioUseCase
    - Test: mensaje procesado exitosamente retorna Mono<Void> completado
    - Test: error en procesamiento NO mata el consumer (retorna Mono.empty())
    - Usar StepVerifier para testear Function<..., Mono<Void>>

[x] [T007] Ejecutar ./gradlew build y verificar que todos los tests pasan
    - 93 tests completados, 0 fallidos, 1 skipped
    - Nuevos tests pasan
    - Compilacion exitosa sin errores

Dependencies & Execution Order

    Phase 1 (Domain) -> Phase 2 (Application) -> Phase 3 (Infrastructure) -> Phase 4 (Tests)

    Phase 2 depende de Phase 1 (necesita las excepciones de dominio)
    Phase 3 depende de Phase 2 (necesita el use case orquestador)
    Phase 4 puede empezar despues de Phase 3 pero se recomienda hacer despues

Success Criteria Mapping

- SC-001 (Generacion < 2 segundos): Medir performance en tests de integracion con StepVerifier con timeout
- SC-002 (Inmutabilidad/auditoria): La liquidacion se guarda con estado PENDIENTE y fecha con el idCliente de BD, rastreable por id_pedido
- SC-003 (1,000 liquidaciones diarias): Pipeline reactivo no bloqueante permite alta concurrencia
- SC de la spec original sobre formula y transporte: NO APLICA (simplificacion aprobada)

Functional Requirements Mapping

- FR-001 (Recibir evento Transporte): NO APLICA - trigger es evento de Inventario (simplificacion)
- FR-002 (Obtener datos del pedido): Datos vienen en el mensaje del Inventario
- FR-003 (Obtener forma de pago): formaPagoRepository.findByIdIdCliente luego de resolver via findByIdNacional
- FR-004 (Guardar estado_final): NO APLICA (simplificacion)
- FR-005 (Consultar productos): InventarioServicePort.consultarProductosPorPedido
- FR-006 (Consultar datos cliente): Se obtienenn de la resolucion via findByIdNacional (sin llamada adicional)
- FR-007 (Generar PDF y guardar URI): GenerarPdfLiquidacionClienteUseCase + guardar en BD
- FR-008 (Registro inmutable): Guardado en BD con fecha y estado PENDIENTE

Decisiones Tecnicas Aprobadas

1. Resolucion de idCliente via findByIdNacional:
   - El id_cliente del mensaje es idNacional (numero de documento)
   - Se debe resolver al idCliente de BD antes de cualquier operacion de base de datos
   - ClienteServicePort.findByIdNacional() retorna Cliente con ambos ids

2. Persistencia reactiva: Mono.fromCallable con JPA existente (Opcion B)
    - No se crea ReactiveLiquidacionRepository
    - Se envuelve repository.saveCliente() en Mono.fromCallable(() -> ...) con Schedulers.boundedElastic()

3. Use case existente: NO modificar RegistrarLiquidacionDesdeInventarioUseCase (Opcion B)
    - Se crea ProcesarPedidoInventarioUseCase nuevo como orquestador
    - RegistrarLiquidacionDesdeInventarioUseCase se mantiene intacto para otros usos/tests

4. Conversion de tipos: Long.parseLong() en mappers con manejo de NumberFormatException
    - Cliente.idCliente (String) -> ClienteLiquidacionDTO.idCliente (Long)
    - Producto.idProducto (String) -> ProductoPedidoDTO.idProducto (Long)
    - Conversion de double a BigDecimal para precios

5. Consumer reactivo: Function<DatosPedidoInventarioMessage, Mono<Void>>
    - Reemplaza Consumer<DatosPedidoInventarioMessage>
    - Aislamiento de errores por mensaje con .onErrorResume(e -> Mono.empty())
    - No se usa .block() en ningun punto del pipeline

6. Simplificacion de negocio:
    - No se procesa evento del Modulo de Transporte
    - No se usan campos: estado_final, tasa_efectividad, tarifa_envio, precio_pedido, total_calculado
    - monto_liquidado = total_pedido (del mensaje del Inventario)
    - estado_liquidacion siempre PENDIENTE
    - Sin formula de calculo

Archivos a Modificar/Crear

| Accion   | Archivo                                                                                       |
|----------|-----------------------------------------------------------------------------------------------|
| Modificar| DomainException.java (agregar nuevas excepciones al sealed)                                   |
| Crear    | ProductosNoEncontradosException.java                                                          |
| Crear    | ClienteNoEncontradoException.java                                                             |
| Crear    | ErrorConsultaProductosException.java                                                          |
| Crear    | ErrorConsultaClienteException.java                                                            |
| Crear    | ProductoMapper.java                                                                           |
| Crear    | ClienteMapper.java                                                                            |
| Crear    | ProcesarPedidoInventarioUseCase.java                                                          |
| Modificar| PedidoEventConsumer.java (refactor a Function<..., Mono<Void>>)                               |
| Crear    | ProcesarPedidoInventarioUseCaseTest.java                                                      |
| Crear    | PedidoEventConsumerTest.java                                                                  |

Consideraciones de Logging

- log.info(): Inicio de procesamiento de mensaje, resolucion exitosa de cliente, exito de generacion de liquidacion
- log.error(): Errores tecnicos (fallo de conexion a servicios externos, excepciones no controladas)
- NO usar log.error() para casos de negocio esperados (cliente no encontrado, productos vacios)
- NO usar log.info() para cada paso intermedio (usar log.debug si es necesario)

Notas de Implementacion

- El flujo de liquidacion recibe `id_cliente` que es el numero de documento (idNacional), NO el idCliente de BD
- Es OBLIGATORIO resolver el idNacional al idCliente de BD usando `ClienteServicePort.findByIdNacional()` antes de:
    * Consultar forma de pago via `FormaPagoClienteRepository.findByIdCliente`
    * Guardar cualquier registro en `liquidaciones_cliente`
- La resolucion del cliente via findByIdNacional proporciona los datos completos del cliente (nombre, telefono, direccion)
    por lo que NO se necesita una llamada adicional a `findById()` despues de la resolucion
- Mantener estilo declarativo con Optional, Stream, lambdas para validaciones
- No modificar archivos de otras features (principio Open/Closed)
- No usar caracteres especiales del espanol (tildes, ñ) en codigo
- Usar nombres en espanol pero sin tildes ni caracteres especiales
- Validar con ./gradlew build despues de cada fase
- Priorizar codificacion en prosa y sintaxis clara
