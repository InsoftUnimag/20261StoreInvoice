# Implementation Plan: Generar PDF de Liquidación del Cliente

**Date:** 20-04-2026  
**Spec:** especificaciones/generar_pdf_liquidacion_cliente.md

---

## Summary
El Sistema Financiero genera un PDF con la información de la liquidación del cliente, lo sube a un sistema de almacenamiento en la nube y guarda la URI en el registro de la liquidación. Esta es una función interna sin endpoint REST.

## Technical Context
- **Language/Version:** Java 21 (LTS)
- **Primary Dependencies:** Spring Boot 3.4.0, Spring WebFlux, Lombok 1.18.36, MapStruct 1.6.3, TestContainers, Flyway
- **Arquitectura:** Hexagonal (Ports & Adapters) con capas domain, application, infrastructure
- **Programming Style:** Reactive, funcional, Optional, streams, lambdas, StringBuilder
- **Testing:** JUnit 5 + Mockito + TestContainers, StepVerifier para pruebas reactivas
- **Performance Goal:** Generación de PDF en menos de 3 segundos

---

## Project Structure Adaptations

```
src/
├── main/
│   └── java/
│       └── com/
│           └── storeinvoice/
│               └── store_invoice_api/
│                   ├── domain/
│                   │   └── exception/
│                   │       └── PdfGenerationException.java
│                   ├── application/
│                   │   ├── service/
│                   │   │   └── liquidacion/
│                   │   │       └── GenerarPdfLiquidacionClienteUseCase.java
│                   │   └── dto/
│                   │       └── command/
│                   │           └── GenerarPdfLiquidacionCommand.java
│                   └── infrastructure/
│                       ├── adapter/
│                       │   └── outbound/
│                       │       ├── pdf/
│                       │       │   └── PdfGeneratorAdapter.java
│                       │       └── storage/
│                       │           └── CloudStorageAdapter.java
│                       ├── port/
│                       │   └── outbound/
│                       │       ├── PdfGeneratorPort.java
│                       │       └── CloudStoragePort.java
│                       └── pdf/
│                           └── LiquidacionPdfTemplate.java
```

---

## Phase 1: Implementación de Funcionalidad (Priority: P1)

### Tests
- [ ] T100 Crear test unitario para `GenerarPdfLiquidacionClienteUseCase.java`
- [ ] T101 Crear test unitario para `PdfGeneratorAdapter.java`
- [ ] T102 Crear test unitario para `CloudStorageAdapter.java`
- [ ] T103 Crear test de integración para flujo completo de generación de PDF

### Implementación
- [ ] T104 Crear excepción de dominio `PdfGenerationException.java` en `domain/exception/`
- [ ] T105 Crear DTO `GenerarPdfLiquidacionCommand.java` en `application/dto/command/`
- [ ] T106 Crear puerto `PdfGeneratorPort.java` en `infrastructure/port/outbound/`
- [ ] T107 Crear puerto `CloudStoragePort.java` en `infrastructure/port/outbound/`
- [ ] T108 Implementar adaptador `PdfGeneratorAdapter.java` en `infrastructure/adapter/outbound/pdf/`
- [ ] T109 Implementar adaptador `CloudStorageAdapter.java` en `infrastructure/adapter/outbound/storage/`
- [ ] T110 Crear caso de uso `GenerarPdfLiquidacionClienteUseCase.java` en `application/service/liquidacion/`
- [ ] T111 Implementar validación de parámetros de entrada
- [ ] T112 Implementar lógica de manejo de errores al subir a la nube
- [ ] T113 Agregar logging estructurado siguiendo convenciones del proyecto
- [ ] T114 Integrar la funcionalidad en el caso de uso existente `GenerarLiquidacionesUseCase.java`

---

## Acceptance Scenarios Implementation

✅ **Scenario 1: Generación exitosa del PDF**
  - Validación de todos los parámetros requeridos
  - Generación correcta del contenido PDF
  - Subida exitosa al almacenamiento en la nube
  - Retorno de URI válida del archivo

✅ **Scenario 2: PDF con múltiples productos**
  - Renderizado de tabla con todos los productos
  - Cálculo correcto de subtotales y total
  - Formato adecuado para listados extensos

---

## Edge Cases Implementation

✅ **Error al subir a la nube**
  - Manejo de excepciones de conexión
  - Retorno de mensaje de error apropiado: "Error al subir el PDF a la nube. Intente más tarde"
  - Log de error técnico para diagnóstico

✅ **Parámetros vacíos o inválidos**
  - Validación con Bean Validation
  - Retorno de errores específicos por campo faltante
  - No generación de PDF si faltan datos

✅ **Tamaño máximo de PDF**
  - Validación de tamaño límite antes de subir
  - Manejo de error por exceso de tamaño

---

## Dependencies & Execution Order

### Dependencias Externas
- Depende de la fase Foundational (Phase 2) del plan general completada
- Depende de `GenerarLiquidacionesUseCase.java` existente (T051 del plan general)
- No tiene dependencias con otros user stories

### Orden de Ejecución Interna
1. Excepciones de dominio
2. DTOs y validaciones
3. Puertos (interfaces)
4. Adaptadores de infraestructura
5. Caso de uso
6. Integración con flujo existente
7. Tests

---

## Success Criteria

| Criterio | Estado |
|----------|--------|
| SC-001: PDF generado en < 3 segundos | [ ] Pendiente |
| SC-002: Todos los productos presentes en PDF | [ ] Pendiente |
| SC-003: URI retornada accesible | [ ] Pendiente |
| SC-004: Manejo correcto de errores | [ ] Pendiente |

---

## Funcional Requirements Checklist

| Requisito | Estado |
|-----------|--------|
| FR-001: Recibir todos los parámetros requeridos | [ ] Pendiente |
| FR-002: Generar PDF con contenido de liquidación | [ ] Pendiente |
| FR-003: Subir PDF a almacenamiento en nube | [ ] Pendiente |
| FR-004: Retornar URI del PDF | [ ] Pendiente |
| FR-005: Validar parámetros antes de generar | [ ] Pendiente |
| FR-006: Manejar errores de subida a nube | [ ] Pendiente |
