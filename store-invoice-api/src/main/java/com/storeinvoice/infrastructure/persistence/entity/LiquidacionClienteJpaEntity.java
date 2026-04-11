package com.storeinvoice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidaciones_cliente")
public final class LiquidacionClienteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_liquidacion")
    private Long idLiquidacion;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "forma_pago", nullable = false, length = 50)
    private String formaPago;

    @Column(name = "estado_liquidacion", nullable = false, length = 50)
    private String estadoLiquidacion;

    @Column(name = "fecha_liquidacion", nullable = false)
    private LocalDateTime fechaLiquidacion;

    @Column(name = "uri_pdf", length = 500)
    private String uriPdf;

    @Column(name = "monto_liquidado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoLiquidado;

    public LiquidacionClienteJpaEntity() {
    }

    public Long getIdLiquidacion() {
        return idLiquidacion;
    }

    public void setIdLiquidacion(final Long idLiquidacion) {
        this.idLiquidacion = idLiquidacion;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(final Long idPedido) {
        this.idPedido = idPedido;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(final Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(final String formaPago) {
        this.formaPago = formaPago;
    }

    public String getEstadoLiquidacion() {
        return estadoLiquidacion;
    }

    public void setEstadoLiquidacion(final String estadoLiquidacion) {
        this.estadoLiquidacion = estadoLiquidacion;
    }

    public LocalDateTime getFechaLiquidacion() {
        return fechaLiquidacion;
    }

    public void setFechaLiquidacion(final LocalDateTime fechaLiquidacion) {
        this.fechaLiquidacion = fechaLiquidacion;
    }

    public String getUriPdf() {
        return uriPdf;
    }

    public void setUriPdf(final String uriPdf) {
        this.uriPdf = uriPdf;
    }

    public BigDecimal getMontoLiquidado() {
        return montoLiquidado;
    }

    public void setMontoLiquidado(final BigDecimal montoLiquidado) {
        this.montoLiquidado = montoLiquidado;
    }
}