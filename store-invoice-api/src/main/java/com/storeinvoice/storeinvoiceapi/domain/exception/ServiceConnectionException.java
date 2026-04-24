package com.storeinvoice.storeinvoiceapi.domain.exception;

/**
 * Excepción lanzada cuando el Sistema Financiero no puede conectarse a un
 * servicio externo (Módulo de Inventario, Módulo de Clientes, etc.).
 * No extiende DomainException porque es un error de infraestructura,
 * no un error de regla de negocio.
 */
public final class ServiceConnectionException extends RuntimeException {

    public ServiceConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
