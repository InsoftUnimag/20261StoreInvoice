package com.storeinvoice.storeinvoiceapi.application.dto.response;

public record ClienteResponse(
        String idCliente,
        String idNacional,
        String nombre,
        String telefono,
        String direccion
){}
