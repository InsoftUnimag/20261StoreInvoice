package com.storeinvoice.storeinvoiceapi.application.service.cliente;

import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultarClientePorIdUseCase {

    private final ClienteServicePort clienteServicePort;

    public ConsultarClientePorIdUseCase(ClienteServicePort clienteServicePort) {
        this.clienteServicePort = clienteServicePort;
    }

    public Mono<Cliente> ejecutar(String idCliente) {
        return clienteServicePort.findById(idCliente)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException("Cliente no encontrado con el ID proporcionado")));
    }
}