package com.storeinvoice.storeinvoiceapi.application.service.liquidacion;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.LiquidacionEntityMapper;
import com.storeinvoice.storeinvoiceapi.infrastructure.port.outbound.LiquidacionRepositoryPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsultarLiquidacionesClienteUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultarLiquidacionesClienteUseCase.class);
    private final LiquidacionRepositoryPort liquidacionRepository;
    private final LiquidacionEntityMapper liquidacionMapper;

    public ConsultarLiquidacionesClienteUseCase(
            final LiquidacionRepositoryPort liquidacionRepository,
            final LiquidacionEntityMapper liquidacionMapper) {
        this.liquidacionRepository = liquidacionRepository;
        this.liquidacionMapper = liquidacionMapper;
    }

    public List<LiquidacionClienteResponse> execute(final Long idCliente, final int pagina, final int tamañoPagina) {
        final int paginaNormalizada = Math.max(0, pagina);
        final var query = new ConsultarLiquidacionesQuery(idCliente, paginaNormalizada, tamañoPagina);
        final List<LiquidacionCliente> liquidaciones = liquidacionRepository.findByIdCliente(query);

        if (liquidaciones.isEmpty()) {
            LOG.warn("No se encontraron liquidaciones para el cliente ID: {}", idCliente);
            throw new LiquidacionNotFoundException("No se encontraron liquidaciones para el cliente ID: " + idCliente);
        }

        return liquidacionMapper.toResponseList(liquidaciones);
    }
}