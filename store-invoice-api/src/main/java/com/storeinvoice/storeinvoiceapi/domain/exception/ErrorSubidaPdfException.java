package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class ErrorSubidaPdfException extends DomainException {

    public ErrorSubidaPdfException(final String message) {
        super(message);
    }

    public ErrorSubidaPdfException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

