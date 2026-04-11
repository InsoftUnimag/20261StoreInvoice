package com.storeinvoice.infrastructure.port.outbound;

import com.storeinvoice.domain.model.LiquidacionCliente;
import com.storeinvoice.application.dto.query.ConsultarLiquidacionesQuery;
import java.util.List;

public interface LiquidacionRepositoryPort {

    List<LiquidacionCliente> findByIdCliente(final ConsultarLiquidacionesQuery query);

    long countByIdCliente(final Long idCliente);
}