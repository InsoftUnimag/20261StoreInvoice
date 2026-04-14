package com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external;

import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ClienteWebClient {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteWebClient.class);

    private final WebClient webClient;

    public ClienteWebClient(
            @Value("${external.cliente.module.base-url:http://localhost:8081}") String baseUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<ClienteClientResponse> consultarClientePorIdNacional(String idNacional) {
        LOG.info("Consultando cliente por ID nacional: {}", idNacional);
        return webClient.get()
                .uri("/api/v1/clientes/{id_nacional}", idNacional)
                .retrieve()
                .bodyToMono(ClienteClientResponse.class)
                .doOnSuccess(r -> LOG.info("Cliente encontrado: {}", r.idCliente()));
    }

    public Mono<ClienteClientResponse> consultarClientePorIdCliente(String idCliente) {
        LOG.info("Consultando cliente por ID: {}", idCliente);
        return webClient.get()
                .uri("/api/v1/clientes/{id_cliente}", idCliente)
                .retrieve()
                .bodyToMono(ClienteClientResponse.class)
                .doOnSuccess(r -> LOG.info("Cliente encontrado: {}", r.idCliente()));
    }
}