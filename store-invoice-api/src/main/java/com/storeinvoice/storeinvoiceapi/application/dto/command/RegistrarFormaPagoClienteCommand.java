package com.storeinvoice.storeinvoiceapi.application.dto.command;

import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import jakarta.validation.constraints.NotNull;

public record RegistrarFormaPagoClienteCommand(
        @NotNull Long idCliente,
        @NotNull FormaPago formaPago
) {
}
