package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class FormaPagoNotFoundException extends DomainException {

    public FormaPagoNotFoundException(final String message) {
        super(message);
    }

    public FormaPagoNotFoundException(final Long idCliente) {
        super("El cliente no tiene forma de pago registrada: " + idCliente);
    }
}

