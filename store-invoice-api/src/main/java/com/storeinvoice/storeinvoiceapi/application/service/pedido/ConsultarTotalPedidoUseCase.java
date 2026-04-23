package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ConsultarTotalPedidoUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public ConsultarTotalPedidoUseCase(final LiquidacionRepository liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    public BigDecimal execute(final Long idPedido) {
        return liquidacionRepository.findMontoLiquidadoByIdPedido(idPedido)
                .orElseThrow(() -> new PedidoNotFoundException(idPedido));
    }
}