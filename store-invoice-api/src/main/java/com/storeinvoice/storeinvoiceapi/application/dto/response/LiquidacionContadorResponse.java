package com.storeinvoice.storeinvoiceapi.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LiquidacionContadorResponse(
        Long idLiquidacion,
        Long idPedido,
        String tipoLiquidacion,
        Long idSujeto,
        BigDecimal monto,
        LocalDateTime fechaLiquidacion,
        String uriDocumento
) {}

