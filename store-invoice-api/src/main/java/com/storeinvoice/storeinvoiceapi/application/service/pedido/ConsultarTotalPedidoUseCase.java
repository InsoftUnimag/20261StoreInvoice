package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener el precio total de un pedido.
 * El precio proviene de la liquidacion del cliente, que almacena los datos
 * recibidos del Modulo de Inventario cuando se proceso el pedido.
 */
@Service
public class ConsultarTotalPedidoUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public ConsultarTotalPedidoUseCase(final LiquidacionRepository liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    /**
     * Obtiene el precio total del pedido desde la liquidacion del cliente.
     *
     * @param idPedido ID del pedido a consultar
     * @return Precio total del pedido
     * @throws PedidoNotFoundException si no existe liquidacion de cliente para el pedido
     */
    public BigDecimal execute(final Long idPedido) {
        return liquidacionRepository.findByIdPedido(idPedido)
                .map(liquidacion -> liquidacion.getMontoLiquidado())
                .filter(monto -> monto.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new PedidoNotFoundException(idPedido));
    }
}
