package com.storeinvoice.storeinvoiceapi.application.dto;

public record ClienteLiquidacionDTO(
        Long idCliente,
        String idNacional,
        String nombre,
        String telefono,
        String direccion) {
}

