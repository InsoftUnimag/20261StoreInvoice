package com.storeinvoice.storeinvoiceapi.application.repository;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LiquidacionRepository {

    List<LiquidacionCliente> findByIdCliente(ConsultarLiquidacionesQuery query);

    long countByIdCliente(Long idCliente);

    boolean existsByIdCliente(Long idCliente);

    Optional<BigDecimal> findMontoLiquidadoByIdPedido(Long idPedido);

    Optional<LiquidacionCliente> findByIdPedido(Long idPedido);
}