package com.storeinvoice.storeinvoiceapi.application.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClienteClientResponse(
    @JsonProperty("id_cliente") String idCliente,
    @JsonProperty("id_nacional") String idNacional,
    @JsonProperty("nombre") String nombre,
    @JsonProperty("telefono") String telefono,
    @JsonProperty("direccion") String direccion
) {}