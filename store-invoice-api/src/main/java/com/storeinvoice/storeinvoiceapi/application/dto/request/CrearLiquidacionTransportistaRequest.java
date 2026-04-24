package com.storeinvoice.storeinvoiceapi.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CrearLiquidacionTransportistaRequest(
        @NotNull(message = "ID de pedido es requerido")
        Long idPedido,

        @NotNull(message = "ID de transportista es requerido")
        Long idTransportista,

        @NotNull(message = "Monto calculado es requerido")
        @DecimalMin(value = "0.0", inclusive = false, message = "Monto debe ser mayor a cero")
        BigDecimal montoCalculado
) {}
