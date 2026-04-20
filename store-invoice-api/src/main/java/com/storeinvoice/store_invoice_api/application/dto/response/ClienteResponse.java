package com.storeinvoice.store_invoice_api.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClienteResponse(
    @JsonProperty("id_cliente") String idCliente,
    @JsonProperty("id_nacional") String idNacional,
    @JsonProperty("nombre") String nombre,
    @JsonProperty("telefono") String telefono,
    @JsonProperty("direccion") String direccion
) {}