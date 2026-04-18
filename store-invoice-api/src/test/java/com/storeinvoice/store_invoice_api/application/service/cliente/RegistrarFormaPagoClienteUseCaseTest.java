package com.storeinvoice.store_invoice_api.application.service.cliente;

import com.storeinvoice.store_invoice_api.application.dto.command.RegistrarFormaPagoCommand;
import com.storeinvoice.store_invoice_api.application.dto.response.FormaPagoResponse;
import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.domain.exception.FormaPagoInvalidaException;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.domain.valueobject.FormaPago;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteWebClient;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.FormaPagoClienteRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarFormaPagoClienteUseCaseTest {

    @Mock
    private FormaPagoClienteRepositoryPort formaPagoClienteRepositoryPort;

    @Mock
    private ClienteWebClient clienteWebClient;

    private RegistrarFormaPagoClienteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarFormaPagoClienteUseCase(
                formaPagoClienteRepositoryPort,
                clienteWebClient
        );
    }

    @Test
    void shouldRegisterFormaPagoSuccessfully() {
        Long idCliente = 1L;
        String formaPago = "CONTRA_ENTREGA";
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(idCliente, formaPago);

        ClienteClientResponse clienteResponse = new ClienteClientResponse(
                "1", "12345678", "Test Cliente", "3001234567", "Calle 123"
        );

        FormaPagoCliente savedFormaPago = new FormaPagoCliente(
                idCliente,
                FormaPago.CONTRA_ENTREGA,
                LocalDateTime.now()
        );

        when(clienteWebClient.consultarClientePorIdCliente(anyString()))
                .thenReturn(Mono.just(clienteResponse));
        when(formaPagoClienteRepositoryPort.save(any(FormaPagoCliente.class)))
                .thenReturn(savedFormaPago);

        StepVerifier.create(useCase.registrarFormaPago(command))
                .expectNextMatches(response ->
                        response.idCliente().equals(idCliente) &&
                        response.formaPago().equals(formaPago))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFormaPagoIsInvalid() {
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(1L, "INVALIDO");

        StepVerifier.create(useCase.registrarFormaPago(command))
                .expectError(FormaPagoInvalidaException.class)
                .verify();
    }

    @Test
    void shouldFailWhenClienteNotFound() {
        Long idCliente = 999L;
        String formaPago = "CONTRA_ENTREGA";
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(idCliente, formaPago);

        when(clienteWebClient.consultarClientePorIdCliente(anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.registrarFormaPago(command))
                .expectError(ClienteNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenIdClienteIsNull() {
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(null, "CONTRA_ENTREGA");

        StepVerifier.create(useCase.registrarFormaPago(command))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}