package com.storeinvoice.store_invoice_api.application.service.liquidacion;

import com.storeinvoice.store_invoice_api.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.store_invoice_api.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.domain.model.LiquidacionCliente;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper.LiquidacionEntityMapper;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.LiquidacionRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
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
class ConsultarLiquidacionesClienteUseCaseEdgeCasesTest {

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
        liquidacionMock = new LiquidacionCliente(
                1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                LocalDateTime.now(), "/pdf/liquidacion-1.pdf", new BigDecimal("1500.00")
        );
        responseMock = new LiquidacionClienteResponse(
                1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                LocalDateTime.now(), "/pdf/liquidacion-1.pdf", new BigDecimal("1500.00")
        );
    }

    @Test
    void execute_pagina_muy_alta_retorna_excepcion() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 10000, 20)))
                .thenReturn(Collections.emptyList());

        assertThrows(ClienteNotFoundException.class, () -> useCase.execute(idCliente, 10000, 20));
    }

    @Test
    void execute_tamano_pagina_maximo_retorna_datos() {
        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 1000)))
                .thenReturn(List.of(liquidacionMock));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMock));

        List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 1000);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void execute_datos_con_uri_pdf_nula() {
        LiquidacionCliente sinUri = new LiquidacionCliente(
                1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                LocalDateTime.now(), null, new BigDecimal("1500.00")
        );
        LiquidacionClienteResponse responseSinUri = new LiquidacionClienteResponse(
                1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                LocalDateTime.now(), null, new BigDecimal("1500.00")
        );

        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(sinUri));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseSinUri));

        List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(null, resultado.get(0).uriPdf());
    }

    @Test
    void execute_datos_con_monto_cero() {
        LiquidacionCliente montoCero = new LiquidacionCliente(
                1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                LocalDateTime.now(), "/pdf/1.pdf", BigDecimal.ZERO
        );
        LiquidacionClienteResponse responseMontoCero = new LiquidacionClienteResponse(
                1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                LocalDateTime.now(), "/pdf/1.pdf", BigDecimal.ZERO
        );

        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(montoCero));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMontoCero));

        List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado.get(0).montoLiquidado());
    }

    @Test
    void execute_retorna_multiples_liquidaciones() {
        LiquidacionCliente l2 = new LiquidacionCliente(
                2L, 101L, idCliente, "CARTERA_COMERCIAL", "PAGADA",
                LocalDateTime.now(), "/pdf/2.pdf", new BigDecimal("2000.00")
        );
        LiquidacionClienteResponse r2 = new LiquidacionClienteResponse(
                2L, 101L, idCliente, "CARTERA_COMERCIAL", "PAGADA",
                LocalDateTime.now(), "/pdf/2.pdf", new BigDecimal("2000.00")
        );

        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidacionMock, l2));
        when(liquidacionMapper.toResponseList(any())).thenReturn(List.of(responseMock, r2));

        List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 20);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void executeforma_pago_diferentes_retorna_datos() {
        LiquidacionCliente[] liquidaciones = new LiquidacionCliente[] {
                new LiquidacionCliente(1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE", LocalDateTime.now(), "/pdf/1.pdf", new BigDecimal("100.00")),
                new LiquidacionCliente(2L, 101L, idCliente, "TARJETA_CREDITO", "PAGADA", LocalDateTime.now(), "/pdf/2.pdf", new BigDecimal("200.00")),
                new LiquidacionCliente(3L, 102L, idCliente, "CARTERA_COMERCIAL", "CANCELADA", LocalDateTime.now(), "/pdf/3.pdf", new BigDecimal("300.00"))
        };

        when(liquidacionRepository.findByIdCliente(new ConsultarLiquidacionesQuery(idCliente, 0, 20)))
                .thenReturn(List.of(liquidaciones));
        when(liquidacionMapper.toResponseList(any())).thenReturn(java.util.Arrays.asList(
                new LiquidacionClienteResponse(1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE", LocalDateTime.now(), "/pdf/1.pdf", new BigDecimal("100.00")),
                new LiquidacionClienteResponse(2L, 101L, idCliente, "TARJETA_CREDITO", "PAGADA", LocalDateTime.now(), "/pdf/2.pdf", new BigDecimal("200.00")),
                new LiquidacionClienteResponse(3L, 102L, idCliente, "CARTERA_COMERCIAL", "CANCELADA", LocalDateTime.now(), "/pdf/3.pdf", new BigDecimal("300.00"))
        ));

        List<LiquidacionClienteResponse> resultado = useCase.execute(idCliente, 0, 20);

        assertEquals(3, resultado.size());
        assertEquals("CONTRA_ENTREGA", resultado.get(0).formaPago());
        assertEquals("TARJETA_CREDITO", resultado.get(1).formaPago());
        assertEquals("CARTERA_COMERCIAL", resultado.get(2).formaPago());
    }
}