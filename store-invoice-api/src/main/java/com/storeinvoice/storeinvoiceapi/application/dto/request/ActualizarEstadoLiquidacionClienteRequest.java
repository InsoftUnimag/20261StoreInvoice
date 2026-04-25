package com.storeinvoice.storeinvoiceapi.application.dto.request;

import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;

public record ActualizarEstadoLiquidacionClienteRequest(
        EstadoLiquidacion estadoLiquidacion
) {}

