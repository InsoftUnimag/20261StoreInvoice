package com.storeinvoice.store_invoice_api.application.service.cliente;

import com.storeinvoice.store_invoice_api.application.dto.command.RegistrarFormaPagoCommand;
import com.storeinvoice.store_invoice_api.application.dto.response.FormaPagoResponse;
import com.storeinvoice.store_invoice_api.domain.exception.FormaPagoInvalidaException;
import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.domain.valueobject.FormaPago;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteWebClient;
import com.storeinvoice.store_invoice_api.infrastructure.port.inbound.FormaPagoInboundPort;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.FormaPagoClienteRepositoryPort;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper.CommandFormaPagoClienteMapper;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper.ResponseFormaPagoClienteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class RegistrarFormaPagoClienteUseCase implements FormaPagoInboundPort {

    private static final Logger log = LoggerFactory.getLogger(RegistrarFormaPagoClienteUseCase.class);

    private final FormaPagoClienteRepositoryPort formaPagoClienteRepositoryPort;
    private final ClienteWebClient clienteWebClient;
    private final CommandFormaPagoClienteMapper commandMapper;
    private final ResponseFormaPagoClienteMapper responseMapper;

    public RegistrarFormaPagoClienteUseCase(
            FormaPagoClienteRepositoryPort formaPagoClienteRepositoryPort,
            ClienteWebClient clienteWebClient,
            CommandFormaPagoClienteMapper commandMapper,
            ResponseFormaPagoClienteMapper responseMapper) {
        this.formaPagoClienteRepositoryPort = formaPagoClienteRepositoryPort;
        this.clienteWebClient = clienteWebClient;
        this.commandMapper = commandMapper;
        this.responseMapper = responseMapper;
    }

    public Mono<FormaPagoResponse> registrarFormaPago(RegistrarFormaPagoCommand command) {
        return validarEntrada(command)
                .flatMap(this::validarFormaPago)
                .flatMap(this::verificarClienteExiste)
                .flatMap(this::guardarFormaPago)
                .map(responseMapper::toResponse);
    }

    private Mono<RegistrarFormaPagoCommand> validarEntrada(RegistrarFormaPagoCommand command) {
        if (command.idCliente() == null) {
            log.error("ID de cliente es requerido pero está vacío o nulo");
            return Mono.error(new IllegalArgumentException("ID de cliente es requerido"));
        }
        return Mono.just(command);
    }

    private Mono<RegistrarFormaPagoCommand> validarFormaPago(RegistrarFormaPagoCommand command) {
        FormaPago formaPago = FormaPago.fromValue(command.formaPago());
        if (formaPago == null) {
            log.error("Forma de pago inválida: {}", command.formaPago());
            return Mono.error(new FormaPagoInvalidaException(
                    "Forma de pago inválida. Debe ser CONTRA_ENTREGA o CARTERA_COMERCIAL"));
        }
        return Mono.just(command);
    }

    private Mono<RegistrarFormaPagoCommand> verificarClienteExiste(RegistrarFormaPagoCommand command) {
        return clienteWebClient.consultarClientePorIdCliente(command.idCliente().toString())
                .map(cliente -> command)
                .switchIfEmpty(Mono.error(new com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException(
                        "Cliente no encontrado con el ID proporcionado")));
    }

    private Mono<FormaPagoCliente> guardarFormaPago(RegistrarFormaPagoCommand command) {
        FormaPagoCliente formaPagoCliente = commandMapper.toDomain(command);
        return Mono.fromCallable(() -> formaPagoClienteRepositoryPort.save(formaPagoCliente));
    }
}