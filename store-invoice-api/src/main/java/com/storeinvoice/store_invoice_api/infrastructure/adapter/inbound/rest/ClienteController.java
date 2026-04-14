package com.storeinvoice.store_invoice_api.infrastructure.adapter.inbound.rest;

import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import com.storeinvoice.store_invoice_api.application.port.inbound.ClienteInboundPort;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.domain.exception.InvalidClientIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteInboundPort clienteInboundPort;

    public ClienteController(ClienteInboundPort clienteInboundPort) {
        this.clienteInboundPort = clienteInboundPort;
    }

    @GetMapping("/nacional/{id_nacional}")
    public ResponseEntity<ClienteResponse> consultarClientePorIdNacional(
            @PathVariable("id_nacional") String idNacional) {
        log.info("Recibida solicitud de consulta de cliente por ID Nacional: {}", idNacional);

        if (idNacional == null || idNacional.isBlank()) {
            log.error("ID Nacional vacío en la solicitud");
            throw new ClienteNotFoundException("El ID Nacional es requerido");
        }

        ClienteResponse response = clienteInboundPort.consultarClientePorIdNacional(idNacional);
        log.info("Consulta exitosa para ID Nacional: {}", idNacional);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bd/{id_cliente}")
    public ResponseEntity<ClienteResponse> consultarClientePorIdCliente(
            @PathVariable("id_cliente") String idCliente) {
        log.info("Recibida solicitud de consulta de cliente por ID de BD: {}", idCliente);

        if (idCliente == null || idCliente.isBlank()) {
            log.error("ID de cliente vacío en la solicitud");
            throw new InvalidClientIdException("ID de cliente inválido");
        }

        if (!idCliente.matches("\\d+")) {
            log.error("ID de cliente con formato inválido: {}", idCliente);
            throw new InvalidClientIdException("ID de cliente inválido");
        }

        ClienteResponse response = clienteInboundPort.consultarClientePorIdCliente(idCliente);
        log.info("Consulta exitosa para ID de BD: {}", idCliente);
        return ResponseEntity.ok(response);
    }
}