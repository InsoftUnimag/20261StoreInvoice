package com.storeinvoice.storeinvoiceapi.application.service.cliente;

import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultarClientePorIdNacionalUseCase {

    private final ClienteServicePort clienteServicePort;

    public ConsultarClientePorIdNacionalUseCase(ClienteServicePort clienteServicePort) {
        this.clienteServicePort = clienteServicePort;
    }

    public Mono<Cliente> ejecutar(String idNacional) {
        return clienteServicePort.findByIdNacional(idNacional)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException("Cliente no encontrado con el ID Nacional proporcionado")));
    }
}
