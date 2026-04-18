package com.storeinvoice.store_invoice_api.domain.valueobject;

public enum FormaPago {
    CONTRA_ENTREGA("CONTRA_ENTREGA"),
    CARTERA_COMERCIAL("CARTERA_COMERCIAL");

    private final String value;

    FormaPago(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FormaPago fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (FormaPago formaPago : FormaPago.values()) {
            if (formaPago.value.equalsIgnoreCase(value)) {
                return formaPago;
            }
        }
        return null;
    }

    public boolean isValid() {
        return this != null;
    }
}