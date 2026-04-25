package com.storeinvoice.storeinvoiceapi.application.dto.messaging;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Mensaje recibido desde el Modulo de Inventario via RabbitMQ
 * cuando se crea un nuevo pedido.
 */
public record DatosPedidoInventarioMessage(
        @NotNull(message = "ID de pedido es requerido")
        @Min(value = 1, message = "ID de pedido debe ser mayor a cero")
        Long idPedido,

        @NotNull(message = "ID de cliente es requerido")
        @Min(value = 1, message = "ID de cliente debe ser mayor a cero")
        Long idCliente,

        @NotNull(message = "Total del pedido es requerido")
        @Min(value = 0, message = "Total del pedido no puede ser negativo")
        Long totalPedido,

        String direccion
) {
}
