package com.storeinvoice.storeinvoiceapi.application.dto;

import java.math.BigDecimal;

public record ProductoPedidoDTO(
        Long idProducto,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}

