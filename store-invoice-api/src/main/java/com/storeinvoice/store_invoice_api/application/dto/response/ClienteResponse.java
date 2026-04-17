package com.storeinvoice.store_invoice_api.application.dto.response;

public record ClienteResponse(
    String idCliente,
    String idNacional,
    String nombre,
    String telefono,
    String direccion
) {}