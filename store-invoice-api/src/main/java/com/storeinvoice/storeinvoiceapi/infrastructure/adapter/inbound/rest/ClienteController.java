package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.ClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.service.cliente.ConsultarClientePorIdNacionalUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.cliente.ConsultarClientePorIdUseCase;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.ClienteMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Clientes", description = "Operaciones relacionadas con clientes")
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
    @Operation(summary = "Consultar cliente por ID nacional")
    public Mono<ResponseEntity<ClienteResponse>> consultarClientePorIdNacional(
            @Parameter(description = "ID nacional del cliente", required = true)
            @PathVariable @NotBlank String idNacional) {
        return consultarClientePorIdNacionalUseCase.ejecutar(idNacional)
                .map(clienteMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{idCliente}")
    @Operation(summary = "Consultar cliente por ID")
    public Mono<ResponseEntity<ClienteResponse>> consultarClientePorId(
            @Parameter(description = "ID único del cliente", required = true)
            @PathVariable @NotBlank String idCliente) {
        return consultarClientePorIdUseCase.ejecutar(idCliente)
                .map(clienteMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
