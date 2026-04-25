package com.storeinvoice.storeinvoiceapi.application.port;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import java.math.BigDecimal;
import java.util.List;
import reactor.core.publisher.Mono;

public interface PdfGeneratorPort {

    Mono<byte[]> generarPdf(List<ProductoPedidoDTO> productos, BigDecimal totalPedido, String formaPago,
            ClienteLiquidacionDTO cliente, Long idPedido);
}

