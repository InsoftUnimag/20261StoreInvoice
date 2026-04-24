package com.storeinvoice.storeinvoiceapi.domain.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Entidad de dominio pura que representa un producto dentro de un pedido.
 * No contiene anotaciones de infraestructura (JPA, JSON, etc.).
 */
public record Producto(
                @NotBlank(message = "ID de producto es requerido") String idProducto,
                @NotBlank(message = "Nombre de producto es requerido") String nombre,
                @NotNull(message = "Cantidad es requerida") @Min(value = 1, message = "Cantidad debe ser al menos 1") Integer cantidad,
                @PositiveOrZero(message = "Precio unitario no puede ser negativo") double precioUnitario,
                @PositiveOrZero(message = "Subtotal no puede ser negativo") double subtotal) {
}
