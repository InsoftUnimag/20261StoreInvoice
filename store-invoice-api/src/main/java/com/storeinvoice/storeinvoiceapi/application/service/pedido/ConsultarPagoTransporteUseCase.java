package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.dto.response.PagoTransporteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class ConsultarPagoTransporteUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public ConsultarPagoTransporteUseCase(LiquidacionRepository liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    public Mono<PagoTransporteResponse> execute(Long idPedido) {
        return Mono.fromSupplier(() -> liquidacionRepository.findByIdPedido(idPedido))
                .flatMap(optLiquidacion -> optLiquidacion
                        .map(liquidacion -> {
                            BigDecimal valorContraEntrega = BigDecimal.ZERO;
                            if (FormaPago.CONTRA_ENTREGA.equals(liquidacion.getFormaPago())) {
                                valorContraEntrega = liquidacion.getMontoLiquidado();
                            }
                            String nombreFormaPago = liquidacion.getFormaPago() != null ? liquidacion.getFormaPago().name() : null;
                            return Mono.just(new PagoTransporteResponse(idPedido, nombreFormaPago, valorContraEntrega));
                        })
                        .orElse(Mono.empty()))
                .switchIfEmpty(Mono.error(new PedidoNotFoundException(idPedido)));
    }
}
