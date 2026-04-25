package com.storeinvoice.storeinvoiceapi.domain.exception;

/**
 * Excepcion lanzada cuando ocurre un error tecnico al consultar datos del cliente
 * al Modulo de Gestion de Clientes.
 */
public final class ErrorConsultaClienteException extends DomainException {

    public ErrorConsultaClienteException(final Long idCliente, final Throwable cause) {
        super(String.format("Error al consultar los datos del cliente %d", idCliente), cause);
    }
}

