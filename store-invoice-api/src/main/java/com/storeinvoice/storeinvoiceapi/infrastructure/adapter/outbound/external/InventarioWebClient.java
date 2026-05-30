package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ServiceConnectionException;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.product.ProductosPedidoResponseExternalDTO;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.ProductoExternalMapper;
import io.netty.channel.ChannelOption;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Component
@Profile("prod")
public class InventarioWebClient implements InventarioServicePort {

    private static final Logger LOG = LoggerFactory.getLogger(InventarioWebClient.class);
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int RESPONSE_TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_BACKOFF_SECONDS = 1;

    private final WebClient webClient;
    private final ProductoExternalMapper productoExternalMapper;

    public InventarioWebClient(
            final ProductoExternalMapper productoExternalMapper,
            @Value("${external.inventario.module.base-url:http://localhost:8082}") final String baseUrl) {
        this.productoExternalMapper = productoExternalMapper;

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
    public Mono<List<Producto>> consultarProductosPorPedido(final String idPedido) {
        LOG.info("Consultando productos del pedido con ID: {}", idPedido);

        return webClient.get()
                .uri("/api/v1/pedidos/{id_pedido}/productos", idPedido)
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.NOT_FOUND,
                        response -> Mono.error(
                                new PedidoNotFoundException("Pedido no encontrado con el ID proporcionado: " + idPedido)))
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new ServiceConnectionException(
                                        "Error al consultar el Modulo de Inventario: " + body, null))))
                .bodyToMono(ProductosPedidoResponseExternalDTO.class)
                .map(response -> {
                    if (response.productos() == null) {
                        LOG.info("No se encontraron productos para el pedido {}", idPedido);
                        return Collections.<Producto>emptyList();
                    }
                    final List<Producto> productos = response.productos().stream()
                            .filter(dto -> dto.idProducto() != null && !dto.idProducto().isBlank())
                            .map(productoExternalMapper::toDomain)
                            .toList();
                    LOG.info("Se encontraron {} productos para el pedido {}", productos.size(), idPedido);
                    return productos;
                })
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(RETRY_BACKOFF_SECONDS))
                        .maxBackoff(Duration.ofSeconds(4))
                        .filter(this::esErrorTransitorio)
                        .doBeforeRetry(signal ->
                                LOG.warn("Reintentando consulta de productos para pedido {}. Intento {}. Causa: {}",
                                        idPedido, signal.totalRetries() + 1, signal.failure().getMessage())))
                .onErrorResume(WebClientRequestException.class, e -> {
                    LOG.error("Error de conexion al consultar productos del pedido {}: {}", idPedido, e.getMessage());
                    return Mono.error(new ServiceConnectionException(
                            "Error al conectar con el Modulo de Inventario", e));
                });
    }

    private boolean esErrorTransitorio(final Throwable throwable) {
        return throwable instanceof WebClientRequestException
                || throwable instanceof TimeoutException
                || throwable instanceof IOException;
    }
}
