package com.storeinvoice.storeinvoiceapi.application.dto.request;

import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoLiquidacionClienteRequest(
        @NotNull(message = "El nuevo estado de liquidacion es requerido")
        EstadoLiquidacion estadoLiquidacion
) {}
