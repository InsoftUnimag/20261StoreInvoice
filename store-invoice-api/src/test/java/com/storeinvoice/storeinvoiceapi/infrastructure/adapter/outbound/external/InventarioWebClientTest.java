package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.product.ProductoExternalDTO;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.ProductoExternalMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del InventarioWebClient usando MockWebServer para simular
 * respuestas HTTP reales del MÃ³dulo de Inventario, y Mockito para el mapper.
 *
 * Nota: ProductoExternalMapper tiene componentModel = "spring", por lo tanto
 * no se puede instanciar con `new`. Se mockea con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class InventarioWebClientTest {

        private MockWebServer mockWebServer;
        private InventarioWebClient inventarioWebClient;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private ProductoExternalMapper productoExternalMapper;

        @BeforeEach
        void setUp() throws IOException {
                mockWebServer = new MockWebServer();
                mockWebServer.start();
                inventarioWebClient = new InventarioWebClient(productoExternalMapper,
                                mockWebServer.url("/").toString());
        }

        @AfterEach
        void tearDown() throws IOException {
                mockWebServer.shutdown();
        }

        @Test
        void consultarProductosPorPedido_cuando_pedido_existe_retorna_lista() throws JsonProcessingException {
                final Producto productoMock1 = new Producto("501", "Gaseosa 1L", 10, BigDecimal.valueOf(5000), BigDecimal.valueOf(50000));
                final Producto productoMock2 = new Producto("502", "Agua 1L", 5, BigDecimal.valueOf(3000), BigDecimal.valueOf(15000));
                when(productoExternalMapper.toDomain(any(ProductoExternalDTO.class)))
                                .thenReturn(productoMock1)
                                .thenReturn(productoMock2);

                final String responseBody = objectMapper.writeValueAsString(Map.of(
                                "productos", List.of(
                                                Map.of("id", "501", "nombre", "Gaseosa 1L",
                                                                "cantidad", 10, "precioUnitario", 5000, "subtotal",
                                                                50000),
                                                Map.of("id", "502", "nombre", "Agua 1L",
                                                                "cantidad", 5, "precioUnitario", 3000, "subtotal",
                                                                15000))));

                mockWebServer.enqueue(new MockResponse()
                                .setBody(responseBody)
                                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

                StepVerifier.create(inventarioWebClient.consultarProductosPorPedido("100"))
                                .expectNextMatches(productos -> {
                                        if (productos.size() != 2)
                                                return false;
                                        final Producto primero = productos.get(0);
                                        return "501".equals(primero.idProducto())
                                                        && "Gaseosa 1L".equals(primero.nombre())
                                                        && primero.cantidad() == 10;
                                })
                                .verifyComplete();
        }

        @Test
        void consultarProductosPorPedido_cuando_pedido_no_existe_lanza_PedidoNotFoundException() {
                mockWebServer.enqueue(new MockResponse().setResponseCode(404));

                StepVerifier.create(inventarioWebClient.consultarProductosPorPedido("999"))
                                .expectError(PedidoNotFoundException.class)
                                .verify();
        }

        @Test
        void consultarProductosPorPedido_cuando_servidor_falla_lanza_excepcion() {
                mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

                StepVerifier.create(inventarioWebClient.consultarProductosPorPedido("100"))
                                .expectError()
                                .verify();
        }
}

