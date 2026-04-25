package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.pdf;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorGeneracionPdfException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfGeneratorAdapterTest {

    private final OpenPdfGeneratorAdapter adapter = new OpenPdfGeneratorAdapter();

    @Test
    void generarPdf_conDatosValidos_retornaBytesMayorACero() {
        final List<ProductoPedidoDTO> productos = List.of(
                new ProductoPedidoDTO(1L, "Producto A", 2, new BigDecimal("100.00"), new BigDecimal("200.00")),
                new ProductoPedidoDTO(2L, "Producto B", 1, new BigDecimal("50.00"), new BigDecimal("50.00")));
        final BigDecimal totalPedido = new BigDecimal("250.00");
        final String formaPago = "CONTRA_ENTREGA";
        final ClienteLiquidacionDTO cliente = new ClienteLiquidacionDTO(1L, "12345678", "Juan Perez", "3001234567",
                "Calle 123");
        final Long idPedido = 100L;

        StepVerifier.create(adapter.generarPdf(productos, totalPedido, formaPago, cliente, idPedido))
                .assertNext(bytes -> {
                    assertTrue(bytes.length > 0, "El PDF debe tener contenido");
                    assertTrue(bytes.length > 100, "El PDF debe tener un tamano razonable");
                })
                .verifyComplete();
    }

    @Test
    void generarPdf_conUnProducto_retornaBytesValidos() {
        final List<ProductoPedidoDTO> productos = List.of(
                new ProductoPedidoDTO(1L, "Producto Unico", 5, new BigDecimal("10.00"), new BigDecimal("50.00")));

        StepVerifier.create(
                adapter.generarPdf(productos, new BigDecimal("50.00"), "CARTERA_COMERCIAL",
                        new ClienteLiquidacionDTO(2L, "87654321", "Maria Lopez", "3009876543", "Avenida 456"), 200L))
                .assertNext(bytes -> assertTrue(bytes.length > 0))
                .verifyComplete();
    }

    @Test
    void generarPdf_conListaVacia_retornaBytesValidos() {
        final List<ProductoPedidoDTO> productos = List.of();

        StepVerifier.create(
                adapter.generarPdf(productos, new BigDecimal("0.00"), "CONTRA_ENTREGA",
                        new ClienteLiquidacionDTO(3L, "11111111", "Carlos Ruiz", "3001111111", "Carrera 789"), 300L))
                .assertNext(bytes -> assertTrue(bytes.length > 0))
                .verifyComplete();
    }
}

