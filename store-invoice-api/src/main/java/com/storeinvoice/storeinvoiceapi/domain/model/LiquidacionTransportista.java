package com.storeinvoice.storeinvoiceapi.domain.model;

import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionException;
import com.storeinvoice.storeinvoiceapi.domain.valueobject.TasaEfectividad;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionTransportista {

    private Long idLiquidacion;
    private Long idPedido;
    private Long idTransportista;
    private BigDecimal montoCalculado;
    private LocalDateTime fechaLiquidacion;

    /**
     * Calcula el monto a pagar al transportista.
     *
     * @param precioPedido    Precio total del pedido (no puede ser nulo ni cero)
     * @param tasaEfectividad Tasa de efectividad ya validada por el Value Object
     * @return Monto calculado redondeado al entero mas cercano
     * @throws LiquidacionException si el precio del pedido es nulo o cero
     */
    public static BigDecimal calcularMonto(final BigDecimal precioPedido, final TasaEfectividad tasaEfectividad) {
        if (precioPedido == null || precioPedido.compareTo(BigDecimal.ZERO) == 0) {
            throw new LiquidacionException("El precio del pedido no puede ser nulo o cero para calcular la liquidacion del transportista");
        }

        final BigDecimal tarifaBase = precioPedido.multiply(new BigDecimal("0.10"));
        final BigDecimal multiplicadorEfectividad = new BigDecimal(tasaEfectividad.getValor()).divide(new BigDecimal("100"));
        final BigDecimal monto = tarifaBase.multiply(multiplicadorEfectividad);

        return monto.setScale(0, RoundingMode.HALF_UP);
    }
}

