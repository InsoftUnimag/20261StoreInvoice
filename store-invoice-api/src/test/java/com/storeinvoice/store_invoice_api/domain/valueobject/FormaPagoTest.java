package com.storeinvoice.store_invoice_api.domain.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FormaPagoTest {

    @Test
    void shouldReturnCorrectValue() {
        assertEquals("CONTRA_ENTREGA", FormaPago.CONTRA_ENTREGA.getValue());
        assertEquals("CARTERA_COMERCIAL", FormaPago.CARTERA_COMERCIAL.getValue());
    }

    @Test
    void shouldParseValidFormaPago() {
        assertEquals(FormaPago.CONTRA_ENTREGA, FormaPago.fromValue("CONTRA_ENTREGA"));
        assertEquals(FormaPago.CARTERA_COMERCIAL, FormaPago.fromValue("CARTERA_COMERCIAL"));
        assertEquals(FormaPago.CONTRA_ENTREGA, FormaPago.fromValue("contra_entrega"));
        assertEquals(FormaPago.CARTERA_COMERCIAL, FormaPago.fromValue("cartera_comercial"));
    }

    @Test
    void shouldReturnNullForInvalidFormaPago() {
        assertNull(FormaPago.fromValue("INVALIDO"));
        assertNull(FormaPago.fromValue(""));
        assertNull(FormaPago.fromValue(null));
    }

    @Test
    void isValidShouldWork() {
        assertTrue(FormaPago.CONTRA_ENTREGA.isValid());
        assertTrue(FormaPago.CARTERA_COMERCIAL.isValid());
    }
}