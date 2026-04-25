package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ServiceConnectionException;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.product.ProductosPedidoResponseExternalDTO;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.ProductoExternalMapper;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
@Profile("prod")
public class InventarioWebClient implements InventarioServicePort {

    private static final Logger LOG = LoggerFactory.getLogger(InventarioWebClient.class);

    private final WebClient webClient;
    private final ProductoExternalMapper productoExternalMapper;

    public InventarioWebClient(
            final ProductoExternalMapper productoExternalMapper,
            @Value("${external.inventario.module.base-url:http://localhost:8082}") final String baseUrl) {
        this.productoExternalMapper = productoExternalMapper;
        this.webClient = WebClient.builder()
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
                                        "Error al consultar el MÃ³dulo de Inventario: " + body, null))))
                .bodyToMono(ProductosPedidoResponseExternalDTO.class)
                .map(response -> {
                    final List<Producto> productos = response.productos() == null
                            ? Collections.emptyList()
                            : response.productos().stream()
                                    .map(productoExternalMapper::toDomain)
                                    .toList();
                    LOG.info("Se encontraron {} productos para el pedido {}", productos.size(), idPedido);
                    return productos;
                })
                .onErrorResume(WebClientRequestException.class, e -> {
                    LOG.error("Error de conexiÃ³n al consultar productos del pedido {}: {}", idPedido, e.getMessage());
                    return Mono.error(new ServiceConnectionException(
                            "Error al conectar con el MÃ³dulo de Inventario", e));
                });
    }
}

