package com.storeinvoice.storeinvoiceapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
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
    private FormaPago formaPago;
    private EstadoLiquidacion estadoLiquidacion;
    private LocalDateTime fechaLiquidacion;
    private String uriPdf;
    private BigDecimal montoLiquidado;

    public void validar() {
        Optional.ofNullable(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("ID de pedido es requerido"));
        
        Optional.ofNullable(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("ID de cliente es requerido"));
        
        Optional.ofNullable(formaPago)
                .orElseThrow(() -> new IllegalArgumentException("Forma de pago es requerida"));
                
        Optional.ofNullable(montoLiquidado)
                .filter(monto -> monto.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new IllegalArgumentException("Monto liquidado es requerido y debe ser mayor a cero"));
                
        Optional.ofNullable(estadoLiquidacion)
                .orElseThrow(() -> new IllegalArgumentException("Estado de liquidacion es requerido"));
                
        Optional.ofNullable(fechaLiquidacion)
                .orElseThrow(() -> new IllegalArgumentException("Fecha de liquidacion es requerida"));
    }
}
