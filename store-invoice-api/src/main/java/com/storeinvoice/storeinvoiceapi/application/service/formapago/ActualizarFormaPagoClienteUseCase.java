package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ActualizarFormaPagoClienteUseCase {

    private final FormaPagoClienteRepository formaPagoClienteRepository;

    public ActualizarFormaPagoClienteUseCase(FormaPagoClienteRepository formaPagoClienteRepository) {
        this.formaPagoClienteRepository = formaPagoClienteRepository;
    }

    public Mono<FormaPagoClienteResponse> ejecutar(Long idCliente, FormaPago formaPago) {
        return Mono.just(idCliente)
                .flatMap(this::buscarFormaPagoExistente)
                .map(formaPagoCliente -> actualizarFormaPago(formaPagoCliente, formaPago))
                .map(formaPagoClienteRepository::update)
                .map(this::toResponse);
    }

    private Mono<FormaPagoCliente> buscarFormaPagoExistente(Long idCliente) {
        return Mono.justOrEmpty(formaPagoClienteRepository.findByIdCliente(idCliente))
                .switchIfEmpty(Mono.error(new FormaPagoNotFoundException(idCliente)));
    }

    private FormaPagoCliente actualizarFormaPago(FormaPagoCliente formaPagoCliente, FormaPago formaPago) {
        formaPagoCliente.setFormaPago(formaPago);
        formaPagoCliente.setFechaRegistro(LocalDateTime.now());
        return formaPagoCliente;
    }

    private FormaPagoClienteResponse toResponse(FormaPagoCliente formaPagoCliente) {
        return new FormaPagoClienteResponse(
                formaPagoCliente.getIdCliente(),
                formaPagoCliente.getFormaPago().name(),
                formaPagoCliente.getFechaRegistro()
        );
    }
}