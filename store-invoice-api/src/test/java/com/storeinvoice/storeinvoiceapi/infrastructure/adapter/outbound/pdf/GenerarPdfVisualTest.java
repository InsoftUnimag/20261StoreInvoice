package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.pdf;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/**
 * Test de verificacion visual del PDF generado.
 * Genera un PDF con datos de ejemplo y lo guarda en /tmp para revision manual.
 */
class GenerarPdfVisualTest {

    private final OpenPdfGeneratorAdapter adapter = new OpenPdfGeneratorAdapter();

    @Test
    void generarPdfConMultiplesProductos_yGuardarEnDisco() throws Exception {
        // Given: Datos de ejemplo para un pedido realista
        final List<ProductoPedidoDTO> productos = List.of(
                new ProductoPedidoDTO(1L, "Laptop Dell XPS 13", 1, new BigDecimal("1250.00"), new BigDecimal("1250.00")),
                new ProductoPedidoDTO(2L, "Mouse Logitech MX Master", 2, new BigDecimal("89.99"), new BigDecimal("179.98")),
                new ProductoPedidoDTO(3L, "Teclado Mecanico Keychron", 1, new BigDecimal("120.00"), new BigDecimal("120.00")),
                new ProductoPedidoDTO(4L, "Monitor LG 27 pulgadas", 2, new BigDecimal("350.00"), new BigDecimal("700.00"))
        );

        final BigDecimal totalPedido = new BigDecimal("2249.98");
        final String formaPago = "CARTERA_COMERCIAL";
        final ClienteLiquidacionDTO cliente = new ClienteLiquidacionDTO(
                42L,
                "1234567890",
                "Carlos Rodriguez",
                "3105558844",
                "Carrera 45 # 26-85, Bogota"
        );
        final Long idPedido = 9876L;

        // When: Generar el PDF
        StepVerifier.create(adapter.generarPdf(productos, totalPedido, formaPago, cliente, idPedido))
                .assertNext(bytes -> {
                    try {
                        // Guardar en disco para revision visual
                        final Path directorio = Paths.get("/tmp/store-invoice/pdfs");
                        if (!Files.exists(directorio)) {
                            Files.createDirectories(directorio);
                        }
                        final Path rutaPdf = directorio.resolve("liquidacion-test-visual.pdf");

                        try (final FileOutputStream fos = new FileOutputStream(rutaPdf.toFile())) {
                            fos.write(bytes);
                        }

                        // Verificaciones
                        final long tamanoBytes = Files.size(rutaPdf);
                        System.out.println("=".repeat(60));
                        System.out.println("PDF GENERADO EXITOSAMENTE");
                        System.out.println("=".repeat(60));
                        System.out.println("Ruta: " + rutaPdf.toAbsolutePath());
                        System.out.println("Tamano: " + tamanoBytes + " bytes (" + (tamanoBytes / 1024) + " KB)");
                        System.out.println("Productos en PDF: " + productos.size());
                        System.out.println("Total pedido: $" + totalPedido);
                        System.out.println("Cliente: " + cliente.nombre());
                        System.out.println("=".repeat(60));

                        org.junit.jupiter.api.Assertions.assertTrue(tamanoBytes > 1000,
                                "El PDF debe tener al menos 1KB de contenido");

                    } catch (final Exception e) {
                        throw new RuntimeException("Error al guardar PDF de prueba", e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void generarPdfConUnSoloProducto() {
        final List<ProductoPedidoDTO> productos = List.of(
                new ProductoPedidoDTO(1L, "Producto Unico", 1, new BigDecimal("500.00"), new BigDecimal("500.00"))
        );

        final ClienteLiquidacionDTO cliente = new ClienteLiquidacionDTO(
                1L, "999888777", "Maria Gomez", "3001112233", "Avenida Siempre Viva 123"
        );

        StepVerifier.create(adapter.generarPdf(productos, new BigDecimal("500.00"), "CONTRA_ENTREGA", cliente, 100L))
                .assertNext(bytes -> {
                    org.junit.jupiter.api.Assertions.assertTrue(bytes.length > 500,
                            "PDF con un producto debe tener al menos 500 bytes");
                    System.out.println("PDF con 1 producto generado: " + bytes.length + " bytes");
                })
                .verifyComplete();
    }
}
