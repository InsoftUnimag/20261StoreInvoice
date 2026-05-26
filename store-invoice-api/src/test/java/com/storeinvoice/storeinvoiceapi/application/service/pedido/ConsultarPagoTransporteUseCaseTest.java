package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.dto.response.PagoTransporteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarPagoTransporteUseCaseTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @InjectMocks
    private ConsultarPagoTransporteUseCase useCase;

    private LiquidacionCliente liquidacionCliente;

    @BeforeEach
    void setUp() {
        liquidacionCliente = new LiquidacionCliente();
        liquidacionCliente.setIdPedido(1L);
        liquidacionCliente.setMontoLiquidado(new BigDecimal("50000.00"));
    }

    @Test
    void execute_FormaPagoContraEntrega_ReturnsTotal() {
        liquidacionCliente.setFormaPago(FormaPago.CONTRA_ENTREGA);
        when(liquidacionRepository.findByIdPedido(1L)).thenReturn(Optional.of(liquidacionCliente));

        Mono<PagoTransporteResponse> result = useCase.execute(1L);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.idPedido().equals(1L) &&
                        response.formaPago().equals("CONTRA_ENTREGA") &&
                        response.valorContraEntrega().compareTo(new BigDecimal("50000.00")) == 0)
                .verifyComplete();
    }

    @Test
    void execute_FormaPagoCartera_ReturnsZero() {
        liquidacionCliente.setFormaPago(FormaPago.CARTERA_COMERCIAL);
        when(liquidacionRepository.findByIdPedido(1L)).thenReturn(Optional.of(liquidacionCliente));

        Mono<PagoTransporteResponse> result = useCase.execute(1L);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.idPedido().equals(1L) &&
                        response.formaPago().equals("CARTERA_COMERCIAL") &&
                        response.valorContraEntrega().compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }

    @Test
    void execute_FormaPagoNull_ReturnsZeroAndNullName() {
        liquidacionCliente.setFormaPago(null);
        when(liquidacionRepository.findByIdPedido(1L)).thenReturn(Optional.of(liquidacionCliente));

        Mono<PagoTransporteResponse> result = useCase.execute(1L);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.idPedido().equals(1L) &&
                        response.formaPago() == null &&
                        response.valorContraEntrega().compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }

    @Test
    void execute_PedidoNotFound_ThrowsException() {
        when(liquidacionRepository.findByIdPedido(1L)).thenReturn(Optional.empty());

        Mono<PagoTransporteResponse> result = useCase.execute(1L);

        StepVerifier.create(result)
                .expectError(PedidoNotFoundException.class)
                .verify();
    }
}
