package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@Profile("!prod")
public class InventarioMockAdapter implements InventarioServicePort {

    private static final Logger LOG = LoggerFactory.getLogger(InventarioMockAdapter.class);

    @Override
    public Mono<List<Producto>> consultarProductosPorPedido(final String idPedido) {
        LOG.warn("MOCK: Consultando productos para el pedido con ID: {}", idPedido);

        final List<Producto> productosMock = List.of(
                new Producto("501", "Gaseosa 1L", 10, 5000, 50000),
                new Producto("502", "Agua 1L", 5, 3000, 15000),
                new Producto("503", "Jugo Naranja 500ml", 8, 2500, 20000)
        );

        return Mono.just(productosMock);
    }
}

