package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class EstadoFinalInvalidoException extends DomainException {

    public EstadoFinalInvalidoException(final String message) {
        super(message);
    }

    public EstadoFinalInvalidoException(final String campo, final String motivo) {
        super(String.format("Dato invalido en campo '%s': %s", campo, motivo));
    }
}
