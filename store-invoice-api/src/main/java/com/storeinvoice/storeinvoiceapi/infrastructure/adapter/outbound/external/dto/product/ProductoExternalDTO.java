package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO externo que representa un producto individual en la respuesta del
 * MÃ³dulo de GestiÃ³n de Inventario. Este DTO sÃ³lo vive en la capa de
 * infraestructura y nunca debe cruzar hacia la capa de dominio.
 */
public record ProductoExternalDTO(
        @JsonProperty("id_producto") String idProducto,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("cantidad") Integer cantidad,
        @JsonProperty("precio_unitario") Integer precioUnitario,
        @JsonProperty("subtotal") Integer subtotal
) {}

