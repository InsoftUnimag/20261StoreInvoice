package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.repository.EventoRecibidoRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoEvento;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcesarEstadoFinalUseCaseTest {

    @Mock
    private EventoRecibidoRepository eventoRecibidoRepository;

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Spy
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ProcesarEstadoFinalUseCase useCase;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.doAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        }).when(transactionTemplate).execute(any(org.springframework.transaction.support.TransactionCallback.class));
    }

    @Test
    void execute_flujoExitoso_completaMono() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 100, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);

        EventoRecibido eventoPendiente = new EventoRecibido(100L, 100, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);

        LiquidacionCliente liquidacionCliente = new LiquidacionCliente(null, 100L, 1L, FormaPago.CONTRA_ENTREGA, EstadoLiquidacion.ENVIADO, LocalDateTime.now(), null, new BigDecimal("1000.00"));
        when(liquidacionRepository.findByIdPedido(100L)).thenReturn(Optional.of(liquidacionCliente));

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        ArgumentCaptor<LiquidacionTransportista> liquidacionCaptor = ArgumentCaptor.forClass(LiquidacionTransportista.class);
        verify(liquidacionRepository).saveTransportista(liquidacionCaptor.capture());

        LiquidacionTransportista liquidacionGuardada = liquidacionCaptor.getValue();
        assertEquals(100L, liquidacionGuardada.getIdPedido());
        assertEquals(5L, liquidacionGuardada.getIdTransportista());
        assertEquals(new BigDecimal("100"), liquidacionGuardada.getMontoCalculado());

        assertEquals(EstadoEvento.PROCESADO, eventoPendiente.getEstado());
        assertNotNull(eventoPendiente.getFechaProcesado());
    }

    @Test
    void execute_eventoYaProcesado_completaMonoSinGuardar() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(true);

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        verify(eventoRecibidoRepository, never()).save(any());
        verify(liquidacionRepository, never()).findByIdPedido(any());
        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_comandoNulo_completaMonoSinGuardar() {
        StepVerifier.create(useCase.execute(null))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_idPedidoNulo_completaMonoSinGuardar() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(null, 90, 5L);

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_tasaEfectividadNula_completaMonoSinGuardar() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, null, 5L);

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_idTransportistaNulo_completaMonoSinGuardar() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, null);

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_liquidacionClienteNoEncontrada_marcaError_completaMono() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);

        EventoRecibido eventoPendiente = new EventoRecibido(100L, 90, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);

        when(liquidacionRepository.findByIdPedido(100L)).thenReturn(Optional.empty());

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        assertEquals(EstadoEvento.ERROR, eventoPendiente.getEstado());
        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_montoLiquidadoCero_marcaError_completaMono() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 90, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);

        EventoRecibido eventoPendiente = new EventoRecibido(100L, 90, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);

        LiquidacionCliente liquidacionCliente = new LiquidacionCliente(null, 100L, 1L, FormaPago.CONTRA_ENTREGA, EstadoLiquidacion.ENVIADO, LocalDateTime.now(), null, BigDecimal.ZERO);
        when(liquidacionRepository.findByIdPedido(100L)).thenReturn(Optional.of(liquidacionCliente));

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        assertEquals(EstadoEvento.ERROR, eventoPendiente.getEstado());
        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_tasaEfectividadInvalida_marcaError_completaMono() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 150, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);

        EventoRecibido eventoPendiente = new EventoRecibido(100L, 150, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);

        LiquidacionCliente liquidacionCliente = new LiquidacionCliente(null, 100L, 1L, FormaPago.CONTRA_ENTREGA, EstadoLiquidacion.ENVIADO, LocalDateTime.now(), null, new BigDecimal("1000.00"));
        when(liquidacionRepository.findByIdPedido(100L)).thenReturn(Optional.of(liquidacionCliente));

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        assertEquals(EstadoEvento.ERROR, eventoPendiente.getEstado());
        verify(liquidacionRepository, never()).saveTransportista(any());
    }

    @Test
    void execute_tasaCero_guardaMontoCero() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(100L, 0, 5L);
        when(eventoRecibidoRepository.existsByIdPedidoAndEstado(100L, EstadoEvento.PROCESADO)).thenReturn(false);

        EventoRecibido eventoPendiente = new EventoRecibido(100L, 0, 5L);
        when(eventoRecibidoRepository.save(any(EventoRecibido.class))).thenReturn(eventoPendiente);

        LiquidacionCliente liquidacionCliente = new LiquidacionCliente(null, 100L, 1L, FormaPago.CONTRA_ENTREGA, EstadoLiquidacion.ENVIADO, LocalDateTime.now(), null, new BigDecimal("1000.00"));
        when(liquidacionRepository.findByIdPedido(100L)).thenReturn(Optional.of(liquidacionCliente));

        StepVerifier.create(useCase.execute(command))
                .verifyComplete();

        ArgumentCaptor<LiquidacionTransportista> liquidacionCaptor = ArgumentCaptor.forClass(LiquidacionTransportista.class);
        verify(liquidacionRepository).saveTransportista(liquidacionCaptor.capture());

        assertEquals(new BigDecimal("0"), liquidacionCaptor.getValue().getMontoCalculado());
        assertEquals(EstadoEvento.PROCESADO, eventoPendiente.getEstado());
    }
}
