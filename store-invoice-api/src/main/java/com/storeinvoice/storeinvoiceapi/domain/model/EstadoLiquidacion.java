package com.storeinvoice.storeinvoiceapi.domain.model;

public enum EstadoLiquidacion {
    ENVIADO,
    ENTREGADO_COMPLETO(100),
    RECHAZO_PARCIAL(80),
    NO_ENTREGADO(0),
    DEVOLUCION_ERROR_EMPRESA(0),
    FALTANTE_INVENTARIO(-100);

    private final Integer tasaEfectividad;

    EstadoLiquidacion() {
        this.tasaEfectividad = null;
    }

    EstadoLiquidacion(final Integer tasaEfectividad) {
        this.tasaEfectividad = tasaEfectividad;
    }

    public Integer getTasaEfectividad() {
        return tasaEfectividad;
    }

    public boolean esEstadoFinal() {
        return this != ENVIADO;
    }
}
