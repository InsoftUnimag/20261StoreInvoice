package com.storeinvoice.storeinvoiceapi.application.port;

import reactor.core.publisher.Mono;

public interface PdfStoragePort {

    Mono<String> subirPdf(byte[] contenido, String nombreArchivo);
}

