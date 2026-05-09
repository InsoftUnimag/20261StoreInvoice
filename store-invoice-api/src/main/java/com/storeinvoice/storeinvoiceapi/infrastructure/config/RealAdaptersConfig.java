package com.storeinvoice.storeinvoiceapi.infrastructure.config;

import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.pdf.OpenPdfGeneratorAdapter;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.storage.SupabaseStorageAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "app.temp.use-real-adapters", havingValue = "true")
public class RealAdaptersConfig {

    @Bean
    @Primary
    public OpenPdfGeneratorAdapter openPdfGeneratorAdapter() {
        return new OpenPdfGeneratorAdapter();
    }

    @Bean
    @Primary
    public SupabaseStorageAdapter supabaseStorageAdapter(
            @Value("${supabase.url:}") final String supabaseUrl,
            @Value("${supabase.api-key:}") final String apiKey,
            @Value("${supabase.bucket:liquidaciones-pdf}") final String bucket) {
        return new SupabaseStorageAdapter(supabaseUrl, apiKey, bucket);
    }
}
