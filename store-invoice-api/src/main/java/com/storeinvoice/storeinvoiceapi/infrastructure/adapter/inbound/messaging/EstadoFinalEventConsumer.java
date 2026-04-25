package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ProcesarEstadoFinalUseCase;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada (Driving Adapter) que consume eventos de estado final
 * publicados por el Módulo de Transporte mediante Spring Cloud Stream.
 *
 * <p>
 * Delega inmediatamente al caso de uso {@link ProcesarEstadoFinalUseCase},
 * manteniendo el adaptador libre de lógica de negocio (SRP).
 * El registro del bean Consumer se hace en {@code EventConfig}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstadoFinalEventConsumer {

    private final ProcesarEstadoFinalUseCase procesarEstadoFinalUseCase;

    /**
     * Procesa un comando de estado final recibido desde el broker de mensajes.
     *
     * @return Consumer funcional compatible con Spring Cloud Stream
     */
    public Consumer<ProcesarEstadoFinalCommand> processFinalState() {
        return command -> {
            procesarEstadoFinalUseCase.execute(command);
        };
    }
}
