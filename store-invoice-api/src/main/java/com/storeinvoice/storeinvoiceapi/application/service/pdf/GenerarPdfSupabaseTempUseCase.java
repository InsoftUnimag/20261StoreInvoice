package com.storeinvoice.storeinvoiceapi.application.service.pdf;

import com.storeinvoice.storeinvoiceapi.application.dto.request.GenerarPdfSupabaseRequest;
import com.storeinvoice.storeinvoiceapi.application.port.PdfGeneratorPort;
import com.storeinvoice.storeinvoiceapi.application.port.PdfStoragePort;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class GenerarPdfSupabaseTempUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(GenerarPdfSupabaseTempUseCase.class);

    private final LiquidacionRepository liquidacionRepository;
    private final PdfGeneratorPort pdfGeneratorPort;
    private final PdfStoragePort pdfStoragePort;
    private final TransactionTemplate transactionTemplate;

    public GenerarPdfSupabaseTempUseCase(
            final LiquidacionRepository liquidacionRepository,
            final PdfGeneratorPort pdfGeneratorPort,
            final PdfStoragePort pdfStoragePort,
            final TransactionTemplate transactionTemplate) {
        this.liquidacionRepository = liquidacionRepository;
        this.pdfGeneratorPort = pdfGeneratorPort;
        this.pdfStoragePort = pdfStoragePort;
        this.transactionTemplate = transactionTemplate;
    }

    public Mono<String> ejecutar(final GenerarPdfSupabaseRequest request) {
        return Mono.fromCallable(() -> liquidacionRepository.findClienteById(request.idLiquidacion())
                .orElseThrow(() -> new LiquidacionNotFoundException(
                        "Liquidacion no encontrada con ID: " + request.idLiquidacion())))
            .flatMap(liquidacion -> {
                LOG.info("Generando PDF real para liquidacion ID: {}, pedido ID: {}",
                        request.idLiquidacion(), request.idPedido());

                return pdfGeneratorPort.generarPdf(
                        request.productos(),
                        request.totalPedido(),
                        request.formaPago(),
                        request.cliente(),
                        request.idPedido()
                ).flatMap(pdfBytes -> {
                    final String nombreArchivo = construirNombreArchivo(request.idPedido());
                    LOG.info("Subiendo PDF a Supabase: {}", nombreArchivo);

                    return pdfStoragePort.subirPdf(pdfBytes, nombreArchivo)
                            .flatMap(uri -> {
                                liquidacion.setUriPdf(uri);
                                LOG.info("PDF subido exitosamente. URI: {}", uri);

                                return Mono.fromCallable(() ->
                                        transactionTemplate.execute(status -> {
                                            liquidacionRepository.saveCliente(liquidacion);
                                            return uri;
                                        })
                                ).subscribeOn(Schedulers.boundedElastic());
                            });
                });
            });
    }

    private String construirNombreArchivo(final Long idPedido) {
        final String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "liquidacion-pedido-" + idPedido + "-" + fecha + ".pdf";
    }
}
