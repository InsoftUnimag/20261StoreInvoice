package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class ErrorGeneracionPdfException extends DomainException {

    public ErrorGeneracionPdfException(final String message) {
        super(message);
    }

    public ErrorGeneracionPdfException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

