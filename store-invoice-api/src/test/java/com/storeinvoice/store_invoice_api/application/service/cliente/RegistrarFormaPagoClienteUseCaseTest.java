package com.storeinvoice.store_invoice_api.application.service.cliente;

import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.application.dto.command.RegistrarFormaPagoCommand;
import com.storeinvoice.store_invoice_api.application.dto.response.FormaPagoResponse;
import com.storeinvoice.store_invoice_api.domain.exception.FormaPagoInvalidaException;
import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.domain.valueobject.FormaPago;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteWebClient;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.FormaPagoClienteRepositoryPort;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper.CommandFormaPagoClienteMapper;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper.ResponseFormaPagoClienteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarFormaPagoClienteUseCaseTest {

    @Mock
    private FormaPagoClienteRepositoryPort repositoryMock;

    @Mock
    private ClienteWebClient clienteWebClientMock;

    @Mock
    private CommandFormaPagoClienteMapper commandMapperMock;

    @Mock
    private ResponseFormaPagoClienteMapper responseMapperMock;

    private RegistrarFormaPagoClienteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarFormaPagoClienteUseCase(
                repositoryMock,
                clienteWebClientMock,
                commandMapperMock,
                responseMapperMock
        );
    }

    @Test
    void registrarFormaPago_deberiaDevolverRespuestaExitosa_cuandoComandoEsValido() {
        // Arrange
        Long idCliente = 1L;
        String formaPago = "CONTRA_ENTREGA";
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(idCliente, formaPago);
        ClienteClientResponse clienteResponse = new ClienteClientResponse("1", "123", "Test", "123", "Test");
        FormaPagoCliente formaPagoCliente = new FormaPagoCliente();
        formaPagoCliente.setIdCliente(idCliente);
        formaPagoCliente.setFormaPago(FormaPago.fromValue(formaPago));
        formaPagoCliente.setFechaRegistro(java.time.LocalDateTime.now());
        FormaPagoResponse response = new FormaPagoResponse(idCliente, formaPago, formaPagoCliente.getFechaRegistro());

        when(clienteWebClientMock.consultarClientePorIdCliente(idCliente.toString()))
                .thenReturn(Mono.just(clienteResponse));
        when(commandMapperMock.toDomain(command)).thenReturn(formaPagoCliente);
        when(repositoryMock.save(formaPagoCliente)).thenReturn(formaPagoCliente);
        when(responseMapperMock.toResponse(formaPagoCliente)).thenReturn(response);

        // Act
        var resultado = useCase.registrarFormaPago(command);

        // Assert
        StepVerifier.create(resultado)
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void registrarFormaPago_deberiaLanzarExcepcion_cuandoIdClienteEsNulo() {
        // Arrange
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(null, "CONTRA_ENTREGA");

        // Act & Assert
        StepVerifier.create(useCase.registrarFormaPago(command))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void registrarFormaPago_deberiaLanzarExcepcion_cuandoFormaPagoEsInvalida() {
        // Arrange
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(1L, "INVALIDO");

        // Act & Assert
        StepVerifier.create(useCase.registrarFormaPago(command))
                .expectError(FormaPagoInvalidaException.class)
                .verify();
    }

    @Test
    void registrarFormaPago_deberiaLanzarExcepcion_cuandoClienteNoExiste() {
        // Arrange
        Long idCliente = 1L;
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(idCliente, "CONTRA_ENTREGA");
        when(clienteWebClientMock.consultarClientePorIdCliente(idCliente.toString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.registrarFormaPago(command))
                .expectError(com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException.class)
                .verify();
    }
}