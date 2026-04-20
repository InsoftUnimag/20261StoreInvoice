package com.storeinvoice.store_invoice_api.infrastructure.adapter.inbound.rest;

import com.storeinvoice.store_invoice_api.StoreInvoiceApiApplication;
import com.storeinvoice.store_invoice_api.TestcontainersConfiguration;
import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

/**
 * Test de integración T017 para el endpoint POST /api/v1/clientes/ingresar-forma-pago.
 * 
 * Valida:
 * - TC-001: POST válido devuelve 200 OK con FormaPagoResponse
 * - TC-002: FormaPago inválida devuelve FormaPagoInvalidaException
 * - TC-003: Cliente no encontrado devuelve ClienteNotFoundException
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = StoreInvoiceApiApplication.class)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class FormaPagoControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ClienteWebClient clienteWebClient;

    @BeforeEach
    void setUp() {
        // Configuración por defecto - cliente válido
    }

    @Test
    void T017_deberiaDevolver200Ok_cuandoComandoEsValido() {
        // Arrange
        Long idCliente = 1L;
        String formaPago = "CONTRA_ENTREGA";
        
        var clienteResponse = new ClienteClientResponse("1", "123", "Test Client", "123", "Test");
        when(clienteWebClient.consultarClientePorIdCliente(idCliente.toString()))
            .thenReturn(Mono.just(clienteResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "idCliente": 1,
                        "formaPago": "CONTRA_ENTREGA"
                    }
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.idCliente").isEqualTo(1)
                .jsonPath("$.formaPago").isEqualTo("CONTRA_ENTREGA");
    }

    @Test
    void T017_deberiaDevolver200Ok_conCarteraComercial() {
        // Arrange
        Long idCliente = 2L;
        String formaPago = "CARTERA_COMERCIAL";
        
        var clienteResponse = new ClienteClientResponse("2", "456", "Test Client 2", "456", "Test");
        when(clienteWebClient.consultarClientePorIdCliente(idCliente.toString()))
            .thenReturn(Mono.just(clienteResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "idCliente": 2,
                        "formaPago": "CARTERA_COMERCIAL"
                    }
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.idCliente").isEqualTo(2)
                .jsonPath("$.formaPago").isEqualTo("CARTERA_COMERCIAL");
    }

    @Test
    void T017_deberiaDevolver400_cuandoFormaPagoEsInvalida() {
        // Arrange
        Long idCliente = 3L;
        String formaPagoInvalida = "INVALIDO";
        
        var clienteResponse = new ClienteClientResponse("3", "789", "Test Client 3", "789", "Test");
        when(clienteWebClient.consultarClientePorIdCliente(idCliente.toString()))
            .thenReturn(Mono.just(clienteResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "idCliente": 3,
                        "formaPago": "INVALIDO"
                    }
                    """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void T017_deberiaDevolver404_cuandoClienteNoExiste() {
        // Arrange
        Long idCliente = 999L;
        
        when(clienteWebClient.consultarClientePorIdCliente(idCliente.toString()))
            .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "idCliente": 999,
                        "formaPago": "CONTRA_ENTREGA"
                    }
                    """)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void T017_deberiaDevolver400_cuandoIdClienteEsNulo() {
        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "idCliente": null,
                        "formaPago": "CONTRA_ENTREGA"
                    }
                    """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void T017_deberiaDevolver400_cuandoFormaPagoEsVacio() {
        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "idCliente": 1,
                        "formaPago": ""
                    }
                    """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void T017_deberiaDevolver400_cuandoFormaPagoEsNull() {
        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "idCliente": 1,
                        "formaPago": null
                    }
                    """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void T017_deberiaDevolver400_cuandoFaltaCuerpo() {
        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/clientes/ingresar-forma-pago")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange()
                .expectStatus().isBadRequest();
    }
}