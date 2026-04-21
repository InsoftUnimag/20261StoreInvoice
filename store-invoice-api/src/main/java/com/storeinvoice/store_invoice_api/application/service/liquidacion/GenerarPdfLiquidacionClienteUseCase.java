package com.storeinvoice.store_invoice_api.application.service.liquidacion;

import com.storeinvoice.store_invoice_api.application.dto.command.GenerarPdfLiquidacionCommand;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.CloudStoragePort;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.PdfGeneratorPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class GenerarPdfLiquidacionClienteUseCase {

    private final PdfGeneratorPort pdfGeneratorPort;
    private final CloudStoragePort cloudStoragePort;

    public Mono<String> ejecutar(@Valid final GenerarPdfLiquidacionCommand command) {
        log.debug("Iniciando generación de PDF para pedido ID: {}", command.idPedido());

        return pdfGeneratorPort.generarPdfLiquidacion(command)
            .flatMap(contenidoPdf ->
                cloudStoragePort.subirArchivoPdf(contenidoPdf, "liquidacion_" + command.idPedido() + ".pdf")
            )
            .doOnSuccess(uri ->
                log.info("PDF de liquidación generado exitosamente para pedido ID: {}. URI: {}",
                    command.idPedido(), uri)
            )
            .doOnError(e ->
                log.error("Error en flujo de generación de PDF para pedido ID: {}", command.idPedido(), e)
            );
    }
}
