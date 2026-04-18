package com.storeinvoice.store_invoice_api.application.dto.response;

import java.time.LocalDateTime;

public record FormaPagoResponse(
    Long idCliente,
    String formaPago,
    LocalDateTime fechaRegistro
) {}