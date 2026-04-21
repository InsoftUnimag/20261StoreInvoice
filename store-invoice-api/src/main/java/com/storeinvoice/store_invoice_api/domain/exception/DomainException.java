package com.storeinvoice.store_invoice_api.domain.exception;

public sealed class DomainException extends RuntimeException 
    permits LiquidacionNotFoundException, ClienteNotFoundException, PdfGenerationException {

    protected DomainException(final String message) {
        super(message);
    }

    protected DomainException(final String message, final Throwable cause) {
        super(message, cause);
    }
}