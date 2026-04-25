package com.storeinvoice.storeinvoiceapi.application.dto.request;

import java.math.BigDecimal;

public record ActualizarMontoLiquidacionTransportistaRequest(
        BigDecimal montoCalculado
) {}
