package com.storeinvoice.storeinvoiceapi.application.dto.request;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import java.math.BigDecimal;
import java.util.List;

public record GenerarPdfSupabaseRequest(
    Long idLiquidacion,
    List<ProductoPedidoDTO> productos,
    BigDecimal totalPedido,
    String formaPago,
    ClienteLiquidacionDTO cliente,
    Long idPedido
) {}
