package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.repository.PedidoRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import java.math.BigDecimal;
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
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private ConsultarTotalPedidoUseCase useCase;

    @Test
    void execute_cuando_existe_liquidacion_retorna_monto() {
        final Long idPedido = 100L;
        final BigDecimal montoEsperado = new BigDecimal("1500.00");
        when(pedidoRepository.findPrecioPedidoByIdPedido(idPedido))
                .thenReturn(Optional.of(montoEsperado));

        final BigDecimal resultado = useCase.execute(idPedido);

        assertEquals(montoEsperado, resultado);
    }

    @Test
    void execute_cuando_no_existe_liquidacion_lanza_excepcion() {
        final Long idPedido = 999L;
        when(pedidoRepository.findPrecioPedidoByIdPedido(idPedido))
                .thenReturn(Optional.empty());

        assertThrows(PedidoNotFoundException.class, () -> useCase.execute(idPedido));
    }
}
