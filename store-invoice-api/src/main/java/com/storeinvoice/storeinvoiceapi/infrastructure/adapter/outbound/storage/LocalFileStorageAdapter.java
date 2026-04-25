package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.storage;

import com.storeinvoice.storeinvoiceapi.application.port.PdfStoragePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorSubidaPdfException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Profile({"local", "test"})
public class LocalFileStorageAdapter implements PdfStoragePort {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileStorageAdapter.class);

    private final String directorioBase;

    public LocalFileStorageAdapter(
            @Value("${pdf.storage.local.path:/tmp/store-invoice/pdfs}") final String directorioBase) {
        this.directorioBase = directorioBase;
    }

    @Override
    public Mono<String> subirPdf(final byte[] contenido, final String nombreArchivo) {
        return Mono.fromCallable(() -> guardarArchivo(contenido, nombreArchivo))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> {
                    LOG.error("Error al guardar archivo PDF local {}: {}", nombreArchivo, e.getMessage());
                    return new ErrorSubidaPdfException("Error al guardar el archivo PDF en almacenamiento local", e);
                });
    }

    private String guardarArchivo(final byte[] contenido, final String nombreArchivo) throws IOException {
        final Path archivo = Paths.get(directorioBase).resolve(nombreArchivo);
        Files.createDirectories(archivo.getParent());
        return Files.write(archivo, contenido, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                .toUri()
                .toString();
    }
}

