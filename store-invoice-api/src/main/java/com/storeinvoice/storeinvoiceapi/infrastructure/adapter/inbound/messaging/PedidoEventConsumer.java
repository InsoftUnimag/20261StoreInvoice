package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.ProcesarPedidoInventarioUseCase;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Configuracion de consumers de mensajeria para eventos entrantes.
 * Utiliza Spring Cloud Stream con API funcional reactiva.
 */
@Configuration
@RequiredArgsConstructor
public class PedidoEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(PedidoEventConsumer.class);

    private final ProcesarPedidoInventarioUseCase procesarPedidoInventarioUseCase;


    @Bean
    public Function<DatosPedidoInventarioMessage, Mono<Void>> recibirPedido() {
        return mensaje -> {
            LOG.info("Mensaje recibido desde modulo de inventario. idPedido={}", mensaje.idPedido());
            return procesarPedidoInventarioUseCase.ejecutar(mensaje)
                    .doOnSuccess(v -> LOG.info("Mensaje procesado exitosamente. idPedido={}", mensaje.idPedido()))
                    .onErrorResume(e -> {
                        LOG.error("Error procesando mensaje idPedido={}: {}", mensaje.idPedido(), e.getMessage());
                        return Mono.empty();
                    });
        };
    }
}

