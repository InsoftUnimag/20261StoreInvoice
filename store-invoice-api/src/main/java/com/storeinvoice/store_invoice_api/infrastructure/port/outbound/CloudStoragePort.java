package com.storeinvoice.store_invoice_api.infrastructure.port.outbound;

import reactor.core.publisher.Mono;

public interface CloudStoragePort {

    Mono<String> subirArchivoPdf(byte[] contenidoPdf, String nombreArchivo);
}
