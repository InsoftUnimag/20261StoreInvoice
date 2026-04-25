package com.storeinvoice.storeinvoiceapi.domain.exception;

/**
 * Excepcion lanzada cuando el Modulo de Inventario retorna una lista vacia de productos.
 */
public final class ProductosNoEncontradosException extends DomainException {

    public ProductosNoEncontradosException(final Long idPedido) {
        super(String.format("No se encontraron productos para el pedido %d", idPedido));
    }
}
