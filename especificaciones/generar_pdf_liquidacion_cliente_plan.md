Implementation Plan: Generar PDF de Liquidacion del Cliente

Date: 2026-04-24
Spec: especificaciones/generar_pdf_liquidacion_cliente.md

Summary

Implementar una funcion interna que genere un PDF con los datos de la liquidacion de un cliente (productos, total, forma de pago, datos del cliente, id del pedido), lo suba a un sistema de almacenamiento y retorne la URI. La funcion no expone endpoint REST; es invocada por el caso de uso de generacion de liquidacion.

Technical Context

Language/Version: Java 21 (LTS)
Primary Dependencies: Spring Boot 3.4.0, Spring WebFlux, OpenPDF 2.0.3, Lombok 1.18.36, MapStruct 1.6.3
Storage: Port de almacenamiento de archivos con adapter Supabase Storage para desarrollo/produccion y adapter local para tests sin conexion
Programming style: Programacion funcional/reactiva donde aplique, Optional, excepciones de dominio especificas, logging estructurado con SLF4J, validacion Bean Validation
Arquitectura: Arquitectura limpia (hexagonal) - domain, application, infrastructure. Puertos en application, adaptadores en infrastructure
Testing: Test unitarios con Mockito, tests de integracion con TestContainers
Target Platform: Linux server, EC2
Performance Goals: Generacion de PDF < 3 segundos (SC-001)

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
│   │               │   │   └── Producto.java              # Ya existe
│   │               │   └── exception/
│   │               │       ├── DomainException.java       # Ya existe (sealed)
│   │               │       ├── ErrorGeneracionPdfException.java
│   │               │       ├── ErrorSubidaPdfException.java
│   │               │       └── DatosPdfInvalidosException.java
│   │               ├── application/
│   │               │   ├── dto/
│   │               │   │   ├── ProductoPedidoDTO.java
│   │               │   │   └── ClienteLiquidacionDTO.java
│   │               │   ├── port/
│   │               │   │   ├── PdfGeneratorPort.java
│   │               │   │   └── PdfStoragePort.java
│   │               │   └── service/
│   │               │       └── pdf/
│   │               │           └── GenerarPdfLiquidacionClienteUseCase.java
│   │               └── infrastructure/
│   │                   ├── adapter/
│   │                   │   └── outbound/
│   │                   │       ├── pdf/
│   │                   │       │   └── OpenPdfGeneratorAdapter.java
│   │                   │       └── storage/
│   │                   │           ├── LocalFileStorageAdapter.java
│   │                   │           └── SupabaseStorageAdapter.java
│   │                   └── config/
│   │                       └── PdfStorageConfig.java
│   └── resources/
│       └── application.yml                              # Agregar config de storage
│
└── test/
    └── java/
        └── com/
            └── storeinvoice/
                └── storeinvoiceapi/
                    ├── application/
                    │   └── service/
                    │       └── pdf/
                    │           └── GenerarPdfLiquidacionClienteUseCaseTest.java
                    └── infrastructure/
                        └── adapter/
                            └── outbound/
                                ├── pdf/
                                │   └── OpenPdfGeneratorAdapterTest.java
                                └── storage/
                                    └── LocalFileStorageAdapterTest.java

Phase 1: Domain Layer

[T001] Crear excepciones de dominio para errores de PDF
    - DatosPdfInvalidosException: parametros nulos o vacios
    - ErrorGeneracionPdfException: falla al generar el PDF
    - ErrorSubidaPdfException: falla al subir a almacenamiento
    Heredar de DomainException (sealed class). Actualizar DomainException.java para permitir las nuevas clases.

Phase 2: Application Layer

[T002] Crear DTOs de entrada para la funcion
    - ProductoPedidoDTO: idProducto, nombre, cantidad, precioUnitario, subtotal
    - ClienteLiquidacionDTO: idCliente, idNacional, nombre, telefono, direccion

[T003] Definir puertos (interfaces) outbound
    - PdfGeneratorPort: Mono<byte[]> generarPdf(List<ProductoPedidoDTO> productos, BigDecimal totalPedido, String formaPago, ClienteLiquidacionDTO cliente, Long idPedido)
    - PdfStoragePort: Mono<String> subirPdf(byte[] contenido, String nombreArchivo)

[T004] Implementar caso de uso GenerarPdfLiquidacionClienteUseCase
    - Validar parametros de entrada (no nulos, lista de productos no vacia, etc.)
    - Invocar pdfGeneratorPort.generarPdf(...)
    - Invocar pdfStoragePort.subirPdf(...)
    - Retornar Mono<String> con la URI
    - Manejar errores con las excepciones de dominio creadas
    - Logging con log.error() solo para errores tecnicos reales

Phase 3: Infrastructure Layer

[T005] Agregar dependencia OpenPDF en build.gradle
    - implementation 'com.github.librepdf:openpdf:2.0.3'

[T005a] Configurar proyecto Supabase Storage
    - Crear proyecto en https://supabase.com
    - Crear bucket publico: liquidaciones-pdf
    - Obtener Project URL y Anon Key desde Settings > API
    - Configurar variables de entorno: SUPABASE_URL, SUPABASE_API_KEY, SUPABASE_BUCKET
    - El bucket publico permite URL directa sin token adicional

[T006] Implementar OpenPdfGeneratorAdapter
    - Generar PDF con tabla de productos (nombre, cantidad, precio unitario, subtotal)
    - Mostrar total del pedido, forma de pago, datos del cliente, id del pedido
    - Retornar byte[] del PDF generado
    - Manejar ErrorGeneracionPdfException

[T007] Implementar LocalFileStorageAdapter (perfil !prod)
    - Guardar archivo PDF en directorio local configurado
    - Retornar URI tipo file:// o path local
    - Usar para desarrollo y tests

[T008] Implementar SupabaseStorageAdapter (perfil default)
    - Subir archivo a Supabase Storage via API REST
    - Bucket publico para URL directa sin autenticacion adicional
    - Retornar URL publica: https://<ref>.supabase.co/storage/v1/object/public/<bucket>/<nombreArchivo>
    - Usar WebClient (reactive) para consistencia con el proyecto

[T009] Crear configuracion de almacenamiento
    - PdfStorageConfig: seleccionar adapter segun perfil activo
    - Propiedades en application.yml:
        supabase.url, supabase.api-key, supabase.bucket (para Supabase)
        pdf.storage.local.path (para local)

Phase 4: Tests

[T010] Crear tests unitarios para GenerarPdfLiquidacionClienteUseCase
    - Mock de PdfGeneratorPort y PdfStoragePort
    - Test: generacion exitosa retorna URI
    - Test: parametros invalidos lanza DatosPdfInvalidosException
    - Test: error de generacion lanza ErrorGeneracionPdfException
    - Test: error de subida lanza ErrorSubidaPdfException

[T011] Crear tests unitarios para OpenPdfGeneratorAdapter
    - Verificar que genera PDF valido (no vacio, bytes > 0)
    - Verificar que incluye datos del pedido

[T012] Crear tests unitarios para LocalFileStorageAdapter
    - Verificar que guarda archivo correctamente
    - Verificar que retorna URI valida
    - Verificar manejo de errores de escritura

Dependencies & Execution Order

    Phase 1 (Domain) debe completarse antes de Phase 2 (Application)
    Phase 2 debe completarse antes de Phase 3 (Infrastructure)
    Phase 4 (Tests) puede hacerse en paralelo con Phase 3 pero se recomienda despues

Success Criteria Mapping

- SC-001 (PDF < 3 segundos): Medir performance en tests de integracion
- SC-002 (Productos correctos): Verificar en tests unitarios de OpenPdfGeneratorAdapter
- SC-003 (URI accesible): Verificar en tests de LocalFileStorageAdapter y SupabaseStorageAdapter
- SC-004 (Manejo de errores): Cubrir en tests unitarios del use case

Functional Requirements Mapping

- FR-001 (Parametros): Validar en GenerarPdfLiquidacionClienteUseCase
- FR-002 (Generar PDF): OpenPdfGeneratorAdapter
- FR-003 (Subir a nube): PdfStoragePort + adapters
- FR-004 (Retornar URI): Flujo completo del use case
- FR-005 (Validar parametros): Validacion en use case
- FR-006 (Manejar errores subida): ErrorSubidaPdfException

Decisiones Tecnicas

- Libreria PDF: OpenPDF 2.0.3 (open source, compatible con licencias comerciales, API similar a iText)
- Almacenamiento: Port + Adapter pattern. Supabase Storage para dev/prod (URL publica directa), Local para tests sin conexion
- Reactividad: Mono<String> para consistencia con WebFlux del proyecto
- Formato PDF: Tabla de productos + seccion de totales + datos del cliente + id del pedido
