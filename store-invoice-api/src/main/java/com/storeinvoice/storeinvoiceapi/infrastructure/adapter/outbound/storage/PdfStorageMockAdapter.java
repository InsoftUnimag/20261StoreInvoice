package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.storage;

import com.storeinvoice.storeinvoiceapi.application.port.PdfStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("!local & !test & !prod")
public class PdfStorageMockAdapter implements PdfStoragePort {

    private static final Logger LOG = LoggerFactory.getLogger(PdfStorageMockAdapter.class);

    @Override
    public Mono<String> subirPdf(final byte[] contenido, final String nombreArchivo) {
        LOG.warn("MOCK: Simulando subida de PDF '{}'. Tamano: {} bytes", nombreArchivo, contenido.length);
        
        // Retorna una URL fake como si se hubiera subido exitosamente
        final String mockUrl = "https://mock-storage.local/pdfs/" + nombreArchivo;
        return Mono.just(mockUrl);
    }
}
