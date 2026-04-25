package com.storeinvoice.storeinvoiceapi.domain.valueobject;

import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidTasaEfectividadException;


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

