package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.pdf;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.application.port.PdfGeneratorPort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorGeneracionPdfException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class OpenPdfGeneratorAdapter implements PdfGeneratorPort {

    private static final Logger LOG = LoggerFactory.getLogger(OpenPdfGeneratorAdapter.class);

    private static final Font TITULO_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font SUBTITULO_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

    @Override
    public Mono<byte[]> generarPdf(final List<ProductoPedidoDTO> productos, final BigDecimal totalPedido,
            final String formaPago, final ClienteLiquidacionDTO cliente, final Long idPedido) {
        return Mono.fromCallable(() -> generarPdfBytes(productos, totalPedido, formaPago, cliente, idPedido))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> {
                    LOG.error("Error al generar PDF para pedido {}: {}", idPedido, e.getMessage());
                    return new ErrorGeneracionPdfException("Error al generar el PDF de liquidacion", e);
                });
    }

    private byte[] generarPdfBytes(final List<ProductoPedidoDTO> productos, final BigDecimal totalPedido,
            final String formaPago, final ClienteLiquidacionDTO cliente, final Long idPedido) throws DocumentException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        agregarTitulo(document, idPedido);
        agregarDatosCliente(document, cliente);
        agregarTablaProductos(document, productos);
        agregarTotales(document, totalPedido, formaPago);

        document.close();
        return baos.toByteArray();
    }

    private void agregarTitulo(final Document document, final Long idPedido) throws DocumentException {
        final Paragraph titulo = new Paragraph("Liquidacion de Pedido", TITULO_FONT);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        final Paragraph pedido = new Paragraph("Pedido N: " + idPedido, SUBTITULO_FONT);
        pedido.setAlignment(Element.ALIGN_CENTER);
        pedido.setSpacingAfter(20);
        document.add(pedido);
    }

    private void agregarDatosCliente(final Document document, final ClienteLiquidacionDTO cliente)
            throws DocumentException {
        final Paragraph subtitulo = new Paragraph("Datos del Cliente", SUBTITULO_FONT);
        subtitulo.setSpacingAfter(10);
        document.add(subtitulo);

        document.add(new Paragraph("Nombre: " + cliente.nombre(), NORMAL_FONT));
        document.add(new Paragraph("ID Nacional: " + cliente.idNacional(), NORMAL_FONT));
        document.add(new Paragraph("Telefono: " + cliente.telefono(), NORMAL_FONT));
        document.add(new Paragraph("Direccion: " + cliente.direccion(), NORMAL_FONT));
        document.add(Paragraph.getInstance(" "));
    }

    private void agregarTablaProductos(final Document document, final List<ProductoPedidoDTO> productos)
            throws DocumentException {
        final Paragraph subtitulo = new Paragraph("Productos", SUBTITULO_FONT);
        subtitulo.setSpacingAfter(10);
        document.add(subtitulo);

        final PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3, 1, 2, 2});

        agregarCeldaHeader(tabla, "Producto");
        agregarCeldaHeader(tabla, "Cantidad");
        agregarCeldaHeader(tabla, "Precio Unitario");
        agregarCeldaHeader(tabla, "Subtotal");

        for (final ProductoPedidoDTO producto : productos) {
            agregarCelda(tabla, producto.nombre());
            agregarCelda(tabla, String.valueOf(producto.cantidad()));
            agregarCelda(tabla, producto.precioUnitario().toString());
            agregarCelda(tabla, producto.subtotal().toString());
        }

        document.add(tabla);
        document.add(Paragraph.getInstance(" "));
    }

    private void agregarCeldaHeader(final PdfPTable tabla, final String texto) {
        final PdfPCell celda = new PdfPCell(new Phrase(texto, HEADER_FONT));
        celda.setBackgroundColor(Color.DARK_GRAY);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(5);
        tabla.addCell(celda);
    }

    private void agregarCelda(final PdfPTable tabla, final String texto) {
        final PdfPCell celda = new PdfPCell(new Phrase(texto, NORMAL_FONT));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(5);
        tabla.addCell(celda);
    }

    private void agregarTotales(final Document document, final BigDecimal totalPedido, final String formaPago)
            throws DocumentException {
        final Paragraph total = new Paragraph("Total del Pedido: " + totalPedido.toString(), SUBTITULO_FONT);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(20);
        document.add(total);

        final Paragraph pago = new Paragraph("Forma de Pago: " + formaPago, SUBTITULO_FONT);
        pago.setAlignment(Element.ALIGN_RIGHT);
        document.add(pago);
    }
}
