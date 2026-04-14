package com.storeinvoice.store_invoice_api.domain.exception;

public class InvalidClientIdException extends RuntimeException {
    
    public InvalidClientIdException(String message) {
        super(message);
    }
    
    public InvalidClientIdException(String message, Throwable cause) {
        super(message, cause);
    }
}