package com.storeinvoice.storeinvoiceapi.domain.model;

import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionException;
import com.storeinvoice.storeinvoiceapi.domain.valueobject.TasaEfectividad;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LiquidacionTransportistaTest {

    @Test
    void calcularMonto_exitoso_tasa_positiva() {
        // (1000 * 0.10) * (95 / 100) = 100 * 0.95 = 95
        BigDecimal precioPedido = new BigDecimal("1000.00");
        TasaEfectividad tasa = new TasaEfectividad(95);

        BigDecimal resultado = LiquidacionTransportista.calcularMonto(precioPedido, tasa);

        assertEquals(new BigDecimal("95"), resultado);
    }

    @Test
    void calcularMonto_exitoso_redondeo_half_up() {
        // (105 * 0.10) * (95 / 100) = 10.5 * 0.95 = 9.975 -> 10
        BigDecimal precioPedido = new BigDecimal("105.00");
        TasaEfectividad tasa = new TasaEfectividad(95);

        BigDecimal resultado = LiquidacionTransportista.calcularMonto(precioPedido, tasa);

        assertEquals(new BigDecimal("10"), resultado);
    }

    @Test
    void calcularMonto_exitoso_tasa_cero_retorna_cero() {
        // (1000 * 0.10) * (0 / 100) = 0
        BigDecimal precioPedido = new BigDecimal("1000.00");
        TasaEfectividad tasa = new TasaEfectividad(0);

        BigDecimal resultado = LiquidacionTransportista.calcularMonto(precioPedido, tasa);

        assertEquals(new BigDecimal("0"), resultado);
    }

    @Test
    void calcularMonto_exitoso_tasa_negativa() {
        // (1000 * 0.10) * (-10 / 100) = -10
        BigDecimal precioPedido = new BigDecimal("1000.00");
        TasaEfectividad tasa = new TasaEfectividad(-10);

        BigDecimal resultado = LiquidacionTransportista.calcularMonto(precioPedido, tasa);

        assertEquals(new BigDecimal("-10"), resultado);
    }

    @Test
    void calcularMonto_precio_nulo_lanza_excepcion() {
        TasaEfectividad tasa = new TasaEfectividad(100);

        assertThrows(
                LiquidacionException.class,
                () -> LiquidacionTransportista.calcularMonto(null, tasa)
        );
    }

    @Test
    void calcularMonto_precio_cero_lanza_excepcion() {
        TasaEfectividad tasa = new TasaEfectividad(100);

        assertThrows(
                LiquidacionException.class,
                () -> LiquidacionTransportista.calcularMonto(BigDecimal.ZERO, tasa)
        );
    }
}

