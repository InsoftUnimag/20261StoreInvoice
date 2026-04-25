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

/**
 * Modelo de dominio que representa la liquidación a pagar a un transportista.
 * Contiene la lógica de negocio para calcular el monto según la tasa de efectividad.
 *
 * <p>Fórmula: Monto = (Precio Pedido × 10%) × (tasa_efectividad / 100)
 * El resultado se redondea al entero más cercano (HALF_UP).
 */
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
     * @return Monto calculado redondeado al entero más cercano
     * @throws LiquidacionException si el precio del pedido es nulo o cero
     */
    public static BigDecimal calcularMonto(final BigDecimal precioPedido, final TasaEfectividad tasaEfectividad) {
        if (precioPedido == null || precioPedido.compareTo(BigDecimal.ZERO) == 0) {
            throw new LiquidacionException("El precio del pedido no puede ser nulo o cero para calcular la liquidación del transportista");
        }

        // Fórmula: (Precio Pedido × 10%) × (tasa_efectividad / 100)
        final BigDecimal tarifaBase = precioPedido.multiply(new BigDecimal("0.10"));
        final BigDecimal multiplicadorEfectividad = new BigDecimal(tasaEfectividad.getValor()).divide(new BigDecimal("100"));
        final BigDecimal monto = tarifaBase.multiply(multiplicadorEfectividad);

        // Redondeo al número entero más cercano según spec
        return monto.setScale(0, RoundingMode.HALF_UP);
    }
}
