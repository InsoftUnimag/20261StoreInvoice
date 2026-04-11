package com.storeinvoice.domain.exception;

public sealed class DomainException extends RuntimeException permits LiquidacionNotFoundException {

    protected DomainException(final String message) {
        super(message);
    }

    protected DomainException(final String message, final Throwable cause) {
        super(message, cause);
    }
}