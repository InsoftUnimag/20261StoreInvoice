package com.storeinvoice.storeinvoiceapi.domain.model;

import java.math.BigDecimal;

public record Producto(
                String idProducto,
                String nombre,
                Integer cantidad,
                BigDecimal precioUnitario,
                BigDecimal subtotal) {

    public Producto {
        if (idProducto == null || idProducto.isBlank()) {
            throw new IllegalArgumentException("ID de producto es requerido");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre de producto es requerido");
        }
        if (cantidad == null) {
            throw new IllegalArgumentException("Cantidad es requerida");
        }
        if (cantidad < 1) {
            throw new IllegalArgumentException("Cantidad debe ser al menos 1");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio unitario no puede ser negativo");
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal no puede ser negativo");
        }
    }
}