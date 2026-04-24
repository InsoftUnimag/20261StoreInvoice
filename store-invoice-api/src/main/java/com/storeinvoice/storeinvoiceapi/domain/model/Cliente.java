package com.storeinvoice.storeinvoiceapi.domain.model;

import jakarta.validation.constraints.NotBlank;

public record Cliente(
    @NotBlank String idCliente,
    @NotBlank String idNacional,
    String nombre,
    String telefono,
    String direccion
) {}