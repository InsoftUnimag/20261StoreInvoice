package com.storeinvoice.storeinvoiceapi.domain.exception;

/**
 * Excepcion lanzada cuando ocurre un error tecnico al consultar productos
 * al Modulo de Inventario.
 */
public final class ErrorConsultaProductosException extends DomainException {

    public ErrorConsultaProductosException(final Long idPedido, final Throwable cause) {
        super(String.format("Error al consultar los productos del pedido %d", idPedido), cause);
    }

    public ErrorConsultaProductosException(final String mensaje) {
        super(mensaje);
    }
}

