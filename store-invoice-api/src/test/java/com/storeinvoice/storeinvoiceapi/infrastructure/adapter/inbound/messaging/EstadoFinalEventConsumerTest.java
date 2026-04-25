package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ProcesarEstadoFinalUseCase;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EstadoFinalEventConsumerTest {

    @Mock
    private ProcesarEstadoFinalUseCase procesarEstadoFinalUseCase;

    @InjectMocks
    private EstadoFinalEventConsumer consumer;

    @Test
    void processFinalState_delega_al_caso_de_uso() {
        ProcesarEstadoFinalCommand command = new ProcesarEstadoFinalCommand(1L, 100, 2L);
        Consumer<ProcesarEstadoFinalCommand> actualConsumer = consumer.processFinalState();

        actualConsumer.accept(command);

        verify(procesarEstadoFinalUseCase).execute(command);
    }
}

