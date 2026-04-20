package com.storeinvoice.store_invoice_api.domain.exception;

public class ServiceConnectionException extends RuntimeException {

    public ServiceConnectionException(String message) {
        super(message);
    }

    public ServiceConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}