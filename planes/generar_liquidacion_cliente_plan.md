Implementation Plan: Generar Liquidacion de Cliente

Date: 2026-04-24
Spec: especificaciones/generar_liquidacion_cliente.md

Summary

Implementar la orquestacion reactiva del flujo completo de liquidacion de cliente. Al recibir el evento del Modulo de Inventario, el sistema debe: validar el mensaje, consultar la forma de pago del cliente, consultar los productos del pedido al Modulo de Inventario, consultar los datos del cliente al Modulo de Gestion de Clientes, generar el PDF de liquidacion, subirlo a almacenamiento en la nube, y guardar el registro de liquidacion con la URI del PDF y estado PENDIENTE.

Nota: Esta implementacion SIMPLIFICA la spec original. No se procesa el evento del Modulo de Transporte ni se aplican campos como estado_final, tasa_efectividad, tarifa_envio, ni la formula de calculo. El flujo completo se ejecuta al recibir el evento del Modulo de Inventario.

Technical Context

Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Spring Cloud Stream (para eventos), Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
Storage: PostgreSQL con modelo puertos y adaptadores (arquitectura limpia)
Programming style: Programacion reactiva y funcional, Optional, streams, lambdas, StringBuilder para concatenacion, excepciones de dominio especificas, logging estructurado con SLF4J, validacion Bean Validation
Arquitectura: Arquitectura limpia (hexagonal) - domain, application, infrastructure. Puertos en application, adaptadores en infrastructure
Testing: Test unitarios con Mockito, test de integracion con TestContainers
Target Platform: Linux server, EC2
Performance Goals: <2s para generacion completa de liquidacion (incluye consultas externas + PDF + subida), soportar 100 solicitudes concurrentes
Constraints: <200ms p95 para endpoints criticos, alta disponibilidad para procesos financieros

Project Structure

Source Code (repository root)

src/
â”œâ”€â”€ main/
â”‚   â”œâ”€â”€ java/
â”‚   â”‚   â””â”€â”€ com/
â”‚   â”‚       â””â”€â”€ storeinvoice/
â”‚   â”‚           â””â”€â”€ storeinvoiceapi/
â”‚   â”‚               â”œâ”€â”€ domain/
â”‚   â”‚               â”‚   â”œâ”€â”€ model/
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ Cliente.java                    # Ya existe
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ Producto.java                   # Ya existe
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ LiquidacionCliente.java         # Ya existe
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ FormaPagoCliente.java           # Ya existe
â”‚   â”‚               â”‚   â””â”€â”€ exception/
â”‚   â”‚               â”‚       â”œâ”€â”€ DomainException.java            # Ya existe (sealed)
â”‚   â”‚               â”‚       â”œâ”€â”€ ProductosNoEncontradosException.java
â”‚   â”‚               â”‚       â”œâ”€â”€ ClienteNoEncontradoException.java
â”‚   â”‚               â”‚       â”œâ”€â”€ ErrorConsultaProductosException.java
â”‚   â”‚               â”‚       â””â”€â”€ ErrorConsultaClienteException.java
â”‚   â”‚               â”œâ”€â”€ application/
â”‚   â”‚               â”‚   â”œâ”€â”€ dto/
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ messaging/
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ DatosPedidoInventarioMessage.java   # Ya existe
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ ProductoPedidoDTO.java          # Ya existe
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ ClienteLiquidacionDTO.java      # Ya existe
â”‚   â”‚               â”‚   â”œâ”€â”€ port/
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ InventarioServicePort.java      # Ya existe
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ ClienteServicePort.java         # Ya existe
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ PdfGeneratorPort.java           # Ya existe
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ PdfStoragePort.java             # Ya existe
â”‚   â”‚               â”‚   â”œâ”€â”€ repository/
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ LiquidacionRepository.java      # Ya existe
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ FormaPagoClienteRepository.java # Ya existe
â”‚   â”‚               â”‚   â”œâ”€â”€ service/
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ liquidacion/
â”‚   â”‚               â”‚   â”‚       â”œâ”€â”€ cliente/
â”‚   â”‚               â”‚   â”‚       â”‚   â”œâ”€â”€ RegistrarLiquidacionDesdeInventarioUseCase.java   # Ya existe (NO modificar)
â”‚   â”‚               â”‚   â”‚       â”‚   â””â”€â”€ ProcesarPedidoInventarioUseCase.java              # NUEVO
â”‚   â”‚               â”‚   â”‚       â””â”€â”€ mapper/
â”‚   â”‚               â”‚   â”‚           â”œâ”€â”€ ProductoMapper.java     # NUEVO
â”‚   â”‚               â”‚   â”‚           â””â”€â”€ ClienteMapper.java      # NUEVO
â”‚   â”‚               â””â”€â”€ infrastructure/
â”‚   â”‚                   â”œâ”€â”€ adapter/
â”‚   â”‚                   â”‚   â”œâ”€â”€ inbound/
â”‚   â”‚                   â”‚   â”‚   â””â”€â”€ messaging/
â”‚   â”‚                   â”‚   â”‚       â””â”€â”€ PedidoEventConsumer.java            # MODIFICAR (refactor a reactivo)
â”‚   â”‚                   â”‚   â””â”€â”€ outbound/
â”‚   â”‚                   â”‚       â”œâ”€â”€ persistence/
â”‚   â”‚                   â”‚       â”‚   â””â”€â”€ LiquidacionRepositoryAdapter.java   # Ya existe
â”‚   â”‚                   â”‚       â”œâ”€â”€ external/
â”‚   â”‚                   â”‚       â”‚   â”œâ”€â”€ InventarioWebClient.java            # Ya existe
â”‚   â”‚                   â”‚       â”‚   â””â”€â”€ ClienteWebClient.java               # Ya existe
â”‚   â”‚                   â”‚       â”œâ”€â”€ pdf/
â”‚   â”‚                   â”‚       â”‚   â””â”€â”€ OpenPdfGeneratorAdapter.java        # Ya existe
â”‚   â”‚                   â”‚       â””â”€â”€ storage/
â”‚   â”‚                   â”‚           â”œâ”€â”€ LocalFileStorageAdapter.java        # Ya existe
â”‚   â”‚                   â”‚           â””â”€â”€ SupabaseStorageAdapter.java         # Ya existe
â”‚   â”‚                   â””â”€â”€ persistence/
â”‚   â”‚                       â”œâ”€â”€ entity/
â”‚   â”‚                       â”‚   â””â”€â”€ LiquidacionClienteJpaEntity.java        # Ya existe
â”‚   â”‚                       â””â”€â”€ mapper/
â”‚   â”‚                           â””â”€â”€ LiquidacionEntityMapper.java            # Ya existe
â”‚   â””â”€â”€ resources/
â”‚       â”œâ”€â”€ application.yml                                     # Ya existe
â”‚       â””â”€â”€ db/migration/
â”‚           â”œâ”€â”€ V1__create_liquidacion_cliente_table.sql        # Ya existe
â”‚           â””â”€â”€ (sin migraciones nuevas - no cambia schema)
â”‚
â””â”€â”€ test/
    â””â”€â”€ java/
        â””â”€â”€ com/
            â””â”€â”€ storeinvoice/
                â””â”€â”€ storeinvoiceapi/
                    â”œâ”€â”€ application/
                    â”‚   â””â”€â”€ service/
                    â”‚       â””â”€â”€ liquidacion/
                    â”‚           â””â”€â”€ cliente/
                    â”‚               â”œâ”€â”€ RegistrarLiquidacionDesdeInventarioUseCaseTest.java   # Ya existe
                    â”‚               â””â”€â”€ ProcesarPedidoInventarioUseCaseTest.java              # NUEVO
                    â””â”€â”€ infrastructure/
                        â””â”€â”€ adapter/
                            â””â”€â”€ inbound/
                                â””â”€â”€ messaging/
                                    â””â”€â”€ PedidoEventConsumerTest.java                          # NUEVO

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
        1. Validar mensaje (declarativo con Optional, igual que RegistrarLiquidacionDesdeInventarioUseCase)
        2. Consultar forma de pago (formaPagoRepository.findByIdCliente -> Mono.justOrEmpty)
        3. Consultar productos (inventarioPort.consultarProductosPorPedido)
        4. Consultar cliente (clientePort.findById)
        5. Mapear productos y cliente a DTOs con los mappers
        6. Generar PDF (generarPdfUseCase.ejecutar)
        7. Construir LiquidacionCliente con URI del PDF
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
        * mensaje valido -> forma pago encontrada -> productos encontrados -> cliente encontrado -> PDF generado -> URI retornada -> guardado en BD
    - Test: mensaje nulo -> completa Mono sin guardar
    - Test: idPedido invalido (cero) -> completa Mono sin guardar
    - Test: idCliente invalido (cero) -> completa Mono sin guardar
    - Test: totalPedido negativo -> completa Mono sin guardar
    - Test: forma de pago no encontrada -> completa Mono sin guardar
    - Test: error al consultar productos del Inventario -> completa Mono sin guardar
    - Test: lista de productos vacia -> completa Mono sin guardar
    - Test: cliente no encontrado -> completa Mono sin guardar
    - Test: error al consultar datos del cliente -> completa Mono sin guardar
    - Test: error al generar PDF -> completa Mono sin guardar
    - Usar StepVerifier para testear pipelines reactivos

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
- SC-002 (Inmutabilidad/auditoria): La liquidacion se guarda con estado PENDIENTE y fecha actual, rastreable por id_pedido
- SC-003 (1,000 liquidaciones diarias): Pipeline reactivo no bloqueante permite alta concurrencia
- SC de la spec original sobre formula y transporte: NO APLICA (simplificacion aprobada)

Functional Requirements Mapping

- FR-001 (Recibir evento Transporte): NO APLICA - trigger es evento de Inventario (simplificacion)
- FR-002 (Obtener datos del pedido): Datos vienen en el mensaje del Inventario
- FR-003 (Obtener forma de pago): formaPagoRepository.findByIdCliente
- FR-004 (Guardar estado_final): NO APLICA (simplificacion)
- FR-005 (Consultar productos): InventarioServicePort.consultarProductosPorPedido
- FR-006 (Consultar datos cliente): ClienteServicePort.findById
- FR-007 (Generar PDF y guardar URI): GenerarPdfLiquidacionClienteUseCase + guardar en BD
- FR-008 (Registro inmutable): Guardado en BD con fecha y estado PENDIENTE

Decisiones Tecnicas Aprobadas

1. Persistencia reactiva: Mono.fromCallable con JPA existente (Opcion B)
   - No se crea ReactiveLiquidacionRepository
   - Se envuelve repository.saveCliente() en Mono.fromCallable(() -> ...) con Schedulers.boundedElastic()

2. Use case existente: NO modificar RegistrarLiquidacionDesdeInventarioUseCase (Opcion B)
   - Se crea ProcesarPedidoInventarioUseCase nuevo como orquestador
   - RegistrarLiquidacionDesdeInventarioUseCase se mantiene intacto para otros usos/tests

3. Conversion de tipos: Long.parseLong() en mappers con manejo de NumberFormatException
   - Cliente.idCliente (String) -> ClienteLiquidacionDTO.idCliente (Long)
   - Producto.idProducto (String) -> ProductoPedidoDTO.idProducto (Long)
   - Conversion de double a BigDecimal para precios

4. Consumer reactivo: Function<DatosPedidoInventarioMessage, Mono<Void>>
   - Reemplaza Consumer<DatosPedidoInventarioMessage>
   - Aislamiento de errores por mensaje con .onErrorResume(e -> Mono.empty())
   - No se usa .block() en ningun punto del pipeline

5. Simplificacion de negocio:
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

- log.info(): Inicio de procesamiento de mensaje, exito de generacion de liquidacion
- log.error(): Errores tecnicos (fallo de conexion a servicios externos, excepciones no controladas)
- NO usar log.error() para casos de negocio esperados (cliente no encontrado, productos vacios)
- NO usar log.info() para cada paso intermedio (usar log.debug si es necesario)

Notas de Implementacion

- Mantener estilo declarativo con Optional, Stream, lambdas donde aplique
- No modificar archivos de otras features (principio Open/Closed)
- No usar caracteres especiales del espanol (tildes, n) en codigo
- Usar nombres en espanol pero sin tildes ni caracteres especiales
- Validar con ./gradlew build despues de cada fase
- Priorizar codificacion en prosa y sintaxis clara

