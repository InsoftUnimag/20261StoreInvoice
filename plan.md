Implementation Plan: [FEATURE]

Date: [DATE] Spec: [link]
Summary

[Extract from feature spec: primary requirement + technical approach from research]
Technical Context


Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.4.0, Spring Data JPA, Spring WebFlux, Spring Cloud Stream (para eventos), Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
Storage: PostgreSQL con modelo puertos y adaptadores (arquitectura limpia) para no acoplarse a una BD especÃ­fica
Programming style: Usar programaciÃ³n reactiva, funcional, usar Optional, streams, lambdas, usar desarrollo con el menor memory leak (usar StringBuilder en vez de "2 " + "asda") de acuerdo al lenguaje. Usar excepciones particulares del dominio para manejar excepciones de dominio y usar global exceptions handler para manejar excepciones globales, usar logging para loguear errores y excepciones, usar validaciÃ³n de datos para validar datos en entrada, usar Spring Security para autenticar y autorizar usuarios. Priorizar la codificaciÃ³n en prosa y la codificaciÃ³n en sintaxis, permitiendo entender el cÃ³digo mÃ¡s fÃ¡cilmente. Lombok para evitar boilerplate (getters, setters, constructors, builders en entities y adapters). Records para DTOs de respuesta y de entrada de datos (inmutables,equals/hashCode/toString). Validaciones de acuerdo a los test comentados en la especificaciÃ³n.
Arquitectura: Arquitectura limpia (domain, use cases, infrastructure) con principios SOLID, sin acoplamiento entre capas
Testing: Test unitarios con Mockito, test de integraciÃ³n con TestContainers
Target Platform: Linux server, EC2
Project Type: Backend API (sistema financiero que se comunica mediante eventos y APIs REST)
Performance Goals: <500ms para consultas, <2s para generaciÃ³n de liquidaciones, soportar 100 solicitudes concurrentes
Constraints: <200ms p95 para endpoints crÃ­ticos, <100MB memoria heap, alta disponibilidad para procesos financieros
Scale/Scope: 10k transacciones diarias, 1M registros en BD, 50 endpoints API
Project Structure
Documentation (this feature)

specs/[feature]/
â”œâ”€â”€ plan.md              # This file 
â””â”€â”€ spec.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)

Source Code (repository root)
<!-- ACTION REQUIRED: Replace the placeholder tree below with the concrete layout for this feature. Delete unused options and expand the chosen structure with real paths (e.g., apps/admin, packages/something). The delivered plan must not include Option labels. -->

# Estructura de Proyecto: Backend API (Sistema Financiero) - Arquitectura Hexagonal
#
# PRINCIPIOS DE ARQUITECTURA HEXAGONAL (PUERTOS Y ADAPTADORES):
# - Domain (hexÃ¡gono central): NO depende de nada externo. Contiene entidades, value objects, eventos de dominio
# - Application: Depende SOLO de domain. Contiene casos de uso, repositorios (contratos), DTOs
# - Infrastructure: Depende de application y domain. Contiene adaptadores concretos (REST, messaging, persistence)
#
# FLUJO DE DEPENDENCIAS: application â†’ domain (infraestructura depende de aplicaciÃ³n)
#
# CONSIDERACIONES ARQUITECTÃ“NICAS:
# 1. Repositorios (interfaces puras, sin tecnologÃ­a) van en application/repository/
# 2. Implementaciones de repositorios van en infrastructure/adapter/outbound/persistence/
# 3. Todos los mappers van en infrastructure/mapper/
# 4. Use cases retornan DOMAIN MODELS (no DTOs) - el use case ES el inbound port
# 5. Controllers inyectan los use cases directamente
# 6. FLUJO DE DATOS:
#    - Controller (entrada): Request DTO â†’ Domain Model
#    - Controller (salida): Domain Model â†’ Response DTO
#    - Use Case: Trabaja con Domain Models (retorna Domain Model)
#    - Repository Adapter: Domain Model â†” JPA Entity
#
src/
â”œâ”€â”€ main/
â”‚   â”œâ”€â”€ java/
â”‚   â”‚   â””â”€â”€ com/
â”‚   â”‚       â””â”€â”€ storeinvoice/
â”‚   â”‚           â””â”€â”€ storeinvoiceapi/
â”‚   â”‚               â”œâ”€â”€ StoreInvoiceApiApplication.java
â”‚   â”‚               â”‚
â”‚   â”‚               â”œâ”€â”€ domain/                              # HEXÃGONO CENTRAL - Sin dependencias externas
â”‚   â”‚               â”‚   â”œâ”€â”€ model/                           # Entidades de dominio (POJOs ricos con lÃ³gica)
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ Pedido.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ Cliente.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ LiquidacionCliente.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ LiquidacionTransportista.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ FormaPago.java
â”‚   â”‚               â”‚   â”œâ”€â”€ valueobject/                     # Value Objects (objetos inmutables por valor)
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ IdNacional.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ Direccion.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ Monto.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ TasaEfectividad.java
â”‚   â”‚               â”‚   â”œâ”€â”€ event/                           # Eventos de dominio
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ PedidoRegistradoEvent.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ LiquidacionGeneradaEvent.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ EstadoFinalEntregaEvent.java
â”‚   â”‚               â”‚   â””â”€â”€ exception/                       # Excepciones de dominio
â”‚   â”‚               â”‚       â”œâ”€â”€ PedidoNotFoundException.java
â”‚   â”‚               â”‚       â”œâ”€â”€ ClienteNotFoundException.java
â”‚   â”‚               â”‚       â”œâ”€â”€ LiquidacionException.java
â”‚   â”‚               â”‚       â””â”€â”€ DomainException.java
â”‚   â”‚               â”‚
â”‚   â”‚               â”œâ”€â”€ application/                         # CAPA DE APLICACIÃ“N - Depende solo de domain
â”‚   â”‚               â”‚   â”œâ”€â”€ service/                         # CASOS DE USO (Application Services) - Implementan Inbound Ports
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ cliente/
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ ConsultarClientePorIdNacionalUseCase.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ ConsultarFormaPagoClienteUseCase.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ pedido/
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ RegistrarPedidoUseCase.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ ConsultarPedidoUseCase.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ liquidacion/
â”‚   â”‚               â”‚   â”‚       â”œâ”€â”€ GenerarLiquidacionesUseCase.java
â”‚   â”‚               â”‚   â”‚       â””â”€â”€ ConsultarLiquidacionesUseCase.java
â”‚   â”‚               â”‚   â”œâ”€â”€ repository/                      # REPOSITORIOS (Interfaces puras - sin tecnologÃ­a)
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ LiquidacionRepository.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ ClienteRepository.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ PedidoRepository.java
â”‚   â”‚               â”‚   â”œâ”€â”€ dto/                             # DTOs - Transferencia de datos
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ command/                     # Comandos (escritura)
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ RegistrarPedidoCommand.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ ProcesarEstadoFinalCommand.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ query/                       # Consultas (lectura con filtros)
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ ConsultarLiquidacionesQuery.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ ConsultarClienteQuery.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ response/                    # Respuestas (lectura)
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ ClienteResponse.java
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ FormaPagoResponse.java
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ LiquidacionClienteResponse.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ LiquidacionTransportistaResponse.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ client/                       # DTOs para clientes externos
â”‚   â”‚               â”‚   â”‚       â””â”€â”€ ClienteDto.java
â”‚   â”‚               â”‚   â””â”€â”€ exception/                       # Excepciones de aplicaciÃ³n
â”‚   â”‚               â”‚       â”œâ”€â”€ ApplicationException.java
â”‚   â”‚               â”‚       â””â”€â”€ UseCaseExecutionException.java
â”‚   â”‚               â”‚
â”‚   â”‚               â”œâ”€â”€ infrastructure/                      # INFRAESTRUCTURA - Adaptadores concretos
â”‚   â”‚               â”‚   â”œâ”€â”€ adapter/
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ inbound/                     # ADAPTADORES ENTRANTES (Driving Adapters)
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ rest/                    # Controllers REST (consumen Use Cases/Inbound Ports)
â”‚   â”‚               â”‚   â”‚   â”‚   â”‚   â”œâ”€â”€ ClienteController.java
â”‚   â”‚               â”‚   â”‚   â”‚   â”‚   â”œâ”€â”€ PedidoController.java
â”‚   â”‚               â”‚   â”‚   â”‚   â”‚   â””â”€â”€ LiquidacionController.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ messaging/               # Consumers de eventos
â”‚   â”‚               â”‚   â”‚   â”‚       â”œâ”€â”€ PedidoEventConsumer.java
â”‚   â”‚               â”‚   â”‚   â”‚       â””â”€â”€ EstadoFinalEventConsumer.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ outbound/                    # ADAPTADORES SALIENTES (Driven Adapters)
â”‚   â”‚               â”‚   â”‚       â”œâ”€â”€ persistence/             # Implementaciones de repositorios
â”‚   â”‚               â”‚   â”‚       â”‚   â”œâ”€â”€ PedidoRepositoryAdapter.java
â”‚   â”‚               â”‚   â”‚       â”‚   â”œâ”€â”€ ClienteRepositoryAdapter.java
â”‚   â”‚               â”‚   â”‚       â”‚   â””â”€â”€ LiquidacionRepositoryAdapter.java
â”‚   â”‚               â”‚   â”‚       â””â”€â”€ external/                # Implementaciones de servicios externos
â”‚   â”‚               â”‚   â”‚           â”œâ”€â”€ ClienteWebClient.java
â”‚   â”‚               â”‚   â”‚           â”œâ”€â”€ InventarioServiceAdapter.java
â”‚   â”‚               â”‚   â”‚           â””â”€â”€ TransporteServiceAdapter.java
â”‚   â”‚               â”‚   â”œâ”€â”€ persistence/                     # CONFIGURACIÃ“N DE PERSISTENCIA
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ entity/                      # Entidades JPA (mapeo a BD)
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ PedidoJpaEntity.java
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ ClienteJpaEntity.java
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ LiquidacionClienteJpaEntity.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ LiquidacionTransportistaJpaEntity.java
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ mapper/                      # MAPPERS JPA â†” Domain
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ PedidoJpaMapper.java
â”‚   â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ ClienteJpaMapper.java
â”‚   â”‚               â”‚   â”‚   â”‚   â””â”€â”€ LiquidacionJpaMapper.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ DatabaseConfig.java
â”‚   â”‚               â”‚   â”œâ”€â”€ messaging/                       # CONFIGURACIÃ“N DE MENSAJERÃA
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ EventConfig.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ DeadLetterQueueConfig.java
â”‚   â”‚               â”‚   â”œâ”€â”€ security/                        # CONFIGURACIÃ“N DE SEGURIDAD
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ SecurityConfig.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ JwtAuthenticationFilter.java
â”‚   â”‚               â”‚   â”œâ”€â”€ pdf/                             # GENERACIÃ“N DE PDF
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ PdfGeneratorAdapter.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ PdfTemplateConfig.java
â”‚   â”‚               â”‚
â”‚   â”‚               â””â”€â”€ config/                              # CONFIGURACIÃ“N GENERAL
â”‚   â”‚                   â”œâ”€â”€ ApplicationProperties.java
â”‚   â”‚                   â””â”€â”€ SwaggerConfig.java
â”‚   â”‚
â”‚   â””â”€â”€ resources/
â”‚       â”œâ”€â”€ application.yml
â”‚       â”œâ”€â”€ application-dev.yml
â”‚       â”œâ”€â”€ application-prod.yml
â”‚       â””â”€â”€ db/migration/
â”‚
â””â”€â”€ test/
    â”œâ”€â”€ java/
    â”‚   â””â”€â”€ com/
    â”‚       â””â”€â”€ storeinvoice/
    â”‚           â””â”€â”€ storeinvoiceapi/
    â”‚               â”œâ”€â”€ domain/
    â”‚               â”‚   â”œâ”€â”€ model/
    â”‚               â”‚   â”‚   â””â”€â”€ PedidoTest.java
    â”‚               â”‚   â”œâ”€â”€ valueobject/
    â”‚               â”‚   â”‚   â”œâ”€â”€ IdNacionalTest.java
    â”‚               â”‚   â”‚   â””â”€â”€ MontoTest.java
    â”‚               â”‚   â”œâ”€â”€ event/
    â”‚               â”‚   â”‚   â””â”€â”€ # Tests de eventos de dominio
    â”‚               â”‚   â””â”€â”€ exception/
    â”‚               â”‚       â””â”€â”€ # Tests de excepciones de dominio
    â”‚               â”œâ”€â”€ application/
    â”‚               â”‚   â”œâ”€â”€ service/
    â”‚               â”‚   â”‚   â”œâ”€â”€ cliente/
    â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ ConsultarClientePorIdNacionalUseCaseTest.java
    â”‚               â”‚   â”‚   â”‚   â””â”€â”€ ConsultarFormaPagoClienteUseCaseTest.java
    â”‚               â”‚   â”‚   â”œâ”€â”€ pedido/
    â”‚               â”‚   â”‚   â”‚   â”œâ”€â”€ RegistrarPedidoUseCaseTest.java
    â”‚               â”‚   â”‚   â”‚   â””â”€â”€ ConsultarPedidoUseCaseTest.java
    â”‚               â”‚   â”‚   â””â”€â”€ liquidacion/
    â”‚               â”‚   â”‚       â”œâ”€â”€ GenerarLiquidacionesUseCaseTest.java
    â”‚               â”‚   â”‚       â””â”€â”€ ConsultarLiquidacionesUseCaseTest.java
    â”‚               â”‚   â””â”€â”€ dto/
    â”‚               â”‚       â””â”€â”€ # Tests de validaciÃ³n de DTOs
    â”‚               â””â”€â”€ infrastructure/
    â”‚                   â”œâ”€â”€ adapter/
    â”‚                   â”‚   â”œâ”€â”€ inbound/
    â”‚                   â”‚   â”‚   â”œâ”€â”€ rest/
    â”‚                   â”‚   â”‚   â”‚   â”œâ”€â”€ ClienteControllerTest.java
    â”‚                   â”‚   â”‚   â”‚   â”œâ”€â”€ PedidoControllerTest.java
    â”‚                   â”‚   â”‚   â”‚   â””â”€â”€ LiquidacionControllerTest.java
    â”‚                   â”‚   â”‚   â””â”€â”€ messaging/
    â”‚                   â”‚   â”‚       â”œâ”€â”€ PedidoEventConsumerTest.java
    â”‚                   â”‚   â”‚       â””â”€â”€ EstadoFinalEventConsumerTest.java
    â”‚                   â”‚   â””â”€â”€ outbound/
    â”‚                   â”‚       â”œâ”€â”€ persistence/
    â”‚                   â”‚       â”‚   â”œâ”€â”€ PedidoRepositoryAdapterTest.java
    â”‚                   â”‚       â”‚   â”œâ”€â”€ ClienteRepositoryAdapterTest.java
    â”‚                   â”‚       â”‚   â””â”€â”€ LiquidacionRepositoryAdapterTest.java
    â”‚                   â”‚       â””â”€â”€ external/
    â”‚                   â”‚           â”œâ”€â”€ InventarioServiceAdapterTest.java
    â”‚                   â”‚           â””â”€â”€ TransporteServiceAdapterTest.java
    â”‚                   â””â”€â”€ integration/
    â”‚                       â”œâ”€â”€ PedidoIntegrationTest.java
    â”‚                       â”œâ”€â”€ ClienteIntegrationTest.java
    â”‚                       â””â”€â”€ LiquidacionIntegrationTest.java
    â”‚               â””â”€â”€ TestcontainersConfiguration.java
    â”‚       â”œâ”€â”€ test-application.yml
    â”‚       â””â”€â”€ test-containers-config.yml

Structure Decision: Backend API con arquitectura limpia (Clean Architecture) basada en capas de dominio, aplicaciÃ³n e infraestructura. El proyecto sigue el patrÃ³n puertos y adaptadores para mantener el dominio desacoplado de la infraestructura. Se utiliza Spring Boot 3.x con Java 21 para implementar un sistema financiero que se comunica mediante eventos y APIs REST.

Consideraciones de ProgramaciÃ³n Reactiva y Funcional:

- Utilizar programaciÃ³n reactiva con Spring WebFlux para endpoints crÃ­ticos que requieran alta concurrencia
- Implementar streams y lambdas para operaciones de colecciones y transformaciones de datos
- Usar Optional para evitar NullPointerExceptions y manejar valores opcionales de forma segura
- Emplear StringBuilder en lugar de concatenaciÃ³n de strings con el operador + para evitar memory leaks
- Implementar excepciones particulares del dominio (Domain Exceptions) para manejo de errores especÃ­ficos del negocio
- Utilizar Global Exception Handler para manejar excepciones globales de forma consistente
- Implementar logging estructurado con SLF4J para trazabilidad y monitoreo
- Aplicar validaciÃ³n de datos en capa de entrada con Bean Validation (JSR-380)
- Configurar Spring Security para autenticaciÃ³n y autorizaciÃ³n robusta
- Aplicar principios SOLID para mantener cÃ³digo limpio y mantenible
- Implementar tests unitarios con Mockito para simular dependencias externas
- Utilizar TestContainers para pruebas de integraciÃ³n con bases de datos reales en contenedores Docker
- Implementar tests de contrato con Spring Cloud Contract para validar contratos entre servicios
- Priorizar la codificaciÃ³n en prosa y la codificaciÃ³n en sintaxis, permitiendo entender el cÃ³digo mÃ¡s fÃ¡cilmente
<!-- ============================================================================ IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only. You MUST replace these with actual tasks based on: - User stories from spec.md - Feature requirements from this file - Entities required for the use case - Endpoints required DO NOT keep these sample tasks. ============================================================================ -->
Phase 1: Setup (Shared Infrastructure)

Purpose: Project initialization and basic structure

    [ ] T001 Crear proyecto Maven/Gradle con Java 21 y Spring Boot 3.x
    [ ] T002 Configurar dependencias principales (Spring Data JPA, Spring Security, Spring Cloud Stream, TestContainers)
    [ ] T003 Crear configuraciÃ³n base de Docker y Docker Compose para PostgreSQL y Redis
    [ ] T004 Configurar CI/CD bÃ¡sico (GitHub Actions o GitLab CI)
    [ ] T005 Crear documentaciÃ³n inicial del proyecto (README, arquitectura, guÃ­a de desarrollo)

Phase 2: Foundational (Blocking Prerequisites)

Purpose: Core infrastructure that MUST be complete before ANY user story can be implemented

âš ï¸ CRITICAL: No user story work can begin until this phase is complete

    [ ] T006 Crear entidades base del dominio (Pedido, Cliente, LiquidacionCliente, LiquidacionTransportista, FormaPago)
    [ ] T007 Configurar base de datos PostgreSQL con Flyway para migraciones
    [ ] T008 Implementar capa de persistencia con JPA/Hibernate (puertos y adaptadores)
    [ ] T009 Configurar manejo de eventos con Spring Cloud Stream (RabbitMQ) // 
    [ ] T010 Implementar configuraciÃ³n de logging estructurado (SLF4J + Logback)
    [ ] T011 Configurar manejo de excepciones global y respuestas de error estandarizadas
    [ ] T012 Implementar configuraciÃ³n de seguridad bÃ¡sica (Spring Security)
    [ ] T013 Configurar validaciÃ³n de datos con Bean Validation (JSR-380)
    [ ] T014 Crear DTOs base para transferencia de datos entre capas
    [ ] T015 Configurar Swagger/OpenAPI para documentaciÃ³n de APIs
    [ ] T016 Implementar configuraciÃ³n de entornos (dev, test, prod)
    [ ] T017 Crear pruebas unitarias base para capa de dominio

Checkpoint: Foundation ready - user story implementation can now begin in parallel

Checkpoint: Foundation ready - user story implementation can now begin in parallel
Phase 3: User Story 1 - Consulta de Cliente por ID Nacional (Priority: P1)

Goal: Permitir al Sistema Financiero consultar un cliente utilizando su ID Nacional (nÃºmero de documento) para obtener su ID de base de datos y poder guardar la forma de pago asociada al cliente correcto en la base de datos.

Independent Test: El endpoint debe retornar el ID de base de datos del cliente en menos de 500ms cuando se proporciona un ID Nacional vÃ¡lido.

Tests for User Story 1

    [ ] T018 Crear test de contrato para GET /api/v1/clientes/{id_nacional} en tests/contract/test_consultar_cliente_id_nacional.java
    [ ] T019 Crear test de integraciÃ³n para flujo completo de consulta de cliente en tests/integration/test_consulta_cliente_completa.java

Implementation for User Story 1

    [ ] T020 Crear entidad Cliente en src/main/java/com/storeinvoice/storeinvoiceapi/domain/model/Cliente.java
    [ ] T021 Crear repositorio ClienteRepository en src/main/java/com/storeinvoice/storeinvoiceapi/application/repository/ClienteRepository.java
    [ ] T022 Implementar ClienteRepositoryAdapter en src/main/java/com/storeinvoice/storeinvoiceapi/infrastructure/adapter/outbound/persistence/ClienteRepositoryAdapter.java
    [ ] T023 Crear caso de uso ConsultarClientePorIdNacionalUseCase en src/main/java/com/storeinvoice/storeinvoiceapi/application/service/cliente/
    [ ] T024 Crear ClienteController en src/main/java/com/storeinvoice/storeinvoiceapi/infrastructure/adapter/inbound/rest/ClienteController.java
    [ ] T025 Implementar endpoint GET /api/v1/clientes/{id_nacional} con validaciÃ³n y manejo de errores
    [ ] T026 Agregar logging para operaciones de consulta de cliente
    [ ] T027 Crear ClienteResponse en src/main/java/com/storeinvoice/storeinvoiceapi/application/dto/response/

Checkpoint: At this point, User Story 1 should be fully functional and testable independently
Phase 4: User Story 2 - Consulta de Forma de Pago del Cliente (Priority: P2)

Goal: Permitir al Sistema Financiero consultar la forma de pago de un cliente por su ID para usar en funciones internas como la generaciÃ³n de liquidaciones.

Independent Test: El endpoint debe retornar la forma de pago del cliente en menos de 500ms cuando se proporciona un ID de pedido vÃ¡lido.

Tests for User Story 2

    [ ] T028 Crear test de contrato para GET /api/v1/pedidos/{id_pedido}/forma-pago en tests/contract/test_consultar_forma_pago.java
    [ ] T029 Crear test de integraciÃ³n para flujo completo de consulta de forma de pago en tests/integration/test_consulta_forma_pago_completa.java

Implementation for User Story 2

    [ ] T030 Crear lÃ³gica de dominio para FormaPago en src/main/java/com/storeinvoice/storeinvoiceapi/domain/model/FormaPago.java
    [ ] T031 Implementar lÃ³gica para buscar forma de pago por ID de cliente
    [ ] T032 Crear caso de uso ConsultarFormaPagoClienteUseCase en src/main/java/com/storeinvoice/storeinvoiceapi/application/service/cliente/
    [ ] T033 Crear endpoint GET /api/v1/pedidos/{id_pedido}/forma-pago en ClienteController
    [ ] T034 Implementar validaciÃ³n de formas de pago vÃ¡lidas (CONTRA_ENTREGA, CARTERA_COMERCIAL)
    [ ] T035 Agregar logging para operaciones de consulta de forma de pago
    [ ] T036 Crear FormaPagoResponse en src/main/java/com/storeinvoice/storeinvoiceapi/application/dto/response/

Checkpoint: At this point, User Stories 1 AND 2 should both work independently
Phase 5: User Story 3 - RecepciÃ³n de Datos del Pedido desde MÃ³dulo de Inventario (Priority: P3)

Goal: Permitir al Sistema Financiero recibir los datos del pedido del MÃ³dulo de Inventario cuando se crea un nuevo pedido, para usarlos en la liquidaciÃ³n final.

Independent Test: El sistema debe recibir y almacenar exitosamente los datos del pedido (id_pedido, id_cliente, total_pedido, direccion) en menos de 2 segundos.

Tests for User Story 3

    [ ] T037 Crear test de contrato para evento de recepciÃ³n de pedido en tests/contract/test_recepcion_pedido_event.java
    [ ] T038 Crear test de integraciÃ³n para flujo completo de recepciÃ³n y almacenamiento de pedido en tests/integration/test_recepcion_pedido_completa.java

Implementation for User Story 3

    [ ] T039 Crear entidad Pedido en src/main/java/com/storeinvoice/storeinvoiceapi/domain/model/Pedido.java
    [ ] T040 Crear repositorio PedidoRepository en src/main/java/com/storeinvoice/storeinvoiceapi/application/repository/PedidoRepository.java
    [ ] T041 Implementar PedidoRepositoryAdapter en src/main/java/com/storeinvoice/storeinvoiceapi/infrastructure/adapter/outbound/persistence/PedidoRepositoryAdapter.java
    [ ] T042 Crear caso de uso RegistrarPedidoUseCase en src/main/java/com/storeinvoice/storeinvoiceapi/application/service/pedido/
    [ ] T043 Crear PedidoEventConsumer en src/main/java/com/storeinvoice/storeinvoiceapi/infrastructure/adapter/inbound/messaging/PedidoEventConsumer.java
    [ ] T044 Implementar lÃ³gica para consultar forma de pago del cliente al recibir el pedido
    [ ] T045 Crear RegistrarPedidoCommand en src/main/java/com/storeinvoice/storeinvoiceapi/application/dto/command/
    [ ] T046 Implementar validaciÃ³n de datos del pedido recibido
    [ ] T047 Agregar logging para operaciones de recepciÃ³n de pedido

Checkpoint: All user stories should now be independently functional

Phase 6: User Story 4 - RecepciÃ³n de Estado Final de Entrega del MÃ³dulo de Transporte (Priority: P4)

Goal: Permitir al Sistema Financiero recibir el estado final de entrega del MÃ³dulo de Transporte para generar las liquidaciones del cliente y transportista.

Independent Test: El sistema debe recibir el evento, validar los datos y procesar las liquidaciones en menos de 5 segundos.

Tests for User Story 4

    [ ] T048 Crear test de contrato para evento de estado final en tests/contract/test_estado_final_event.java
    [ ] T049 Crear test de integraciÃ³n para flujo completo de recepciÃ³n y procesamiento de liquidaciones en tests/integration/test_procesamiento_liquidaciones_completa.java

Implementation for User Story 4

    [ ] T050 Crear EstadoFinalEventConsumer en src/main/java/com/storeinvoice/storeinvoiceapi/infrastructure/adapter/inbound/messaging/EstadoFinalEventConsumer.java
    [ ] T051 Crear lÃ³gica de generaciÃ³n de liquidaciones en src/main/java/com/storeinvoice/storeinvoiceapi/application/service/liquidacion/GenerarLiquidacionesUseCase.java
    [ ] T052 Implementar lÃ³gica para generar liquidaciÃ³n del cliente segÃºn fÃ³rmula: (precio_pedido + tarifa_envÃ­o) Ã— (tasa_efectividad / 100)
    [ ] T053 Implementar lÃ³gica para generar liquidaciÃ³n del transportista segÃºn matriz de porcentajes
    [ ] T054 Crear PdfGeneratorAdapter en src/main/java/com/storeinvoice/storeinvoiceapi/infrastructure/adapter/outbound/pdf/PdfGeneratorAdapter.java
    [ ] T055 Implementar validaciÃ³n de datos del evento (id_pedido, estado_final, tasa_efectividad, id_transportista)
    [ ] T056 Crear entidades LiquidacionCliente y LiquidacionTransportista en domain/model/
    [ ] T057 Implementar repositorio LiquidacionRepository en application/repository/ y adapter en infrastructure/adapter/outbound/persistence/
    [ ] T058 Agregar logging para operaciones de generaciÃ³n de liquidaciones

Phase 7: User Story 5 - Consulta de Liquidaciones (Priority: P5)

Goal: Permitir a clientes, transportistas y contadores consultar las liquidaciones generadas.

Independent Test: Los endpoints deben retornar las liquidaciones paginadas en menos de 3 segundos.

Tests for User Story 5

    [ ] T059 Crear test de contrato para endpoints de consulta de liquidaciones en tests/contract/test_consulta_liquidaciones.java
    [ ] T060 Crear test de integraciÃ³n para flujo completo de consulta de liquidaciones en tests/integration/test_consulta_liquidaciones_completa.java

Implementation for User Story 5

    [ ] T061 Crear LiquidacionController en src/main/java/com/storeinvoice/storeinvoiceapi/infrastructure/adapter/inbound/rest/LiquidacionController.java
    [ ] T062 Implementar endpoint GET /api/v1/pedidos/{id_pedido}/liquidaciones para clientes
    [ ] T063 Implementar endpoint GET /api/v1/pedidos/{id_pedido}/liquidaciones para transportistas
    [ ] T064 Implementar endpoint GET /api/v1/liquidaciones con filtros para contadores
    [ ] T065 Implementar paginaciÃ³n (20 registros por pÃ¡gina por defecto)
    [ ] T066 Implementar filtros por tipo, cliente, transportista y rango de fechas
    [ ] T067 Crear LiquidacionClienteResponse y LiquidacionTransportistaResponse en application/dto/response/
    [ ] T068 Agregar logging para operaciones de consulta de liquidaciones

Phase N: Polish & Cross-Cutting Concerns

Purpose: Improvements that affect multiple user stories

    [ ] T069 Actualizaciones de documentaciÃ³n en docs/
    [ ] T070 Limpieza y refactorizaciÃ³n de cÃ³digo
    [ ] T071 OptimizaciÃ³n de performance en todas las historias de usuario
    [ ] T072 Pruebas unitarias adicionales en tests/unit/
    [ ] T073 Refuerzo de seguridad y validaciÃ³n de datos
    [ ] T074 ConfiguraciÃ³n de monitoreo y mÃ©tricas (Prometheus/Grafana)
    [ ] T075 Pruebas de carga y estrÃ©s
    [ ] T076 DocumentaciÃ³n de API completa con ejemplos
    [ ] T077 GuÃ­a de despliegue y operaciÃ³n

Phase N+1: Correcciones Arquitectura Hexagonal

Purpose: Ajustar la estructura del proyecto para cumplir estrictamente con arquitectura hexagonal

    [ ] T078 Crear repositorios en application/repository/ (puertos outbound - interfaces puras)
    [ ] T079 Mappers en infrastructure/mapper/ (Domain â†” JPA)
    [ ] T080 Crear puertos para servicios externos (ClienteServicePort â†’ ClienteServiceAdapter)
    [ ] T081 Use cases retornan DOMAIN MODELS (no DTOs)
    [ ] T082 Controllers convierten: Domain Model â†’ Response DTO (salida) / Request DTO â†’ Domain Model (entrada)
    [ ] T083 Repository adapters convierten: Domain Model â†” JPA Entity
    [ ] T084 Refactorizar use cases para depender de puertos/interfaces, no de adaptadores concretos

Dependencies & Execution Order
Phase Dependencies

    Setup (Phase 1): No dependencies - can start immediately
    Foundational (Phase 2): Depends on Setup completion - BLOCKS all user stories
    User Stories (Phase 3+): All depend on Foundational phase completion
        User stories can then proceed in parallel (if staffed)
        Or sequentially in priority order (P1 â†’ P2 â†’ P3)
    Polish (Final Phase): Depends on all desired user stories being complete

User Story Dependencies

    User Story 1 (P1): Can start after Foundational (Phase 2) - No dependencies on other stories
    User Story 2 (P2): Can start after Foundational (Phase 2) - Depende de US1 para consultar clientes
    User Story 3 (P3): Can start after Foundational (Phase 2) - No dependencies directas, pero usa entidades de US1
    User Story 4 (P4): Can start after Foundational (Phase 2) - Depende de US3 para datos del pedido
    User Story 5 (P5): Can start after Foundational (Phase 2) - Depende de US4 para datos de liquidaciones

Within Each User Story

    Models before services
    Services before endpoints
    Core implementation before integration
    Story complete before moving to next priority
    Tests after implementation

Prioridades de ImplementaciÃ³n

    P1 (Alta): Consulta de Cliente por ID Nacional - Base para todas las operaciones
    P2 (Alta): Consulta de Forma de Pago - Necesaria para liquidaciones
    P3 (Media): RecepciÃ³n de Datos del Pedido - Base para procesamiento de liquidaciones
    P4 (Alta): RecepciÃ³n de Estado Final y GeneraciÃ³n de Liquidaciones - Funcionalidad central
    P5 (Media): Consulta de Liquidaciones - Para usuarios finales y contadores

Notes

    [Story] label maps task to specific user story for traceability
    Each user story should be independently completable and testable
    Verify tests pass
    Commit after each task or logical group
    Stop at any checkpoint to validate story independently
    Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

Decisiones TÃ©cnicas Clave

    Java 21 LTS: VersiÃ³n estable con mejor performance y features modernas
    Spring Boot 3.x: Framework principal para desarrollo rÃ¡pido y robusto
    PostgreSQL: Base de datos relacional con soporte para transacciones complejas
    Arquitectura Limpia: PatrÃ³n puertos y adaptadores para mantener dominio desacoplado
    Event-Driven: ComunicaciÃ³n asÃ­ncrona con otros mÃ³dulos mediante eventos
    TestContainers: Pruebas de integraciÃ³n con contenedores Docker reales
    CI/CD: IntegraciÃ³n continua para validaciÃ³n automÃ¡tica de cambios

Consideraciones EspecÃ­ficas del Dominio Financiero

    Alta disponibilidad: El sistema debe estar disponible 99.9% del tiempo
    Consistencia transaccional: Todas las operaciones financieras deben ser atÃ³micas
    AuditorÃ­a completa: Logging detallado de todas las operaciones crÃ­ticas
    ValidaciÃ³n estricta: ValidaciÃ³n de datos en mÃºltiples capas (entrada, dominio, persistencia)
    Seguridad: Spring Security para autenticaciÃ³n y autorizaciÃ³n
    Performance: Tiempos de respuesta crÃ­ticos para endpoints de consulta (<500ms)

Especificaciones de Negocio Implementadas

    Matriz de liquidaciÃ³n transportista: Entregado Completo (100%), Rechazo Parcial (80%), DevoluciÃ³n (0%), Faltante de Inventario (-100%)
    Formas de pago: CONTRA_ENTREGA, CARTERA_COMERCIAL
    FÃ³rmula de liquidaciÃ³n cliente: (precio_pedido + tarifa_envÃ­o) Ã— (tasa_efectividad / 100)
    ComunicaciÃ³n con mÃ³dulos externos: Eventos asÃ­ncronos para evitar acoplamiento
