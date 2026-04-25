package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.storage;

import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorSubidaPdfException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageAdapterTest {

    private static final String DIRECTORIO_TEST = "/tmp/store-invoice-test/pdfs";
    private LocalFileStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalFileStorageAdapter(DIRECTORIO_TEST);
    }

    @AfterEach
    void tearDown() throws IOException {
        final Path directorio = Paths.get(DIRECTORIO_TEST);
        if (Files.exists(directorio)) {
            Files.walk(directorio)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (final IOException e) {
                            // Ignorar errores de limpieza
                        }
                    });
        }
    }

    @Test
    void subirPdf_guardaArchivoCorrectamente() {
        final byte[] contenido = "Contenido de prueba PDF".getBytes();
        final String nombreArchivo = "test-liquidacion.pdf";

        StepVerifier.create(adapter.subirPdf(contenido, nombreArchivo))
                .assertNext(uri -> {
                    assertTrue(uri.contains(nombreArchivo), "La URI debe contener el nombre del archivo");
                    final Path rutaArchivo = Paths.get(DIRECTORIO_TEST, nombreArchivo);
                    assertTrue(Files.exists(rutaArchivo), "El archivo debe existir en el sistema de archivos");
                })
                .verifyComplete();
    }

    @Test
    void subirPdf_retornaUriValida() {
        final byte[] contenido = new byte[]{1, 2, 3, 4, 5};
        final String nombreArchivo = "test-uri.pdf";

        StepVerifier.create(adapter.subirPdf(contenido, nombreArchivo))
                .assertNext(uri -> {
                    assertTrue(uri.startsWith("file:///"), "La URI debe comenzar con file:///");
                    assertTrue(uri.endsWith(nombreArchivo), "La URI debe terminar con el nombre del archivo");
                })
                .verifyComplete();
    }

    @Test
    void subirPdf_creaDirectorioSiNoExiste() {
        final String directorioNuevo = "/tmp/store-invoice-test-nuevo/pdfs";
        final LocalFileStorageAdapter adapterNuevo = new LocalFileStorageAdapter(directorioNuevo);
        final byte[] contenido = "test".getBytes();

        StepVerifier.create(adapterNuevo.subirPdf(contenido, "test.pdf"))
                .assertNext(uri -> {
                    final Path directorio = Paths.get(directorioNuevo);
                    assertTrue(Files.exists(directorio), "El directorio debe haberse creado");
                })
                .verifyComplete();

        // Limpieza
        try {
            Files.walk(Paths.get(directorioNuevo))
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (final IOException e) {
                            // Ignorar
                        }
                    });
        } catch (final IOException e) {
            // Ignorar
        }
    }
}
