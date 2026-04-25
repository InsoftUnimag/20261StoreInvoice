package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.TotalPedidoResponse;
import com.storeinvoice.storeinvoiceapi.application.service.pedido.ConsultarTotalPedidoUseCase;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private ConsultarTotalPedidoUseCase consultarTotalPedidoUseCase;

    @InjectMocks
    private PedidoController controller;

    @Test
    void consultarTotal_exitoso_retorna_200_con_json_correcto() {
        final Long idPedido = 100L;
        final BigDecimal totalEsperado = new BigDecimal("1500.00");
        when(consultarTotalPedidoUseCase.execute(idPedido)).thenReturn(totalEsperado);

        final ResponseEntity<TotalPedidoResponse> resultado = controller.consultarTotal(idPedido);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(idPedido, resultado.getBody().idPedido());
        assertEquals(totalEsperado, resultado.getBody().totalPedido());
    }

    @Test
    void consultarTotal_cuando_no_existe_liquidacion_lanza_excepcion() {
        final Long idPedido = 999L;
        when(consultarTotalPedidoUseCase.execute(idPedido))
                .thenThrow(new PedidoNotFoundException(idPedido));

        assertThrows(PedidoNotFoundException.class, () -> controller.consultarTotal(idPedido));
    }
}
