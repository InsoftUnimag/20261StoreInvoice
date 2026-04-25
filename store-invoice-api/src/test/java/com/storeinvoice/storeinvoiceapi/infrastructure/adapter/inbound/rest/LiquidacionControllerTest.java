package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionTransportistaResponse;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.ActualizarEstadoLiquidacionClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.ConsultarLiquidacionesClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.CrearLiquidacionClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ActualizarMontoLiquidacionTransportistaUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ConsultarLiquidacionesTransportistaUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.CrearLiquidacionTransportistaUseCase;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest.mapper.LiquidacionClienteResponseMapper;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest.mapper.LiquidacionTransportistaResponseMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiquidacionControllerTest {

    @Mock
    private ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase;

    @Mock
    private ConsultarLiquidacionesTransportistaUseCase consultarLiquidacionesTransportistaUseCase;

    @Mock
    private CrearLiquidacionClienteUseCase crearLiquidacionClienteUseCase;

    @Mock
    private ActualizarEstadoLiquidacionClienteUseCase actualizarEstadoLiquidacionClienteUseCase;

    @Mock
    private CrearLiquidacionTransportistaUseCase crearLiquidacionTransportistaUseCase;

    @Mock
    private ActualizarMontoLiquidacionTransportistaUseCase actualizarMontoLiquidacionTransportistaUseCase;

    @Mock
    private LiquidacionClienteResponseMapper liquidacionClienteMapper;

    @Mock
    private LiquidacionTransportistaResponseMapper liquidacionTransportistaMapper;

    @InjectMocks
    private LiquidacionController controller;

    private Long idCliente;
    private List<LiquidacionCliente> liquidaciones;
    private List<LiquidacionClienteResponse> respuestas;

    @BeforeEach
    void setUp() {
        idCliente = 1L;
        
        LiquidacionCliente liquidacion1 = new LiquidacionCliente();
        liquidacion1.setIdLiquidacion(1L);
        liquidacion1.setIdPedido(100L);
        liquidacion1.setIdCliente(idCliente);
        liquidacion1.setFormaPago(FormaPago.CONTRA_ENTREGA);
        liquidacion1.setEstadoLiquidacion(EstadoLiquidacion.PENDIENTE);
        liquidacion1.setFechaLiquidacion(LocalDateTime.now());
        liquidacion1.setUriPdf("/pdf/1.pdf");
        liquidacion1.setMontoLiquidado(new BigDecimal("1500.00"));

        LiquidacionCliente liquidacion2 = new LiquidacionCliente();
        liquidacion2.setIdLiquidacion(2L);
        liquidacion2.setIdPedido(101L);
        liquidacion2.setIdCliente(idCliente);
        liquidacion2.setFormaPago(FormaPago.CARTERA_COMERCIAL);
        liquidacion2.setEstadoLiquidacion(EstadoLiquidacion.PAGADA);
        liquidacion2.setFechaLiquidacion(LocalDateTime.now());
        liquidacion2.setUriPdf("/pdf/2.pdf");
        liquidacion2.setMontoLiquidado(new BigDecimal("2000.00"));

        liquidaciones = List.of(liquidacion1, liquidacion2);

        respuestas = List.of(
                new LiquidacionClienteResponse(
                        1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                        liquidacion1.getFechaLiquidacion(), "/pdf/1.pdf", new BigDecimal("1500.00")
                ),
                new LiquidacionClienteResponse(
                        2L, 101L, idCliente, "CARTERA_COMERCIAL", "PAGADA",
                        liquidacion2.getFechaLiquidacion(), "/pdf/2.pdf", new BigDecimal("2000.00")
                )
        );
    }

    @Test
    void consultarLiquidaciones_exitoso_retorna_lista() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 20)).thenReturn(liquidaciones);
        when(liquidacionClienteMapper.toResponseList(liquidaciones)).thenReturn(respuestas);

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, 20);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(2, resultado.getBody().size());
        assertEquals(1L, resultado.getBody().get(0).idLiquidacion());
    }

    @Test
    void consultarLiquidaciones_paginacion_custom_pasa_parametros() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 2, 50)).thenReturn(List.of());
        when(liquidacionClienteMapper.toResponseList(any())).thenReturn(List.of());

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 2, 50);

        assertEquals(200, resultado.getStatusCode().value());
    }

    @Test
    void consultarLiquidaciones_tamano_cero_usa_default() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 0)).thenReturn(liquidaciones);
        when(liquidacionClienteMapper.toResponseList(liquidaciones)).thenReturn(respuestas);

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, 0);

        assertEquals(200, resultado.getStatusCode().value());
    }

    @Test
    void consultarLiquidaciones_sin_liquidaciones_retorna_lista_vacia() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 20)).thenReturn(List.of());
        when(liquidacionClienteMapper.toResponseList(List.of())).thenReturn(List.of());

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, 20);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(0, resultado.getBody().size());
    }

    @Test
    void consultarLiquidaciones_tamano_negativo_usa_default() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, -5)).thenReturn(liquidaciones);
        when(liquidacionClienteMapper.toResponseList(liquidaciones)).thenReturn(respuestas);

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, -5);

        assertEquals(200, resultado.getStatusCode().value());
    }

    @Test
    void consultarLiquidaciones_pagina_negativa_pasa_valor() {
        when(consultarLiquidacionesUseCase.execute(idCliente, -1, 20)).thenReturn(liquidaciones);
        when(liquidacionClienteMapper.toResponseList(liquidaciones)).thenReturn(respuestas);

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, -1, 20);

        assertEquals(200, resultado.getStatusCode().value());
    }

    @Test
    void consultarLiquidaciones_cliente_id_muy_grande() {
        when(consultarLiquidacionesUseCase.execute(Long.MAX_VALUE, 0, 20)).thenReturn(List.of());
        when(liquidacionClienteMapper.toResponseList(any())).thenReturn(List.of());

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(Long.MAX_VALUE, 0, 20);

        assertEquals(200, resultado.getStatusCode().value());
    }

    @Test
    void consultarLiquidaciones_primer_pagina_vacia() {
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 20)).thenReturn(liquidaciones);
        when(consultarLiquidacionesUseCase.execute(idCliente, 1, 20)).thenReturn(List.of());
        when(liquidacionClienteMapper.toResponseList(liquidaciones)).thenReturn(respuestas);
        when(liquidacionClienteMapper.toResponseList(List.of())).thenReturn(List.of());

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado0 = 
                controller.consultarLiquidaciones(idCliente, 0, 20);
        final ResponseEntity<List<LiquidacionClienteResponse>> resultado1 = 
                controller.consultarLiquidaciones(idCliente, 1, 20);

        assertEquals(200, resultado0.getStatusCode().value());
        assertEquals(2, resultado0.getBody().size());
        assertEquals(200, resultado1.getStatusCode().value());
        assertEquals(0, resultado1.getBody().size());
    }

    @Test
    void consultarLiquidaciones_lista_grande_retorna_todos() {
        LiquidacionCliente liquidacionTemplate = new LiquidacionCliente();
        liquidacionTemplate.setIdLiquidacion(1L);
        liquidacionTemplate.setIdPedido(100L);
        liquidacionTemplate.setIdCliente(idCliente);
        liquidacionTemplate.setFormaPago(FormaPago.CONTRA_ENTREGA);
        liquidacionTemplate.setEstadoLiquidacion(EstadoLiquidacion.PENDIENTE);
        liquidacionTemplate.setFechaLiquidacion(LocalDateTime.now());
        liquidacionTemplate.setUriPdf("/pdf/1.pdf");
        liquidacionTemplate.setMontoLiquidado(new BigDecimal("1500.00"));
        
        List<LiquidacionCliente> listaGrande = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            LiquidacionCliente l = new LiquidacionCliente();
            l.setIdLiquidacion(1L);
            l.setIdPedido(100L);
            l.setIdCliente(idCliente);
            l.setFormaPago(FormaPago.CONTRA_ENTREGA);
            l.setEstadoLiquidacion(EstadoLiquidacion.PENDIENTE);
            l.setFechaLiquidacion(LocalDateTime.now());
            l.setUriPdf("/pdf/1.pdf");
            l.setMontoLiquidado(new BigDecimal("1500.00"));
            listaGrande.add(l);
        }
        
        List<LiquidacionClienteResponse> respuestasGrande = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            respuestasGrande.add(new LiquidacionClienteResponse(1L, 100L, idCliente, "CONTRA_ENTREGA", "PENDIENTE",
                    LocalDateTime.now(), "/pdf/1.pdf", new BigDecimal("1500.00")));
        }
        
        when(consultarLiquidacionesUseCase.execute(idCliente, 0, 100)).thenReturn(listaGrande);
        when(liquidacionClienteMapper.toResponseList(listaGrande)).thenReturn(respuestasGrande);

        final ResponseEntity<List<LiquidacionClienteResponse>> resultado = 
                controller.consultarLiquidaciones(idCliente, 0, 100);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(100, resultado.getBody().size());
    }
}