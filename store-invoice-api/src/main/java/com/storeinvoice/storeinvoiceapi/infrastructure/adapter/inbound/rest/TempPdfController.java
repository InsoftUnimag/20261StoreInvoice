package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.request.GenerarPdfSupabaseRequest;
import com.storeinvoice.storeinvoiceapi.application.service.pdf.GenerarPdfSupabaseTempUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/temp")
@Tag(name = "Temp - PDF Supabase", description = "Endpoint temporal para generar PDF y subir a Supabase")
public class TempPdfController {

    private final GenerarPdfSupabaseTempUseCase useCase;

    public TempPdfController(final GenerarPdfSupabaseTempUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/generar-pdf-supabase")
    @Operation(
        summary = "Generar PDF y subir a Supabase",
        description = "Genera un PDF real con los datos proporcionados, lo sube a Supabase Storage "
                + "y actualiza la URL en la liquidacion especificada. "
                + "Requiere la property app.temp.use-real-adapters=true al iniciar la app."
    )
    public Mono<ResponseEntity<Map<String, String>>> generarPdfSupabase(
            @RequestBody final GenerarPdfSupabaseRequest request) {
        return useCase.ejecutar(request)
                .map(uri -> ResponseEntity.ok(Map.of("uri", uri)));
    }
}
