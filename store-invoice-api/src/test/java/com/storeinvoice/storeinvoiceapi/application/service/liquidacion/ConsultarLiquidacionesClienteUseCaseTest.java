package com.storeinvoice.storeinvoiceapi.application.service.liquidacion;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.application.mapper.LiquidacionEntityMapper;
import com.storeinvoice.storeinvoiceapi.application.port.outbound.LiquidacionRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarLiquidacionesClienteUseCaseTest {

    @Mock
    private LiquidacionRepositoryPort liquidacionRepository;

    @Mock
    private LiquidacionEntityMapper liquidacionMapper;

    @InjectMocks
    private ConsultarLiquidacionesClienteUseCase useCase;

    private Long idCliente;
    private LiquidacionCliente liquidacionMock;
    private LiquidacionClienteResponse responseMock;

    @BeforeEach
    void setUp() {
        idCliente = 1L;
        liquidacionMock = new LiquidacionCliente();
        liquidacionMock.setIdLiquidacion(1L);
        liquidacionMock.setIdPedido(100L);
        liquidacionMock.setIdCliente(idCliente);
        liquidacionMock.setFormaPago("CONTRA_ENTREGA");
        liquidacionMock.setEstadoLiquidacion("PENDIENTE");
        liquidacionMock.setFechaLiquidacion(LocalDateTime.now());
        liquidacionMock.setUriPdf("/pdf/liquidacion-1.pdf");
        liquidacionMock.setMontoLiquidado(new BigDecimal("1500.00"));

        responseMock = new LiquidacionClienteResponse(
                1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                LocalDateTime.now(), "/pdf/liquidacion-1.pdf", new BigDecimal("1500.00")
        );
    }

    @Test
    void execute_exitoso_retorna_lista_de_liquidaciones() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidacionMock));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMock));

        final List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).idLiquidacion());
        assertEquals(100L, resultado.get(0).idPedido());
        assertEquals(idCliente, resultado.get(0).idCliente());
        assertEquals("CONTRA_ENTREGA", resultado.get(0).formaPago());
        verify(liquidacionRepository).findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20));
    }

    @Test
    void execute_sin_liquidaciones_lanza_excepcion() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of());

        final LiquidacionNotFoundException excepcion = assertThrows(
                LiquidacionNotFoundException.class,
                () -> useCase.execute(idCliente, 0, 20)
        );

        assertNotNull(excepcion);
    }

    @Test
    void execute_paginacion_correcta_pasa_parametros() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 2, 50)))
                .thenReturn(List.of(liquidacionMock));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMock));

        useCase.execute(idCliente, 2, 50);

        verify(liquidacionRepository).findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 2, 50));
    }

    @Test
    void execute_pagina_cero_retorna_resultados() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidacionMock));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMock));

        final List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void execute_pagina_negativa_trata_como_cero() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidacionMock));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMock));

        final List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, -1, 20);

        assertNotNull(resultado);
    }

    @Test
    void execute_tamano_pagina_muy_grande_maneja_memoria() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 10000)))
                .thenReturn(List.of(liquidacionMock));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMock));

        final List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 10000);

        assertNotNull(resultado);
    }

    @Test
    void execute_lista_vacia_retorna_excepcion() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of());

        assertThrows(LiquidacionNotFoundException.class, () -> useCase.execute(idCliente, 0, 20));
    }
}