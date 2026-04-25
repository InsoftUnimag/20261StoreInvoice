package com.storeinvoice.storeinvoiceapi.infrastructure.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging.EstadoFinalEventConsumer;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de mensajería: registra los beans Consumer de Spring Cloud Stream.
 * Mantiene la configuración separada de la lógica de los adaptadores (SRP).
 *
 * <p>El nombre del bean 'processFinalState' debe coincidir con la propiedad
 * spring.cloud.stream.bindings.processFinalState-in-0 en application.yml.
 */
@Configuration
@RequiredArgsConstructor
public class EventConfig {

    private final EstadoFinalEventConsumer estadoFinalEventConsumer;

    @Bean
    public Consumer<ProcesarEstadoFinalCommand> processFinalState() {
        return estadoFinalEventConsumer.processFinalState();
    }
}
