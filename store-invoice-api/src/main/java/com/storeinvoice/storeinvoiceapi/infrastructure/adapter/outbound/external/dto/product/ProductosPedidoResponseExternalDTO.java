package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO envoltorio que representa la respuesta completa del endpoint
 * GET /api/v1/pedidos/{id_pedido}/productos del MÃ³dulo de Inventario.
 * Encapsula la lista de productos bajo la clave "productos" del JSON.
 *
 * Ejemplo de respuesta:
 * {
 *   "productos": [
 *     { "id_producto": "501", "nombre": "Gaseosa 1L", ... }
 *   ]
 * }
 */
public record ProductosPedidoResponseExternalDTO(
        @JsonProperty("productos") List<ProductoExternalDTO> productos
) {}

