package com.storeinvoice.storeinvoiceapi.application.dto.command;

import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;

public record RegistrarFormaPagoClienteCommand(
        Long idCliente,
        FormaPago formaPago
) {
}

