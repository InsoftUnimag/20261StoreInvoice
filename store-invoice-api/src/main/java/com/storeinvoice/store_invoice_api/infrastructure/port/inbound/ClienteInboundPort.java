package com.storeinvoice.store_invoice_api.infrastructure.port.inbound;

import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import reactor.core.publisher.Mono;

public interface ClienteInboundPort {
    
    Mono<ClienteResponse> consultarClientePorIdNacional(String idNacional);
    
    Mono<ClienteResponse> consultarClientePorIdCliente(String idCliente);
}