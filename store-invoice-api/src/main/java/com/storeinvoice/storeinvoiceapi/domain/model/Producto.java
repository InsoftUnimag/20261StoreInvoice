package com.storeinvoice.storeinvoiceapi.domain.model;

/**
 * Entidad de dominio pura que representa un producto dentro de un pedido.
 * No contiene anotaciones de infraestructura (JPA, JSON, etc.).
 */
public record Producto(
                String idProducto,
                String nombre,
                Integer cantidad,
                double precioUnitario,
                double subtotal) {
}
