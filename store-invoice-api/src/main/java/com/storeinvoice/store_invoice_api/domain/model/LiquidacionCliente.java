package com.storeinvoice.store_invoice_api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionCliente {

    private Long idLiquidacion;
    private Long idPedido;
    private Long idCliente;
    private String formaPago;
    private String estadoLiquidacion;
    private LocalDateTime fechaLiquidacion;
    private String uriPdf;
    private BigDecimal montoLiquidado;
}