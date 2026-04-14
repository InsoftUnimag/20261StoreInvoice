# Implementation Plan: Consultar Cliente (Consumo Externo)

**Date:** 13-04-2026  
**Spec:** especificaciones/consultar_cliente.md

---

## Summary

El Sistema Financiero consume endpoints del Módulo de Gestión de Clientes para consultar datos de clientes. **No hay persistencia propia** - solo consumo de API externa.

**Technical Approach:** Implementación de cliente Feign para consumir endpoints del Módulo de Clientes.

---

## Technical Context

**Language/Version:** Java 21 (LTS)  
**Primary Dependencies:** Spring Boot 3.x, Spring Cloud OpenFeign, Spring Data JPA, Spring Security, Lombok 1.18.36  
**Storage:** No requiere BD propia - consume servicio externo  
**Programming Style:** Programación reactiva, funcional, Optional, streams, lambdas, StringBuilder, excepciones particulares del dominio, global exceptions handler, logging, validación de datos, Spring Security, Lombok para entities, Records para DTOs  
**Architecture:** Arquitectura limpia (domain, use cases, infrastructure) con principios SOLID  
**Testing:** Test unitarios con Mockito  

---

## User Stories & Scenarios

### User Story 1 - Consulta de Cliente por ID Nacional (Priority: P1)

**Consumo externo:** `GET /api/v1/clientes/{id_nacional}` del Módulo de Clientes

### User Story 2 - Consulta de Cliente por ID de BD (Priority: P1)

**Consumo externo:** `GET /api/v1/clientes/{id_cliente}` del Módulo de Clientes

---

## Success Criteria

- **SC-001:** El sistema debe retornar el ID de BD del cliente en menos de 500ms después de recibir la solicitud.
- **SC-002:** El 100% de las consultas con ID Nacional válido de clientes existentes deben retornar el ID de BD correcto.
- **SC-003:** El sistema debe retornar un mensaje de error apropiado cuando el cliente no existe.
- **SC-004:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultáneas.

---

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── storeinvoice/
│               └── store_invoice_api/
│                   ├── domain/
│                   │   └── exception/
│                   │       ├── ClienteNotFoundException.java
│                   │       └── InvalidClientIdException.java
│                   │
│                   ├── application/
│                   │   ├── service/
│                   │   │   └── cliente/
│                   │   │       └── ConsultarClientePorIdNacionalUseCase.java
│                   │   └── dto/
│                   │       ├── response/
│                   │       │   └── ClienteResponse.java
│                   │       └── client/
│                   │           └── ClienteClientResponse.java
│                   │
│                   └── infrastructure/
│                       ├── port/
│                       │   └── inbound/
│                       │       └── ClienteInboundPort.java
│                       └── adapter/
│                           └── outbound/
│                               └── external/
│                                   └── ClienteServiceClient.java
│
└── test/
    └── java/
        └── com/
            └── storeinvoice/
                └── store_invoice_api/
                    └── application/
                        └── service/
                            └── cliente/
                                └── ConsultarClientePorIdNacionalUseCaseTest.java
```

---

## Implementation Tasks

### Phase 1: Verificar Componentes Existentes

**Purpose:** Verificar que los componentes requeridos ya existen

- [x] T001 ClienteServiceClient ya existe en `infrastructure/adapter/outbound/external/ClienteServiceClient.java`
- [x] T002 ConsultarClientePorIdNacionalUseCase ya existe en `application/service/cliente/ConsultarClientePorIdNacionalUseCase.java`
- [x] T003 ClienteResponse ya existe en `application/dto/response/ClienteResponse.java`
- [x] T004 ClienteClientResponse ya existe en `application/dto/client/ClienteClientResponse.java`
- [x] T005 ClienteInboundPort ya existe en `infrastructure/port/inbound/ClienteInboundPort.java`
- [x] T006 ClienteNotFoundException ya existe en `domain/exception/ClienteNotFoundException.java`
- [x] T007 ClienteController **ELIMINADO** - No se expone endpoint, solo se consume servicio externo

### Phase 2: Tasks Pendientes

**Purpose:** Completar lo que falta

- [x] T008 InvalidClientIdException ya existe en `domain/exception/InvalidClientIdException.java`
- [x] T009 GlobalExceptionHandler actualizado para manejar InvalidClientIdException y error genérico de conexión
- [x] T010 Test unitario ya existe en `application/service/cliente/ConsultarClientePorIdNacionalUseCaseTest.java`
- [x] T011 Manejo de error de conexión agregado en GlobalExceptionHandler

---

## Estado de Implementación

✅ **COMPLETADO** - Todos los componentes están implementados:
- ClienteServiceClient (consumo externo via Feign)
- ConsultarClientePorIdNacionalUseCase (use case)
- ClienteInboundPort (puerto inbound en infrastructure)
- ClienteResponse / ClienteClientResponse (DTOs)
- ClienteNotFoundException / InvalidClientIdException (excepciones)
- GlobalExceptionHandler (manejo de errores incluyendo conexión)
- Tests unitarios
- Dependencias Feign configuradas en build.gradle
- @EnableFeignClients agregado en StoreInvoiceApiApplication

---

## Notes

- Este módulo **no tiene BD propia** - consume servicio externo via Feign
- Puerto inbound va en `infrastructure/port/inbound/` (no en application)
- Puerto outbound es `ClienteServiceClient` (Feign client)
- Sin Controller - no expone endpoints
- Validar que el ID Nacional no esté vacío antes de realizar la consulta
- Manejar errores de conexión al módulo de forma graceful
