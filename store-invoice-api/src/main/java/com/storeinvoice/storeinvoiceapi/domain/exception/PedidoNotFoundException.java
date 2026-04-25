package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class PedidoNotFoundException extends DomainException {

    public PedidoNotFoundException(final String message) {
        super(message);
    }

    public PedidoNotFoundException(final Long idPedido) {
        super("Pedido no encontrado con el ID proporcionado: " + idPedido);
    }
}
