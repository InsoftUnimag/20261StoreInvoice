package com.storeinvoice.storeinvoiceapi.domain.exception;

public final class LiquidacionNotFoundException extends DomainException {

    public LiquidacionNotFoundException(final String message) {
        super(message);
    }

    public LiquidacionNotFoundException(final Long idLiquidacion) {
        super("Liquidacion no encontrada con el ID proporcionado: " + idLiquidacion);
    }
}
