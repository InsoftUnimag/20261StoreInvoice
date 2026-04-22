package com.storeinvoice.storeinvoiceapi.infrastructure.port.outbound;

import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import java.util.List;

public interface LiquidacionRepositoryPort {

    List<LiquidacionCliente> findByIdCliente(final ConsultarLiquidacionesQuery query);

    long countByIdCliente(final Long idCliente);

    boolean existsByIdCliente(final Long idCliente);
}