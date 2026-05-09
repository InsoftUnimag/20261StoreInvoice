package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.pdf;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.application.port.PdfGeneratorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("!prod")
public class PdfGeneratorMockAdapter implements PdfGeneratorPort {

    private static final Logger LOG = LoggerFactory.getLogger(PdfGeneratorMockAdapter.class);

    @Override
    public Mono<byte[]> generarPdf(final List<ProductoPedidoDTO> productos, final BigDecimal totalPedido,
            final String formaPago, final ClienteLiquidacionDTO cliente, final Long idPedido) {
        
        LOG.warn("MOCK: Generando PDF simulado para pedido ID: {}, cliente: {}", idPedido, cliente.nombre());
        
        // Retorna bytes de un PDF minimo/valido
        final String pdfContent = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R>>endobj\nxref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \ntrailer<</Size 4/Root 1 0 R>>startxref\n190\n%%EOF";
        
        return Mono.just(pdfContent.getBytes());
    }
}
