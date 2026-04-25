package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class ClienteNotFoundException extends DomainException {

    public ClienteNotFoundException(final String message) {
        super(message);
    }

    public ClienteNotFoundException(final Long idCliente) {
        super("Cliente no encontrado con el ID proporcionado: " + idCliente);
    }
}
