package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarProductosPedidoUseCaseTest {

    @Mock
    private InventarioServicePort inventarioServicePort;

    @InjectMocks
    private ConsultarProductosPedidoUseCase useCase;

    @Test
    void ejecutar_cuando_pedido_existe_retorna_lista_de_productos() {
        final String idPedido = "100";
        final List<Producto> productosEsperados = List.of(
                new Producto("501", "Gaseosa 1L", 10, 5000, 50000),
                new Producto("502", "Agua 1L", 5, 3000, 15000)
        );
        when(inventarioServicePort.consultarProductosPorPedido(idPedido))
                .thenReturn(Mono.just(productosEsperados));

        StepVerifier.create(useCase.ejecutar(idPedido))
                .expectNext(productosEsperados)
                .verifyComplete();
    }

    @Test
    void ejecutar_cuando_pedido_no_existe_lanza_excepcion() {
        final String idPedido = "999";
        when(inventarioServicePort.consultarProductosPorPedido(idPedido))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.ejecutar(idPedido))
                .expectError(PedidoNotFoundException.class)
                .verify();
    }

    @Test
    void ejecutar_cuando_id_es_nulo_lanza_excepcion_inmediatamente() {
        StepVerifier.create(useCase.ejecutar(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void ejecutar_cuando_id_es_vacio_lanza_excepcion_inmediatamente() {
        StepVerifier.create(useCase.ejecutar("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void ejecutar_cuando_inventario_falla_propaga_error() {
        final String idPedido = "100";
        when(inventarioServicePort.consultarProductosPorPedido(idPedido))
                .thenReturn(Mono.error(new RuntimeException("Error de conexion")));

        StepVerifier.create(useCase.ejecutar(idPedido))
                .expectError(RuntimeException.class)
                .verify();
    }
}

