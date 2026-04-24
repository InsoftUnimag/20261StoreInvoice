# Implementation Plan: Consultar Cliente - Consumo desde Sistema Financiero

**Date:** 23-04-2026  
**Spec:** especificaciones/consultar_cliente.md

## Summary

Permitir al Sistema Financiero consultar clientes del Módulo de Gestión de Clientes mediante endpoints REST síncronos. El sistema debe consumir `GET /api/v1/clientes/nacional/{id_nacional}` y `GET /api/v1/clientes/{id_cliente}` para obtener datos de clientes usando ID Nacional (documento) o ID de base de datos.

## Technical Context

### Language/Version
Java 21 (LTS)

### Primary Dependencies
Spring Boot 3.4.0, Spring WebFlux (WebClient reactivo), Lombok 1.18.36, MapStruct 1.6.3

### Programming Style
Programación reactiva con WebClient para llamadas externas síncronas. Usar Optional para manejar valores nulos. Excepciones de dominio particulares. Validación de datos en entrada. Logging para errores de conexión.

### Arquitectura
Arquitectura limpia (domain, application, infrastructure) con principios SOLID

### Project Structure

```
src/main/
├── java/com/storeinvoice/storeinvoiceapi/
│   ├── domain/
│   │   ├── model/                    # Entidades de dominio
│   │   │   └── Cliente.java
│   │   └── exception/               # Excepciones de dominio
│   │       ├── ClienteNotFoundException.java
│   │       └── InvalidClientIdException.java
│   │
│   ├── application/
│   │   ├── service/                 # CASOS DE USO
│   │   │   └── cliente/
│   │   │       ├── ConsultarClientePorIdNacionalUseCase.java
│   │   │       └── ConsultarClientePorIdUseCase.java
│   │   ├── port/                    # PUERTOS (interfaces)
│   │   │   └── ClienteServicePort.java
│   │   └── dto/
│   │       └── response/
│   │           └── ClienteResponse.java
│   │
│   └── infrastructure/
│       └── adapter/
│           ├── inbound/
│           │   └── rest/
│           │       └── ClienteController.java
│           └── outbound/
│               └── external/
│                   └── ClienteWebClient.java  # Adaptador para llamado a módulo externo
```

## Implementation Phases

### Phase 1: Domain Layer

Purpose: Crear entidades y excepciones de dominio para representar el cliente

    [x] T001 Crear entidad Cliente en domain/model/Cliente.java
          - Campos: idCliente, idNacional, nombre, telefono, direccion
          - Validaciones con Bean Validation
    
    [x] T002 Crear exception ClienteNotFoundException en domain/exception/
    
    [x] T003 Crear exception InvalidClientIdException en domain/exception/

### Phase 2: Application Layer - Ports

Purpose: Definir puertos outbound para consume de servicios externos

    [x] T004 Crear puerto ClienteServicePort en application/port/
          - Metodo:Mono<Cliente> findByIdNacional(String idNacional)
          - Metodo:Optional<Cliente> findById(String idCliente)

### Phase 3: Infrastructure Layer - External Adapter

Purpose: Implementar llamado a endpoints del Módulo de Gestión de Clientes

    [x] T005 Crear/configurar ClienteWebClient en infrastructure/adapter/outbound/external/
          - Endpoint: GET /api/v1/clientes/nacional/{idNacional}
          - Endpoint: GET /api/v1/clientes/{idCliente}
          - Timeout: 500ms
          - Manejo de errores de conexión
    
    [x] T006 Implementar ClienteServiceAdapter que usa ClienteWebClient
          - Implementa ClienteServicePort
          - Mapeo de response externo a modelo de dominio

### Phase 4: Application Layer - Use Cases

Purpose: Crear casos de uso que consumen el puerto

    [x] T007 Crear ConsultarClientePorIdNacionalUseCase
          - Input: idNacional (String)
          - Output: Cliente (domain model)
          - Validacion: idNacional no puede ser vacio
    
    [x] T008 Crear ConsultarClientePorIdUseCase
          - Input: idCliente (String)
          - Output: Cliente (domain model)
          - Validacion: idCliente no puede ser vacio

### Phase 5: Infrastructure Layer - Controllers

Purpose: Exponer endpoints REST para consumo interno

    [x] T009 Crear ClienteController
          - GET /api/v1/clientes/nacional/{idNacional}
          - GET /api/v1/clientes/{idCliente}
    
    [x] T010 Implementar manejo de errores con GlobalExceptionHandler
          - Respuestas: Cliente no encontrado, ID requerido, Servicio no disponible

### Phase 6: Tests

Purpose: Verificar funcionamiento correcto

    [x] T011 Crear test unitario ConsultarClientePorIdNacionalUseCaseTest
    
    [x] T012 Crear test unitario ConsultarClientePorIdUseCaseTest
    
    [ ] T013 Crear test de integracion ClienteControllerTest

### Dependencies

    Phase 1 (Domain): No dependencies
    Phase 2 (Ports): Depends on Domain
    Phase 3 (External Adapter): Depends on Port interface
    Phase 4 (Use Cases): Depends on Ports + Domain
    Phase 5 (Controllers): Depends on Use Cases
    Phase 6 (Tests): Depends on all implementation

## Success Criteria

- **SC-001:** El sistema debe retornar el ID de BD del cliente en menos de 500ms despues de recibir la solicitud
- **SC-002:** El 100% de las consultas con ID Nacional valido de clientes existentes deben retornar el ID de BD correcto
- **SC-003:** El sistema debe retornar un mensaje de error apropiado cuando el cliente no existe
- **SC-004:** La consulta debe funcionar correctamente bajo carga de al menos 100 solicitudes simultaneas

## Edge Cases

- **ID Nacional vacio:** Retornar error "El ID Nacional es requerido"
- **ID de cliente vacio:** Retornar error "ID de cliente invalido"
- **Cliente no encontrado:** Retornar error apropiado segun endpoint
- **Falla de conexion al Módulo de Clientes:** Retornar "Error al consultar el Módulo de Clientes. Intente más tarde"

## Functional Requirements

### Endpoint 1: GET /api/v1/clientes/nacional/{id_nacional}

**Request Parameters:**
- id_nacional (path, requerido): Numero de documento del cliente

**Success Response (200):**
```json
{
  "id_cliente": "100",
  "id_nacional": "12345678",
  "nombre": "Juan Perez",
  "telefono": "3001234567",
  "direccion": "Calle 123 #45-67"
}
```

**Error Responses:**
- 400: "El ID Nacional es requerido" (cuando esta vacio)
- 404: "Cliente no encontrado con el ID Nacional proporcionado"

### Endpoint 2: GET /api/v1/clientes/{id_cliente}

**Request Parameters:**
- id_cliente (path, requerido): ID del cliente en BD

**Success Response (200):**
```json
{
  "id_cliente": "100",
  "id_nacional": "12345678",
  "nombre": "Juan Perez",
  "telefono": "3001234567",
  "direccion": "Calle 123 #45-67"
}
```

**Error Responses:**
- 400: "ID de cliente invalido" (cuando esta vacio)
- 404: "Cliente no encontrado con el ID proporcionado"

## Notes

- Esta feature consume endpoints del Módulo de Gestión de Clientes (sistema externo)
- El WebClient debe configurarse con timeout de 500ms para cumplir SC-001
- Usar programacion reactiva (Mono/Flux) para las llamadas externas
- El ClienteServicePort es el contrato, ClienteWebClient es la implementacion
- Seguir principio Open/Closed: no modificar adaptadores existentes, crear nuevos si hay cambios