package com.storeinvoice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class LiquidacionCliente {

    private final Long idLiquidacion;
    private final Long idPedido;
    private final Long idCliente;
    private final String formaPago;
    private final String estadoLiquidacion;
    private final LocalDateTime fechaLiquidacion;
    private final String uriPdf;
    private final BigDecimal montoLiquidado;

    public LiquidacionCliente(
            final Long idLiquidacion,
            final Long idPedido,
            final Long idCliente,
            final String formaPago,
            final String estadoLiquidacion,
            final LocalDateTime fechaLiquidacion,
            final String uriPdf,
            final BigDecimal montoLiquidado) {
        this.idLiquidacion = idLiquidacion;
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.formaPago = formaPago;
        this.estadoLiquidacion = estadoLiquidacion;
        this.fechaLiquidacion = fechaLiquidacion;
        this.uriPdf = uriPdf;
        this.montoLiquidado = montoLiquidado;
    }

    public Long getIdLiquidacion() {
        return idLiquidacion;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public String getEstadoLiquidacion() {
        return estadoLiquidacion;
    }

    public LocalDateTime getFechaLiquidacion() {
        return fechaLiquidacion;
    }

    public String getUriPdf() {
        return uriPdf;
    }

    public BigDecimal getMontoLiquidado() {
        return montoLiquidado;
    }
}