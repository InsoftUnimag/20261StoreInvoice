package com.storeinvoice.storeinvoiceapi.application.port;

import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (Outbound Port) para el Módulo de Gestión de Inventario.
 * Define el contrato que debe cumplir cualquier adaptador que quiera proveer
 * información de productos. La capa de aplicación depende de esta interfaz,
 * no de ninguna implementación concreta.
 */
public interface InventarioServicePort {

    /**
     * Consulta la lista de productos asociados a un pedido.
     *
     * @param idPedido ID del pedido cuyas productos se desea consultar.
     * @return Mono con la lista de productos del pedido.
     */
    Mono<List<Producto>> consultarProductosPorPedido(String idPedido);
}
