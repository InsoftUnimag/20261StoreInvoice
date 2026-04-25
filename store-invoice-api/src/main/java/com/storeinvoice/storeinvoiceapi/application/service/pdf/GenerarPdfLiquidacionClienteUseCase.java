package com.storeinvoice.storeinvoiceapi.application.service.pdf;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.application.port.PdfGeneratorPort;
import com.storeinvoice.storeinvoiceapi.application.port.PdfStoragePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPdfInvalidosException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GenerarPdfLiquidacionClienteUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(GenerarPdfLiquidacionClienteUseCase.class);

    private final PdfGeneratorPort pdfGeneratorPort;
    private final PdfStoragePort pdfStoragePort;


    public Mono<String> ejecutar(final List<ProductoPedidoDTO> productos, final BigDecimal totalPedido,
            final String formaPago, final ClienteLiquidacionDTO cliente, final Long idPedido) {
        return validarParametros(productos, totalPedido, formaPago, cliente, idPedido)
                .flatMap(params -> pdfGeneratorPort.generarPdf(params.productos(), params.totalPedido(),
                        params.formaPago(), params.cliente(), params.idPedido()))
                .flatMap(bytes -> {
                    final String nombreArchivo = generarNombreArchivo(idPedido);
                    return pdfStoragePort.subirPdf(bytes, nombreArchivo);
                })
                .doOnSuccess(uri -> LOG.info("PDF de liquidacion generado exitosamente para pedido {}: {}", idPedido,
                        uri));
    }

    private Mono<ParametrosPdf> validarParametros(final List<ProductoPedidoDTO> productos,
            final BigDecimal totalPedido, final String formaPago, final ClienteLiquidacionDTO cliente,
            final Long idPedido) {
        return Mono.justOrEmpty(productos)
                .filter(lista -> !lista.isEmpty())
                .switchIfEmpty(Mono.error(new DatosPdfInvalidosException("productos",
                        "La lista de productos no puede ser nula ni vacia")))
                .flatMap(lista -> Mono.justOrEmpty(totalPedido)
                        .filter(t -> t.compareTo(BigDecimal.ZERO) >= 0)
                        .switchIfEmpty(
                                Mono.error(new DatosPdfInvalidosException("totalPedido", "El total no puede ser nulo")))
                        .flatMap(t -> Mono.justOrEmpty(formaPago)
                                .filter(fp -> !fp.isBlank())
                                .switchIfEmpty(Mono.error(
                                        new DatosPdfInvalidosException("formaPago", "La forma de pago es requerida")))
                                .flatMap(fp -> Mono.justOrEmpty(cliente)
                                        .switchIfEmpty(Mono.error(new DatosPdfInvalidosException("cliente",
                                                "Los datos del cliente son requeridos")))
                                        .flatMap(c -> Mono.justOrEmpty(idPedido)
                                                .switchIfEmpty(Mono.error(new DatosPdfInvalidosException("idPedido",
                                                        "El ID del pedido es requerido")))
                                                .map(i -> new ParametrosPdf(lista, t, fp, c, i))))));
    }

    private String generarNombreArchivo(final Long idPedido) {
        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("liquidacion-pedido-%d-%s.pdf", idPedido, timestamp);
    }

    private record ParametrosPdf(
            List<ProductoPedidoDTO> productos,
            BigDecimal totalPedido,
            String formaPago,
            ClienteLiquidacionDTO cliente,
            Long idPedido) {
    }
}

