package com.storeinvoice.store_invoice_api.infrastructure.port.outbound;

import com.storeinvoice.store_invoice_api.domain.model.LiquidacionCliente;
import com.storeinvoice.store_invoice_api.application.dto.query.ConsultarLiquidacionesQuery;
import java.util.List;

public interface LiquidacionRepositoryPort {

    List<LiquidacionCliente> findByIdCliente(final ConsultarLiquidacionesQuery query);

    long countByIdCliente(final Long idCliente);

    boolean existsByIdCliente(final Long idCliente);
}