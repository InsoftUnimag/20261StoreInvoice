package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.messaging;

import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.ProcesarPedidoInventarioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoEventConsumerTest {

    @Mock
    private ProcesarPedidoInventarioUseCase procesarPedidoInventarioUseCase;

    private PedidoEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PedidoEventConsumer(procesarPedidoInventarioUseCase);
    }

    @Test
    void recibirPedido_mensajeValido_delegaAUsecaseYCompleta() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");
        final Function<DatosPedidoInventarioMessage, Mono<Void>> funcion = consumer.recibirPedido();

        when(procesarPedidoInventarioUseCase.ejecutar(mensaje)).thenReturn(Mono.empty());

        StepVerifier.create(funcion.apply(mensaje))
                .verifyComplete();

        verify(procesarPedidoInventarioUseCase).ejecutar(mensaje);
    }

    @Test
    void recibirPedido_errorEnUsecase_noMataConsumer() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");
        final Function<DatosPedidoInventarioMessage, Mono<Void>> funcion = consumer.recibirPedido();

        when(procesarPedidoInventarioUseCase.ejecutar(mensaje))
                .thenReturn(Mono.error(new RuntimeException("Error interno")));

        StepVerifier.create(funcion.apply(mensaje))
                .verifyComplete();

        verify(procesarPedidoInventarioUseCase).ejecutar(mensaje);
    }
}
