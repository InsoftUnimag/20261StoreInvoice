package com.storeinvoice.store_invoice_api.application.service.liquidacion;

import com.storeinvoice.store_invoice_api.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.store_invoice_api.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.domain.model.LiquidacionCliente;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper.LiquidacionEntityMapper;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.LiquidacionRepositoryPort;
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
        LOG.info("Consultando liquidaciones para cliente: {}, pagina: {}, tamaño: {}", idCliente, pagina, tamañoPagina);
        
        final var query = new ConsultarLiquidacionesQuery(idCliente, pagina, tamañoPagina);
        final List<LiquidacionCliente> liquidaciones = liquidacionRepository.findByIdCliente(query);
        
        LOG.info("Se encontraron {} liquidaciones para cliente: {}", liquidaciones.size(), idCliente);
        
        if (liquidaciones.isEmpty()) {
            LOG.warn("No se encontraron liquidaciones para cliente: {}", idCliente);
            throw new ClienteNotFoundException(idCliente);
        }
        
        return liquidacionMapper.toResponseList(liquidaciones);
    }
}