package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClienteExternalResponse(
    @JsonProperty("id_cliente") String idCliente,
    @JsonProperty("id_nacional") String idNacional,
    @JsonProperty("nombre") String nombre,
    @JsonProperty("telefono") String telefono,
    @JsonProperty("direccion") String direccion
) {}
