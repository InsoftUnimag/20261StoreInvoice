package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultarFormaPagoPorClienteUseCase {

    private final FormaPagoClienteRepository formaPagoClienteRepository;

    public ConsultarFormaPagoPorClienteUseCase(FormaPagoClienteRepository formaPagoClienteRepository) {
        this.formaPagoClienteRepository = formaPagoClienteRepository;
    }

    public Mono<FormaPagoClienteResponse> ejecutar(Long idCliente) {
        return Mono.just(idCliente)
                .filterWhen(id -> Mono.just(formaPagoClienteRepository.existsByIdCliente(id)).filter(Boolean::booleanValue))
                .switchIfEmpty(Mono.error(new ClienteNotFoundException(idCliente)))
                .flatMap(id -> Mono.justOrEmpty(formaPagoClienteRepository.findByIdCliente(id))
                        .switchIfEmpty(Mono.error(new FormaPagoNotFoundException(idCliente))))
                .map(this::toResponse);
    }

    private FormaPagoClienteResponse toResponse(FormaPagoCliente formaPagoCliente) {
        return new FormaPagoClienteResponse(
                formaPagoCliente.getIdCliente(),
                formaPagoCliente.getFormaPago().name(),
                formaPagoCliente.getFechaRegistro()
        );
    }
}
