package com.storeinvoice.storeinvoiceapi.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LiquidacionClienteResponse(
        Long idLiquidacion,
        Long idPedido,
        Long idCliente,
        String formaPago,
        String estadoLiquidacion,
        LocalDateTime fechaLiquidacion,
        String uriPdf,
        BigDecimal montoLiquidado
) {}
