package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.repository.EventoRecibidoRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.PedidoRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionException;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoEvento;
import com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import java.math.BigDecimal;
import java.util.Optional;
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
class ProcesarEstadoFinalUseCaseTest {

    @Mock
    private EventoRecibidoRepository eventoRecibidoRepository;

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private ProcesarEstadoFinalUseCase useCase;

    @Test
    void execute_exitoso_procesa_y_guarda() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);
        
        EventoRecibido eventoPendiente = new EventoRecibido(100L, 90, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);
        
        when(pedidoRepository.findPrecioPedidoByIdPedido(100L)).thenReturn(Optional.of(new BigDecimal("1000.00")));

        useCase.execute(command);

        // Verify liquidacion is saved
        ArgumentCaptor<LiquidacionTransportista> liquidacionCaptor = ArgumentCaptor.forClass(LiquidacionTransportista.class);
        verify(liquidacionRepository).saveTransportista(liquidacionCaptor.capture());
        
        LiquidacionTransportista liquidacionGuardada = liquidacionCaptor.getValue();
        assertEquals(100L, liquidacionGuardada.getIdPedido());
        assertEquals(5L, liquidacionGuardada.getIdTransportista());
        assertEquals(new BigDecimal("90"), liquidacionGuardada.getMontoCalculado());

        // Verify event is marked as processed
        assertEquals(EstadoEvento.PROCESADO, eventoPendiente.getEstado());
        assertNotNull(eventoPendiente.getFechaProcesado());
    }

    @Test
    void execute_evento_ya_procesado_ignora() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(true);

        useCase.execute(command);

        verify(eventoRecibidoRepository, never()).save(any());
        verify(pedidoRepository, never()).findPrecioPedidoByIdPedido(any());
        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_pedido_no_encontrado_marca_error_y_lanza_excepcion() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);
        
        EventoRecibido eventoPendiente = new EventoRecibido(100L, 90, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);
        
        when(pedidoRepository.findPrecioPedidoByIdPedido(100L)).thenReturn(Optional.empty());

        assertThrows(PedidoNotFoundException.class, () -> useCase.execute(command));

        assertEquals(EstadoEvento.ERROR, eventoPendiente.getEstado());
        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_precio_cero_marca_error_y_lanza_excepcion() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);
        
        EventoRecibido eventoPendiente = new EventoRecibido(100L, 90, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);
        
        when(pedidoRepository.findPrecioPedidoByIdPedido(100L)).thenReturn(Optional.of(BigDecimal.ZERO));

        assertThrows(LiquidacionException.class, () -> useCase.execute(command));

        assertEquals(EstadoEvento.ERROR, eventoPendiente.getEstado());
        verify(liquidacionRepository, never()).saveTransportista(any());
    }
}

