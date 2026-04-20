package com.storeinvoice.store_invoice_api.application.service.cliente;

import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.domain.exception.InvalidClientIdException;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarClientePorIdNacionalUseCaseTest {

    @Mock
    private ClienteWebClient clienteWebClient;

    @InjectMocks
    private ConsultarClientePorIdNacionalUseCase useCase;

    private ClienteResponse clienteResponse;

    @BeforeEach
    void setUp() {
        clienteResponse = new ClienteResponse(
                "100",
                "12345678",
                "Juan Perez",
                "3001234567",
                "Calle 123 #45-67"
        );
    }

    @Test
    void consultarClientePorIdNacional_exitoso_retornaCliente() {
        when(clienteWebClient.consultarClientePorIdNacional("12345678"))
                .thenReturn(Mono.just(clienteResponse));

        useCase.consultarClientePorIdNacional("12345678")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("100", result.idCliente());
                    assertEquals("12345678", result.idNacional());
                    assertEquals("Juan Perez", result.nombre());
                })
                .verifyComplete();

        verify(clienteWebClient).consultarClientePorIdNacional("12345678");
    }

    @Test
    void consultarClientePorIdNacional_clienteNoExistente_lanzaExcepcion() {
        when(clienteWebClient.consultarClientePorIdNacional("99999999"))
                .thenReturn(Mono.empty());

        useCase.consultarClientePorIdNacional("99999999")
                .as(StepVerifier::create)
                .expectError(ClienteNotFoundException.class)
                .verify();
    }

    @Test
    void consultarClientePorIdNacional_idNacionalVacio_lanzaExcepcion() {
        useCase.consultarClientePorIdNacional("")
                .as(StepVerifier::create)
                .expectError(InvalidClientIdException.class)
                .verify();
    }

    @Test
    void consultarClientePorIdNacional_idNacionalNull_lanzaExcepcion() {
        useCase.consultarClientePorIdNacional(null)
                .as(StepVerifier::create)
                .expectError(InvalidClientIdException.class)
                .verify();
    }

    @Test
    void consultarClientePorIdCliente_exitoso_retornaCliente() {
        when(clienteWebClient.consultarClientePorIdCliente("100"))
                .thenReturn(Mono.just(clienteResponse));

        useCase.consultarClientePorIdCliente("100")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("100", result.idCliente());
                    assertEquals("Juan Perez", result.nombre());
                })
                .verifyComplete();
    }

    @Test
    void consultarClientePorIdCliente_clienteNoExistente_lanzaExcepcion() {
        when(clienteWebClient.consultarClientePorIdCliente("999"))
                .thenReturn(Mono.empty());

        useCase.consultarClientePorIdCliente("999")
                .as(StepVerifier::create)
                .expectError(ClienteNotFoundException.class)
                .verify();
    }
}