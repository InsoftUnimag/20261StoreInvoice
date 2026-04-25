package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class DatosPedidoInvalidosException extends DomainException {

    public DatosPedidoInvalidosException(final String message) {
        super(message);
    }

    public DatosPedidoInvalidosException(final String campo, final String motivo) {
        super(String.format("Dato invalido en campo '%s': %s", campo, motivo));
    }
}

