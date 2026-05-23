package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarTotalPedidoUseCaseTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @InjectMocks
    private ConsultarTotalPedidoUseCase useCase;

    @Test
    void execute_cuando_existe_liquidacion_retorna_monto() {
        final Long idPedido = 100L;
        final BigDecimal montoEsperado = new BigDecimal("1500.00");
        final LiquidacionCliente liquidacion = new LiquidacionCliente(
                null, idPedido, 1L, FormaPago.CONTRA_ENTREGA, EstadoLiquidacion.ENVIADO,
                LocalDateTime.now(), null, montoEsperado);
        when(liquidacionRepository.findByIdPedido(idPedido))
                .thenReturn(Optional.of(liquidacion));

        final BigDecimal resultado = useCase.execute(idPedido);

        assertEquals(montoEsperado, resultado);
    }

    @Test
    void execute_cuando_no_existe_liquidacion_lanza_excepcion() {
        final Long idPedido = 999L;
        when(liquidacionRepository.findByIdPedido(idPedido))
                .thenReturn(Optional.empty());

        assertThrows(PedidoNotFoundException.class, () -> useCase.execute(idPedido));
    }

    @Test
    void execute_cuando_monto_es_cero_lanza_excepcion() {
        final Long idPedido = 200L;
        final LiquidacionCliente liquidacion = new LiquidacionCliente(
                null, idPedido, 1L, FormaPago.CONTRA_ENTREGA, EstadoLiquidacion.ENVIADO,
                LocalDateTime.now(), null, BigDecimal.ZERO);
        when(liquidacionRepository.findByIdPedido(idPedido))
                .thenReturn(Optional.of(liquidacion));

        assertThrows(PedidoNotFoundException.class, () -> useCase.execute(idPedido));
    }
}
