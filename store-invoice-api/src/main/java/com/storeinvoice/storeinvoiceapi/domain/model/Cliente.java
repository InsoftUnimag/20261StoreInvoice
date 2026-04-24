package com.storeinvoice.storeinvoiceapi.domain.model;

import jakarta.validation.constraints.NotBlank;

public record Cliente(
    @NotBlank String idCliente,
    @NotBlank String idNacional,
    @NotBlank String nombre,
    @NotBlank String telefono,
    @NotBlank String direccion
) {}