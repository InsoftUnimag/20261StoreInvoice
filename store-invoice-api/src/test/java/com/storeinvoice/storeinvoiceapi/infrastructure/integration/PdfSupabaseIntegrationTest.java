package com.storeinvoice.storeinvoiceapi.infrastructure.integration;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.application.service.pdf.GenerarPdfLiquidacionClienteUseCase;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

/**
 * Test de integracion end-to-end: genera un PDF real con datos de ejemplo
 * y lo sube a Supabase Storage usando Service Role Key.
 *
 * Requiere variables de entorno cargadas desde .env:
 *   export $(cat .env | xargs)
 *
 * Perfil: default (no test) para activar SupabaseStorageAdapter.
 */
@SpringBootTest
@ActiveProfiles("default")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.cloud.stream.bindings.recibirPedido-in-0.destination=pedidos.test"
})
class PdfSupabaseIntegrationTest {

    @Autowired
    private GenerarPdfLiquidacionClienteUseCase useCase;

    @Test
    void generarYSubirPdfASupabase_exitoso() {
        // Given: Pedido realista
        final List<ProductoPedidoDTO> productos = List.of(
                new ProductoPedidoDTO(1L, "Laptop Dell XPS 13", 1, new BigDecimal("1250.00"), new BigDecimal("1250.00")),
                new ProductoPedidoDTO(2L, "Mouse Logitech MX Master", 2, new BigDecimal("89.99"), new BigDecimal("179.98")),
                new ProductoPedidoDTO(3L, "Teclado Mecanico Keychron K2", 1, new BigDecimal("120.00"), new BigDecimal("120.00"))
        );

        final BigDecimal totalPedido = new BigDecimal("1549.98");
        final String formaPago = "CONTRA_ENTREGA";
        final ClienteLiquidacionDTO cliente = new ClienteLiquidacionDTO(
                42L, "1234567890", "Carlos Rodriguez", "3105558844", "Carrera 45 # 26-85, Bogota"
        );
        final Long idPedido = 9999L;

        // When + Then
        StepVerifier.create(useCase.ejecutar(productos, totalPedido, formaPago, cliente, idPedido))
                .assertNext(uri -> {
                    System.out.println("=".repeat(70));
                    System.out.println("PDF SUBIDO EXITOSAMENTE A SUPABASE STORAGE");
                    System.out.println("=".repeat(70));
                    System.out.println("URI: " + uri);
                    System.out.println("=".repeat(70));

                    // Verificar que la URI tiene el formato de Supabase public URL
                    org.junit.jupiter.api.Assertions.assertTrue(
                            uri.contains("supabase.co/storage/v1/object/public/liquidaciones-pdf/"),
                            "La URI debe ser una URL publica de Supabase Storage"
                    );

                    // Guardar URI en archivo para referencia
                    try {
                        final Path directorio = Paths.get("/tmp/store-invoice/pdfs");
                        if (!Files.exists(directorio)) {
                            Files.createDirectories(directorio);
                        }
                        Files.writeString(
                                directorio.resolve("ultima-uri-supabase.txt"),
                                uri + System.lineSeparator()
                        );
                    } catch (final Exception e) {
                        // No fallar el test si no se puede escribir el archivo de referencia
                        System.err.println("No se pudo guardar URI en disco: " + e.getMessage());
                    }
                })
                .verifyComplete();
    }
}

