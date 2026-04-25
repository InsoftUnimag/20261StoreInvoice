package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.storage;

import com.storeinvoice.storeinvoiceapi.application.port.PdfStoragePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorSubidaPdfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@Profile("!local & !test")
public class SupabaseStorageAdapter implements PdfStoragePort {

    private static final Logger LOG = LoggerFactory.getLogger(SupabaseStorageAdapter.class);

    private final WebClient webClient;
    private final String bucket;
    private final String supabaseUrl;

    public SupabaseStorageAdapter(
            @Value("${supabase.url:}") final String supabaseUrl,
            @Value("${supabase.api-key:}") final String apiKey,
            @Value("${supabase.bucket:liquidaciones-pdf}") final String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader("apikey", apiKey)
                .build();
    }

    @Override
    public Mono<String> subirPdf(final byte[] contenido, final String nombreArchivo) {
        return webClient.post()
                .uri("/storage/v1/object/{bucket}/{nombreArchivo}", bucket, nombreArchivo)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .bodyValue(contenido)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> construirUrlPublica(nombreArchivo))
                .onErrorMap(WebClientResponseException.class, e -> {
                    LOG.error("Error al subir PDF a Supabase Storage {}: {}", nombreArchivo, e.getMessage());
                    return new ErrorSubidaPdfException("Error al subir el archivo PDF a Supabase Storage", e);
                });
    }

    private String construirUrlPublica(final String nombreArchivo) {
        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, nombreArchivo);
    }
}
