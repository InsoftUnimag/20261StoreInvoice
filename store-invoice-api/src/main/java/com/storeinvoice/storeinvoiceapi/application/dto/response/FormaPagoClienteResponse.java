package com.storeinvoice.storeinvoiceapi.application.dto.response;

import java.time.LocalDateTime;

public record FormaPagoClienteResponse(
        Long idCliente,
        String formaPago,
        LocalDateTime fechaRegistro
) {
}
