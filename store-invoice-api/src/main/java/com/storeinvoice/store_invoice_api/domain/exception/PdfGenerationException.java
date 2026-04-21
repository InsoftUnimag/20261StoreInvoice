package com.storeinvoice.store_invoice_api.domain.exception;

public final class PdfGenerationException extends DomainException {

    public PdfGenerationException(final String message) {
        super(message);
    }

    public PdfGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
