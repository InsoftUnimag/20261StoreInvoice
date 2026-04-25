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
â”œâ”€â”€ main/
â”‚   â”œâ”€â”€ java/
â”‚   â”‚   â””â”€â”€ com/
â”‚   â”‚       â””â”€â”€ storeinvoice/
â”‚   â”‚           â””â”€â”€ storeinvoiceapi/
â”‚   â”‚               â”œâ”€â”€ domain/
â”‚   â”‚               â”‚   â”œâ”€â”€ model/
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ Producto.java              # Ya existe
â”‚   â”‚               â”‚   â””â”€â”€ exception/
â”‚   â”‚               â”‚       â”œâ”€â”€ DomainException.java       # Ya existe (sealed)
â”‚   â”‚               â”‚       â”œâ”€â”€ ErrorGeneracionPdfException.java
â”‚   â”‚               â”‚       â”œâ”€â”€ ErrorSubidaPdfException.java
â”‚   â”‚               â”‚       â””â”€â”€ DatosPdfInvalidosException.java
â”‚   â”‚               â”œâ”€â”€ application/
â”‚   â”‚               â”‚   â”œâ”€â”€ dto/
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ ProductoPedidoDTO.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ ClienteLiquidacionDTO.java
â”‚   â”‚               â”‚   â”œâ”€â”€ port/
â”‚   â”‚               â”‚   â”‚   â”œâ”€â”€ PdfGeneratorPort.java
â”‚   â”‚               â”‚   â”‚   â””â”€â”€ PdfStoragePort.java
â”‚   â”‚               â”‚   â””â”€â”€ service/
â”‚   â”‚               â”‚       â””â”€â”€ pdf/
â”‚   â”‚               â”‚           â””â”€â”€ GenerarPdfLiquidacionClienteUseCase.java
â”‚   â”‚               â””â”€â”€ infrastructure/
â”‚   â”‚                   â”œâ”€â”€ adapter/
â”‚   â”‚                   â”‚   â””â”€â”€ outbound/
â”‚   â”‚                   â”‚       â”œâ”€â”€ pdf/
â”‚   â”‚                   â”‚       â”‚   â””â”€â”€ OpenPdfGeneratorAdapter.java
â”‚   â”‚                   â”‚       â””â”€â”€ storage/
â”‚   â”‚                   â”‚           â”œâ”€â”€ LocalFileStorageAdapter.java
â”‚   â”‚                   â”‚           â””â”€â”€ SupabaseStorageAdapter.java
â”‚   â”‚                   â””â”€â”€ config/
â”‚   â”‚                       â””â”€â”€ PdfStorageConfig.java
â”‚   â””â”€â”€ resources/
â”‚       â””â”€â”€ application.yml                              # Agregar config de storage
â”‚
â””â”€â”€ test/
    â””â”€â”€ java/
        â””â”€â”€ com/
            â””â”€â”€ storeinvoice/
                â””â”€â”€ storeinvoiceapi/
                    â”œâ”€â”€ application/
                    â”‚   â””â”€â”€ service/
                    â”‚       â””â”€â”€ pdf/
                    â”‚           â””â”€â”€ GenerarPdfLiquidacionClienteUseCaseTest.java
                    â””â”€â”€ infrastructure/
                        â””â”€â”€ adapter/
                            â””â”€â”€ outbound/
                                â”œâ”€â”€ pdf/
                                â”‚   â””â”€â”€ OpenPdfGeneratorAdapterTest.java
                                â””â”€â”€ storage/
                                    â””â”€â”€ LocalFileStorageAdapterTest.java

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

[~T009] Crear configuracion de almacenamiento - NO APLICA
    - Originalmente se planeo crear PdfStorageConfig para seleccionar adapter segun perfil
    - Implementacion actual usa @Profile directamente en los adapters:
        * LocalFileStorageAdapter: @Profile({"local", "test"})
        * SupabaseStorageAdapter: @Profile("!local & !test")
    - Spring Boot selecciona automaticamente el bean correcto segun el perfil activo
    - No se requiere clase de configuracion adicional

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

