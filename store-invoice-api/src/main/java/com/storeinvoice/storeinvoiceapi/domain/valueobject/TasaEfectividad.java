package com.storeinvoice.storeinvoiceapi.domain.valueobject;

import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidTasaEfectividadException;

/**
 * Value Object que representa la tasa de efectividad de entrega de un pedido.
 * El rango válido es -100 a 100 (valores negativos representan devoluciones/multas).
 * Es inmutable y garantiza su invariante en construcción.
 */
public class TasaEfectividad {

    private final int valor;

    public TasaEfectividad(final int valor) {
        if (valor < -100 || valor > 100) {
            throw new InvalidTasaEfectividadException(valor);
        }
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}
