package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record ProductoExternalDTO(
        @JsonProperty("id") String idProducto,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("cantidad") Integer cantidad,
        @JsonProperty("precioUnitario") BigDecimal precioUnitario,
        @JsonProperty("subtotal") BigDecimal subtotal
) {}

