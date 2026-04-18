package com.storeinvoice.store_invoice_api.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarFormaPagoCommand(
    @NotNull(message = "ID de cliente es requerido")
    Long idCliente,

    @NotBlank(message = "Forma de pago es requerida")
    @Size(max = 50, message = "Forma de pago demasiado larga")
    String formaPago
) {}