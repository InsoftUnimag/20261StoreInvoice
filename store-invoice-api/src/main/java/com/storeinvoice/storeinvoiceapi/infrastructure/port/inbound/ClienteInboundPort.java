package com.storeinvoice.storeinvoiceapi.infrastructure.port.inbound;

import com.storeinvoice.storeinvoiceapi.application.dto.response.ClienteResponse;
import reactor.core.publisher.Mono;

public interface ClienteInboundPort {
    
    Mono<ClienteResponse> consultarClientePorIdNacional(String idNacional);
    
    Mono<ClienteResponse> consultarClientePorIdCliente(String idCliente);
}