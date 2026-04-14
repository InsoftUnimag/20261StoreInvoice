package com.storeinvoice.store_invoice_api.application.service.cliente;

import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import com.storeinvoice.store_invoice_api.infrastructure.port.inbound.ClienteInboundPort;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.domain.exception.InvalidClientIdException;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultarClientePorIdNacionalUseCase implements ClienteInboundPort {

    private static final Logger log = LoggerFactory.getLogger(ConsultarClientePorIdNacionalUseCase.class);

    private final ClienteWebClient clienteWebClient;

    public ConsultarClientePorIdNacionalUseCase(ClienteWebClient clienteWebClient) {
        this.clienteWebClient = clienteWebClient;
    }

    @Override
    public Mono<ClienteResponse> consultarClientePorIdNacional(String idNacional) {
        if (idNacional == null || idNacional.isBlank()) {
            log.error("ID Nacional es requerido pero está vacío o nulo");
            return Mono.error(new InvalidClientIdException("El ID Nacional es requerido"));
        }

        return clienteWebClient.consultarClientePorIdNacional(idNacional)
                .map(this::mapToClienteResponse)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException("Cliente no encontrado con el ID Nacional proporcionado")));
    }

    private ClienteResponse mapToClienteResponse(ClienteClientResponse cliente) {
        return new ClienteResponse(
            cliente.idCliente(),
            cliente.idNacional(),
            cliente.nombre(),
            cliente.telefono(),
            cliente.direccion()
        );
    }

    @Override
    public Mono<ClienteResponse> consultarClientePorIdCliente(String idCliente) {
        if (idCliente == null || idCliente.isBlank()) {
            log.error("ID de cliente es requerido pero está vacío o nulo");
            return Mono.error(new InvalidClientIdException("ID de cliente inválido"));
        }

        return clienteWebClient.consultarClientePorIdCliente(idCliente)
                .map(this::mapToClienteResponse)
                .switchIfEmpty(Mono.error(new ClienteNotFoundException("Cliente no encontrado con el ID proporcionado")));
    }
}