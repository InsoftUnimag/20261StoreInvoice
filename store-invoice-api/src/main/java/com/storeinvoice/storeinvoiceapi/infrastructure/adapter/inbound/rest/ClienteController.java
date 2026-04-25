package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.ClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.service.cliente.ConsultarClientePorIdNacionalUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.cliente.ConsultarClientePorIdUseCase;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.ClienteMapper;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ConsultarClientePorIdNacionalUseCase consultarClientePorIdNacionalUseCase;
    private final ConsultarClientePorIdUseCase consultarClientePorIdUseCase;
    private final ClienteMapper clienteMapper;

    public ClienteController(ConsultarClientePorIdNacionalUseCase consultarClientePorIdNacionalUseCase,
            ConsultarClientePorIdUseCase consultarClientePorIdUseCase,
            ClienteMapper clienteMapper) {
        this.consultarClientePorIdNacionalUseCase = consultarClientePorIdNacionalUseCase;
        this.consultarClientePorIdUseCase = consultarClientePorIdUseCase;
        this.clienteMapper = clienteMapper;
    }

    @GetMapping("/nacional/{idNacional}")
    public Mono<ResponseEntity<ClienteResponse>> consultarClientePorIdNacional(
            @PathVariable @NotBlank String idNacional) {
        return consultarClientePorIdNacionalUseCase.ejecutar(idNacional)
                .map(clienteMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{idCliente}")
    public Mono<ResponseEntity<ClienteResponse>> consultarClientePorId(
            @PathVariable @NotBlank String idCliente) {
        return consultarClientePorIdUseCase.ejecutar(idCliente)
                .map(clienteMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
