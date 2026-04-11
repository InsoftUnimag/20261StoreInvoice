package com.storeinvoice.application.service.liquidacion;

import com.storeinvoice.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.domain.model.LiquidacionCliente;
import com.storeinvoice.infrastructure.port.outbound.LiquidacionRepositoryPort;
import java.util.List;

public final class ConsultarLiquidacionesClienteUseCase {

    private final LiquidacionRepositoryPort liquidacionRepository;

    public ConsultarLiquidacionesClienteUseCase(final LiquidacionRepositoryPort liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    public List<LiquidacionClienteResponse> execute(final Long idCliente, final int pagina, final int tamañoPagina) {
        final var query = new ConsultarLiquidacionesQuery(idCliente, pagina, tamañoPagina);
        final List<LiquidacionCliente> liquidaciones = liquidacionRepository.findByIdCliente(query);
        
        return liquidaciones.stream()
                .map(LiquidacionClienteResponse::fromDomain)
                .toList();
    }
}