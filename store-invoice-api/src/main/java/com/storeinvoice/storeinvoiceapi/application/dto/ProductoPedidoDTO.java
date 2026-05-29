package com.storeinvoice.storeinvoiceapi.application.dto;

import java.math.BigDecimal;

public record ProductoPedidoDTO(
        String id,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}

