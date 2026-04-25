package com.storeinvoice.storeinvoiceapi.application.dto.response;

import java.math.BigDecimal;

public record TotalPedidoResponse(
        Long idPedido,
        BigDecimal totalPedido
) {}
