package com.storeinvoice.storeinvoiceapi.application.dto.response;

public record TieneFormaPagoResponse(
        Long idCliente,
        boolean tieneFormaPago
) {
}

