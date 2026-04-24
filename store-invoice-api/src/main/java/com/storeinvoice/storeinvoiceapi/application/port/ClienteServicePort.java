package com.storeinvoice.storeinvoiceapi.application.port;

import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import reactor.core.publisher.Mono;

public interface ClienteServicePort {

    Mono<Cliente> findByIdNacional(String idNacional);

    Mono<Cliente> findById(String idCliente);
}