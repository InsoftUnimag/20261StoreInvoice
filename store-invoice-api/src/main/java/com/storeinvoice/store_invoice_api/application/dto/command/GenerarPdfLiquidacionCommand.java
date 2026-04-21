package com.storeinvoice.store_invoice_api.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record GenerarPdfLiquidacionCommand(

    @NotNull(message = "La lista de productos es requerida")
    List<ProductoPedidoDTO> productos,

    @NotNull(message = "El total del pedido es requerido")
    @Positive(message = "El total del pedido debe ser mayor que cero")
    Integer totalPedido,

    @NotBlank(message = "La forma de pago es requerida")
    String formaPago,

    @NotNull(message = "Los datos del cliente son requeridos")
    ClienteDTO cliente,

    @NotNull(message = "El ID del pedido es requerido")
    @Positive(message = "El ID del pedido debe ser mayor que cero")
    Integer idPedido
) {

    public record ProductoPedidoDTO(
        Integer idProducto,
        String nombre,
        Integer cantidad,
        Integer precioUnitario,
        Integer subtotal
    ) {}

    public record ClienteDTO(
        Integer idCliente,
        String idNacional,
        String nombre,
        String apellido,
        String direccion,
        String telefono
    ) {}
}
