package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarLiquidacionesClienteUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public ConsultarLiquidacionesClienteUseCase(final LiquidacionRepository liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    public List<LiquidacionCliente> execute(final Long idCliente, final int pagina, final int tamanoPagina) {
        final var query = new ConsultarLiquidacionesQuery(idCliente, Math.max(0, pagina), tamanoPagina);
        return liquidacionRepository.findByIdCliente(query);
    }
}
