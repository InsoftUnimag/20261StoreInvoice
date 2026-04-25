package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarLiquidacionesClienteUseCaseTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @InjectMocks
    private ConsultarLiquidacionesClienteUseCase useCase;

    private Long idCliente;
    private LiquidacionCliente liquidacionMock;

    @BeforeEach
    void setUp() {
        idCliente = 1L;
        liquidacionMock = new LiquidacionCliente();
        liquidacionMock.setIdLiquidacion(1L);
        liquidacionMock.setIdPedido(100L);
        liquidacionMock.setIdCliente(idCliente);
        liquidacionMock.setFormaPago(FormaPago.CONTRA_ENTREGA);
        liquidacionMock.setEstadoLiquidacion(EstadoLiquidacion.PENDIENTE);
        liquidacionMock.setFechaLiquidacion(LocalDateTime.now());
        liquidacionMock.setUriPdf("/pdf/liquidacion-1.pdf");
        liquidacionMock.setMontoLiquidado(new BigDecimal("1500.00"));
    }

    @Test
    void execute_exitoso_retorna_lista_de_liquidaciones() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidacionMock));

        final List<LiquidacionCliente> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getIdLiquidacion());
        assertEquals(100L, resultado.get(0).getIdPedido());
        assertEquals(idCliente, resultado.get(0).getIdCliente());
        assertEquals(FormaPago.CONTRA_ENTREGA, resultado.get(0).getFormaPago());
    }

    @Test
    void execute_sin_liquidaciones_retorna_lista_vacia() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of());

        final List<LiquidacionCliente> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }

    @Test
    void execute_paginacion_correcta_pasa_parametros() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 2, 50)))
                .thenReturn(List.of(liquidacionMock));

        useCase.execute(idCliente, 2, 50);

        verify(liquidacionRepository).findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 2, 50));
    }

    @Test
    void execute_pagina_cero_retorna_resultados() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidacionMock));

        final List<LiquidacionCliente> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void execute_pagina_negativa_trata_como_cero() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidacionMock));

        final List<LiquidacionCliente> resultado = useCase.execute(idCliente, -1, 20);

        assertNotNull(resultado);
    }

    @Test
    void execute_tamano_pagina_muy_grande_maneja_memoria() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 10000)))
                .thenReturn(List.of(liquidacionMock));

        final List<LiquidacionCliente> resultado = useCase.execute(idCliente, 0, 10000);

        assertNotNull(resultado);
    }

    @Test
    void execute_lista_vacia_retorna_lista_vacia() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of());

        final List<LiquidacionCliente> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }
}
