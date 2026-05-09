package com.storeinvoice.storeinvoiceapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI storeInvoiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StoreInvoice API")
                        .description("API de liquidaciones de pedidos - StoreInvoice")
                        .version("1.0.0"));
    }
}
