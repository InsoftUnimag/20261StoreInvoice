package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ProcesarEstadoFinalUseCase;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Configuracion de consumers de mensajeria para eventos de estado final del Modulo de Transporte.
 * Utiliza Spring Cloud Stream con API funcional reactiva.
 */
@Configuration
@RequiredArgsConstructor
public class EstadoFinalEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(EstadoFinalEventConsumer.class);

    private final ProcesarEstadoFinalUseCase procesarEstadoFinalUseCase;

    @Bean
    public Function<ProcesarEstadoFinalCommand, Mono<Void>> processFinalState() {
        return command -> {
            LOG.info("Mensaje recibido desde modulo de transporte. idPedido={}, id_transpo = {}, tasaefecti={}",
                    command != null ? command.getId_pedido() : "null",
                    command!=null ? command.getId_transportista() : "null",
                    command != null ? command.getTasa_efectividad() : "null"
                    );
            return procesarEstadoFinalUseCase.execute(command)
                    .doOnSuccess(v -> LOG.info("Mensaje procesado exitosamente. idPedido={}",
                            command != null ? command.getId_pedido() : "null"))
                    .doOnError(e -> LOG.error("Error procesando mensaje idPedido={}: {}",
                            command != null ? command.getId_pedido() : "null", e.getMessage(), e));
        };
    }
}
