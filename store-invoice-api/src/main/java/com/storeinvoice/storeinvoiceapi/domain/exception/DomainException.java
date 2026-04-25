package com.storeinvoice.storeinvoiceapi.domain.exception;

public sealed class DomainException extends RuntimeException permits LiquidacionNotFoundException, LiquidacionException, ClienteNotFoundException, PedidoNotFoundException, ServiceConnectionException, FormaPagoNotFoundException, FormaPagoAlreadyExistsException, InvalidFormaPagoException, InvalidClientIdException, InvalidTasaEfectividadException {

    protected DomainException(final String message) {
        super(message);
    }

    protected DomainException(final String message, final Throwable cause) {
        super(message, cause);
    }
}