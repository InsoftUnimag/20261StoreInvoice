package com.storeinvoice.storeinvoiceapi.application.service.pdf;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.application.port.PdfGeneratorPort;
import com.storeinvoice.storeinvoiceapi.application.port.PdfStoragePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPdfInvalidosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorGeneracionPdfException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorSubidaPdfException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerarPdfLiquidacionClienteUseCaseTest {

    @Mock
    private PdfGeneratorPort pdfGeneratorPort;

    @Mock
    private PdfStoragePort pdfStoragePort;

    @InjectMocks
    private GenerarPdfLiquidacionClienteUseCase useCase;

    private List<ProductoPedidoDTO> productos;
    private BigDecimal totalPedido;
    private String formaPago;
    private ClienteLiquidacionDTO cliente;
    private Long idPedido;

    @BeforeEach
    void setUp() {
        productos = List.of(
                new ProductoPedidoDTO("1", "Producto A", 2, new BigDecimal("100.00"), new BigDecimal("200.00")));
        totalPedido = new BigDecimal("200.00");
        formaPago = "CONTRA_ENTREGA";
        cliente = new ClienteLiquidacionDTO(1L, "12345678", "Juan Perez", "3001234567", "Calle 123");
        idPedido = 100L;
    }

    @Test
    void ejecutar_generacionExitosa_retornaUri() {
        final byte[] pdfBytes = new byte[]{1, 2, 3};
        final String uriEsperada = "file:///tmp/store-invoice/pdfs/liquidacion-pedido-100-20240101120000.pdf";

        when(pdfGeneratorPort.generarPdf(any(), any(), anyString(), any(), any())).thenReturn(Mono.just(pdfBytes));
        when(pdfStoragePort.subirPdf(any(), anyString())).thenReturn(Mono.just(uriEsperada));

        StepVerifier.create(useCase.ejecutar(productos, totalPedido, formaPago, cliente, idPedido))
                .expectNextMatches(uri -> uri.startsWith("file:///tmp/store-invoice/pdfs/liquidacion-pedido-100-"))
                .verifyComplete();

        verify(pdfGeneratorPort).generarPdf(productos, totalPedido, formaPago, cliente, idPedido);
        verify(pdfStoragePort).subirPdf(any(), anyString());
    }

    @Test
    void ejecutar_parametrosInvalidos_lanzaDatosPdfInvalidosException() {
        StepVerifier.create(useCase.ejecutar(null, totalPedido, formaPago, cliente, idPedido))
                .expectError(DatosPdfInvalidosException.class)
                .verify();
    }

    @Test
    void ejecutar_listaProductosVacia_lanzaDatosPdfInvalidosException() {
        StepVerifier.create(useCase.ejecutar(List.of(), totalPedido, formaPago, cliente, idPedido))
                .expectError(DatosPdfInvalidosException.class)
                .verify();
    }

    @Test
    void ejecutar_errorGeneracion_lanzaErrorGeneracionPdfException() {
        when(pdfGeneratorPort.generarPdf(any(), any(), anyString(), any(), any()))
                .thenReturn(Mono.error(new ErrorGeneracionPdfException("Error al generar")));

        StepVerifier.create(useCase.ejecutar(productos, totalPedido, formaPago, cliente, idPedido))
                .expectError(ErrorGeneracionPdfException.class)
                .verify();
    }

    @Test
    void ejecutar_errorSubida_lanzaErrorSubidaPdfException() {
        final byte[] pdfBytes = new byte[]{1, 2, 3};

        when(pdfGeneratorPort.generarPdf(any(), any(), anyString(), any(), any())).thenReturn(Mono.just(pdfBytes));
        when(pdfStoragePort.subirPdf(any(), anyString()))
                .thenReturn(Mono.error(new ErrorSubidaPdfException("Error al subir")));

        StepVerifier.create(useCase.ejecutar(productos, totalPedido, formaPago, cliente, idPedido))
                .expectError(ErrorSubidaPdfException.class)
                .verify();
    }

    @Test
    void ejecutar_formaPagoVacia_lanzaDatosPdfInvalidosException() {
        StepVerifier.create(useCase.ejecutar(productos, totalPedido, "", cliente, idPedido))
                .expectError(DatosPdfInvalidosException.class)
                .verify();
    }

    @Test
    void ejecutar_totalPedidoNulo_lanzaDatosPdfInvalidosException() {
        StepVerifier.create(useCase.ejecutar(productos, null, formaPago, cliente, idPedido))
                .expectError(DatosPdfInvalidosException.class)
                .verify();
    }

    @Test
    void ejecutar_clienteNulo_lanzaDatosPdfInvalidosException() {
        StepVerifier.create(useCase.ejecutar(productos, totalPedido, formaPago, null, idPedido))
                .expectError(DatosPdfInvalidosException.class)
                .verify();
    }

    @Test
    void ejecutar_idPedidoNulo_lanzaDatosPdfInvalidosException() {
        StepVerifier.create(useCase.ejecutar(productos, totalPedido, formaPago, cliente, null))
                .expectError(DatosPdfInvalidosException.class)
                .verify();
    }
}

