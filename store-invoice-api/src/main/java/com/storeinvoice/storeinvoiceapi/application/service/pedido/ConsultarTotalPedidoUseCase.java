package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.repository.PedidoRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener el precio total de un pedido.
 * El precio proviene de la tabla 'pedidos', que almacena los datos
 * recibidos del MÃ³dulo de Inventario cuando se creÃ³ el pedido.
 */
@Service
public class ConsultarTotalPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public ConsultarTotalPedidoUseCase(final PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Obtiene el precio total del pedido desde la BD.
     *
     * @param idPedido ID del pedido a consultar
     * @return Precio total del pedido
     * @throws PedidoNotFoundException si el pedido no existe en el sistema
     */
    public BigDecimal execute(final Long idPedido) {
        return pedidoRepository.findPrecioPedidoByIdPedido(idPedido)
                .orElseThrow(() -> new PedidoNotFoundException(idPedido));
    }
}
