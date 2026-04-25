package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoAlreadyExistsException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegistrarFormaPagoClienteUseCase {

    private final FormaPagoClienteRepository formaPagoClienteRepository;

    public RegistrarFormaPagoClienteUseCase(FormaPagoClienteRepository formaPagoClienteRepository) {
        this.formaPagoClienteRepository = formaPagoClienteRepository;
    }

    public Mono<FormaPagoClienteResponse> ejecutar(Long idCliente, FormaPago formaPago) {
        return Mono.just(idCliente)
                .filterWhen(id -> Mono.just(!formaPagoClienteRepository.existsByIdCliente(id)))
                .switchIfEmpty(Mono.error(new FormaPagoAlreadyExistsException(idCliente)))
                .map(id -> crearFormaPagoCliente(idCliente, formaPago))
                .map(formaPagoClienteRepository::save)
                .map(this::toResponse);
    }

    private FormaPagoCliente crearFormaPagoCliente(Long idCliente, FormaPago formaPago) {
        return FormaPagoCliente.builder()
                .idCliente(idCliente)
                .formaPago(formaPago)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    private FormaPagoClienteResponse toResponse(FormaPagoCliente formaPagoCliente) {
        return new FormaPagoClienteResponse(
                formaPagoCliente.getIdCliente(),
                formaPagoCliente.getFormaPago().name(),
                formaPagoCliente.getFechaRegistro()
        );
    }
}

