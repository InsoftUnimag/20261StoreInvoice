package com.storeinvoice.store_invoice_api.infrastructure.adapter.inbound.rest;

import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import com.storeinvoice.store_invoice_api.infrastructure.port.inbound.ClienteInboundPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteInboundPort clienteInboundPort;

    public ClienteController(ClienteInboundPort clienteInboundPort) {
        this.clienteInboundPort = clienteInboundPort;
    }

    @GetMapping("/nacional/{id_nacional}")
    public Mono<ResponseEntity<ClienteResponse>> consultarClientePorIdNacional(
            @PathVariable("id_nacional") String idNacional) {
        return clienteInboundPort.consultarClientePorIdNacional(idNacional)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id_cliente}")
    public Mono<ResponseEntity<ClienteResponse>> consultarClientePorId(
            @PathVariable("id_cliente") String idCliente) {
        return clienteInboundPort.consultarClientePorIdCliente(idCliente)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}