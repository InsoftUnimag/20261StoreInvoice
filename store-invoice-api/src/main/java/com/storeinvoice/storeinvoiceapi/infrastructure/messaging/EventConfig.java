package com.storeinvoice.storeinvoiceapi.infrastructure.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging.EstadoFinalEventConsumer;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class EventConfig {

    private final EstadoFinalEventConsumer estadoFinalEventConsumer;

    @Bean
    public Consumer<ProcesarEstadoFinalCommand> processFinalState() {
        return estadoFinalEventConsumer.processFinalState();
    }
}

