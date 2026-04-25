package com.storeinvoice.storeinvoiceapi.domain.valueobject;

import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidTasaEfectividadException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TasaEfectividadTest {

    @Test
    void creacion_exitosa_con_valores_limite() {
        assertDoesNotThrow(() -> new TasaEfectividad(100));
        assertDoesNotThrow(() -> new TasaEfectividad(-100));
        assertDoesNotThrow(() -> new TasaEfectividad(0));
    }

    @Test
    void creacion_exitosa_guarda_valor() {
        TasaEfectividad tasa = new TasaEfectividad(85);
        assertEquals(85, tasa.getValor());
    }

    @Test
    void valor_menor_al_limite_lanza_excepcion() {
        InvalidTasaEfectividadException ex = assertThrows(
                InvalidTasaEfectividadException.class,
                () -> new TasaEfectividad(-101)
        );
        assertEquals("La tasa de efectividad debe estar entre -100 y 100, pero se recibió: -101", ex.getMessage());
    }

    @Test
    void valor_mayor_al_limite_lanza_excepcion() {
        InvalidTasaEfectividadException ex = assertThrows(
                InvalidTasaEfectividadException.class,
                () -> new TasaEfectividad(101)
        );
        assertEquals("La tasa de efectividad debe estar entre -100 y 100, pero se recibió: 101", ex.getMessage());
    }
}
