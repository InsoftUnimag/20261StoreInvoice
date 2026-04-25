package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.TieneFormaPagoResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarTieneFormaPagoLiquidacionUseCaseTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    private VerificarTieneFormaPagoLiquidacionUseCase useCase;

    private LiquidacionCliente liquidacionCliente;

    @BeforeEach
    void setUp() {
        useCase = new VerificarTieneFormaPagoLiquidacionUseCase(liquidacionRepository);

        liquidacionCliente = new LiquidacionCliente();
        liquidacionCliente.setIdLiquidacion(100L);
        liquidacionCliente.setIdPedido(10L);
        liquidacionCliente.setIdCliente(1L);
        liquidacionCliente.setFormaPago(FormaPago.CONTRA_ENTREGA);
        liquidacionCliente.setFechaLiquidacion(LocalDateTime.now());
        liquidacionCliente.setMontoLiquidado(BigDecimal.valueOf(50000));
    }

    @Test
    void ejecutar_pedidoConLiquidacion_retornaTrue() {
        when(liquidacionRepository.findByIdPedido(10L)).thenReturn(Optional.of(liquidacionCliente));

        useCase.ejecutar(10L)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.idCliente());
                    assertTrue(result.tieneFormaPago());
                })
                .verifyComplete();
    }

    @Test
    void ejecutar_pedidoNoExistente_lanzaExcepcion() {
        when(liquidacionRepository.findByIdPedido(999L)).thenReturn(Optional.empty());

        useCase.ejecutar(999L)
                .as(StepVerifier::create)
                .expectError(PedidoNotFoundException.class)
                .verify();
    }
}

