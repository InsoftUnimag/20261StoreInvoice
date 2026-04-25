package com.storeinvoice.storeinvoiceapi.domain.exception;

/**
 * Excepción de dominio para errores en la generación o procesamiento de liquidaciones.
 * Debe usarse cuando los datos de entrada impiden calcular una liquidación válida,
 * por ejemplo precio de pedido nulo/cero o datos inconsistentes.
 */
public final class LiquidacionException extends DomainException {

    public LiquidacionException(final String message) {
        super(message);
    }

    public LiquidacionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
