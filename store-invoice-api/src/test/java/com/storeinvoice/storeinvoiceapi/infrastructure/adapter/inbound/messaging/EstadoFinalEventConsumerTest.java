package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ProcesarEstadoFinalUseCase;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoFinalEventConsumerTest {

    @Mock
    private ProcesarEstadoFinalUseCase procesarEstadoFinalUseCase;

    @InjectMocks
    private EstadoFinalEventConsumer consumer;

    @Test
    void processFinalState_delega_al_caso_de_uso_y_completa() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(1L, 100, 2L);
        when(procesarEstadoFinalUseCase.execute(any())).thenReturn(Mono.empty());

        Function<ProcesarEstadoFinalCommand, Mono<Void>> function = consumer.processFinalState();

        StepVerifier.create(function.apply(command))
                .verifyComplete();

        verify(procesarEstadoFinalUseCase).execute(command);
    }

    @Test
    void processFinalState_error_en_use_case_no_mata_consumer() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(1L, 100, 2L);
        when(procesarEstadoFinalUseCase.execute(any())).thenReturn(Mono.error(new RuntimeException("fallo")));

        Function<ProcesarEstadoFinalCommand, Mono<Void>> function = consumer.processFinalState();

        StepVerifier.create(function.apply(command))
                .verifyComplete();

        verify(procesarEstadoFinalUseCase).execute(command);
    }
}
