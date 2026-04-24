package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("!prod")
public class ClienteMockAdapter implements ClienteServicePort {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteMockAdapter.class);

    @Override
    public Mono<Cliente> findByIdNacional(String idNacional) {
        LOG.warn("MOCK: Consultando cliente por ID nacional: {}", idNacional);
        Cliente cliente = new Cliente(
            "100",
            idNacional,
            "Juan Perez Mock",
            "3001234567",
            "Calle 123 #45-67"
        );
        return Mono.just(cliente);
    }

    @Override
    public Mono<Cliente> findById(String idCliente) {
        LOG.warn("MOCK: Consultando cliente por ID: {}", idCliente);
        Cliente cliente = new Cliente(
            idCliente,
            "12345678",
            "Juan Perez Mock",
            "3001234567",
            "Calle 123 #45-67"
        );
        return Mono.just(cliente);
    }
}