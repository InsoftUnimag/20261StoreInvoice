package com.storeinvoice.storeinvoiceapi.domain.exception;

/**
 * ExcepciÃ³n de dominio para valores de tasa de efectividad fuera del rango permitido (-100 a 100).
 * Se lanza desde el Value Object TasaEfectividad para mantener la invariante de dominio.
 */
public final class InvalidTasaEfectividadException extends DomainException {

    public InvalidTasaEfectividadException(final int valor) {
        super("La tasa de efectividad debe estar entre -100 y 100, pero se recibio: " + valor);
    }
}

