package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.TieneFormaPagoResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class VerificarTieneFormaPagoLiquidacionUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public VerificarTieneFormaPagoLiquidacionUseCase(LiquidacionRepository liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    public Mono<TieneFormaPagoResponse> ejecutar(Long idPedido) {
        return Mono.fromSupplier(() -> liquidacionRepository.findByIdPedido(idPedido))
                .flatMap(optLiquidacion -> optLiquidacion
                        .map(l -> Mono.just(new TieneFormaPagoResponse(l.getIdCliente(), true)))
                        .orElseGet(Mono::empty))
                .switchIfEmpty(Mono.error(new PedidoNotFoundException(idPedido)));
    }
}

