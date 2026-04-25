package com.storeinvoice.storeinvoiceapi.domain.model;

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
public class LiquidacionContable {

    private Long idLiquidacion;
    private Long idPedido;
    private String tipoLiquidacion;
    private Long idSujeto;
    private BigDecimal monto;
    private LocalDateTime fechaLiquidacion;
    private String uriDocumento;

    public void validar() {
        if (idPedido == null) {
            throw new IllegalArgumentException("ID de pedido es requerido");
        }
        if (tipoLiquidacion == null || tipoLiquidacion.isBlank()) {
            throw new IllegalArgumentException("Tipo de liquidacion es requerido");
        }
        if (idSujeto == null) {
            throw new IllegalArgumentException("ID de sujeto es requerido");
        }
        if (monto == null) {
            throw new IllegalArgumentException("Monto es requerido");
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monto debe ser mayor a cero");
        }
        if (fechaLiquidacion == null) {
            throw new IllegalArgumentException("Fecha de liquidacion es requerida");
        }
    }
}

