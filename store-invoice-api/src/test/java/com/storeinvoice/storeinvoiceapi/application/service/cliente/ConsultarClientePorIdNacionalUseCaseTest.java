package com.storeinvoice.storeinvoiceapi.application.service.cliente;

import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
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
    private ClienteServicePort clienteServicePort;

    @InjectMocks
    private ConsultarClientePorIdNacionalUseCase useCase;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("100", "12345678", "Juan Perez", "3001234567", "Calle 123 #45-67");
    }

    @Test
    void ejecutar_exitoso_retornaCliente() {
        when(clienteServicePort.findByIdNacional("12345678"))
                .thenReturn(Mono.just(cliente));

        useCase.ejecutar("12345678")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("100", result.idCliente());
                    assertEquals("12345678", result.idNacional());
                })
                .verifyComplete();
    }

    @Test
    void ejecutar_clienteNoExistente_lanzaExcepcion() {
        when(clienteServicePort.findByIdNacional("99999999"))
                .thenReturn(Mono.empty());

        useCase.ejecutar("99999999")
                .as(StepVerifier::create)
                .expectError(ClienteNotFoundException.class)
                .verify();
    }
}
