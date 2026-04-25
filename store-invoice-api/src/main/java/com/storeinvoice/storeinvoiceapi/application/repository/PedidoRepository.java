package com.storeinvoice.storeinvoiceapi.application.repository;

import com.storeinvoice.storeinvoiceapi.domain.model.Pedido;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Puerto de salida (Outbound Port) para persistencia de Pedido.
 * Define el contrato puro de repositorio sin dependencias de tecnologÃ­a.
 * La capa de aplicaciÃ³n depende de esta interfaz, nunca de la implementaciÃ³n concreta.
 */
public interface PedidoRepository {

    /**
     * Guarda o actualiza un pedido en la BD.
     *
     * @param pedido El pedido a persistir
     * @return El pedido persistido con su estado actualizado
     */
    Pedido save(Pedido pedido);

    /**
     * Obtiene el precio total de un pedido por su ID.
     * Este es el precio real del pedido recibido del MÃ³dulo de Inventario,
     * utilizado como base para calcular la liquidaciÃ³n del transportista.
     *
     * @param idPedido ID del pedido
     * @return Optional con el precio total, vacÃ­o si el pedido no existe
     */
    Optional<BigDecimal> findPrecioPedidoByIdPedido(Long idPedido);

    /**
     * Busca un pedido completo por su ID.
     *
     * @param idPedido ID del pedido
     * @return Optional con el pedido, vacÃ­o si no existe
     */
    Optional<Pedido> findById(Long idPedido);
}

