package com.storeinvoice.storeinvoiceapi.application.repository;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LiquidacionRepository {

    List<LiquidacionCliente> findByIdCliente(ConsultarLiquidacionesQuery query);

    long countByIdCliente(Long idCliente);

    boolean existsByIdCliente(Long idCliente);

    Optional<BigDecimal> findMontoLiquidadoByIdPedido(Long idPedido);

    List<LiquidacionTransportista> findByIdTransportista(Long idTransportista, int pagina, int tamanoPagina);

    LiquidacionCliente saveCliente(LiquidacionCliente liquidacion);

    Optional<LiquidacionCliente> findClienteById(Long idLiquidacion);

    LiquidacionTransportista saveTransportista(LiquidacionTransportista liquidacion);

    Optional<LiquidacionTransportista> findTransportistaById(Long idLiquidacion);
    Optional<LiquidacionCliente> findByIdPedido(Long idPedido);
}