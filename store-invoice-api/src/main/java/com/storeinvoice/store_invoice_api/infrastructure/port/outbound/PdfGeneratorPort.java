package com.storeinvoice.store_invoice_api.infrastructure.port.outbound;

import com.storeinvoice.store_invoice_api.application.dto.command.GenerarPdfLiquidacionCommand;
import reactor.core.publisher.Mono;

public interface PdfGeneratorPort {

    Mono<byte[]> generarPdfLiquidacion(GenerarPdfLiquidacionCommand command);
}
