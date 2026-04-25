package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPedidoInvalidosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoClienteNoEncontradaException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarLiquidacionDesdeInventarioUseCaseTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private FormaPagoClienteRepository formaPagoClienteRepository;

    @InjectMocks
    private RegistrarLiquidacionDesdeInventarioUseCase useCase;

    private FormaPagoCliente formaPagoCliente;

    @BeforeEach
    void setUp() {
        formaPagoCliente = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    @Test
    void ejecutar_mensajeValido_guardaLiquidacion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");
        final LiquidacionCliente guardada = new LiquidacionCliente();
        guardada.setIdLiquidacion(1L);
        guardada.setIdPedido(100L);

        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoCliente));
        when(liquidacionRepository.saveCliente(any(LiquidacionCliente.class))).thenReturn(guardada);

        final LiquidacionCliente resultado = useCase.ejecutar(mensaje);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdLiquidacion());

        final ArgumentCaptor<LiquidacionCliente> captor = ArgumentCaptor.forClass(LiquidacionCliente.class);
        verify(liquidacionRepository).saveCliente(captor.capture());

        final LiquidacionCliente capturada = captor.getValue();
        assertEquals(100L, capturada.getIdPedido());
        assertEquals(1L, capturada.getIdCliente());
        assertEquals(FormaPago.CARTERA_COMERCIAL, capturada.getFormaPago());
        assertEquals(EstadoLiquidacion.PENDIENTE, capturada.getEstadoLiquidacion());
        assertEquals(new BigDecimal("5000"), capturada.getMontoLiquidado());
        assertNotNull(capturada.getFechaLiquidacion());
    }

    @Test
    void ejecutar_mensajeNulo_lanzaExcepcion() {
        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(null));
        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_idPedidoNulo_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(null, 1L, 5000L, "Calle 123");

        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(mensaje));
        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_idPedidoCero_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(0L, 1L, 5000L, "Calle 123");

        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(mensaje));
        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_idClienteNulo_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, null, 5000L, "Calle 123");

        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(mensaje));
        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_idClienteCero_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 0L, 5000L, "Calle 123");

        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(mensaje));
        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_totalPedidoNegativo_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, -1L, "Calle 123");

        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(mensaje));
        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_formaPagoNoEncontrada_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.empty());

        assertThrows(FormaPagoClienteNoEncontradaException.class, () -> useCase.ejecutar(mensaje));
        verify(liquidacionRepository, never()).saveCliente(any());
    }
}

