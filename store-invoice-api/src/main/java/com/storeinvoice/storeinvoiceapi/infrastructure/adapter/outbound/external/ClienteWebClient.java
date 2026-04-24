package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ServiceConnectionException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.ClienteExternalResponse;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.ClienteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Profile("prod")
public class ClienteWebClient implements ClienteServicePort {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteWebClient.class);

    private final WebClient webClient;
    private final ClienteMapper clienteMapper;

    public ClienteWebClient(ClienteMapper clienteMapper,
                        @Value("${external.cliente.module.base-url:http://localhost:8081}") String baseUrl) {
        this.clienteMapper = clienteMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Mono<Cliente> findByIdNacional(String idNacional) {
        LOG.info("Consultando cliente por ID nacional: {}", idNacional);
        return webClient.get()
                .uri("/api/v1/clientes/nacional/{id_nacional}", idNacional)
                .retrieve()
                .bodyToMono(ClienteExternalResponse.class)
                .map(clienteMapper::toDomain)
                .doOnSuccess(r -> LOG.info("Cliente encontrado: {}", r.idCliente()))
                .onErrorResume(WebClientRequestException.class, e -> {
                    LOG.error("Error de conexión al consultar cliente por ID nacional {}: {}", idNacional, e.getMessage());
                    return Mono.error(new ServiceConnectionException("Error al conectar con el Módulo de Clientes", e));
                });
    }

    @Override
    public Mono<Cliente> findById(String idCliente) {
        LOG.info("Consultando cliente por ID: {}", idCliente);
        return webClient.get()
                .uri("/api/v1/clientes/{id_cliente}", idCliente)
                .retrieve()
                .bodyToMono(ClienteExternalResponse.class)
                .map(clienteMapper::toDomain)
                .doOnSuccess(r -> LOG.info("Cliente encontrado: {}", r.idCliente()))
                .onErrorResume(WebClientRequestException.class, e -> {
                    LOG.error("Error de conexión al consultar cliente por ID {}: {}", idCliente, e.getMessage());
                    return Mono.error(new ServiceConnectionException("Error al conectar con el Módulo de Clientes", e));
                });
    }
}