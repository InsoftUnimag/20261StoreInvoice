package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class InvalidClientIdException extends DomainException {
    
    public InvalidClientIdException(String message) {
        super(message);
    }
    
    public InvalidClientIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
