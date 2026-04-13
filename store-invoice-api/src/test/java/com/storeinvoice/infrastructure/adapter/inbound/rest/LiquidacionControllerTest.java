package com.storeinvoice.store_invoice_api.infrastructure.adapter.inbound.rest;

import com.storeinvoice.store_invoice_api.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.store_invoice_api.application.service.liquidacion.ConsultarLiquidacionesClienteUseCase;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiquidacionControllerTest {

    @Mock
    private ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase;

    @InjectMocks
    private LiquidacionController controller;

    private Long idCliente;
    private List<LiquidacionClienteResponse> respuestas;

    @BeforeEach
    void setUp() {
        idCliente = 1L;
        respuestas = List.of(
                new LiquidacionClienteResponse(
                        1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                        LocalDateTime.now(), "/pdf/1.pdf", new BigDecimal("1500.00")
                ),
                new LiquidacionClienteResponse(
                        2L, 101L, idCliente, "CARTERA_COMERCIAL", "PAGADA",
                        LocalDateTime.now(), "/pdf/2.pdf", new BigDecimal("2000.00")
                )
        );
    }

    @Test
    void consultarLiquidaciones_exitoso_retorna_lista() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 20)).thenReturn(respuestas);

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, 20);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(2, resultado.getBody().size());
        assertEquals(1L, resultado.getBody().get(0).idLiquidacion());
    }

    @Test
    void consultarLiquidaciones_paginacion_custom_pasa_parametros() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 2, 50)).thenReturn(List.of());

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 2, 50);

        assertEquals(200, resultado.getStatusCode().value());
    }

    @Test
    void consultarLiquidaciones_tamano_cero_usa_default() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 20)).thenReturn(respuestas);

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, 0);

        assertEquals(200, resultado.getStatusCode().value());
    }

    @Test
    void consultarLiquidaciones_sin_liquidaciones_retorna_lista_vacia() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 20)).thenReturn(List.of());

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, 20);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(0, resultado.getBody().size());
    }
}