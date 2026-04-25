package com.storeinvoice.storeinvoiceapi.application.dto.messaging;

/**
 * Mensaje recibido desde el Modulo de Inventario via RabbitMQ
 * cuando se crea un nuevo pedido.
 */
public record DatosPedidoInventarioMessage(
        Long idPedido,
        Long idCliente,
        Long totalPedido,
        String direccion
) {
}

