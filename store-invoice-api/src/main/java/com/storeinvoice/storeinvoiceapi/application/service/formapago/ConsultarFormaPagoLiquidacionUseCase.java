package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultarFormaPagoLiquidacionUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public ConsultarFormaPagoLiquidacionUseCase(LiquidacionRepository liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    public Mono<FormaPagoClienteResponse> ejecutar(Long idPedido) {
        return Mono.fromSupplier(() -> liquidacionRepository.findByIdPedido(idPedido))
                .flatMap(optLiquidacion -> optLiquidacion
                        .map(liquidacion -> Mono.just(toResponse(liquidacion)))
                        .orElse(Mono.empty()))
                .switchIfEmpty(Mono.error(new PedidoNotFoundException(idPedido)));
    }

    private FormaPagoClienteResponse toResponse(LiquidacionCliente liquidacion) {
        return new FormaPagoClienteResponse(
                liquidacion.getIdCliente(),
                liquidacion.getFormaPago().name(),
                liquidacion.getFechaLiquidacion()
        );
    }
}
