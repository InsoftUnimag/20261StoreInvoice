package com.storeinvoice.storeinvoiceapi.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ActualizarMontoLiquidacionTransportistaRequest(
        @NotNull(message = "El nuevo monto calculado es requerido")
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a cero")
        BigDecimal montoCalculado
) {}
