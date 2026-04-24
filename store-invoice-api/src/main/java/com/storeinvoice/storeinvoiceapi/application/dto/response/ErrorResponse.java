package com.storeinvoice.storeinvoiceapi.application.dto.response;

public record ErrorResponse(
        int status,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String message, String path) {
        return new ErrorResponse(status, message, path);
    }
}