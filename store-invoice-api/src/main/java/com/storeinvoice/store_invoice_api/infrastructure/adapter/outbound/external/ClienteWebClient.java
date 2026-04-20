package com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external;

import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ClienteWebClient {

    private final WebClient webClient;

    public ClienteWebClient(
            @Value("${external.cliente.module.base-url:http://localhost:8081}") String baseUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<ClienteResponse> consultarClientePorIdNacional(String idNacional) {
        return webClient.get()
                .uri("/api/v1/clientes/nacional/{id_nacional}", idNacional)
                .retrieve()
                .bodyToMono(ClienteResponse.class);
    }

    public Mono<ClienteResponse> consultarClientePorIdCliente(String idCliente) {
        return webClient.get()
                .uri("/api/v1/clientes/{id_cliente}", idCliente)
                .retrieve()
                .bodyToMono(ClienteResponse.class);
    }
}