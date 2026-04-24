package com.storeinvoice.storeinvoiceapi.application.dto.request;

import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CrearLiquidacionClienteRequest(
        @NotNull(message = "ID de pedido es requerido")
        Long idPedido,

        @NotNull(message = "ID de cliente es requerido")
        Long idCliente,

        @NotNull(message = "Forma de pago es requerida")
        FormaPago formaPago,

        @NotNull(message = "Monto liquidado es requerido")
        @DecimalMin(value = "0.0", inclusive = false, message = "Monto debe ser mayor a cero")
        BigDecimal montoLiquidado
) {}
