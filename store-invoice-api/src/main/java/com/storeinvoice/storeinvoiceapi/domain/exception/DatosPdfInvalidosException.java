package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class DatosPdfInvalidosException extends DomainException {

    public DatosPdfInvalidosException(final String message) {
        super(message);
    }

    public DatosPdfInvalidosException(final String campo, final String motivo) {
        super(String.format("Dato invalido en campo '%s': %s", campo, motivo));
    }
}

