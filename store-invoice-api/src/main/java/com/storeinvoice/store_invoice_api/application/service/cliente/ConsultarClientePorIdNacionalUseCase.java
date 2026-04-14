package com.storeinvoice.store_invoice_api.application.service.cliente;

import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import com.storeinvoice.store_invoice_api.infrastructure.port.inbound.ClienteInboundPort;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsultarClientePorIdNacionalUseCase implements ClienteInboundPort {

    private static final Logger log = LoggerFactory.getLogger(ConsultarClientePorIdNacionalUseCase.class);

    private final ClienteServiceClient clienteServiceClient;

    public ConsultarClientePorIdNacionalUseCase(ClienteServiceClient clienteServiceClient) {
        this.clienteServiceClient = clienteServiceClient;
    }

    @Override
    public ClienteResponse consultarClientePorIdNacional(String idNacional) {
        if (idNacional == null || idNacional.isBlank()) {
            log.error("ID Nacional es requerido pero está vacío o nulo");
            throw new ClienteNotFoundException("El ID Nacional es requerido");
        }

        var response = clienteServiceClient.consultarClientePorIdNacional(idNacional);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return mapToClienteResponse(response.getBody());
        }

        throw new ClienteNotFoundException("Cliente no encontrado con el ID Nacional proporcionado");
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
    public ClienteResponse consultarClientePorIdCliente(String idCliente) {
        if (idCliente == null || idCliente.isBlank()) {
            log.error("ID de cliente es requerido pero está vacío o nulo");
            throw new ClienteNotFoundException("ID de cliente inválido");
        }

        var response = clienteServiceClient.consultarClientePorIdCliente(idCliente);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return mapToClienteResponse(response.getBody());
        }

        throw new ClienteNotFoundException("Cliente no encontrado con el ID proporcionado");
    }
}