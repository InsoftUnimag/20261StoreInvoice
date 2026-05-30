package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ServiceConnectionException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.ClienteExternalResponse;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.ClienteMapper;
import io.netty.channel.ChannelOption;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Component
@Profile("prod")
public class ClienteWebClient implements ClienteServicePort {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteWebClient.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int RESPONSE_TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_BACKOFF_SECONDS = 1;

    private final WebClient webClient;
    private final ClienteMapper clienteMapper;

    public ClienteWebClient(ClienteMapper clienteMapper,
                        @Value("${external.cliente.module.base-url:http://localhost:8081}") String baseUrl) {
        this.clienteMapper = clienteMapper;

        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS));

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Mono<Cliente> findByIdNacional(String idNacional) {
        return webClient.get()
                .uri("/api/v1/clientes/nacional/{id_nacional}", idNacional)
                .retrieve()
                .onStatus(
                        status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        response -> Mono.error(
                                new ClienteNotFoundException("Cliente no encontrado con ID nacional: " + idNacional)))
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new ServiceConnectionException(
                                        "Error al consultar el Modulo de Clientes: " + body, null))))
                .bodyToMono(ClienteExternalResponse.class)
                .map(clienteMapper::toDomain)
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(RETRY_BACKOFF_SECONDS))
                        .maxBackoff(Duration.ofSeconds(4))
                        .filter(this::esErrorTransitorio)
                        .doBeforeRetry(signal ->
                                LOG.warn("Reintentando consulta de cliente por ID nacional {}. Intento {}. Causa: {}",
                                        idNacional, signal.totalRetries() + 1, signal.failure().getMessage())))
                .onErrorResume(WebClientRequestException.class, e -> {
                    LOG.error("Error de conexion al consultar cliente por ID nacional {}: {}", idNacional, e.getMessage());
                    return Mono.error(new ServiceConnectionException("Error al conectar con el Modulo de Clientes", e));
                });
    }

    @Override
    public Mono<Cliente> findById(String idCliente) {
        return webClient.get()
                .uri("/api/v1/clientes/{id_cliente}", idCliente)
                .retrieve()
                .onStatus(
                        status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        response -> Mono.error(
                                new ClienteNotFoundException("Cliente no encontrado con ID: " + idCliente)))
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new ServiceConnectionException(
                                        "Error al consultar el Modulo de Clientes: " + body, null))))
                .bodyToMono(ClienteExternalResponse.class)
                .map(clienteMapper::toDomain)
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(RETRY_BACKOFF_SECONDS))
                        .maxBackoff(Duration.ofSeconds(4))
                        .filter(this::esErrorTransitorio)
                        .doBeforeRetry(signal ->
                                LOG.warn("Reintentando consulta de cliente por ID {}. Intento {}. Causa: {}",
                                        idCliente, signal.totalRetries() + 1, signal.failure().getMessage())))
                .onErrorResume(WebClientRequestException.class, e -> {
                    LOG.error("Error de conexion al consultar cliente por ID {}: {}", idCliente, e.getMessage());
                    return Mono.error(new ServiceConnectionException("Error al conectar con el Modulo de Clientes", e));
                });
    }

    private boolean esErrorTransitorio(final Throwable throwable) {
        return throwable instanceof WebClientRequestException
                || throwable instanceof TimeoutException
                || throwable instanceof IOException;
    }
}
