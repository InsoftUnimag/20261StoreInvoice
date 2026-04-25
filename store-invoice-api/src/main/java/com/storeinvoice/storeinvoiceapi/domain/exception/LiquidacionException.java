package com.storeinvoice.storeinvoiceapi.domain.exception;

/**
 * ExcepciÃ³n de dominio para errores en la generaciÃ³n o procesamiento de liquidaciones.
 * Debe usarse cuando los datos de entrada impiden calcular una liquidaciÃ³n vÃ¡lida,
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

