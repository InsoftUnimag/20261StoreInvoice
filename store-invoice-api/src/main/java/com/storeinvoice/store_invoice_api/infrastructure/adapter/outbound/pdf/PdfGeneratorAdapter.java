package com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.pdf;

import com.storeinvoice.store_invoice_api.application.dto.command.GenerarPdfLiquidacionCommand;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.PdfGeneratorPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
public class PdfGeneratorAdapter implements PdfGeneratorPort {

    private static final int MARGIN = 50;
    private static final int PAGE_WIDTH = 612;
    private static final int LINE_HEIGHT = 15;

    @Override
    public Mono<byte[]> generarPdfLiquidacion(final GenerarPdfLiquidacionCommand command) {
        return Mono.fromCallable(() -> {
            try (final PDDocument document = new PDDocument();
                 final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                final PDPage page = new PDPage();
                document.addPage(page);

                try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                    // Título
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                    contentStream.newLineAtOffset(MARGIN, 750);
                    contentStream.showText("Liquidación de Cliente");
                    contentStream.endText();

                    // Datos del cliente
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(MARGIN, 700);
                    contentStream.showText("Datos del Cliente:");
                    contentStream.endText();

                    int yPosition = 680;
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("ID Cliente: " + command.cliente().idCliente());
                    contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                    contentStream.showText("Nombre: " + command.cliente().nombre() + " " + command.cliente().apellido());
                    contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                    contentStream.showText("Documento: " + command.cliente().idNacional());
                    contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                    contentStream.showText("Dirección: " + command.cliente().direccion());
                    contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                    contentStream.showText("Teléfono: " + command.cliente().telefono());
                    contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                    contentStream.showText("Forma de Pago: " + command.formaPago());
                    contentStream.endText();

                    // Tabla de productos
                    yPosition -= 80;
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Detalle de Productos:");
                    contentStream.endText();

                    yPosition -= 30;

                    // Encabezados de tabla
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("ID");
                    contentStream.newLineAtOffset(50, 0);
                    contentStream.showText("Producto");
                    contentStream.newLineAtOffset(200, 0);
                    contentStream.showText("Cant");
                    contentStream.newLineAtOffset(50, 0);
                    contentStream.showText("Precio Unit.");
                    contentStream.newLineAtOffset(80, 0);
                    contentStream.showText("Subtotal");
                    contentStream.endText();

                    yPosition -= LINE_HEIGHT;

                    for (final GenerarPdfLiquidacionCommand.ProductoPedidoDTO producto : command.productos()) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.newLineAtOffset(MARGIN, yPosition);
                        contentStream.showText(String.valueOf(producto.idProducto()));
                        contentStream.newLineAtOffset(50, 0);
                        contentStream.showText(producto.nombre());
                        contentStream.newLineAtOffset(200, 0);
                        contentStream.showText(String.valueOf(producto.cantidad()));
                        contentStream.newLineAtOffset(50, 0);
                        contentStream.showText("$" + producto.precioUnitario());
                        contentStream.newLineAtOffset(80, 0);
                        contentStream.showText("$" + producto.subtotal());
                        contentStream.endText();
                        yPosition -= LINE_HEIGHT;
                    }

                    // Total
                    yPosition -= 20;
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 100, yPosition);
                    contentStream.showText("TOTAL: $" + command.totalPedido());
                    contentStream.endText();
                }

                document.save(outputStream);
                return outputStream.toByteArray();

            } catch (final IOException e) {
                log.error("Error generando PDF de liquidación para pedido ID: {}", command.idPedido(), e);
                throw new com.storeinvoice.store_invoice_api.domain.exception.PdfGenerationException(
                    "Error al generar el PDF de liquidación", e);
            }
        });
    }
}
