package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class FormaPagoClienteNoEncontradaException extends DomainException {

    public FormaPagoClienteNoEncontradaException(final Long idCliente) {
        super("Forma de pago no encontrada para el cliente con ID: " + idCliente);
    }
}

