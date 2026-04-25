package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class FormaPagoAlreadyExistsException extends DomainException {

    public FormaPagoAlreadyExistsException(final String message) {
        super(message);
    }

    public FormaPagoAlreadyExistsException(final Long idCliente) {
        super("El cliente ya tiene forma de pago registrada: " + idCliente);
    }
}

