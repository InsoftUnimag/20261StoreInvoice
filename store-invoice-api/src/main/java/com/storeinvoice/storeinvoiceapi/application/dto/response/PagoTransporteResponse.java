package com.storeinvoice.storeinvoiceapi.application.dto.response;

import java.math.BigDecimal;

public record PagoTransporteResponse(
        Long idPedido,
        String formaPago,
        BigDecimal valorContraEntrega
) {}
