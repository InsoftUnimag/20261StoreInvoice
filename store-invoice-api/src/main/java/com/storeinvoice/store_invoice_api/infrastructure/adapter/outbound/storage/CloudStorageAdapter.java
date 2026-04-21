package com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.storage;

import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.CloudStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class CloudStorageAdapter implements CloudStoragePort {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String BASE_URI = "https://storage.storeinvoice.com/liquidaciones/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd/");

    @Override
    public Mono<String> subirArchivoPdf(final byte[] contenidoPdf, final String nombreArchivo) {
        return Mono.fromCallable(() -> {

            // Validación de tamaño
            if (contenidoPdf.length > MAX_FILE_SIZE) {
                log.error("El PDF excede el tamaño máximo permitido: {} bytes", contenidoPdf.length);
                throw new IllegalArgumentException("El archivo PDF excede el tamaño máximo permitido (10 MB)");
            }

            // Generar ruta única
            final String fechaRuta = LocalDateTime.now().format(DATE_FORMATTER);
            final String nombreUnico = UUID.randomUUID() + "_" + nombreArchivo;
            final String uriCompleta = BASE_URI + fechaRuta + nombreUnico;

            // TODO: Implementar subida real al servicio de almacenamiento en nube
            // Por ahora simulamos la subida exitosa

            log.debug("PDF subido exitosamente: {}", uriCompleta);
            return uriCompleta;
        })
        .onErrorMap(e -> {
            log.error("Error al subir PDF a la nube. Nombre archivo: {}", nombreArchivo, e);
            return new com.storeinvoice.store_invoice_api.domain.exception.PdfGenerationException(
                "Error al subir el PDF a la nube. Intente más tarde", e);
        });
    }
}
