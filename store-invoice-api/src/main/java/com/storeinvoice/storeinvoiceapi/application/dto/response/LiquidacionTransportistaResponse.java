package com.storeinvoice.storeinvoiceapi.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LiquidacionTransportistaResponse(
        Long idLiquidacion,
        Long idPedido,
        Long idTransportista,
        BigDecimal montoCalculado,
        LocalDateTime fechaLiquidacion
) {}

