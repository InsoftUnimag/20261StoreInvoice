package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class InvalidFormaPagoException extends DomainException {

    public InvalidFormaPagoException(final String message) {
        super(message);
    }

    public InvalidFormaPagoException(final String formaPago, final String valoresValidos) {
        super("Forma de pago invalida: " + formaPago + ". Valores validos: " + valoresValidos);
    }
}

