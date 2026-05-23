package com.storeinvoice.storeinvoiceapi.domain.valueobject;

import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidTasaEfectividadException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;


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

    public EstadoLiquidacion mapearAEstadoLiquidacion() {
        return switch (valor) {
            case 100 -> EstadoLiquidacion.ENTREGADO_COMPLETO;
            case 80 -> EstadoLiquidacion.RECHAZO_PARCIAL;
            case 0 -> EstadoLiquidacion.NO_ENTREGADO;
            case -100 -> EstadoLiquidacion.FALTANTE_INVENTARIO;
            default -> throw new InvalidTasaEfectividadException(valor);
        };
    }
}

